package app.irmodels.properties.typography

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class LineHeightProperty(
    val height: LineHeight
) : IRProperty {
    override val propertyName = "line-height"

    @Serializable
    sealed interface LineHeight {
        @Serializable
        data class Normal(val unit: Unit = Unit) : LineHeight

        @Serializable
        data class NumberValue(val number: IRNumber) : LineHeight

        @Serializable
        data class LengthValue(val length: IRLength) : LineHeight

        @Serializable
        data class PercentageValue(val percentage: IRPercentage) : LineHeight
    }
}
