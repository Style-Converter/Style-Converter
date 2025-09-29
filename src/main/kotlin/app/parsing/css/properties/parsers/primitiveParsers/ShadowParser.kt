package app.parsing.css.properties.parsers.primitiveParsers

import app.IRColor
import app.IRLength
import app.IRShadow

object ShadowParser {
    /**
     * Parse one shadow definition like:
     * inset 2px 4px 6px 1px #ff0000
     */
    fun parse(tokens: List<String>): IRShadow? {
        var inset = false
        val lengths = mutableListOf<IRLength>()
        var color: IRColor? = null

        for (t in tokens) {
            val lower = t.lowercase()
            if (lower == "inset") {
                inset = true
                continue
            }

            // try color first
            val parsedColor = ColorParser.parse(t)
            if (parsedColor != null) {
                color = parsedColor
                continue
            }

            // try length
            val parsedLength = LengthParser.parse(t)
            if (parsedLength != null) {
                lengths.add(parsedLength)
                continue
            }

            // ignore unknown tokens for now
        }

        return IRShadow(
            xOffset = lengths.getOrNull(0),
            yOffset = lengths.getOrNull(1),
            blur = lengths.getOrNull(2),
            spread = lengths.getOrNull(3),
            color = color,
            inset = inset
        )
    }
}