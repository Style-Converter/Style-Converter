package app.irmodels.properties.lists

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class ListStyleImageProperty(
    val image: ListImage
) : IRProperty {
    override val propertyName = "list-style-image"

    @Serializable
    sealed interface ListImage {
        @Serializable
        data class None(val unit: kotlin.Unit = kotlin.Unit) : ListImage

        @Serializable
        data class Url(val url: String) : ListImage
    }
}
