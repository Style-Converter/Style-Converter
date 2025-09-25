package app.parsing.css.styles

import app.PropertyIR
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private val BLOCK_BORDER_STYLES = setOf(
	"none","hidden","dotted","dashed","solid","double","groove","ridge","inset","outset"
)

private fun refactor(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val style = input.takeIf { BLOCK_BORDER_STYLES.contains(it.lowercase()) } ?: return acc
    val objs = listOf(
        kotlinx.serialization.json.buildJsonObject { put("border-inline-start-style", kotlinx.serialization.json.JsonPrimitive(style)) },
        kotlinx.serialization.json.buildJsonObject { put("border-inline-end-style", kotlinx.serialization.json.JsonPrimitive(style)) }
    )
	for (obj in objs) parseBaseStyles(obj, acc)
	return acc
}

fun parseBorderBlockStyle(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-block-style"]?.jsonPrimitive?.contentOrNull ?: return acc
	return refactor(raw, acc)
}