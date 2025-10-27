package app.irmodels.properties.borders

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class BorderStyleProperty(
    val values: BorderStyleValues
) : IRProperty {
    override val propertyName = "border-style"

    @Serializable
    sealed interface BorderStyleValues {
        @Serializable
        data class All(val value: LineStyle) : BorderStyleValues

        @Serializable
        data class FourSides(
            val top: LineStyle,
            val right: LineStyle,
            val bottom: LineStyle,
            val left: LineStyle
        ) : BorderStyleValues
    }

    enum class LineStyle {
        NONE, HIDDEN, DOTTED, DASHED, SOLID,
        DOUBLE, GROOVE, RIDGE, INSET, OUTSET
    }
}
