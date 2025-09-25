package app.parsing.css.styles

import app.PropertyIR
import app.parsing.parseSizeToken
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private fun refactor(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val (num, unit) = parseSizeToken(input)
	val n = num ?: return acc
	val token = if (unit != null) "$n$unit" else "$n"
	// border-block-end maps to the block-end side (logical), which for top-to-bottom LTR maps to bottom
	val obj = kotlinx.serialization.json.buildJsonObject { put("border-bottom-width", kotlinx.serialization.json.JsonPrimitive(token)) }
	parseBaseStyles(obj, acc)
	return acc
}

fun parseBorderBlockEndWidth(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-block-end-width"]?.jsonPrimitive?.contentOrNull ?: return acc
	return refactor(raw, acc)
}