package app.irmodels.properties.scrolling

import app.irmodels.IRProperty
import app.irmodels.IRLength
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `scroll-margin-inline` property.
 *
 * ## CSS Property
 * **Syntax**: `scroll-margin-inline: <length>{1,2}`
 *
 * ## Description
 * Shorthand for setting scroll-margin-inline-start and scroll-margin-inline-end.
 * Defines the inline dimension (horizontal in horizontal writing mode) scroll margins.
 *
 * ## Examples
 * ```kotlin
 * // Both sides
 * ScrollMarginInlineProperty(
 *     start = IRLength(10.0, LengthUnit.PX),
 *     end = IRLength(10.0, LengthUnit.PX)
 * )
 * ```
 *
 * @property start Inline-start scroll margin (left in LTR)
 * @property end Inline-end scroll margin (right in LTR)
 * @see [MDN scroll-margin-inline](https://developer.mozilla.org/en-US/docs/Web/CSS/scroll-margin-inline)
 */
@Serializable
data class ScrollMarginInlineProperty(
    val start: IRLength,
    val end: IRLength
) : IRProperty {
    override val propertyName = "scroll-margin-inline"

    companion object {
        fun all(margin: IRLength) = ScrollMarginInlineProperty(margin, margin)
    }
}
