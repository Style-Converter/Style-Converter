package app.irmodels.properties.table

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class BorderSpacingProperty(
    val spacing: Spacing
) : IRProperty {
    override val propertyName = "border-spacing"

    @Serializable
    sealed interface Spacing {
        @Serializable
        data class Single(val length: IRLength) : Spacing

        @Serializable
        data class TwoValues(
            val horizontal: IRLength,
            val vertical: IRLength
        ) : Spacing
    }
}
