package app.irmodels.properties.borders

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class OutlineWidthProperty(
    val width: BorderWidthProperty.LineWidth
) : IRProperty {
    override val propertyName = "outline-width"
}
