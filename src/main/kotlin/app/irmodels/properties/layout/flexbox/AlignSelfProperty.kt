package app.irmodels.properties.layout.flexbox

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class AlignSelfProperty(
    val value: AlignSelf
) : IRProperty {
    override val propertyName = "align-self"

    enum class AlignSelf {
        AUTO, FLEX_START, FLEX_END, CENTER, BASELINE,
        STRETCH, START, END, SELF_START, SELF_END
    }
}
