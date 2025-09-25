package app.parsing.css.styles

import app.PropertyIR
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private val COLOR_SCHEME_ALLOWED = setOf("normal","light","dark","only")
private val GLOBAL_KEYWORDS = setOf("inherit","initial","unset","revert","revert-layer")

private fun define(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val name = "color-scheme"
	val idx = acc.indexOfFirst { it.name == name }
	val existing = if (idx >= 0) acc[idx] else PropertyIR(name = name)
	val tokens = input.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
	if (tokens.size == 1 && tokens[0].lowercase() in GLOBAL_KEYWORDS) {
		val mergedGlobal = existing.copy(value = tokens[0].lowercase())
		if (idx >= 0) acc[idx] = mergedGlobal else acc.add(mergedGlobal)
		return acc
	}
	val valid = tokens.all { it.lowercase() in COLOR_SCHEME_ALLOWED }
	if (!valid) return acc
	val merged = existing.copy(value = tokens.joinToString(" ") { it.lowercase() })
	if (idx >= 0) acc[idx] = merged else acc.add(merged)
	return acc
}

fun parseColorScheme(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["color-scheme"]?.jsonPrimitive?.contentOrNull ?: return acc
	return define(raw, acc)
}