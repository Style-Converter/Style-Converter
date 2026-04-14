package app.parsing.css

import app.irmodels.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import app.parsing.css.properties.PropertiesParser
import app.parsing.css.selectors.parseSelectors
import app.parsing.css.mediaQueries.parseMedia

/**
 * Main CSS parsing entry point.
 *
 * ## Processing Pipeline
 * ```
 * JSON Input
 *   ↓ JsonInputToCssComponents()
 * CssComponents (intermediate model)
 *   ↓ cssParsing()
 * IRDocument (final IR model)
 * ```
 *
 * ## Responsibilities
 * - Parse JSON input into intermediate CssComponents model
 * - Delegate property parsing to PropertiesParser
 * - Delegate selector parsing to parseSelectors
 * - Delegate media query parsing to parseMedia
 * - Assemble final IRDocument with all parsed components
 *
 * @see PropertiesParser for CSS property parsing
 * @see parseSelectors for pseudo-class selector handling
 * @see parseMedia for media query handling
 */

/**
 * Transforms a raw JSON object into the strongly-typed CssComponents model.
 *
 * Handles JSON structure:
 * ```json
 * {
 *   "components": {
 *     "ComponentName": {
 *       "properties": { "color": "red", ... },
 *       "selectors": [{ "selector": ":hover", "properties": {...} }],
 *       "media": [{ "query": "(min-width: 768px)", "properties": {...} }]
 *     }
 *   }
 * }
 * ```
 */
fun JsonInputToCssComponents(doc: JsonObject): CssComponents {
    val componentsJson = doc["components"]?.jsonObject ?: return CssComponents(emptyMap())

    fun toValue(el: JsonElement): CssPropertyValue {
        val prim = el.jsonPrimitive
        val content = when {
            prim.isString -> prim.content
            prim.booleanOrNull != null -> prim.booleanOrNull.toString()
            prim.intOrNull != null -> prim.intOrNull.toString()  // Check int BEFORE double
            prim.doubleOrNull != null -> prim.doubleOrNull.toString()
            else -> prim.content
        }
        return CssPropertyValue(content)
    }

    fun toProperties(obj: JsonObject?): Map<String, CssPropertyValue>? {
        if (obj == null) return null
        return obj.mapValues { (_, v) -> toValue(v) }
    }

    // Recursive function to parse a component and its children
    fun parseComponent(obj: JsonObject): CssComponent {
        val props = toProperties(obj["properties"]?.jsonObject)

        val selectors = obj["selectors"]?.jsonArray?.mapNotNull { el ->
            val selObj = el.jsonObject
            val selectorStr = selObj["selector"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val selProps = toProperties(selObj["properties"]?.jsonObject) ?: emptyMap()
            CssSelector(selector = selectorStr, properties = selProps)
        }

        val media = obj["media"]?.jsonArray?.mapNotNull { el ->
            val mObj = el.jsonObject
            val query = mObj["query"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val mProps = toProperties(mObj["properties"]?.jsonObject) ?: emptyMap()
            CssMedia(query = query, properties = mProps)
        }

        // Parse nested children recursively
        val children = obj["children"]?.jsonObject?.mapValues { (_, childNode) ->
            parseComponent(childNode.jsonObject)
        }

        return CssComponent(
            properties = props,
            selectors = selectors,
            media = media,
            children = children
        )
    }

    val components = componentsJson.mapValues { (_, node) ->
        parseComponent(node.jsonObject)
    }

    return CssComponents(components)
}

/**
 * Main entry point: parses JSON input into an IRDocument.
 *
 * Steps:
 * 1. Convert JSON to CssComponents intermediate model
 * 2. Parse each component's properties via PropertiesParser
 * 3. Parse selectors and media queries
 * 4. Generate unique IDs for each component
 * 5. Recursively parse children for SDUI support
 * 6. Assemble into IRDocument
 *
 * @param doc Raw JSON input containing component definitions
 * @return IRDocument with all parsed components and their properties
 */
fun cssParsing(doc: JsonObject): IRDocument {
    val components = JsonInputToCssComponents(doc)

    // Counter for generating unique component IDs
    var componentCounter = 0

    // Recursive function to convert CssComponent to IRComponent
    fun convertToIR(name: String, component: CssComponent): IRComponent {
        componentCounter++
        val id = "${name.lowercase().replace(" ", "-")}-${componentCounter.toString().padStart(3, '0')}"

        // Parse properties directly to specific property classes
        val properties = component.properties?.let { PropertiesParser.parse(it) } ?: mutableListOf()

        val selectors = parseSelectors(component.selectors)
        val media = parseMedia(component.media)

        // Recursively convert children
        val children = component.children?.map { (childName, childComponent) ->
            convertToIR(childName, childComponent)
        }

        return IRComponent(
            id = id,
            name = name,
            properties = properties,
            selectors = selectors,
            media = media,
            children = children
        )
    }

    val irComponents = components.components.map { (name, component) ->
        convertToIR(name, component)
    }
    return IRDocument(irComponents)
}