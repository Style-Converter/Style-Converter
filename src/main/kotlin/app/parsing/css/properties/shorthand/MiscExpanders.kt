package app.parsing.css.properties.shorthand

import app.parsing.css.properties.tokenizers.WhitespaceTokenizer

/**
 * Background shorthand is extremely complex. For now, we'll do a simplified version
 * that handles common cases. Full syntax supports multiple layers separated by commas.
 *
 * Simplified syntax per layer:
 * <bg-color> || <bg-image> || <bg-position> [ / <bg-size> ]? || <bg-repeat> || <bg-attachment> || <bg-origin> || <bg-clip>
 */
object BackgroundExpander : ShorthandExpander {
    private val repeatValues = setOf("repeat", "repeat-x", "repeat-y", "no-repeat", "space", "round")
    private val attachmentValues = setOf("scroll", "fixed", "local")
    private val boxValues = setOf("border-box", "padding-box", "content-box", "text")

    override fun expand(value: String): Map<String, String> {
        // For simplicity, if there are multiple layers (comma), keep as composite
        if (value.count { it == ',' } > 0) {
            // Multiple backgrounds - too complex for full expansion
            // Just extract what we can
            return mapOf(
                "background-image" to value,
                "background-position" to "0% 0%",
                "background-size" to "auto",
                "background-repeat" to "repeat",
                "background-attachment" to "scroll",
                "background-origin" to "padding-box",
                "background-clip" to "border-box",
                "background-color" to "transparent"
            )
        }

        val trimmed = value.trim()
        var color = "transparent"
        var image = "none"
        var position = "0% 0%"
        var size = "auto"
        var repeat = "repeat"
        var attachment = "scroll"
        var origin = "padding-box"
        var clip = "border-box"

        // Simple keyword shortcuts
        when (trimmed.lowercase()) {
            "none" -> {
                image = "none"
                color = "transparent"
            }
            else -> {
                val tokens = WhitespaceTokenizer.tokenize(trimmed)
                var i = 0

                while (i < tokens.size) {
                    val token = tokens[i]

                    when {
                        // Color (hex, rgb, named)
                        token.startsWith("#") || token.startsWith("rgb") ||
                        token.startsWith("hsl") || token.matches(Regex("^[a-z]+$")) -> {
                            // Could be color or keyword, check if it's a known keyword first
                            when (token.lowercase()) {
                                in repeatValues -> repeat = token
                                in attachmentValues -> attachment = token
                                in boxValues -> {
                                    if (clip == "border-box") clip = token
                                    else origin = token
                                }
                                else -> color = token
                            }
                        }

                        // URL (image)
                        token.startsWith("url(") -> image = token

                        // Gradient functions
                        token.contains("gradient(") -> image = token

                        // Position with slash for size
                        token.contains("/") -> {
                            val parts = token.split("/")
                            position = parts[0]
                            size = parts.getOrNull(1) ?: "auto"
                        }

                        // Length or percentage (could be position or size)
                        token.matches(Regex("^[0-9.]+[a-z%]*$")) -> {
                            // Try to determine if it's position
                            if (position == "0% 0%") {
                                position = "$token ${tokens.getOrNull(i + 1) ?: "0%"}"
                                if (i + 1 < tokens.size) i++ // Skip next token
                            }
                        }

                        else -> {
                            // Try to match keywords
                            when (token.lowercase()) {
                                in repeatValues -> repeat = token
                                in attachmentValues -> attachment = token
                                in boxValues -> {
                                    if (clip == "border-box") clip = token
                                    else origin = token
                                }
                            }
                        }
                    }
                    i++
                }
            }
        }

        return mapOf(
            "background-color" to color,
            "background-image" to image,
            "background-position" to position,
            "background-size" to size,
            "background-repeat" to repeat,
            "background-attachment" to attachment,
            "background-origin" to origin,
            "background-clip" to clip
        )
    }
}

/**
 * Expands mask shorthand (similar to background).
 * Simplified version.
 */
object MaskExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        // Very simplified - just pass most values through
        return mapOf(
            "mask-image" to value,
            "mask-position" to "center",
            "mask-size" to "auto",
            "mask-repeat" to "no-repeat",
            "mask-origin" to "border-box",
            "mask-clip" to "border-box",
            "mask-mode" to "match-source"
        )
    }
}

/**
 * Expands offset shorthand (motion path).
 * Syntax: <offset-position> || <offset-path> [ <offset-distance> || <offset-rotate> ]? || <offset-anchor>
 */
object OffsetExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        // Simplified - just keep the value
        return mapOf(
            "offset-path" to value,
            "offset-distance" to "0",
            "offset-rotate" to "auto",
            "offset-anchor" to "auto",
            "offset-position" to "auto"
        )
    }
}

/**
 * Expands all shorthand (resets all properties).
 */
object AllExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        // 'all' is special - it should reset everything
        // For now, we just pass it through
        return mapOf("all" to value)
    }
}

/** Expands inset-block shorthand. Syntax: <top> <bottom>? or single value */
val InsetBlockExpander = TwoValueExpander("inset-block-start", "inset-block-end")

/** Expands inset-inline shorthand. Syntax: <left> <right>? or single value */
val InsetInlineExpander = TwoValueExpander("inset-inline-start", "inset-inline-end")

/**
 * Expands border-block-start shorthand.
 */
object BorderBlockStartExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        val base = BorderExpander().expand(value)
        return base.mapKeys { (k, _) ->
            k.replace("border-", "border-block-start-")
        }
    }
}

/**
 * Expands border-block-end shorthand.
 */
object BorderBlockEndExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        val base = BorderExpander().expand(value)
        return base.mapKeys { (k, _) ->
            k.replace("border-", "border-block-end-")
        }
    }
}

/**
 * Expands border-inline-start shorthand.
 */
object BorderInlineStartExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        val base = BorderExpander().expand(value)
        return base.mapKeys { (k, _) ->
            k.replace("border-", "border-inline-start-")
        }
    }
}

/**
 * Expands border-inline-end shorthand.
 */
object BorderInlineEndExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        val base = BorderExpander().expand(value)
        return base.mapKeys { (k, _) ->
            k.replace("border-", "border-inline-end-")
        }
    }
}

/** Expands scroll-margin shorthand (1-4 values like margin). */
val ScrollMarginExpander = FourValueExpander(
    "scroll-margin-top",
    "scroll-margin-right",
    "scroll-margin-bottom",
    "scroll-margin-left"
)

/** Expands scroll-padding shorthand (1-4 values like padding). */
val ScrollPaddingExpander = FourValueExpander(
    "scroll-padding-top",
    "scroll-padding-right",
    "scroll-padding-bottom",
    "scroll-padding-left"
)
