package app.parsing.css.properties.shorthand

import app.parsing.css.properties.tokenizers.WhitespaceTokenizer

/**
 * Expands border-image shorthand (complex).
 * Syntax: <source> || <slice> [ / <width> [ / <outset> ]? ]? || <repeat>
 *
 * Example: url(border.png) 30 / 20px / 10px round
 */
object BorderImageExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        val trimmed = value.trim()

        if (trimmed.lowercase() == "none") {
            return mapOf(
                "border-image-source" to "none",
                "border-image-slice" to "100%",
                "border-image-width" to "1",
                "border-image-outset" to "0",
                "border-image-repeat" to "stretch"
            )
        }

        // Simplified parser - detect source, slice, width, outset, repeat
        var source = "none"
        var slice = "100%"
        var width = "1"
        var outset = "0"
        var repeat = "stretch"

        // Look for url() or gradient
        if (trimmed.contains("url(") || trimmed.contains("gradient(")) {
            val sourceMatch = Regex("(url\\([^)]+\\)|[a-z-]+gradient\\([^)]+\\))").find(trimmed)
            if (sourceMatch != null) {
                source = sourceMatch.value
            }
        }

        // Look for repeat keywords
        val repeatKeywords = setOf("stretch", "repeat", "round", "space")
        for (keyword in repeatKeywords) {
            if (trimmed.contains(keyword)) {
                repeat = keyword
                break
            }
        }

        // Try to parse slash-separated values for slice/width/outset
        val slashParts = trimmed.split("/")
        if (slashParts.size >= 2) {
            slice = WhitespaceTokenizer.tokenize(slashParts[0].trim()).lastOrNull() ?: "100%"
            width = WhitespaceTokenizer.tokenize(slashParts[1].trim()).firstOrNull() ?: "1"
            if (slashParts.size >= 3) {
                outset = WhitespaceTokenizer.tokenize(slashParts[2].trim()).firstOrNull() ?: "0"
            }
        }

        return mapOf(
            "border-image-source" to source,
            "border-image-slice" to slice,
            "border-image-width" to width,
            "border-image-outset" to outset,
            "border-image-repeat" to repeat
        )
    }
}

/**
 * Expands mask-border shorthand (similar to border-image).
 */
object MaskBorderExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        // Delegate to border-image logic and rename properties
        val borderImage = BorderImageExpander.expand(value)
        return borderImage.mapKeys { (k, _) ->
            k.replace("border-image", "mask-border")
        }
    }
}

/**
 * Expands text-emphasis shorthand.
 * Syntax: <style> || <color>
 * Example: filled circle #ff0000
 */
object TextEmphasisExpander : ShorthandExpander {
    private val styles = setOf(
        "none", "filled", "open", "dot", "circle", "double-circle",
        "triangle", "sesame"
    )

    override fun expand(value: String): Map<String, String> {
        val tokens = WhitespaceTokenizer.tokenize(value.trim())
        var style = "none"
        var color = "currentcolor"

        for (token in tokens) {
            when {
                token.lowercase() in styles -> style = token
                else -> color = token // Assume it's a color
            }
        }

        return mapOf(
            "text-emphasis-style" to style,
            "text-emphasis-color" to color
        )
    }
}

/**
 * Expands marker shorthand (SVG).
 * Syntax: <marker-start> || <marker-mid> || <marker-end>
 * Usually same value for all three.
 */
object MarkerExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        return mapOf(
            "marker-start" to value,
            "marker-mid" to value,
            "marker-end" to value
        )
    }
}

/**
 * Expands container shorthand.
 * Syntax: <name> / <type>
 * Example: sidebar / inline-size
 */
object ContainerExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        val trimmed = value.trim()

        if (trimmed.lowercase() == "none") {
            return mapOf(
                "container-name" to "none",
                "container-type" to "normal"
            )
        }

        if (trimmed.contains("/")) {
            val parts = trimmed.split("/").map { it.trim() }
            return mapOf(
                "container-name" to (parts.getOrNull(0) ?: "none"),
                "container-type" to (parts.getOrNull(1) ?: "normal")
            )
        }

        // Single value - could be name or type
        val types = setOf("size", "inline-size", "normal")
        return if (trimmed.lowercase() in types) {
            mapOf(
                "container-name" to "none",
                "container-type" to trimmed
            )
        } else {
            mapOf(
                "container-name" to trimmed,
                "container-type" to "normal"
            )
        }
    }
}

/**
 * Expands font-synthesis shorthand.
 * Syntax: none | [ weight || style || small-caps ]
 */
object FontSynthesisExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        val trimmed = value.trim().lowercase()

        if (trimmed == "none") {
            return mapOf(
                "font-synthesis-weight" to "none",
                "font-synthesis-style" to "none",
                "font-synthesis-small-caps" to "none"
            )
        }

        val hasWeight = trimmed.contains("weight")
        val hasStyle = trimmed.contains("style")
        val hasSmallCaps = trimmed.contains("small-caps")

        return mapOf(
            "font-synthesis-weight" to (if (hasWeight) "auto" else "none"),
            "font-synthesis-style" to (if (hasStyle) "auto" else "none"),
            "font-synthesis-small-caps" to (if (hasSmallCaps) "auto" else "none")
        )
    }
}

/**
 * Expands font-variant shorthand (complex - many sub-properties).
 * For simplicity, handle common cases.
 */
object FontVariantExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        val trimmed = value.trim().lowercase()

        if (trimmed == "normal") {
            return mapOf(
                "font-variant-ligatures" to "normal",
                "font-variant-caps" to "normal",
                "font-variant-numeric" to "normal",
                "font-variant-alternates" to "normal",
                "font-variant-east-asian" to "normal"
            )
        }

        if (trimmed == "none") {
            return mapOf(
                "font-variant-ligatures" to "none",
                "font-variant-caps" to "normal",
                "font-variant-numeric" to "normal",
                "font-variant-alternates" to "normal",
                "font-variant-east-asian" to "normal"
            )
        }

        // Specific values
        val caps = if (trimmed.contains("small-caps")) "small-caps" else "normal"

        return mapOf(
            "font-variant-ligatures" to "normal",
            "font-variant-caps" to caps,
            "font-variant-numeric" to "normal",
            "font-variant-alternates" to "normal",
            "font-variant-east-asian" to "normal"
        )
    }
}

/**
 * Expands scroll-snap-type shorthand.
 * Syntax: none | [ x | y | block | inline | both ] [ mandatory | proximity ]?
 */
object ScrollSnapTypeExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        // This is actually not a true shorthand in most specs
        // but for completeness, we handle axis + strictness
        return mapOf("scroll-snap-type" to value)
    }
}

/**
 * Expands background-position shorthand (x/y).
 * Already handled by background, but can be standalone.
 */
object BackgroundPositionExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        val trimmed = value.trim()

        // Check for explicit x/y with slash
        if (trimmed.contains("/")) {
            val parts = trimmed.split("/").map { it.trim() }
            return mapOf(
                "background-position-x" to (parts.getOrNull(0) ?: "0%"),
                "background-position-y" to (parts.getOrNull(1) ?: "0%")
            )
        }

        // Space-separated x y values
        val tokens = WhitespaceTokenizer.tokenize(trimmed)
        return when (tokens.size) {
            1 -> {
                // Single value - check if it's a keyword
                when (tokens[0].lowercase()) {
                    "center" -> mapOf("background-position-x" to "center", "background-position-y" to "center")
                    "top" -> mapOf("background-position-x" to "center", "background-position-y" to "top")
                    "bottom" -> mapOf("background-position-x" to "center", "background-position-y" to "bottom")
                    "left" -> mapOf("background-position-x" to "left", "background-position-y" to "center")
                    "right" -> mapOf("background-position-x" to "right", "background-position-y" to "center")
                    else -> mapOf("background-position-x" to tokens[0], "background-position-y" to "center")
                }
            }
            2 -> mapOf(
                "background-position-x" to tokens[0],
                "background-position-y" to tokens[1]
            )
            else -> mapOf(
                "background-position-x" to "0%",
                "background-position-y" to "0%"
            )
        }
    }
}

/**
 * Expands text-wrap shorthand (modern CSS).
 * Syntax: wrap | nowrap | balance | pretty | stable
 */
object TextWrapExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        // text-wrap expands to text-wrap-mode and text-wrap-style
        val trimmed = value.trim().lowercase()

        return when (trimmed) {
            "wrap" -> mapOf("text-wrap-mode" to "wrap", "text-wrap-style" to "auto")
            "nowrap" -> mapOf("text-wrap-mode" to "nowrap", "text-wrap-style" to "auto")
            "balance" -> mapOf("text-wrap-mode" to "wrap", "text-wrap-style" to "balance")
            "pretty" -> mapOf("text-wrap-mode" to "wrap", "text-wrap-style" to "pretty")
            "stable" -> mapOf("text-wrap-mode" to "wrap", "text-wrap-style" to "stable")
            else -> mapOf("text-wrap-mode" to trimmed, "text-wrap-style" to "auto")
        }
    }
}

/**
 * Expands overscroll-behavior shorthand.
 * Syntax: <overscroll-behavior-x> <overscroll-behavior-y>?
 */
object OverscrollBehaviorExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        val tokens = WhitespaceTokenizer.tokenize(value.trim())
        return when (tokens.size) {
            1 -> mapOf(
                "overscroll-behavior-x" to tokens[0],
                "overscroll-behavior-y" to tokens[0]
            )
            2 -> mapOf(
                "overscroll-behavior-x" to tokens[0],
                "overscroll-behavior-y" to tokens[1]
            )
            else -> emptyMap()
        }
    }
}

/**
 * Expands scroll-timeline shorthand.
 * Syntax: <name> <axis>?
 */
object ScrollTimelineExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        val tokens = WhitespaceTokenizer.tokenize(value.trim())
        return when (tokens.size) {
            1 -> mapOf(
                "scroll-timeline-name" to tokens[0],
                "scroll-timeline-axis" to "block"
            )
            2 -> mapOf(
                "scroll-timeline-name" to tokens[0],
                "scroll-timeline-axis" to tokens[1]
            )
            else -> emptyMap()
        }
    }
}

/**
 * Expands border-block-width shorthand.
 * Syntax: <width-start> <width-end>?
 */
val BorderBlockWidthExpander = TwoValueExpander("border-block-start-width", "border-block-end-width")

/**
 * Expands border-block-style shorthand.
 * Syntax: <style-start> <style-end>?
 */
val BorderBlockStyleExpander = TwoValueExpander("border-block-start-style", "border-block-end-style")

/** Expands border-block-color shorthand. Syntax: <color-start> <color-end>? */
val BorderBlockColorExpander = TwoValueExpander("border-block-start-color", "border-block-end-color")

/** Expands border-inline-width shorthand. Syntax: <width-start> <width-end>? */
val BorderInlineWidthExpander = TwoValueExpander("border-inline-start-width", "border-inline-end-width")

/** Expands border-inline-style shorthand. Syntax: <style-start> <style-end>? */
val BorderInlineStyleExpander = TwoValueExpander("border-inline-start-style", "border-inline-end-style")

/** Expands border-inline-color shorthand. Syntax: <color-start> <color-end>? */
val BorderInlineColorExpander = TwoValueExpander("border-inline-start-color", "border-inline-end-color")

/** Expands margin-block shorthand. Syntax: <margin-start> <margin-end>? */
val MarginBlockExpander = TwoValueExpander("margin-block-start", "margin-block-end")

/** Expands margin-inline shorthand. Syntax: <margin-start> <margin-end>? */
val MarginInlineExpander = TwoValueExpander("margin-inline-start", "margin-inline-end")

/** Expands padding-block shorthand. Syntax: <padding-start> <padding-end>? */
val PaddingBlockExpander = TwoValueExpander("padding-block-start", "padding-block-end")

/** Expands padding-inline shorthand. Syntax: <padding-start> <padding-end>? */
val PaddingInlineExpander = TwoValueExpander("padding-inline-start", "padding-inline-end")

/** Expands scroll-margin-block shorthand. Syntax: <margin-start> <margin-end>? */
val ScrollMarginBlockExpander = TwoValueExpander("scroll-margin-block-start", "scroll-margin-block-end")

/** Expands scroll-margin-inline shorthand. Syntax: <margin-start> <margin-end>? */
val ScrollMarginInlineExpander = TwoValueExpander("scroll-margin-inline-start", "scroll-margin-inline-end")

/** Expands scroll-padding-block shorthand. Syntax: <padding-start> <padding-end>? */
val ScrollPaddingBlockExpander = TwoValueExpander("scroll-padding-block-start", "scroll-padding-block-end")

/** Expands scroll-padding-inline shorthand. Syntax: <padding-start> <padding-end>? */
val ScrollPaddingInlineExpander = TwoValueExpander("scroll-padding-inline-start", "scroll-padding-inline-end")

/**
 * Expands overflow-clip-margin shorthand (not really a shorthand but included for completeness).
 * Syntax: <visual-box> || <length>
 */
object OverflowClipMarginExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        // This is actually a longhand, but keeping for consistency
        return mapOf("overflow-clip-margin" to value)
    }
}
