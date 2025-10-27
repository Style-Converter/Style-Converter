package app.irmodels.properties.borders

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class BorderRightStyleProperty(
    val style: BorderStyle
) : IRProperty {
    override val propertyName = "border-right-style"

    enum class BorderStyle {
        NONE, HIDDEN, DOTTED, DASHED, SOLID,
        DOUBLE, GROOVE, RIDGE, INSET, OUTSET
    }
}
