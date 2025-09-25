package app.parsing.css.styles

import app.PropertyIR
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private val BOTTOM_BORDER_STYLES = setOf(
	"none","hidden","dotted","dashed","solid","double","groove","ridge","inset","outset"
)

fun parseBorderBottomStyle(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-bottom-style"]?.jsonPrimitive?.contentOrNull ?: return acc
	return define(raw, acc)
}

private fun define(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val style = input.takeIf { BOTTOM_BORDER_STYLES.contains(it.lowercase()) } ?: return acc
	val name = "border-bottom"
	val idx = acc.indexOfFirst { it.name == name }
	val existing = if (idx >= 0) acc[idx] else PropertyIR(name = name)
	val merged = existing.copy(value = style)
	if (idx >= 0) acc[idx] = merged else acc.add(merged)
	return acc
}


