package app.irmodels.properties.effects

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class OverflowXProperty(
    val value: OverflowProperty.Overflow
) : IRProperty {
    override val propertyName = "overflow-x"
}
