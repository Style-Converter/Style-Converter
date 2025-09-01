package app.parsing.css.styles.border

import app.BaseIR
import app.BorderIR
import app.BorderRadius
import app.BorderSide
import app.ColorRgba
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object border {
    fun applyBorderSide(side: String, value: JsonElement, acc: BaseIR): BaseIR {
        val spec = value.jsonPrimitive.content.trim()
        val parsed = parseBorderShorthand(spec) ?: return acc
        val current = acc.border ?: BorderIR()
        val updated = when (side) {
            "top" -> current.copy(top = parsed)
            "right" -> current.copy(right = parsed)
            "bottom" -> current.copy(bottom = parsed)
            "left" -> current.copy(left = parsed)
            else -> current
        }
        return acc.copy(border = updated)
    }

    fun applyRadiusCorner(corner: String, value: JsonElement, acc: BaseIR): BaseIR {
        val px = parsePx(value.jsonPrimitive.content) ?: return acc
        val current = acc.border ?: BorderIR()
        val radius = current.radius ?: BorderRadius()
        val newRadius = when (corner) {
            "topLeft" -> radius.copy(topLeftPx = px)
            "topRight" -> radius.copy(topRightPx = px)
            "bottomRight" -> radius.copy(bottomRightPx = px)
            "bottomLeft" -> radius.copy(bottomLeftPx = px)
            else -> radius
        }
        return acc.copy(border = current.copy(radius = newRadius))
    }

    private fun parseBorderShorthand(spec: String): BorderSide? {
        if (spec.isEmpty()) return null
        val parts = spec.split(" ").filter { it.isNotBlank() }
        var width: Double? = null
        var style: String? = null
        var color: ColorRgba? = null
        for (token in parts) {
            when {
                token.endsWith("px") || token.toDoubleOrNull() != null -> width = parsePx(token)
                token.startsWith("#") -> color = parseHex(token)
                else -> style = token
            }
        }
        return BorderSide(widthPx = width, color = color, style = style)
    }

    private fun parsePx(v: String): Double? {
        val t = v.trim().lowercase()
        return when {
            t.endsWith("px") -> t.removeSuffix("px").toDoubleOrNull()
            t == "0" -> 0.0
            else -> t.toDoubleOrNull()
        }
    }

    private fun parseHex(hex: String): ColorRgba? {
        val s = hex.removePrefix("#")
        return when (s.length) {
            6 -> ColorRgba(s.substring(0,2).toInt(16), s.substring(2,4).toInt(16), s.substring(4,6).toInt(16), 1.0)
            8 -> {
                val a = s.substring(0,2).toInt(16) / 255.0
                ColorRgba(s.substring(2,4).toInt(16), s.substring(4,6).toInt(16), s.substring(6,8).toInt(16), a)
            }
            else -> null
        }
    }
}