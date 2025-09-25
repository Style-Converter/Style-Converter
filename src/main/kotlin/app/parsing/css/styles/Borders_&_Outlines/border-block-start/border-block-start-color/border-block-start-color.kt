package app.parsing.css.styles

import app.PropertyIR
import app.parsing.parseColor
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private fun refactor(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val color = parseColor(input) ?: return acc
	val obj = kotlinx.serialization.json.buildJsonObject { put("border-top-color", kotlinx.serialization.json.JsonPrimitive(input)) }
	parseBaseStyles(obj, acc)
	val idx = acc.indexOfFirst { it.name == "border-top" }
	if (idx >= 0) acc[idx] = acc[idx].copy(color = color)
	return acc
}

fun parseBorderBlockStartColor(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-block-start-color"]?.jsonPrimitive?.contentOrNull ?: return acc
	return refactor(raw, acc)
}