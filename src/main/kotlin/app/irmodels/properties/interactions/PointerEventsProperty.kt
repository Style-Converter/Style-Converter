package app.irmodels.properties.interactions

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class PointerEventsProperty(
    val value: PointerEvents
) : IRProperty {
    override val propertyName = "pointer-events"

    enum class PointerEvents {
        AUTO, NONE, VISIBLE_PAINTED, VISIBLE_FILL, VISIBLE_STROKE,
        VISIBLE, PAINTED, FILL, STROKE, ALL
    }
}
