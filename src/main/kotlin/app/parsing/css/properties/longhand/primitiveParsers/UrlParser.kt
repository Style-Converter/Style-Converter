package app.parsing.css.properties.longhand.primitiveParsers

import app.IRUrl
import app.parsing.css.properties.tokenizers.CommaTokenizer

object UrlParser {
    private val urlRegex = Regex("""^url\((.*)\)$""", RegexOption.IGNORE_CASE)

    private fun detectScheme(value: String): String {
        return when {
            value.startsWith("#") -> "fragment"
            value.startsWith("data:") -> "data"
            value.startsWith("http://") -> "http"
            value.startsWith("https://") -> "https"
            value.startsWith("ftp://") -> "ftp"
            value.startsWith("mailto:") -> "mailto"
            else -> "relative"
        }
    }

    fun parseMultiple(token: String): List<IRUrl> {
        val parts = CommaTokenizer.tokenize(token)
        return parts.mapNotNull { s ->
            val trimmed = s.trim()
            val match = urlRegex.matchEntire(trimmed) ?: return@mapNotNull null
            var inner = match.groupValues[1].trim()
            val rawInner = inner
            var quoted = false
            if ((inner.startsWith("'") && inner.endsWith("'")) ||
                (inner.startsWith("\"") && inner.endsWith("\""))
            ) {
                quoted = true
                inner = inner.substring(1, inner.length - 1)
            }
            inner = inner.replace("\\\"", "\"").replace("\\'", "'")
            IRUrl(
                raw = rawInner,
                value = inner,
                scheme = detectScheme(inner),
                quoted = quoted
            )
        }
    }

    fun parse(tokens: List<String>): IRUrl? {
        // Use only the first token
        val token = tokens.firstOrNull() ?: return null
        return parseMultiple(token).firstOrNull()
    }
}
