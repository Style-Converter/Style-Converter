package app.irmodels.properties.spacing

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class PaddingProperty(
    val values: PaddingValues
) : IRProperty {
    override val propertyName = "padding"

    @Serializable
    sealed interface PaddingValues {
        @Serializable
        data class All(val value: LengthOrPercentage) : PaddingValues

        @Serializable
        data class VerticalHorizontal(
            val vertical: LengthOrPercentage,
            val horizontal: LengthOrPercentage
        ) : PaddingValues

        @Serializable
        data class FourSides(
            val top: LengthOrPercentage,
            val right: LengthOrPercentage,
            val bottom: LengthOrPercentage,
            val left: LengthOrPercentage
        ) : PaddingValues
    }

    @Serializable
    sealed interface LengthOrPercentage {
        @Serializable
        data class LengthValue(val length: IRLength) : LengthOrPercentage

        @Serializable
        data class PercentageValue(val percentage: IRPercentage) : LengthOrPercentage
    }
}
