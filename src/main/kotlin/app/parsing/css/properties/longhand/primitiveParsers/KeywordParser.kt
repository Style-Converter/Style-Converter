package app.parsing.css.properties.longhand.primitiveParsers

import app.IRKeyword

object KeywordParser {
    // Allow letters, digits, hyphens, underscores, and spaces
    private val keywordRegex = Regex("""^[a-zA-Z0-9\-_ ]+$""")

    fun parse(tokens: List<String>): IRKeyword? {
        // Use only the first token
        val token = tokens.firstOrNull() ?: return null
        var t = token.trim()

        // Strip trailing commas before anything else
        if (t.endsWith(",")) {
            t = t.removeSuffix(",").trim()
        }

        val lower = t.lowercase()

        // Only accept tokens that match the keyword regex
        if (keywordRegex.matches(lower)) {
            // Normalize multiple spaces to a single space
            val normalized = lower.split(Regex("""\s+"""))
                .filter { it.isNotEmpty() }
                .joinToString(" ")

            return IRKeyword(value = normalized)
        }

        // Accept quoted font names as keywords too
        if (t.startsWith("\"") || t.startsWith("'")) {
            return IRKeyword(
                value = t.trim('"', '\'').removeSuffix(",").trim()
            )
        }

        return null
    }
}
