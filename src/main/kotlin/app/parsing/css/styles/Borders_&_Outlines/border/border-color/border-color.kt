package app.parsing.css.styles

import app.PropertyIR
import app.parsing.parseColor
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private fun defineBorderColor(input: String): app.ColorRgba? = parseColor(input)

private fun refactor(colorInput: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
    val tokens = colorInput.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    val sides = when (tokens.size) {
        0 -> return acc
        1 -> listOf(tokens[0], tokens[0], tokens[0], tokens[0])
        2 -> listOf(tokens[0], tokens[1], tokens[0], tokens[1])
        3 -> listOf(tokens[0], tokens[1], tokens[2], tokens[1])
        else -> listOf(tokens[0], tokens[1], tokens[2], tokens[3])
    }
    val keys = listOf("border-top-color","border-right-color","border-bottom-color","border-left-color")
    for (i in 0 until 4) {
        val token = sides[i]
        parseBaseStyles(kotlinx.serialization.json.buildJsonObject { put(keys[i], kotlinx.serialization.json.JsonPrimitive(token)) }, acc)
        val color = defineBorderColor(token)
        val sideName = keys[i].removeSuffix("-color")
        val idx = acc.indexOfFirst { it.name == sideName }
        if (idx >= 0 && color != null) acc[idx] = acc[idx].copy(color = color)
    }
    return acc
}

fun parseBorderColor(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
    val raw = styles["border-color"]?.jsonPrimitive?.contentOrNull ?: return acc
    return refactor(raw, acc)
}