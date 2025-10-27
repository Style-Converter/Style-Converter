package app.irmodels.properties.layout.grid

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class JustifySelfProperty(
    val value: JustifySelf
) : IRProperty {
    override val propertyName = "justify-self"

    enum class JustifySelf {
        AUTO, NORMAL, STRETCH, CENTER, START, END,
        FLEX_START, FLEX_END, SELF_START, SELF_END,
        LEFT, RIGHT, BASELINE
    }
}
