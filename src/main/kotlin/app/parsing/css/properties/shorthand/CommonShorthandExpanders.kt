package app.parsing.css.properties.shorthand

import app.parsing.css.properties.tokenizers.WhitespaceTokenizer
import app.parsing.css.properties.tokenizers.CommaTokenizer

/**
 * Generic expander for properties that take 1-2 values (start, end pattern).
 * Examples: margin-block, padding-inline, border-block-width, etc.
 *
 * @param startProperty The property name for the start side (e.g., "margin-block-start")
 * @param endProperty The property name for the end side (e.g., "margin-block-end")
 */
class TwoValueExpander(
    private val startProperty: String,
    private val endProperty: String
) : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        val tokens = WhitespaceTokenizer.tokenize(value.trim())
        return when (tokens.size) {
            1 -> mapOf(
                startProperty to tokens[0],
                endProperty to tokens[0]
            )
            2 -> mapOf(
                startProperty to tokens[0],
                endProperty to tokens[1]
            )
            else -> emptyMap()
        }
    }
}

/**
 * Generic expander for properties that take 1-4 values (top, right, bottom, left pattern).
 * Examples: margin, padding, border-width, border-style, border-color, inset, etc.
 *
 * @param topProperty The property name for the top side
 * @param rightProperty The property name for the right side
 * @param bottomProperty The property name for the bottom side
 * @param leftProperty The property name for the left side
 */
class FourValueExpander(
    private val topProperty: String,
    private val rightProperty: String,
    private val bottomProperty: String,
    private val leftProperty: String
) : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        val tokens = WhitespaceTokenizer.tokenize(value.trim())
        return when (tokens.size) {
            1 -> mapOf(
                topProperty to tokens[0],
                rightProperty to tokens[0],
                bottomProperty to tokens[0],
                leftProperty to tokens[0]
            )
            2 -> mapOf(
                topProperty to tokens[0],
                rightProperty to tokens[1],
                bottomProperty to tokens[0],
                leftProperty to tokens[1]
            )
            3 -> mapOf(
                topProperty to tokens[0],
                rightProperty to tokens[1],
                bottomProperty to tokens[2],
                leftProperty to tokens[1]
            )
            4 -> mapOf(
                topProperty to tokens[0],
                rightProperty to tokens[1],
                bottomProperty to tokens[2],
                leftProperty to tokens[3]
            )
            else -> emptyMap()
        }
    }
}

/**
 * Base class for multi-value shorthand expanders (comma-separated values).
 * Handles the common pattern of splitting by comma and either processing each item
 * individually or extracting first tokens for simplified multi-value expansion.
 *
 * Examples: animation, transition, background-image, etc.
 */
abstract class MultiValueShorthandExpander : ShorthandExpander {

    /**
     * Expand a single (non-comma-separated) value into longhand properties.
     */
    abstract fun expandSingle(tokens: List<String>): Map<String, String>

    /**
     * Create simplified longhand properties for multi-value cases.
     * Default implementation returns empty map (override if needed).
     *
     * @param items The comma-separated items
     * @return Map of property names to values
     */
    open fun expandMultiple(items: List<String>): Map<String, String> = emptyMap()

    override fun expand(value: String): Map<String, String> {
        val items = CommaTokenizer.tokenize(value)

        if (items.size > 1) {
            return expandMultiple(items)
        }

        return expandSingle(WhitespaceTokenizer.tokenize(value.trim()))
    }
}
