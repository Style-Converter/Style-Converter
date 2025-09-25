package app.parsing.css.styles

import app.PropertyIR
import app.parsing.parseSizeToken
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private fun define(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val (num, unit) = parseSizeToken(input)
	val n = num ?: return acc
	val token = if (unit != null) "$n$unit" else "$n"
	// Logical start-start maps to top-left in LTR
	val name = "border-top-left-radius"
	val idx = acc.indexOfFirst { it.name == name }
	val existing = if (idx >= 0) acc[idx] else PropertyIR(name = name)
	val merged = existing.copy(sizeName = unit ?: existing.sizeName, numericSizeValue = n, stringSizeValue = token)
	if (idx >= 0) acc[idx] = merged else acc.add(merged)
	return acc
}

fun parseBorderStartStartRadius(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-start-start-radius"]?.jsonPrimitive?.contentOrNull ?: return acc
	return define(raw, acc)
}