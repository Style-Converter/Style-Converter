package app.parsing.css.styles

import app.PropertyIR
import app.parsing.parseSizeToken
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private val INLINE_BORDER_STYLES = setOf(
	"none","hidden","dotted","dashed","solid","double","groove","ridge","inset","outset"
)

private fun refactor(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	var sizeToken: String? = null
	var typeToken: String? = null
	var colorToken: String? = null
	val tokens = input.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
	for (t in tokens) {
		val (num, _) = parseSizeToken(t)
		if (sizeToken == null && num != null) sizeToken = t
		if (typeToken == null && t.lowercase() in INLINE_BORDER_STYLES) typeToken = t
		if (colorToken == null && (t.startsWith("#") || t.lowercase().startsWith("rgb"))) colorToken = t
	}
	val objs = buildList<JsonObject> {
		colorToken?.let { add(buildJsonObject { put("border-inline-color", JsonPrimitive(it)) }) }
		typeToken?.let { add(buildJsonObject { put("border-inline-style", JsonPrimitive(it)) }) }
		sizeToken?.let { add(buildJsonObject { put("border-inline-width", JsonPrimitive(it)) }) }
	}
	for (obj in objs) parseBaseStyles(obj, acc)
	return acc
}

fun parseBorderInline(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-inline"]?.jsonPrimitive?.contentOrNull ?: return acc
	return refactor(raw, acc)
}