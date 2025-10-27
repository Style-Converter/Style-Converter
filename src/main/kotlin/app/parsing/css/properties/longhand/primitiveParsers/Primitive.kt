package app.parsing.css.properties.longhand.primitiveParsers

import app.*

/**
 * Represents a parsed CSS primitive value.
 */
sealed class Primitive {
    data class Length(val value: IRLength) : Primitive()
    data class Color(val value: IRColor) : Primitive()
    data class Url(val value: IRUrl) : Primitive()
    data class Keyword(val value: IRKeyword) : Primitive()
    data class Shadow(val value: IRShadow) : Primitive()
    object Unknown : Primitive()
}
