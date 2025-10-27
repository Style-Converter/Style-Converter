package app.irmodels.properties.layout.flexbox

import app.irmodels.IRProperty
import app.irmodels.IRLength
import app.irmodels.IRNumber
import app.irmodels.IRPercentage
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `flex` property.
 *
 * ## CSS Property
 * **Syntax**: `flex: <flex-grow> <flex-shrink>? <flex-basis>?`
 *
 * ## Description
 * Shorthand property for flex-grow, flex-shrink, and flex-basis.
 * Defines how a flex item grows, shrinks, and its base size.
 *
 * ## Examples
 * ```kotlin
 * FlexProperty(
 *     grow = IRNumber(1.0),
 *     shrink = IRNumber(1.0),
 *     basis = FlexBasis.Auto
 * )
 * ```
 *
 * @property grow Flex grow factor (optional)
 * @property shrink Flex shrink factor (optional)
 * @property basis Flex basis (optional)
 * @see [MDN flex](https://developer.mozilla.org/en-US/docs/Web/CSS/flex)
 */
@Serializable
data class FlexProperty(
    val grow: IRNumber? = null,
    val shrink: IRNumber? = null,
    val basis: FlexBasis? = null
) : IRProperty {
    override val propertyName = "flex"
}

/**
 * Represents flex-basis values.
 */
@Serializable
sealed interface FlexBasis {
    @Serializable
    data class Length(val value: IRLength) : FlexBasis

    @Serializable
    data class Percentage(val value: IRPercentage) : FlexBasis

    @Serializable
    data object Auto : FlexBasis

    @Serializable
    data object Content : FlexBasis
}
