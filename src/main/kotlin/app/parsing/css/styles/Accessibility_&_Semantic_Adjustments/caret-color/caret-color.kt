package app.parsing.css.styles

import app.PropertyIR
import app.parsing.parseColor
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private val GLOBAL_KEYWORDS = setOf("inherit","initial","unset","revert","revert-layer")

private fun define(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val v = input.trim()
	val lower = v.lowercase()
	val name = "caret-color"
	val idx = acc.indexOfFirst { it.name == name }
	val existing = if (idx >= 0) acc[idx] else PropertyIR(name = name)
	if (lower in GLOBAL_KEYWORDS || lower == "auto" || lower == "currentcolor") {
		val merged = existing.copy(value = lower)
		if (idx >= 0) acc[idx] = merged else acc.add(merged)
		return acc
	}
	val color = parseColor(v) ?: return acc
	val merged = existing.copy(color = color)
	if (idx >= 0) acc[idx] = merged else acc.add(merged)
	return acc
}

fun parseCaretColor(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["caret-color"]?.jsonPrimitive?.contentOrNull ?: return acc
	return define(raw, acc)
}