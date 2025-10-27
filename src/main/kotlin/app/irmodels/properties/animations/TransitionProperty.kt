package app.irmodels.properties.animations

import app.irmodels.IRProperty
import app.irmodels.IRTime
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `transition` property.
 *
 * ## CSS Property
 * **Syntax**: `transition: <property> <duration> <timing-function> <delay>`
 *
 * ## Description
 * Shorthand property for setting transition-property, transition-duration,
 * transition-timing-function, and transition-delay.
 *
 * ## Examples
 * ```kotlin
 * TransitionProperty(
 *     property = "all",
 *     duration = IRTime(0.3, TimeUnit.S),
 *     timingFunction = "ease",
 *     delay = IRTime(0.0, TimeUnit.S)
 * )
 * ```
 *
 * @property property Property to transition (optional)
 * @property duration Transition duration (optional)
 * @property timingFunction Timing function (optional)
 * @property delay Transition delay (optional)
 * @see [MDN transition](https://developer.mozilla.org/en-US/docs/Web/CSS/transition)
 */
@Serializable
data class TransitionProperty(
    val property: String? = null,
    val duration: IRTime? = null,
    val timingFunction: String? = null,
    val delay: IRTime? = null
) : IRProperty {
    override val propertyName = "transition"
}
