package app.parsing.css.styles

import app.PropertyIR
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private val INLINE_STYLES_ALLOWED = setOf(
	"none","hidden","dotted","dashed","solid","double","groove","ridge","inset","outset"
)

private fun refactor(styleInput: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val style = styleInput.takeIf { INLINE_STYLES_ALLOWED.contains(it.lowercase()) } ?: return acc
	val objs = listOf(
		kotlinx.serialization.json.buildJsonObject { put("border-left-style", kotlinx.serialization.json.JsonPrimitive(style)) },
		kotlinx.serialization.json.buildJsonObject { put("border-right-style", kotlinx.serialization.json.JsonPrimitive(style)) }
	)
	for (obj in objs) parseBaseStyles(obj, acc)
	return acc
}

fun parseBorderInlineStyle(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-inline-style"]?.jsonPrimitive?.contentOrNull ?: return acc
	return refactor(raw, acc)
}