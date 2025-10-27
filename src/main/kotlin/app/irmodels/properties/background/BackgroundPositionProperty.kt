package app.irmodels.properties.background

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class BackgroundPositionProperty(
    val positions: List<BackgroundPosition>
) : IRProperty {
    override val propertyName = "background-position"

    @Serializable
    data class BackgroundPosition(
        val x: PositionValue,
        val y: PositionValue
    )

    @Serializable
    sealed interface PositionValue {
        @Serializable
        data class Keyword(val value: PositionKeyword) : PositionValue

        @Serializable
        data class LengthValue(val length: IRLength) : PositionValue

        @Serializable
        data class PercentageValue(val percentage: IRPercentage) : PositionValue

        enum class PositionKeyword {
            LEFT, CENTER, RIGHT, TOP, BOTTOM
        }
    }
}
