package app.irmodels.properties.animations

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class TransitionPropertyProperty(
    val properties: List<TransitionProperty>
) : IRProperty {
    override val propertyName = "transition-property"

    @Serializable
    sealed interface TransitionProperty {
        @Serializable
        data class None(val unit: Unit = Unit) : TransitionProperty

        @Serializable
        data class All(val unit: Unit = Unit) : TransitionProperty

        @Serializable
        data class PropertyName(val name: String) : TransitionProperty
    }
}
