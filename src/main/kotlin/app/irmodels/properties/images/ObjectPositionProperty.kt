package app.irmodels.properties.images

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class ObjectPositionProperty(
    val position: Position
) : IRProperty {
    override val propertyName = "object-position"

    @Serializable
    data class Position(
        val x: PositionValue,
        val y: PositionValue
    )

    @Serializable
    sealed interface PositionValue {
        @Serializable
        data class LengthValue(val length: IRLength) : PositionValue

        @Serializable
        data class PercentageValue(val percentage: IRPercentage) : PositionValue

        @Serializable
        data class Keyword(val value: PositionKeyword) : PositionValue
    }

    enum class PositionKeyword {
        LEFT, CENTER, RIGHT, TOP, BOTTOM
    }
}
