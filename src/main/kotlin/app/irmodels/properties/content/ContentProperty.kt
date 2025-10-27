package app.irmodels.properties.content

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class ContentProperty(
    val values: List<ContentValue>
) : IRProperty {
    override val propertyName = "content"

    @Serializable
    sealed interface ContentValue {
        @Serializable
        data class Normal(val unit: kotlin.Unit = kotlin.Unit) : ContentValue

        @Serializable
        data class None(val unit: kotlin.Unit = kotlin.Unit) : ContentValue

        @Serializable
        data class StringLiteral(val value: String) : ContentValue

        @Serializable
        data class Url(val url: String) : ContentValue

        @Serializable
        data class AttrValue(val attributeName: String) : ContentValue

        @Serializable
        data class Counter(val name: String, val style: String?) : ContentValue

        @Serializable
        data class OpenQuote(val unit: kotlin.Unit = kotlin.Unit) : ContentValue

        @Serializable
        data class CloseQuote(val unit: kotlin.Unit = kotlin.Unit) : ContentValue
    }
}
