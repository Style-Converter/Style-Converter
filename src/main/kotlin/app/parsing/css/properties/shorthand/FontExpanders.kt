package app.parsing.css.properties.shorthand

import app.parsing.css.properties.tokenizers.WhitespaceTokenizer

/**
 * Expands font shorthand property.
 *
 * Font syntax (complex):
 * [ <font-style> || <font-variant> || <font-weight> || <font-stretch> ]?
 * <font-size> [ / <line-height> ]? <font-family>
 *
 * Examples:
 * - "italic bold 16px/1.5 Arial, sans-serif"
 * - "12px/14px Helvetica"
 * - "bold 1.2em 'Times New Roman', serif"
 */
object FontExpander : ShorthandExpander {
    private val fontStyles = setOf("normal", "italic", "oblique")
    private val fontVariants = setOf("normal", "small-caps")
    private val fontWeights = setOf(
        "normal", "bold", "bolder", "lighter",
        "100", "200", "300", "400", "500", "600", "700", "800", "900"
    )
    private val fontStretches = setOf(
        "normal", "ultra-condensed", "extra-condensed", "condensed", "semi-condensed",
        "semi-expanded", "expanded", "extra-expanded", "ultra-expanded"
    )

    override fun expand(value: String): Map<String, String> {
        val trimmed = value.trim()

        // Handle system font keywords
        val systemFonts = setOf(
            "caption", "icon", "menu", "message-box", "small-caption", "status-bar"
        )
        if (trimmed in systemFonts) {
            return mapOf("font" to trimmed) // Keep as-is for system fonts
        }

        val tokens = WhitespaceTokenizer.tokenize(trimmed)
        var style = "normal"
        var variant = "normal"
        var weight = "normal"
        var stretch = "normal"
        var size: String? = null
        var lineHeight: String? = null
        val familyTokens = mutableListOf<String>()

        var i = 0

        // Parse optional style/variant/weight/stretch (order doesn't matter)
        while (i < tokens.size) {
            val token = tokens[i].lowercase()
            when {
                token in fontStyles && style == "normal" -> style = tokens[i]
                token in fontVariants && variant == "normal" -> variant = tokens[i]
                token in fontWeights && weight == "normal" -> weight = tokens[i]
                token.matches(Regex("^[0-9]+$")) && weight == "normal" -> weight = tokens[i]
                token in fontStretches && stretch == "normal" -> stretch = tokens[i]
                else -> break // Found size
            }
            i++
        }

        // Parse required size (and optional line-height)
        if (i < tokens.size) {
            val sizeToken = tokens[i]
            if (sizeToken.contains("/")) {
                val parts = sizeToken.split("/")
                size = parts[0]
                lineHeight = parts.getOrNull(1) ?: "normal"
            } else {
                size = sizeToken
                lineHeight = "normal"
            }
            i++
        }

        // Remaining tokens are font-family
        while (i < tokens.size) {
            familyTokens.add(tokens[i])
            i++
        }

        val family = if (familyTokens.isNotEmpty()) {
            familyTokens.joinToString(" ")
        } else {
            "serif" // Default fallback
        }

        return mapOf(
            "font-style" to style,
            "font-variant" to variant,
            "font-weight" to weight,
            "font-stretch" to stretch,
            "font-size" to (size ?: "medium"),
            "line-height" to lineHeight!!,
            "font-family" to family
        )
    }
}

/**
 * Expands list-style shorthand.
 * Syntax: <list-style-type> || <list-style-position> || <list-style-image>
 */
object ListStyleExpander : ShorthandExpander {
    private val types = setOf(
        "none", "disc", "circle", "square", "decimal", "decimal-leading-zero",
        "lower-roman", "upper-roman", "lower-greek", "lower-latin", "upper-latin",
        "armenian", "georgian", "lower-alpha", "upper-alpha"
    )
    private val positions = setOf("inside", "outside")

    override fun expand(value: String): Map<String, String> {
        val tokens = WhitespaceTokenizer.tokenize(value.trim())
        var type = "disc"
        var position = "outside"
        var image = "none"

        for (token in tokens) {
            when {
                token.lowercase() in types -> type = token
                token.lowercase() in positions -> position = token
                token.startsWith("url(") -> image = token
                token.lowercase() == "none" -> {
                    type = "none"
                    image = "none"
                }
            }
        }

        return mapOf(
            "list-style-type" to type,
            "list-style-position" to position,
            "list-style-image" to image
        )
    }
}

/**
 * Expands text-decoration shorthand.
 * Syntax: <line> || <style> || <color> || <thickness>
 */
object TextDecorationExpander : ShorthandExpander {
    private val lines = setOf("none", "underline", "overline", "line-through", "blink")
    private val styles = setOf("solid", "double", "dotted", "dashed", "wavy")

    override fun expand(value: String): Map<String, String> {
        val tokens = WhitespaceTokenizer.tokenize(value.trim())
        var line = "none"
        var style = "solid"
        var color = "currentcolor"
        var thickness = "auto"

        for (token in tokens) {
            when {
                token.lowercase() in lines -> line = token
                token.lowercase() in styles -> style = token
                token.matches(Regex("^[0-9.]+[a-z%]*$")) -> thickness = token
                else -> color = token // Assume it's a color
            }
        }

        return mapOf(
            "text-decoration-line" to line,
            "text-decoration-style" to style,
            "text-decoration-color" to color,
            "text-decoration-thickness" to thickness
        )
    }
}

/**
 * Expands columns shorthand.
 * Syntax: <column-width> || <column-count>
 */
object ColumnsExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        val tokens = WhitespaceTokenizer.tokenize(value.trim())
        var width = "auto"
        var count = "auto"

        for (token in tokens) {
            when {
                token.matches(Regex("^[0-9]+$")) -> count = token
                token.matches(Regex("^[0-9.]+[a-z%]+$")) -> width = token
                token.lowercase() == "auto" -> { /* keep defaults */ }
            }
        }

        return mapOf(
            "column-width" to width,
            "column-count" to count
        )
    }
}

/**
 * Expands column-rule shorthand.
 * Syntax: <width> || <style> || <color>
 */
object ColumnRuleExpander : ShorthandExpander {
    private val styles = setOf(
        "none", "hidden", "dotted", "dashed", "solid", "double",
        "groove", "ridge", "inset", "outset"
    )
    private val widthKeywords = setOf("thin", "medium", "thick")

    override fun expand(value: String): Map<String, String> {
        val tokens = WhitespaceTokenizer.tokenize(value.trim())
        var width = "medium"
        var style = "none"
        var color = "currentcolor"

        for (token in tokens) {
            when {
                token.lowercase() in styles -> style = token
                token.lowercase() in widthKeywords -> width = token
                token.matches(Regex("^[0-9.]+[a-z%]*$")) -> width = token
                else -> color = token
            }
        }

        return mapOf(
            "column-rule-width" to width,
            "column-rule-style" to style,
            "column-rule-color" to color
        )
    }
}
