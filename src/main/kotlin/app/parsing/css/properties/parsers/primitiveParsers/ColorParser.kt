package app.parsing.css.properties.parsers.primitiveParsers

import app.*

object ColorParser {
    // Hex codes (#FFF, #123456, #11223344)
    private val hexRegex = Regex("""^#([0-9a-fA-F]{3,8})$""")

    // Generic function pattern (rgb(), rgba(), hsl(), hsla(), color-mix(), etc.)
    private val funcRegex = Regex("""^([a-zA-Z\-_]+)\((.*)\)$""")

    fun parse(token: String): IRColor? {
        val trimmed = token.trim()

        // Hex codes
        hexRegex.matchEntire(trimmed)?.let {
            return IRColor(raw = trimmed)
        }

        // Functional colors
        funcRegex.matchEntire(trimmed)?.let { match ->
            val name = match.groupValues[1].lowercase()
            val inner = match.groupValues[2]

            val args = FunctionParser.parse("$name($inner)")?.args ?: emptyList()
            return IRColor(raw = trimmed, function = IRFunction(name = name, args = args))
        }

        // Everything else (including named colors) is handled by KeywordParser
        return null
    }
}
