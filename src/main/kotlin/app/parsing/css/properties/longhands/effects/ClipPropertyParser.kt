package app.parsing.css.properties.longhands.effects

import app.irmodels.IRProperty
import app.parsing.css.properties.longhands.PropertyParser
import app.irmodels.properties.effects.*
import app.parsing.css.properties.primitiveParsers.LengthParser

/**
 * Parser for `clip` property (legacy).
 */
object ClipPropertyParser : PropertyParser {
    override fun parse(value: String): IRProperty? {
        val trimmed = value.trim().lowercase()
        if (trimmed == "auto") {
            return ClipProperty(ClipValue.Auto)
        }
        if (trimmed.startsWith("rect(") && trimmed.endsWith(")")) {
            val params = trimmed.substring(5, trimmed.length - 1).trim()
            val parts = params.split(",").map { it.trim() }
            if (parts.size != 4) return null
            val top = LengthParser.parse(parts[0]) ?: return null
            val right = LengthParser.parse(parts[1]) ?: return null
            val bottom = LengthParser.parse(parts[2]) ?: return null
            val left = LengthParser.parse(parts[3]) ?: return null
            return ClipProperty(ClipValue.Rect(top, right, bottom, left))
        }
        return null
    }
}
