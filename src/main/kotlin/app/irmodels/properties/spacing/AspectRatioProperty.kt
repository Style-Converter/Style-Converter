package app.irmodels.properties.spacing

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class AspectRatioProperty(
    val ratio: AspectRatio
) : IRProperty {
    override val propertyName = "aspect-ratio"

    @Serializable
    sealed interface AspectRatio {
        @Serializable
        data class Auto(val unit: Unit = Unit) : AspectRatio

        @Serializable
        data class Ratio(val width: IRNumber, val height: IRNumber) : AspectRatio
    }
}
