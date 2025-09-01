package app.parsing.css.styles.background

import app.BaseIR
import app.ColorRgba
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object background {
    fun applyBackgroundColor(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val hex = value.jsonPrimitive.content.trim()
        val color = parseHexColor(hex) ?: return acc
        return acc.copy(backgroundColor = color)
    }

    private fun parseHexColor(input: String): ColorRgba? {
        val s = input.removePrefix("#")
        if (s.length != 6) return null
        val r = s.substring(0, 2).toInt(16)
        val g = s.substring(2, 4).toInt(16)
        val b = s.substring(4, 6).toInt(16)
        return ColorRgba(r, g, b, 1.0)
    }
}

