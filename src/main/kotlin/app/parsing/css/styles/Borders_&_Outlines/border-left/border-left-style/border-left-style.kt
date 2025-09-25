package app.parsing.css.styles

import app.PropertyIR
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private val SIDE_BORDER_STYLES = setOf(
	"none","hidden","dotted","dashed","solid","double","groove","ridge","inset","outset"
)

private fun define(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val style = input.takeIf { SIDE_BORDER_STYLES.contains(it.lowercase()) } ?: return acc
	val name = "border-left"
	val idx = acc.indexOfFirst { it.name == name }
	val existing = if (idx >= 0) acc[idx] else PropertyIR(name = name)
	val merged = existing.copy(value = style)
	if (idx >= 0) acc[idx] = merged else acc.add(merged)
	return acc
}

fun parseBorderLeftStyle(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-left-style"]?.jsonPrimitive?.contentOrNull ?: return acc
	return define(raw, acc)
}