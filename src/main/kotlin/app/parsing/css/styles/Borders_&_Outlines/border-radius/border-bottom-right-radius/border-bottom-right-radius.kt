package app.parsing.css.styles

import app.PropertyIR
import app.parsing.parseSizeToken
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private fun define(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val (num, unit) = parseSizeToken(input)
	val name = "border-bottom-right-radius"
	val idx = acc.indexOfFirst { it.name == name }
	val existing = if (idx >= 0) acc[idx] else PropertyIR(name = name)
	return if (num != null) {
		val token = if (unit != null) "${num}${unit}" else "${num}"
		val merged = existing.copy(sizeName = unit ?: existing.sizeName, numericSizeValue = num, stringSizeValue = token)
		if (idx >= 0) acc[idx] = merged else acc.add(merged)
		acc
	} else {
		val merged = existing.copy(sizeName = null, numericSizeValue = null, stringSizeValue = input)
		if (idx >= 0) acc[idx] = merged else acc.add(merged)
		acc
	}
}

fun parseBorderBottomRightRadius(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-bottom-right-radius"]?.jsonPrimitive?.contentOrNull ?: return acc
	return define(raw, acc)
}