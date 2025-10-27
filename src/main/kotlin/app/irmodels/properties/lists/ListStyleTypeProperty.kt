package app.irmodels.properties.lists

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class ListStyleTypeProperty(
    val type: ListStyleType
) : IRProperty {
    override val propertyName = "list-style-type"

    @Serializable
    sealed interface ListStyleType {
        @Serializable
        data class None(val unit: kotlin.Unit = kotlin.Unit) : ListStyleType

        @Serializable
        data class Keyword(val value: ListMarkerKeyword) : ListStyleType

        @Serializable
        data class CustomString(val value: String) : ListStyleType
    }

    enum class ListMarkerKeyword {
        DISC, CIRCLE, SQUARE, DECIMAL, DECIMAL_LEADING_ZERO,
        LOWER_ROMAN, UPPER_ROMAN, LOWER_GREEK, LOWER_LATIN,
        UPPER_LATIN, LOWER_ALPHA, UPPER_ALPHA, ARMENIAN,
        GEORGIAN, HEBREW, HIRAGANA, KATAKANA
    }
}
