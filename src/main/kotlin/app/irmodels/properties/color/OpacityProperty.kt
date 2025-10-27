package app.irmodels.properties.color

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class OpacityProperty(
    val opacity: Double // 0.0 to 1.0
) : IRProperty {
    override val propertyName = "opacity"
}
