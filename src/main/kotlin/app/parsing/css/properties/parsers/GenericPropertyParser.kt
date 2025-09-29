package app.parsing.css.properties.parsers

import app.*
import app.parsing.css.properties.parsers.primitiveParsers.Primitive
import app.parsing.css.properties.parsers.primitiveParsers.PrimitiveParser

object GenericPropertyParser : CssPropertyParser {

    private fun tokenizeCssValue(value: String): List<String> {
        val tokens = mutableListOf<String>()
        var current = StringBuilder()
        var depth = 0

        for (c in value) {
            when {
                c == '(' -> { depth++; current.append(c) }
                c == ')' -> { depth--; current.append(c) }
                c.isWhitespace() && depth == 0 -> {
                    if (current.isNotBlank()) { tokens.add(current.toString()); current = StringBuilder() }
                }
                else -> current.append(c)
            }
        }
        if (current.isNotBlank()) tokens.add(current.toString())
        return tokens
    }

    override fun parse(propertyName: String, value: Any): IRProperty {
        val lengths = mutableListOf<IRLength>()
        val colors = mutableListOf<IRColor>()
        val urls = mutableListOf<IRUrl>()
        val keywords = mutableListOf<IRKeyword>()
        val shadows = mutableListOf<IRShadow>()
        val raw = value.toString()

        val tokens = tokenizeCssValue(raw)

        for (t in tokens) {
            when (val primitive = PrimitiveParser.parse(t)) {
                is Primitive.Length -> lengths.add(primitive.value)
                is Primitive.Color -> colors.add(primitive.value)
                is Primitive.Url -> urls.add(primitive.value)
                is Primitive.Keyword -> keywords.add(primitive.value)
                is Primitive.Shadow -> shadows.add(primitive.value)
                Primitive.Unknown -> { /* fallback */ }
            }
        }

        return IRProperty(
            propertyName = propertyName,
            lengths = lengths,
            colors = colors,
            urls = urls,
            keywords = keywords,
            shadows = shadows,
            raw = raw
        )
    }
}