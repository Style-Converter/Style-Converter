package app.parsing.css.styles

import app.PropertyIR
import app.parsing.parseColor
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private fun refactor(colorInput: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val color = parseColor(colorInput) ?: return acc
	val objs = listOf(
		kotlinx.serialization.json.buildJsonObject { put("border-left-color", kotlinx.serialization.json.JsonPrimitive(colorInput)) },
		kotlinx.serialization.json.buildJsonObject { put("border-right-color", kotlinx.serialization.json.JsonPrimitive(colorInput)) }
	)
	for (obj in objs) parseBaseStyles(obj, acc)
	val sides = listOf("border-left","border-right")
	for (name in sides) {
		val idx = acc.indexOfFirst { it.name == name }
		if (idx >= 0) acc[idx] = acc[idx].copy(color = color)
	}
	return acc
}

fun parseBorderInlineColor(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-inline-color"]?.jsonPrimitive?.contentOrNull ?: return acc
	return refactor(raw, acc)
}