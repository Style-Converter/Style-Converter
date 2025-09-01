package app.parsing.css.styles.borderinlinestart

import app.BaseIR
import app.BorderIR
import app.BorderSide
import app.ColorRgba
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object borderInlineStartColor {
    fun applyBorderInlineStartColor(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val color = parseHexOrRgba(value.jsonPrimitive.content) ?: return acc
        val current = acc.border ?: BorderIR()
        val left = current.left ?: BorderSide()
        return acc.copy(border = current.copy(left = left.copy(color = color)))
    }

    private fun parseHexOrRgba(input: String): ColorRgba? {
        val s = input.trim()
        if (s.startsWith("#")) {
            val v = s.removePrefix("#")
            if (v.length == 6) return ColorRgba(v.substring(0,2).toInt(16), v.substring(2,4).toInt(16), v.substring(4,6).toInt(16), 1.0)
            if (v.length == 8) {
                val a = v.substring(0,2).toInt(16) / 255.0
                return ColorRgba(v.substring(2,4).toInt(16), v.substring(4,6).toInt(16), v.substring(6,8).toInt(16), a)
            }
            return null
        }
        val re = Regex("^rgba?\\((\\d+),\\s*(\\d+),\\s*(\\d+)(?:,\\s*(\\d*\\.?\\d+))?\\)$")
        val m = re.find(s) ?: return null
        val r = m.groupValues[1].toInt().coerceIn(0,255)
        val g = m.groupValues[2].toInt().coerceIn(0,255)
        val b = m.groupValues[3].toInt().coerceIn(0,255)
        val a = m.groupValues.getOrNull(4)?.takeIf { it.isNotEmpty() }?.toDoubleOrNull()?.coerceIn(0.0,1.0) ?: 1.0
        return ColorRgba(r,g,b,a)
    }
}

