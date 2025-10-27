package app.irmodels.properties.animations

import app.irmodels.IRNumber
import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class AnimationTimingFunctionProperty(
    val functions: List<TimingFunction>
) : IRProperty {
    override val propertyName = "animation-timing-function"

    @Serializable
    sealed interface TimingFunction {
        @Serializable
        data class Keyword(val value: TimingKeyword) : TimingFunction

        @Serializable
        data class CubicBezier(
            val x1: IRNumber,
            val y1: IRNumber,
            val x2: IRNumber,
            val y2: IRNumber
        ) : TimingFunction

        @Serializable
        data class Steps(val count: Int, val position: StepPosition?) : TimingFunction

        enum class TimingKeyword {
            LINEAR, EASE, EASE_IN, EASE_OUT, EASE_IN_OUT, STEP_START, STEP_END
        }

        enum class StepPosition {
            JUMP_START, JUMP_END, JUMP_NONE, JUMP_BOTH, START, END
        }
    }
}
