package app.parsing.css.properties.shorthand

import app.parsing.css.properties.tokenizers.WhitespaceTokenizer

/**
 * Expands outline shorthand (similar to border).
 * Syntax: <width> <style> <color>
 */
object OutlineExpander : ShorthandExpander {
    private val outlineStyles = setOf(
        "none", "hidden", "dotted", "dashed", "solid", "double",
        "groove", "ridge", "inset", "outset", "auto"
    )
    private val widthKeywords = setOf("thin", "medium", "thick")

    override fun expand(value: String): Map<String, String> {
        val tokens = WhitespaceTokenizer.tokenize(value.trim())
        var width: String? = null
        var style: String? = null
        var color: String? = null

        for (token in tokens) {
            when {
                token.lowercase() in outlineStyles -> {
                    if (style == null) style = token
                }
                token.lowercase() in widthKeywords -> {
                    if (width == null) width = token
                }
                token.matches(Regex("^[0-9.]+[a-z%]*$")) -> {
                    if (width == null) width = token
                }
                else -> {
                    if (color == null) color = token
                }
            }
        }

        return mapOf(
            "outline-width" to (width ?: "medium"),
            "outline-style" to (style ?: "none"),
            "outline-color" to (color ?: "currentcolor")
        )
    }
}

/**
 * Expands flex shorthand.
 * Syntax: <flex-grow> <flex-shrink>? <flex-basis>?
 * Common values: "1", "1 1 auto", "0 0 auto", "none" (0 0 auto), "auto" (1 1 auto)
 */
object FlexExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        val trimmed = value.trim()

        // Handle keyword values
        when (trimmed.lowercase()) {
            "none" -> return mapOf(
                "flex-grow" to "0",
                "flex-shrink" to "0",
                "flex-basis" to "auto"
            )
            "auto" -> return mapOf(
                "flex-grow" to "1",
                "flex-shrink" to "1",
                "flex-basis" to "auto"
            )
            "initial" -> return mapOf(
                "flex-grow" to "0",
                "flex-shrink" to "1",
                "flex-basis" to "auto"
            )
        }

        val tokens = WhitespaceTokenizer.tokenize(trimmed)

        return when (tokens.size) {
            1 -> {
                // Single value: could be grow (unitless) or basis (with unit)
                if (tokens[0].matches(Regex("^[0-9.]+$"))) {
                    mapOf(
                        "flex-grow" to tokens[0],
                        "flex-shrink" to "1",
                        "flex-basis" to "0"
                    )
                } else {
                    mapOf(
                        "flex-grow" to "1",
                        "flex-shrink" to "1",
                        "flex-basis" to tokens[0]
                    )
                }
            }
            2 -> {
                // Two values: grow shrink OR grow basis
                if (tokens[1].matches(Regex("^[0-9.]+$"))) {
                    // grow shrink
                    mapOf(
                        "flex-grow" to tokens[0],
                        "flex-shrink" to tokens[1],
                        "flex-basis" to "0"
                    )
                } else {
                    // grow basis
                    mapOf(
                        "flex-grow" to tokens[0],
                        "flex-shrink" to "1",
                        "flex-basis" to tokens[1]
                    )
                }
            }
            3 -> {
                // All three values
                mapOf(
                    "flex-grow" to tokens[0],
                    "flex-shrink" to tokens[1],
                    "flex-basis" to tokens[2]
                )
            }
            else -> emptyMap()
        }
    }
}

/**
 * Expands flex-flow shorthand.
 * Syntax: <flex-direction> <flex-wrap>?
 */
object FlexFlowExpander : ShorthandExpander {
    private val directions = setOf("row", "row-reverse", "column", "column-reverse")
    private val wraps = setOf("nowrap", "wrap", "wrap-reverse")

    override fun expand(value: String): Map<String, String> {
        val tokens = WhitespaceTokenizer.tokenize(value.trim())
        var direction = "row"
        var wrap = "nowrap"

        for (token in tokens) {
            when {
                token.lowercase() in directions -> direction = token
                token.lowercase() in wraps -> wrap = token
            }
        }

        return mapOf(
            "flex-direction" to direction,
            "flex-wrap" to wrap
        )
    }
}

/** Expands gap shorthand (row-gap column-gap). */
val GapExpander = TwoValueExpander("row-gap", "column-gap")

/** Expands overflow shorthand (overflow-x overflow-y). */
val OverflowExpander = TwoValueExpander("overflow-x", "overflow-y")

/** Expands inset shorthand (top right bottom left). */
val InsetExpander = FourValueExpander("top", "right", "bottom", "left")

/** Expands place-items shorthand (align-items justify-items). */
val PlaceItemsExpander = TwoValueExpander("align-items", "justify-items")

/** Expands place-content shorthand (align-content justify-content). */
val PlaceContentExpander = TwoValueExpander("align-content", "justify-content")

/** Expands place-self shorthand (align-self justify-self). */
val PlaceSelfExpander = TwoValueExpander("align-self", "justify-self")
