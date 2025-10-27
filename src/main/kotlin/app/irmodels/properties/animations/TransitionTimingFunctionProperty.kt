package app.irmodels.properties.animations

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class TransitionTimingFunctionProperty(
    val functions: List<AnimationTimingFunctionProperty.TimingFunction>
) : IRProperty {
    override val propertyName = "transition-timing-function"
}
