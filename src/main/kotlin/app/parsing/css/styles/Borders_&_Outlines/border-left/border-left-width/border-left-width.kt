package app.parsing.css.styles

import app.PropertyIR
import app.parsing.parseSizeToken
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private fun define(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
    val (num, unit) = parseSizeToken(input)
    val name = "border-left"
    val idx = acc.indexOfFirst { it.name == name }
    val existing = if (idx >= 0) acc[idx] else PropertyIR(name = name)
    val merged = if (num != null) existing.copy(sizeName = unit ?: existing.sizeName, numericSizeValue = num) else existing.copy(stringSizeValue = input)
    if (idx >= 0) acc[idx] = merged else acc.add(merged)
    return acc
}

fun parseBorderLeftWidth(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-left-width"]?.jsonPrimitive?.contentOrNull ?: return acc
	return define(raw, acc)
}