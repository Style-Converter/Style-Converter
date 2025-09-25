package app.parsing.css.styles

import app.PropertyIR
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private val BORDER_STYLES_ALLOWED = setOf(
	"none","hidden","dotted","dashed","solid","double","groove","ridge","inset","outset"
)

private fun defineBorderStyle(input: String): String? = input.takeIf { BORDER_STYLES_ALLOWED.contains(it.lowercase()) }

private fun refactor(styleInput: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val tokens = styleInput.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
	if (tokens.isEmpty()) return acc
	val sides = when (tokens.size) {
		1 -> listOf(tokens[0], tokens[0], tokens[0], tokens[0])
		2 -> listOf(tokens[0], tokens[1], tokens[0], tokens[1])
		3 -> listOf(tokens[0], tokens[1], tokens[2], tokens[1])
		else -> listOf(tokens[0], tokens[1], tokens[2], tokens[3])
	}
	val keys = listOf("border-top-style","border-right-style","border-bottom-style","border-left-style")
	for (i in 0 until 4) {
		val style = defineBorderStyle(sides[i]) ?: continue
		parseBaseStyles(kotlinx.serialization.json.buildJsonObject { put(keys[i], kotlinx.serialization.json.JsonPrimitive(style)) }, acc)
	}
	return acc
}

fun parseBorderStyle(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-style"]?.jsonPrimitive?.contentOrNull ?: return acc
	return refactor(raw, acc)
}