package app.irmodels.properties.effects

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class OverflowProperty(
    val x: Overflow,
    val y: Overflow
) : IRProperty {
    override val propertyName = "overflow"

    enum class Overflow {
        VISIBLE, HIDDEN, CLIP, SCROLL, AUTO
    }
}
