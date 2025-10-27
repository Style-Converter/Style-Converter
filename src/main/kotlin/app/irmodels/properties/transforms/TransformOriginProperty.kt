package app.irmodels.properties.transforms

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class TransformOriginProperty(
    val x: OriginValue,
    val y: OriginValue,
    val z: IRLength? = null
) : IRProperty {
    override val propertyName = "transform-origin"

    @Serializable
    sealed interface OriginValue {
        @Serializable
        data class LengthValue(val length: IRLength) : OriginValue

        @Serializable
        data class PercentageValue(val percentage: IRPercentage) : OriginValue

        @Serializable
        data class Keyword(val value: OriginKeyword) : OriginValue

        enum class OriginKeyword {
            LEFT, CENTER, RIGHT, TOP, BOTTOM
        }
    }
}
