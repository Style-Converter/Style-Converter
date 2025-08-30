package app.parsing.css

import app.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.buildJsonObject
import app.parsing.css.styles.parseBaseStyles
import app.parsing.css.selectors.parseSelectors
import app.parsing.css.mediaQueries.parseMedia

fun cssParsing(doc: JsonObject): DocumentIR {
	val componentsObj = doc["components"]?.jsonObject ?: return DocumentIR(emptyList())
	val components = componentsObj.map { (name, node) ->
		val obj = node.jsonObject
		val baseStyles = parseBaseStyles(obj["styles"]?.jsonObject ?: buildJsonObject { })
		val selectors = parseSelectors(obj["selectors"]?.jsonArray, ::parseBaseStyles)
		val media = parseMedia(obj["media"]?.jsonArray, ::parseBaseStyles)
		ComponentIR(name = name, base = baseStyles, selectors = selectors, media = media)
	}
	return DocumentIR(components)
}