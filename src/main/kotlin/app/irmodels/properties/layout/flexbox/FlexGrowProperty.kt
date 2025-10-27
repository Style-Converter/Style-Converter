package app.irmodels.properties.layout.flexbox

import app.irmodels.IRNumber
import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class FlexGrowProperty(
    val value: IRNumber
) : IRProperty {
    override val propertyName = "flex-grow"
}
