package app.irmodels.properties.borders

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class BorderBottomStyleProperty(
    val style: BorderStyle
) : IRProperty {
    override val propertyName = "border-bottom-style"

    enum class BorderStyle {
        NONE, HIDDEN, DOTTED, DASHED, SOLID,
        DOUBLE, GROOVE, RIDGE, INSET, OUTSET
    }
}
