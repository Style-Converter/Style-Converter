package app.irmodels.properties.borders

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class BorderRadiusProperty(
    val values: BorderRadiusValues
) : IRProperty {
    override val propertyName = "border-radius"

    @Serializable
    sealed interface BorderRadiusValues {
        @Serializable
        data class All(val value: LengthOrPercentage) : BorderRadiusValues

        @Serializable
        data class FourCorners(
            val topLeft: LengthOrPercentage,
            val topRight: LengthOrPercentage,
            val bottomRight: LengthOrPercentage,
            val bottomLeft: LengthOrPercentage
        ) : BorderRadiusValues
    }

    @Serializable
    sealed interface LengthOrPercentage {
        @Serializable
        data class LengthValue(val length: IRLength) : LengthOrPercentage

        @Serializable
        data class PercentageValue(val percentage: IRPercentage) : LengthOrPercentage
    }
}
