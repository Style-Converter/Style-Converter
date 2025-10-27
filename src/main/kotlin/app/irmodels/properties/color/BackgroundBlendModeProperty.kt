package app.irmodels.properties.color

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class BackgroundBlendModeProperty(
    val mode: MixBlendModeProperty.BlendMode
) : IRProperty {
    override val propertyName = "background-blend-mode"
}
