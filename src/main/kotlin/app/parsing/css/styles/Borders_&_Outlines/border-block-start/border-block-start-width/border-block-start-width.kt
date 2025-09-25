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
	// block-start maps to top in typical top-to-bottom writing modes
	val obj = kotlinx.serialization.json.buildJsonObject { put("border-top-width", kotlinx.serialization.json.JsonPrimitive(token)) }
	parseBaseStyles(obj, acc)
	return acc
}

fun parseBorderBlockStartWidth(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-block-start-width"]?.jsonPrimitive?.contentOrNull ?: return acc
	return refactor(raw, acc)
}