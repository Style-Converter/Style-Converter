package app.parsing.css.properties.parsers.primitiveParsers

import app.IRUrl

object UrlParser {
    private val urlRegex = Regex("""^url\((.*)\)$""", RegexOption.IGNORE_CASE)

    fun parse(token: String): IRUrl? {
        val t = token.trim()

        // Gradients or image functions
        FunctionParser.parse(t)?.let { fn ->
            if (fn.name.contains("gradient") || fn.name == "image") {
                return IRUrl(function = fn)
            }
        }

        // Plain url()
        val match = urlRegex.matchEntire(t)
        if (match != null) {
            val inner = match.groupValues[1].trim().trim('"', '\'')
            return IRUrl(url = inner)
        }

        return null
    }
}