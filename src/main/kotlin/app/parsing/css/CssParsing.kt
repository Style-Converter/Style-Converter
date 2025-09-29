package app.parsing.css

import app.*
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

// Transforms a raw JSON object into the strongly-typed CssComponents model
fun JsonInputToCssComponents(doc: JsonObject): CssComponents {
    val componentsJson = doc["components"]?.jsonObject ?: return CssComponents(emptyMap())

    fun toValue(el: JsonElement): CssPropertyValue {
        val prim = el.jsonPrimitive
        val content = when {
            prim.isString -> prim.content
            prim.booleanOrNull != null -> prim.booleanOrNull.toString()
            prim.doubleOrNull != null -> prim.doubleOrNull.toString()
            prim.intOrNull != null -> prim.intOrNull.toString()
            else -> prim.content
        }
        return CssPropertyValue(content)
    }

    fun toProperties(obj: JsonObject?): Map<String, CssPropertyValue>? {
        if (obj == null) return null
        return obj.mapValues { (_, v) -> toValue(v) }
    }

    val components = componentsJson.mapValues { (_, node) ->
        val obj = node.jsonObject
        val props = toProperties(obj["properties"]?.jsonObject)

        val selectors = obj["selectors"]?.jsonArray?.mapNotNull { el ->
            val selObj = el.jsonObject
            val selectorStr = selObj["when"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val selProps = toProperties(selObj["properties"]?.jsonObject) ?: emptyMap()
            CssSelector(selector = selectorStr, properties = selProps)
        }

        val media = obj["media"]?.jsonArray?.mapNotNull { el ->
            val mObj = el.jsonObject
            val query = mObj["query"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val mProps = toProperties(mObj["properties"]?.jsonObject) ?: emptyMap()
            CssMedia(query = query, properties = mProps)
        }

        CssComponent(
            properties = props,
            selectors = selectors,
            media = media
        )
    }

    return CssComponents(components)
}

fun cssParsing(doc: JsonObject): IRDocument {
    val components = JsonInputToCssComponents(doc)
    val irComponents = components.components.map { (name, component) ->
        val properties = component.properties?.let { PropertiesParser.parse(it) } ?: mutableListOf()
        val selectors = parseSelectors(component.selectors)
        val media = parseMedia(component.media)
        IRComponent(name = name, properties = properties, selectors = selectors, media = media)
    }
    return IRDocument(irComponents)
}