package app.parsing.css.styles

import app.PropertyIR
import app.parsing.parseSizeToken
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private fun defineBorderWidth(input: String): Pair<Double?, String?> = parseSizeToken(input)

private fun refactor(sizeInput: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
    val tokens = sizeInput.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
	if (tokens.isEmpty()) return acc
	val sides = when (tokens.size) {
		1 -> listOf(tokens[0], tokens[0], tokens[0], tokens[0])
		2 -> listOf(tokens[0], tokens[1], tokens[0], tokens[1])
		3 -> listOf(tokens[0], tokens[1], tokens[2], tokens[1])
		else -> listOf(tokens[0], tokens[1], tokens[2], tokens[3])
	}
	val keys = listOf("border-top-width","border-right-width","border-bottom-width","border-left-width")
	for (i in 0 until 4) {
        val (num, unit) = defineBorderWidth(sides[i])
        val token = if (num != null) { if (unit != null) "${num}${unit}" else "${num}" } else sides[i]
        parseBaseStyles(kotlinx.serialization.json.buildJsonObject { put(keys[i], kotlinx.serialization.json.JsonPrimitive(token)) }, acc)
	}
	return acc
}

fun parseBorderWidth(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-width"]?.jsonPrimitive?.contentOrNull ?: return acc
	return refactor(raw, acc)
}