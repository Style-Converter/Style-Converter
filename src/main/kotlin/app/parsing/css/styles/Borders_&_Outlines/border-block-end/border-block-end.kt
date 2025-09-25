package app.parsing.css.styles

import app.PropertyIR
import app.parsing.parseSizeToken
import app.parsing.parseColor
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private val BLOCK_END_BORDER_STYLES = setOf(
	"none","hidden","dotted","dashed","solid","double","groove","ridge","inset","outset"
)

private fun refactor(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	var sizeToken: String? = null
	var typeToken: String? = null
	var colorToken: String? = null
	val tokens = input.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    for (t in tokens) {
        val (num, _) = parseSizeToken(t)
        if (sizeToken == null && (num != null || t.startsWith("calc(") || t.startsWith("min(") || t.startsWith("max(") || t.startsWith("clamp(") )) sizeToken = t
        if (typeToken == null && t.lowercase() in BLOCK_END_BORDER_STYLES) typeToken = t
        if (colorToken == null && parseColor(t) != null) colorToken = t
    }
	val objs = buildList<JsonObject> {
		colorToken?.let { add(buildJsonObject { put("border-block-end-color", JsonPrimitive(it)) }) }
		typeToken?.let { add(buildJsonObject { put("border-block-end-style", JsonPrimitive(it)) }) }
		sizeToken?.let { add(buildJsonObject { put("border-block-end-width", JsonPrimitive(it)) }) }
	}
	for (obj in objs) parseBaseStyles(obj, acc)
	return acc
}

fun parseBorderBlockEnd(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-block-end"]?.jsonPrimitive?.contentOrNull ?: return acc
	return refactor(raw, acc)
}