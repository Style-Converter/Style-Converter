package app.irmodels.properties.spacing

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class MarginProperty(
    val values: MarginValues
) : IRProperty {
    override val propertyName = "margin"

    @Serializable
    sealed interface MarginValues {
        @Serializable
        data class All(val value: LengthPercentageOrAuto) : MarginValues

        @Serializable
        data class VerticalHorizontal(
            val vertical: LengthPercentageOrAuto,
            val horizontal: LengthPercentageOrAuto
        ) : MarginValues

        @Serializable
        data class FourSides(
            val top: LengthPercentageOrAuto,
            val right: LengthPercentageOrAuto,
            val bottom: LengthPercentageOrAuto,
            val left: LengthPercentageOrAuto
        ) : MarginValues
    }

    @Serializable
    sealed interface LengthPercentageOrAuto {
        @Serializable
        data class Auto(val unit: Unit = Unit) : LengthPercentageOrAuto

        @Serializable
        data class LengthValue(val length: IRLength) : LengthPercentageOrAuto

        @Serializable
        data class PercentageValue(val percentage: IRPercentage) : LengthPercentageOrAuto
    }
}
