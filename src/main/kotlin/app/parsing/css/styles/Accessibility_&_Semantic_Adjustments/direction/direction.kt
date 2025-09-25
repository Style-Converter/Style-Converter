package app.parsing.css.styles

import app.PropertyIR
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private val DIRECTION_ALLOWED = setOf("ltr","rtl")
private val GLOBAL_KEYWORDS = setOf("inherit","initial","unset","revert","revert-layer")

private fun define(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val v = input.trim().lowercase()
	val name = "direction"
	val idx = acc.indexOfFirst { it.name == name }
	val existing = if (idx >= 0) acc[idx] else PropertyIR(name = name)
	if (v in GLOBAL_KEYWORDS) {
		val merged = existing.copy(value = v)
		if (idx >= 0) acc[idx] = merged else acc.add(merged)
		return acc
	}
	if (v !in DIRECTION_ALLOWED) return acc
	val merged = existing.copy(value = v)
	if (idx >= 0) acc[idx] = merged else acc.add(merged)
	return acc
}

fun parseDirection(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["direction"]?.jsonPrimitive?.contentOrNull ?: return acc
	return define(raw, acc)
}