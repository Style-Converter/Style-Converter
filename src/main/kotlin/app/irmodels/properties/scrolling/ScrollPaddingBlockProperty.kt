package app.irmodels.properties.scrolling

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `scroll-padding-block` property.
 *
 * ## CSS Property
 * **Syntax**: `scroll-padding-block: <length-percentage>{1,2} | auto`
 *
 * ## Description
 * Shorthand for setting scroll-padding-block-start and scroll-padding-block-end.
 * Defines the block dimension (vertical in horizontal writing mode) scroll padding.
 *
 * ## Examples
 * ```kotlin
 * ScrollPaddingBlockProperty(
 *     start = ScrollPaddingValue.Length(IRLength(10.0, LengthUnit.PX)),
 *     end = ScrollPaddingValue.Length(IRLength(10.0, LengthUnit.PX))
 * )
 * ```
 *
 * @property start Block-start scroll padding (top in LTR)
 * @property end Block-end scroll padding (bottom in LTR)
 * @see [MDN scroll-padding-block](https://developer.mozilla.org/en-US/docs/Web/CSS/scroll-padding-block)
 */
@Serializable
data class ScrollPaddingBlockProperty(
    val start: ScrollPaddingValue,
    val end: ScrollPaddingValue
) : IRProperty {
    override val propertyName = "scroll-padding-block"

    companion object {
        fun all(padding: ScrollPaddingValue) = ScrollPaddingBlockProperty(padding, padding)
    }
}
