package app.irmodels.properties.scrolling

import app.irmodels.IRProperty
import app.irmodels.IRLength
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `scroll-margin` property.
 *
 * ## CSS Property
 * **Syntax**: `scroll-margin: <length>{1,4}`
 *
 * ## Description
 * Shorthand property for setting all scroll margins at once. Defines the margin around
 * the scroll snap area that is used for snapping this box to a snapport.
 *
 * ## Examples
 * ```kotlin
 * // All sides
 * ScrollMarginProperty(all = IRLength(10.0, LengthUnit.PX))
 *
 * // Vertical and horizontal
 * ScrollMarginProperty(
 *     vertical = IRLength(10.0, LengthUnit.PX),
 *     horizontal = IRLength(20.0, LengthUnit.PX)
 * )
 * ```
 *
 * @property top Top scroll margin
 * @property right Right scroll margin
 * @property bottom Bottom scroll margin
 * @property left Left scroll margin
 * @see [MDN scroll-margin](https://developer.mozilla.org/en-US/docs/Web/CSS/scroll-margin)
 */
@Serializable
data class ScrollMarginProperty(
    val top: IRLength,
    val right: IRLength,
    val bottom: IRLength,
    val left: IRLength
) : IRProperty {
    override val propertyName = "scroll-margin"

    companion object {
        /**
         * Creates a ScrollMarginProperty with the same value for all sides.
         */
        fun all(margin: IRLength) = ScrollMarginProperty(margin, margin, margin, margin)

        /**
         * Creates a ScrollMarginProperty with separate vertical and horizontal values.
         */
        fun symmetric(vertical: IRLength, horizontal: IRLength) =
            ScrollMarginProperty(vertical, horizontal, vertical, horizontal)
    }
}
