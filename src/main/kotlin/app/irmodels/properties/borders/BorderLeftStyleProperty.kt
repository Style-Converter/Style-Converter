package app.irmodels.properties.borders

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class BorderLeftStyleProperty(
    val style: BorderStyle
) : IRProperty {
    override val propertyName = "border-left-style"

    enum class BorderStyle {
        NONE, HIDDEN, DOTTED, DASHED, SOLID,
        DOUBLE, GROOVE, RIDGE, INSET, OUTSET
    }
}
