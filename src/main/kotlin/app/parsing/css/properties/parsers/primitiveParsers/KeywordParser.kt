package app.parsing.css.properties.parsers.primitiveParsers

import app.IRKeyword

object KeywordParser {
    private val keywordRegex = Regex("""^[a-zA-Z\-\_]+$""")

    fun parse(token: String): IRKeyword? {
        val t = token.trim().lowercase()

        // Accept typical CSS keywords (auto, block, inherit, etc.)
        if (keywordRegex.matches(t)) {
            return IRKeyword(value = t)
        }

        // Accept quoted font names as keywords too
        if (t.startsWith("\"") || t.startsWith("'")) {
            return IRKeyword(value = t.trim('"', '\''))
        }

        return null
    }
}