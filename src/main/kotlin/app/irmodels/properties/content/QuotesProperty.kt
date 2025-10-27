package app.irmodels.properties.content

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `quotes` property.
 *
 * ## CSS Property
 * **Syntax**: `quotes: none | auto | <string> <string>+`
 *
 * ## Description
 * Sets quotation marks to be used for quotes. Defines pairs of opening and closing quotes.
 *
 * @property quotesValue The quotes value
 * @see [MDN quotes](https://developer.mozilla.org/en-US/docs/Web/CSS/quotes)
 */
@Serializable
data class QuotesProperty(
    val quotesValue: Quotes
) : IRProperty {
    override val propertyName = "quotes"

    @Serializable
    sealed interface Quotes {
        @Serializable
        data class None(val unit: Unit = Unit) : Quotes

        @Serializable
        data class Auto(val unit: Unit = Unit) : Quotes

        @Serializable
        data class QuotePairs(val pairs: List<QuotePair>) : Quotes
    }

    @Serializable
    data class QuotePair(
        val open: String,
        val close: String
    )
}
