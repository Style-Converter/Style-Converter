package app.irmodels.properties.spacing

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class ColumnGapProperty(
    val gap: GapProperty.LengthPercentageOrNormal
) : IRProperty {
    override val propertyName = "column-gap"
}
