package app.parsing.css.styles

import app.PropertyIR
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private val OUTLINE_STYLES_ALLOWED = setOf(
	"none","hidden","dotted","dashed","solid","double","groove","ridge","inset","outset","auto"
)

private fun define(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val style = input.takeIf { OUTLINE_STYLES_ALLOWED.contains(it.lowercase()) || it.isNotBlank() } ?: return acc
	val name = "outline"
	val idx = acc.indexOfFirst { it.name == name }
	val existing = if (idx >= 0) acc[idx] else PropertyIR(name = name)
	val merged = existing.copy(value = style)
	if (idx >= 0) acc[idx] = merged else acc.add(merged)
	return acc
}

fun parseOutlineStyle(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["outline-style"]?.jsonPrimitive?.contentOrNull ?: return acc
	return define(raw, acc)
}