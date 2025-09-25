package app.parsing.css.styles

import app.PropertyIR
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private val REPEAT_ALLOWED = setOf("stretch","repeat","round","space")

private fun define(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
    val tokens = input.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (tokens.isEmpty()) return acc
    if (!tokens.all { REPEAT_ALLOWED.contains(it.lowercase()) }) return acc
    val name = "border-image-repeat"
    val idx = acc.indexOfFirst { it.name == name }
    val existing = if (idx >= 0) acc[idx] else PropertyIR(name = name)
    val merged = existing.copy(value = tokens.joinToString(" ") { it.lowercase() })
    if (idx >= 0) acc[idx] = merged else acc.add(merged)
    return acc
}

fun parseBorderImageRepeat(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-image-repeat"]?.jsonPrimitive?.contentOrNull ?: return acc
	return define(raw, acc)
}