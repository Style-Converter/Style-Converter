package app.irmodels.properties.scrolling

import app.irmodels.IRProperty
import app.irmodels.IRLength
import app.irmodels.IRPercentage
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `scroll-padding` property.
 *
 * ## CSS Property
 * **Syntax**: `scroll-padding: <length-percentage>{1,4}`
 *
 * ## Description
 * Shorthand property for setting all scroll padding at once. Defines padding within
 * the optimal viewing region of the scrollport, used to determine the snap positions.
 *
 * ## Examples
 * ```kotlin
 * // All sides with length
 * ScrollPaddingProperty(
 *     top = ScrollPaddingValue.Length(IRLength(10.0, LengthUnit.PX)),
 *     right = ScrollPaddingValue.Length(IRLength(10.0, LengthUnit.PX)),
 *     bottom = ScrollPaddingValue.Length(IRLength(10.0, LengthUnit.PX)),
 *     left = ScrollPaddingValue.Length(IRLength(10.0, LengthUnit.PX))
 * )
 *
 * // Using percentage
 * ScrollPaddingProperty.all(ScrollPaddingValue.Percentage(IRPercentage(10.0)))
 * ```
 *
 * @property top Top scroll padding
 * @property right Right scroll padding
 * @property bottom Bottom scroll padding
 * @property left Left scroll padding
 * @see [MDN scroll-padding](https://developer.mozilla.org/en-US/docs/Web/CSS/scroll-padding)
 */
@Serializable
data class ScrollPaddingProperty(
    val top: ScrollPaddingValue,
    val right: ScrollPaddingValue,
    val bottom: ScrollPaddingValue,
    val left: ScrollPaddingValue
) : IRProperty {
    override val propertyName = "scroll-padding"

    companion object {
        fun all(padding: ScrollPaddingValue) =
            ScrollPaddingProperty(padding, padding, padding, padding)

        fun symmetric(vertical: ScrollPaddingValue, horizontal: ScrollPaddingValue) =
            ScrollPaddingProperty(vertical, horizontal, vertical, horizontal)
    }
}

/**
 * Represents a scroll-padding value.
 */
@Serializable
sealed interface ScrollPaddingValue {
    @Serializable
    data class Length(val value: IRLength) : ScrollPaddingValue

    @Serializable
    data class Percentage(val value: IRPercentage) : ScrollPaddingValue

    @Serializable
    data object Auto : ScrollPaddingValue
}
