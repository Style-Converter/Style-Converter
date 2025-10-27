package app.irmodels.properties.spacing

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class GapProperty(
    val values: GapValues
) : IRProperty {
    override val propertyName = "gap"

    @Serializable
    sealed interface GapValues {
        @Serializable
        data class Single(val value: LengthPercentageOrNormal) : GapValues

        @Serializable
        data class RowColumn(
            val rowGap: LengthPercentageOrNormal,
            val columnGap: LengthPercentageOrNormal
        ) : GapValues
    }

    @Serializable
    sealed interface LengthPercentageOrNormal {
        @Serializable
        data class Normal(val unit: Unit = Unit) : LengthPercentageOrNormal

        @Serializable
        data class LengthValue(val length: IRLength) : LengthPercentageOrNormal

        @Serializable
        data class PercentageValue(val percentage: IRPercentage) : LengthPercentageOrNormal
    }
}
