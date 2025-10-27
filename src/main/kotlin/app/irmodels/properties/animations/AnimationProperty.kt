package app.irmodels.properties.animations

import app.irmodels.IRProperty
import app.irmodels.IRTime
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `animation` property.
 *
 * ## CSS Property
 * **Syntax**: `animation: <name> <duration> <timing-function> <delay> <iteration-count> <direction> <fill-mode> <play-state>`
 *
 * ## Description
 * Shorthand property for setting multiple animation properties:
 * animation-name, animation-duration, animation-timing-function, animation-delay,
 * animation-iteration-count, animation-direction, animation-fill-mode, animation-play-state.
 *
 * ## Examples
 * ```kotlin
 * AnimationProperty(
 *     name = "fadeIn",
 *     duration = IRTime(1.0, TimeUnit.S),
 *     timingFunction = AnimationTimingFunction.EASE_IN_OUT,
 *     delay = IRTime(0.0, TimeUnit.S),
 *     iterationCount = AnimationIterationCount.Count(1.0),
 *     direction = AnimationDirection.NORMAL,
 *     fillMode = AnimationFillMode.NONE,
 *     playState = AnimationPlayState.RUNNING
 * )
 * ```
 *
 * @property name Animation name (optional)
 * @property duration Animation duration (optional)
 * @property timingFunction Timing function (optional)
 * @property delay Animation delay (optional)
 * @property iterationCount Iteration count (optional)
 * @property direction Animation direction (optional)
 * @property fillMode Fill mode (optional)
 * @property playState Play state (optional)
 * @see [MDN animation](https://developer.mozilla.org/en-US/docs/Web/CSS/animation)
 */
@Serializable
data class AnimationProperty(
    val name: String? = null,
    val duration: IRTime? = null,
    val timingFunction: String? = null,
    val delay: IRTime? = null,
    val iterationCount: String? = null,
    val direction: String? = null,
    val fillMode: String? = null,
    val playState: String? = null
) : IRProperty {
    override val propertyName = "animation"
}
