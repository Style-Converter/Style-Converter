package app.parsing.css.styles

import app.PropertyIR
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private val BLOCK_END_BORDER_STYLES = setOf(
	"none","hidden","dotted","dashed","solid","double","groove","ridge","inset","outset"
)

private fun refactor(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val style = input.takeIf { BLOCK_END_BORDER_STYLES.contains(it.lowercase()) } ?: return acc
	val obj = kotlinx.serialization.json.buildJsonObject { put("border-bottom-style", kotlinx.serialization.json.JsonPrimitive(style)) }
	parseBaseStyles(obj, acc)
	return acc
}

fun parseBorderBlockEndStyle(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-block-end-style"]?.jsonPrimitive?.contentOrNull ?: return acc
	return refactor(raw, acc)
}