package com.styleconverter.test.style.spacing

/**
 * Margin trim value options.
 */
enum class MarginTrimValue {
    NONE,
    BLOCK,
    INLINE,
    BLOCK_START,
    BLOCK_END,
    INLINE_START,
    INLINE_END
}

/**
 * Configuration for CSS margin-trim property.
 * Controls which child margins are trimmed.
 */
data class MarginTrimConfig(
    val marginTrim: Set<MarginTrimValue> = setOf(MarginTrimValue.NONE)
) {
    val hasMarginTrim: Boolean
        get() = marginTrim.isNotEmpty() && !marginTrim.contains(MarginTrimValue.NONE)
}
