package app.irmodels.properties.spacing

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `margin-inline` property.
 *
 * ## CSS Property
 * **Syntax**: `margin-inline: <length-percentage> | auto{1,2}`
 *
 * ## Description
 * Shorthand for setting margin-inline-start and margin-inline-end. Defines the logical
 * inline start and end margins (left and right in LTR, reversed in RTL).
 *
 * ## Examples
 * ```kotlin
 * // Both sides
 * MarginInlineProperty(
 *     start = MarginValue.Length(IRLength(10.0, LengthUnit.PX)),
 *     end = MarginValue.Length(IRLength(10.0, LengthUnit.PX))
 * )
 *
 * // Auto margins for centering
 * MarginInlineProperty.all(MarginValue.Auto)
 * ```
 *
 * @property start Inline-start margin (left in LTR, right in RTL)
 * @property end Inline-end margin (right in LTR, left in RTL)
 * @see [MDN margin-inline](https://developer.mozilla.org/en-US/docs/Web/CSS/margin-inline)
 */
@Serializable
data class MarginInlineProperty(
    val start: MarginValue,
    val end: MarginValue
) : IRProperty {
    override val propertyName = "margin-inline"

    companion object {
        fun all(margin: MarginValue) = MarginInlineProperty(margin, margin)
    }
}
