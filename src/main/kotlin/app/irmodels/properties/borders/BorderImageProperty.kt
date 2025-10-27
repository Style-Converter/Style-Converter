package app.irmodels.properties.borders

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class BorderImageProperty(
    val source: ImageSource?,
    val slice: ImageSlice?,
    val width: ImageWidth?,
    val outset: ImageOutset?,
    val repeat: ImageRepeat?
) : IRProperty {
    override val propertyName = "border-image"

    @Serializable
    sealed interface ImageSource {
        @Serializable
        data class None(val unit: kotlin.Unit = kotlin.Unit) : ImageSource

        @Serializable
        data class Url(val url: String) : ImageSource

        @Serializable
        data class Gradient(val gradient: String) : ImageSource
    }

    @Serializable
    data class ImageSlice(
        val values: List<SliceValue>,
        val fill: Boolean = false
    )

    @Serializable
    sealed interface SliceValue {
        @Serializable
        data class Number(val value: IRNumber) : SliceValue

        @Serializable
        data class Percentage(val value: IRPercentage) : SliceValue
    }

    @Serializable
    sealed interface ImageWidth {
        @Serializable
        data class LengthValue(val length: IRLength) : ImageWidth

        @Serializable
        data class Number(val value: IRNumber) : ImageWidth

        @Serializable
        data class Percentage(val value: IRPercentage) : ImageWidth

        @Serializable
        data class Auto(val unit: kotlin.Unit = kotlin.Unit) : ImageWidth
    }

    @Serializable
    sealed interface ImageOutset {
        @Serializable
        data class LengthValue(val length: IRLength) : ImageOutset

        @Serializable
        data class Number(val value: IRNumber) : ImageOutset
    }

    enum class ImageRepeat {
        STRETCH, REPEAT, ROUND, SPACE
    }
}
