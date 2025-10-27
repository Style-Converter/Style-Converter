package app.irmodels.properties.animations

import app.irmodels.IRNumber
import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class AnimationIterationCountProperty(
    val counts: List<IterationCount>
) : IRProperty {
    override val propertyName = "animation-iteration-count"

    @Serializable
    sealed interface IterationCount {
        @Serializable
        data class Infinite(val unit: Unit = Unit) : IterationCount

        @Serializable
        data class Number(val value: IRNumber) : IterationCount
    }
}
