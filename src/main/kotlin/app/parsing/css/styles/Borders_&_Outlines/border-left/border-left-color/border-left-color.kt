package app.parsing.css.styles

import app.PropertyIR
import app.parsing.parseColor
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private fun define(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val color = parseColor(input) ?: return acc
	val name = "border-left"
	val idx = acc.indexOfFirst { it.name == name }
	val existing = if (idx >= 0) acc[idx] else PropertyIR(name = name)
	val merged = existing.copy(color = color)
	if (idx >= 0) acc[idx] = merged else acc.add(merged)
	return acc
}

fun parseBorderLeftColor(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-left-color"]?.jsonPrimitive?.contentOrNull ?: return acc
	return define(raw, acc)
}