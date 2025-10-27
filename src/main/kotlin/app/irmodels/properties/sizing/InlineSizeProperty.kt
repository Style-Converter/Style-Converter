package app.irmodels.properties.sizing

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class InlineSizeProperty(
    val size: Size
) : IRProperty {
    override val propertyName = "inline-size"

    @Serializable
    sealed interface Size {
        @Serializable
        data class LengthValue(val length: IRLength) : Size

        @Serializable
        data class PercentageValue(val percentage: IRPercentage) : Size

        @Serializable
        data class Auto(val unit: kotlin.Unit = kotlin.Unit) : Size

        @Serializable
        data class MaxContent(val unit: kotlin.Unit = kotlin.Unit) : Size

        @Serializable
        data class MinContent(val unit: kotlin.Unit = kotlin.Unit) : Size

        @Serializable
        data class FitContent(val length: IRLength?) : Size
    }
}
