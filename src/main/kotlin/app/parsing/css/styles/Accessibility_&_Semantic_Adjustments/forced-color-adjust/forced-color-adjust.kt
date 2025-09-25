package app.parsing.css.styles

import app.PropertyIR
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private val FORCED_COLOR_ADJUST_ALLOWED = setOf("auto","none")
private val GLOBAL_KEYWORDS = setOf("inherit","initial","unset","revert","revert-layer")

private fun define(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val v = input.trim().lowercase()
	val name = "forced-color-adjust"
	val idx = acc.indexOfFirst { it.name == name }
	val existing = if (idx >= 0) acc[idx] else PropertyIR(name = name)
	if (v in GLOBAL_KEYWORDS) {
		val merged = existing.copy(value = v)
		if (idx >= 0) acc[idx] = merged else acc.add(merged)
		return acc
	}
	if (v !in FORCED_COLOR_ADJUST_ALLOWED) return acc
	val merged = existing.copy(value = v)
	if (idx >= 0) acc[idx] = merged else acc.add(merged)
	return acc
}

fun parseForcedColorAdjust(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["forced-color-adjust"]?.jsonPrimitive?.contentOrNull ?: return acc
	return define(raw, acc)
}