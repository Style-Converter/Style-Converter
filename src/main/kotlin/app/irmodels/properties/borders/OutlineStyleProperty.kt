package app.irmodels.properties.borders

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class OutlineStyleProperty(
    val style: BorderStyleProperty.LineStyle
) : IRProperty {
    override val propertyName = "outline-style"
}
