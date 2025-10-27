package app.irmodels.properties.columns

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class ColumnGapProperty(
    val gap: ColumnGap
) : IRProperty {
    override val propertyName = "column-gap"

    @Serializable
    sealed interface ColumnGap {
        @Serializable
        data class Normal(val unit: kotlin.Unit = kotlin.Unit) : ColumnGap

        @Serializable
        data class LengthValue(val length: IRLength) : ColumnGap

        @Serializable
        data class PercentageValue(val percentage: IRPercentage) : ColumnGap
    }
}
