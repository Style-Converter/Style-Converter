package app.parsing.css.properties.shorthand

import app.parsing.css.properties.tokenizers.WhitespaceTokenizer

/**
 * Expands border shorthand properties.
 *
 * Border syntax: <width> <style> <color> (in any order)
 * Example: "2px solid red" -> border-width, border-style, border-color
 */
class BorderExpander(private val side: String? = null) : ShorthandExpander {

    private val borderStyles = setOf(
        "none", "hidden", "dotted", "dashed", "solid", "double",
        "groove", "ridge", "inset", "outset"
    )

    private val borderWidthKeywords = setOf("thin", "medium", "thick")

    override fun expand(value: String): Map<String, String> {
        if (value.trim() == "0" || value.trim().lowercase() == "none") {
            return createProperties("0", "none", "currentcolor")
        }

        val tokens = WhitespaceTokenizer.tokenize(value.trim())
        var width: String? = null
        var style: String? = null
        var color: String? = null

        for (token in tokens) {
            when {
                // Check if it's a border style keyword
                token.lowercase() in borderStyles -> {
                    if (style == null) style = token
                }
                // Check if it's a width keyword
                token.lowercase() in borderWidthKeywords -> {
                    if (width == null) width = token
                }
                // Check if it looks like a length (number + unit or just number)
                token.matches(Regex("^[0-9.]+[a-z%]*$")) -> {
                    if (width == null) width = token
                }
                // Otherwise assume it's a color
                else -> {
                    if (color == null) color = token
                }
            }
        }

        return createProperties(
            width ?: "medium",
            style ?: "none",
            color ?: "currentcolor"
        )
    }

    private fun createProperties(width: String, style: String, color: String): Map<String, String> {
        val suffix = if (side != null) "-$side" else ""
        return mapOf(
            "border$suffix-width" to width,
            "border$suffix-style" to style,
            "border$suffix-color" to color
        )
    }
}

// Shorthand expanders for all border sides
object BorderShorthandExpander : ShorthandExpander by BorderExpander()
object BorderTopExpander : ShorthandExpander by BorderExpander("top")
object BorderRightExpander : ShorthandExpander by BorderExpander("right")
object BorderBottomExpander : ShorthandExpander by BorderExpander("bottom")
object BorderLeftExpander : ShorthandExpander by BorderExpander("left")

/** Expands border-width shorthand (1-4 values). */
val BorderWidthExpander = FourValueExpander(
    "border-top-width",
    "border-right-width",
    "border-bottom-width",
    "border-left-width"
)

/** Expands border-style shorthand (1-4 values). */
val BorderStyleExpander = FourValueExpander(
    "border-top-style",
    "border-right-style",
    "border-bottom-style",
    "border-left-style"
)

/** Expands border-color shorthand (1-4 values). */
val BorderColorExpander = FourValueExpander(
    "border-top-color",
    "border-right-color",
    "border-bottom-color",
    "border-left-color"
)

/**
 * Expands border-radius shorthand (1-4 values, optionally with / for elliptical).
 * Examples:
 * - "10px" -> all corners 10px
 * - "10px 20px" -> top-left/bottom-right 10px, top-right/bottom-left 20px
 * - "10px / 20px" -> horizontal 10px, vertical 20px for all corners
 */
object BorderRadiusExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        // Check for elliptical syntax with /
        val parts = value.split("/").map { it.trim() }
        val horizontal = WhitespaceTokenizer.tokenize(parts[0])
        val vertical = if (parts.size > 1) WhitespaceTokenizer.tokenize(parts[1]) else horizontal

        fun getCornerValue(h: List<String>, v: List<String>, index: Int): String {
            val hValue = when (h.size) {
                1 -> h[0]
                2 -> if (index == 0 || index == 2) h[0] else h[1]
                3 -> when (index) {
                    0 -> h[0]
                    1, 3 -> h[1]
                    else -> h[2]
                }
                4 -> h[index]
                else -> h[0]
            }
            val vValue = when (v.size) {
                1 -> v[0]
                2 -> if (index == 0 || index == 2) v[0] else v[1]
                3 -> when (index) {
                    0 -> v[0]
                    1, 3 -> v[1]
                    else -> v[2]
                }
                4 -> v[index]
                else -> v[0]
            }
            return if (hValue == vValue) hValue else "$hValue $vValue"
        }

        return mapOf(
            "border-top-left-radius" to getCornerValue(horizontal, vertical, 0),
            "border-top-right-radius" to getCornerValue(horizontal, vertical, 1),
            "border-bottom-right-radius" to getCornerValue(horizontal, vertical, 2),
            "border-bottom-left-radius" to getCornerValue(horizontal, vertical, 3)
        )
    }
}

/**
 * Expands logical border shorthands (border-block, border-inline).
 */
class LogicalBorderExpander(private val axis: String) : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        val baseExpanded = BorderExpander().expand(value)
        return baseExpanded.flatMap { (key, v) ->
            val property = key.removePrefix("border-")
            listOf(
                "border-$axis-start-$property" to v,
                "border-$axis-end-$property" to v
            )
        }.toMap()
    }
}

object BorderBlockExpander : ShorthandExpander by LogicalBorderExpander("block")
object BorderInlineExpander : ShorthandExpander by LogicalBorderExpander("inline")
