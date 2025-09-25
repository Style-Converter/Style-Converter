package app.parsing.css.styles

import app.PropertyIR
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

// CSS Speech Module: speak-as supports keywords like normal, spell-out, digits, literal-punctuation, no-punctuation
private val SPEAK_AS_ALLOWED = setOf("auto","normal","spell-out","digits","literal-punctuation","no-punctuation")
private val GLOBAL_KEYWORDS = setOf("inherit","initial","unset","revert","revert-layer")

private fun define(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val tokens = input.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.map { it.lowercase() }
	val name = "speak-as"
	val idx = acc.indexOfFirst { it.name == name }
	val existing = if (idx >= 0) acc[idx] else PropertyIR(name = name)
	if (tokens.size == 1 && tokens[0] in GLOBAL_KEYWORDS) {
		val mergedGlobal = existing.copy(value = tokens[0])
		if (idx >= 0) acc[idx] = mergedGlobal else acc.add(mergedGlobal)
		return acc
	}
	if (tokens.isEmpty() || tokens.any { it !in SPEAK_AS_ALLOWED }) return acc
	val merged = existing.copy(value = tokens.joinToString(" "))
	if (idx >= 0) acc[idx] = merged else acc.add(merged)
	return acc
}

fun parseSpeakAs(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["speak-as"]?.jsonPrimitive?.contentOrNull ?: return acc
	return define(raw, acc)
}