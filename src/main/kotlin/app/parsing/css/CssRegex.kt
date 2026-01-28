package app.parsing.css

/**
 * Pre-compiled regex patterns for CSS parsing.
 *
 * These patterns are compiled once at class load time and reused,
 * avoiding the performance cost of recompiling on every parse call.
 *
 * Usage:
 * ```kotlin
 * // Instead of: value.split("""\s+""".toRegex())
 * // Use: value.split(CssRegex.WHITESPACE)
 * ```
 */
object CssRegex {
    /** Split by whitespace (one or more spaces, tabs, newlines) */
    val WHITESPACE = Regex("""\s+""")

    /** Split by comma with optional surrounding whitespace */
    val COMMA = Regex("""\s*,\s*""")

    /** Split by comma or whitespace */
    val COMMA_OR_WHITESPACE = Regex("""[,\s]+""")

    /** Match a CSS function call: name(args) */
    val FUNCTION = Regex("""(\w+[-\w]*)\(([^)]*)\)""")

    /** Match a CSS function with nested parentheses */
    val FUNCTION_NESTED = Regex("""(\w+[-\w]*)\((.+)\)""")

    /** Match a URL: url("...") or url('...') or url(...) */
    val URL = Regex("""url\(['"]?([^'")\s]+)['"]?\)""")

    /** Match a URL with optional suffix: url("...") something */
    val URL_WITH_SUFFIX = Regex("""url\(['"]?([^'")\s]+)['"]?\)(?:\s+(.+))?""")

    /** Match a CSS path: path("...") or path('...') */
    val PATH = Regex("""path\(['"](.+?)['"]\)""")

    /** Match a quoted string: "..." or '...' */
    val QUOTED_STRING = Regex(""""([^"]+)"""")

    /** Match a CSS identifier (property name, class name, etc.) */
    val IDENTIFIER = Regex("""^[a-zA-Z_-][a-zA-Z0-9_-]*$""")

    /** Match a CSS custom property: --name */
    val CUSTOM_PROPERTY = Regex("""^--[a-zA-Z_-][a-zA-Z0-9_-]*$""")

    /** Match a number with optional unit: 10px, 50%, 1.5em */
    val NUMBER_WITH_UNIT = Regex("""^([+-]?\d*\.?\d+)([a-z%]*)$""", RegexOption.IGNORE_CASE)

    /** Match a number (integer or decimal) */
    val NUMBER = Regex("""^[+-]?\d*\.?\d+$""")

    /** Match an integer */
    val INTEGER = Regex("""^[+-]?\d+$""")
}
