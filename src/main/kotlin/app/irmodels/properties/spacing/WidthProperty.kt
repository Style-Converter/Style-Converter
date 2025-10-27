package app.irmodels.properties.spacing

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class WidthProperty(
    val width: WidthValue
) : IRProperty {
    override val propertyName = "width"

    @Serializable
    sealed interface WidthValue {
        @Serializable
        data class Auto(val unit: Unit = Unit) : WidthValue

        @Serializable
        data class LengthValue(val length: IRLength) : WidthValue

        @Serializable
        data class PercentageValue(val percentage: IRPercentage) : WidthValue

        @Serializable
        data class MinContent(val unit: Unit = Unit) : WidthValue

        @Serializable
        data class MaxContent(val unit: Unit = Unit) : WidthValue

        @Serializable
        data class FitContent(val maxSize: IRLength?) : WidthValue
    }
}
