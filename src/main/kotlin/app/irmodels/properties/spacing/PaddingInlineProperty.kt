package app.irmodels.properties.spacing

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `padding-inline` property.
 *
 * ## CSS Property
 * **Syntax**: `padding-inline: <length-percentage>{1,2}`
 *
 * ## Description
 * Shorthand for setting padding-inline-start and padding-inline-end. Defines the logical
 * inline start and end padding (left and right in LTR, reversed in RTL).
 *
 * ## Examples
 * ```kotlin
 * // Both sides
 * PaddingInlineProperty(
 *     start = PaddingValue.Length(IRLength(10.0, LengthUnit.PX)),
 *     end = PaddingValue.Length(IRLength(10.0, LengthUnit.PX))
 * )
 * ```
 *
 * @property start Inline-start padding (left in LTR, right in RTL)
 * @property end Inline-end padding (right in LTR, left in RTL)
 * @see [MDN padding-inline](https://developer.mozilla.org/en-US/docs/Web/CSS/padding-inline)
 */
@Serializable
data class PaddingInlineProperty(
    val start: PaddingValue,
    val end: PaddingValue
) : IRProperty {
    override val propertyName = "padding-inline"

    companion object {
        fun all(padding: PaddingValue) = PaddingInlineProperty(padding, padding)
    }
}
