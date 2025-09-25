package app.parsing.css.styles

import app.PropertyIR
import app.parsing.parseSizeToken
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private fun define(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val (num, unit) = parseSizeToken(input)
	val name = "border-image-outset"
	val idx = acc.indexOfFirst { it.name == name }
	val existing = if (idx >= 0) acc[idx] else PropertyIR(name = name)
	val merged = if (num != null) {
		existing.copy(sizeName = unit ?: existing.sizeName, numericSizeValue = num, stringSizeValue = if (unit != null) "${num}${unit}" else "${num}")
	} else {
		existing.copy(stringSizeValue = input)
	}
	if (idx >= 0) acc[idx] = merged else acc.add(merged)
	return acc
}

fun parseBorderImageOutset(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-image-outset"]?.jsonPrimitive?.contentOrNull ?: return acc
	return define(raw, acc)
}