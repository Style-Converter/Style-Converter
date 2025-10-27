package app.irmodels.properties.spacing

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class MinWidthProperty(
    val minWidth: MinMaxValue
) : IRProperty {
    override val propertyName = "min-width"

    @Serializable
    sealed interface MinMaxValue {
        @Serializable
        data class Auto(val unit: Unit = Unit) : MinMaxValue

        @Serializable
        data class LengthValue(val length: IRLength) : MinMaxValue

        @Serializable
        data class PercentageValue(val percentage: IRPercentage) : MinMaxValue

        @Serializable
        data class MinContent(val unit: Unit = Unit) : MinMaxValue

        @Serializable
        data class MaxContent(val unit: Unit = Unit) : MinMaxValue

        @Serializable
        data class FitContent(val maxSize: IRLength?) : MinMaxValue
    }
}
