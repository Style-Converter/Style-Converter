package app.parsing.css.styles

import app.PropertyIR
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private val INLINE_END_STYLES_ALLOWED = setOf(
	"none","hidden","dotted","dashed","solid","double","groove","ridge","inset","outset"
)

private fun define(styleInput: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val style = styleInput.takeIf { INLINE_END_STYLES_ALLOWED.contains(it.lowercase()) } ?: return acc
    val name = "border-inline-end"
    val idx = acc.indexOfFirst { it.name == name }
    val existing = if (idx >= 0) acc[idx] else PropertyIR(name = name)
    val merged = existing.copy(value = style)
    if (idx >= 0) acc[idx] = merged else acc.add(merged)
    return acc
}

fun parseBorderInlineEndStyle(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-inline-end-style"]?.jsonPrimitive?.contentOrNull ?: return acc
	return define(raw, acc)
}