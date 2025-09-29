package app.parsing.css.properties.parsers.primitiveParsers

import app.*

sealed class Primitive {
    data class Length(val value: IRLength) : Primitive()
    data class Color(val value: IRColor) : Primitive()
    data class Url(val value: IRUrl) : Primitive()
    data class Keyword(val value: IRKeyword) : Primitive()
    data class Shadow(val value: IRShadow) : Primitive()
    object Unknown : Primitive()
}

object PrimitiveParser {

    private val defaultOrder: List<(String) -> Primitive?> = listOf(
        { token -> LengthParser.parse(token)?.let { Primitive.Length(it) } },
        { token -> UrlParser.parse(token)?.let { Primitive.Url(it) } },
        { token -> ColorParser.parse(token)?.let { Primitive.Color(it) } },
        { token -> KeywordParser.parse(token)?.let { Primitive.Keyword(it) } }
    )

    fun parse(token: String, order: List<(String) -> Primitive?> = defaultOrder): Primitive {
        for (parser in order) parser(token)?.let { return it }
        return Primitive.Unknown
    }
}