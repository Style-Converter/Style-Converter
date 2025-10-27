package app.parsing.css.properties.shorthand

/**
 * Interface for CSS shorthand property expanders.
 * Each expander converts a shorthand property into its constituent longhand properties.
 */
interface ShorthandExpander {
    /**
     * Expand a shorthand property value into longhand properties.
     *
     * @param value The shorthand property value
     * @return Map of longhand property names to their values
     */
    fun expand(value: String): Map<String, String>
}
