package app.parsing.css.styles

import app.PropertyIR
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private fun define(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val v = input.trim()
	val name = "border-image-slice"
	val idx = acc.indexOfFirst { it.name == name }
	val existing = if (idx >= 0) acc[idx] else PropertyIR(name = name)
	val merged = existing.copy(stringSizeValue = v)
	if (idx >= 0) acc[idx] = merged else acc.add(merged)
	return acc
}

fun parseBorderImageSlice(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-image-slice"]?.jsonPrimitive?.contentOrNull ?: return acc
	return define(raw, acc)
}