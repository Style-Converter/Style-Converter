package app.irmodels.properties.scrolling

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `scroll-padding-inline` property.
 *
 * ## CSS Property
 * **Syntax**: `scroll-padding-inline: <length-percentage>{1,2} | auto`
 *
 * ## Description
 * Shorthand for setting scroll-padding-inline-start and scroll-padding-inline-end.
 * Defines the inline dimension (horizontal in horizontal writing mode) scroll padding.
 *
 * ## Examples
 * ```kotlin
 * ScrollPaddingInlineProperty(
 *     start = ScrollPaddingValue.Length(IRLength(10.0, LengthUnit.PX)),
 *     end = ScrollPaddingValue.Length(IRLength(10.0, LengthUnit.PX))
 * )
 * ```
 *
 * @property start Inline-start scroll padding (left in LTR)
 * @property end Inline-end scroll padding (right in LTR)
 * @see [MDN scroll-padding-inline](https://developer.mozilla.org/en-US/docs/Web/CSS/scroll-padding-inline)
 */
@Serializable
data class ScrollPaddingInlineProperty(
    val start: ScrollPaddingValue,
    val end: ScrollPaddingValue
) : IRProperty {
    override val propertyName = "scroll-padding-inline"

    companion object {
        fun all(padding: ScrollPaddingValue) = ScrollPaddingInlineProperty(padding, padding)
    }
}
