package app.irmodels.properties.borders

import app.irmodels.IRColor
import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class BorderColorProperty(
    val values: BorderColorValues
) : IRProperty {
    override val propertyName = "border-color"

    @Serializable
    sealed interface BorderColorValues {
        @Serializable
        data class All(val value: IRColor) : BorderColorValues

        @Serializable
        data class FourSides(
            val top: IRColor,
            val right: IRColor,
            val bottom: IRColor,
            val left: IRColor
        ) : BorderColorValues
    }
}
