package app.irmodels.properties.animations

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class AnimationNameProperty(
    val names: List<AnimationName>
) : IRProperty {
    override val propertyName = "animation-name"

    @Serializable
    sealed interface AnimationName {
        @Serializable
        data class None(val unit: Unit = Unit) : AnimationName

        @Serializable
        data class Identifier(val name: String) : AnimationName
    }
}
