package app.irmodels.properties.scrolling

import app.irmodels.IRProperty
import app.irmodels.IRLength
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `scroll-margin-block` property.
 *
 * ## CSS Property
 * **Syntax**: `scroll-margin-block: <length>{1,2}`
 *
 * ## Description
 * Shorthand for setting scroll-margin-block-start and scroll-margin-block-end.
 * Defines the block dimension (vertical in horizontal writing mode) scroll margins.
 *
 * ## Examples
 * ```kotlin
 * // Both sides
 * ScrollMarginBlockProperty(
 *     start = IRLength(10.0, LengthUnit.PX),
 *     end = IRLength(10.0, LengthUnit.PX)
 * )
 * ```
 *
 * @property start Block-start scroll margin (top in LTR)
 * @property end Block-end scroll margin (bottom in LTR)
 * @see [MDN scroll-margin-block](https://developer.mozilla.org/en-US/docs/Web/CSS/scroll-margin-block)
 */
@Serializable
data class ScrollMarginBlockProperty(
    val start: IRLength,
    val end: IRLength
) : IRProperty {
    override val propertyName = "scroll-margin-block"

    companion object {
        fun all(margin: IRLength) = ScrollMarginBlockProperty(margin, margin)
    }
}
