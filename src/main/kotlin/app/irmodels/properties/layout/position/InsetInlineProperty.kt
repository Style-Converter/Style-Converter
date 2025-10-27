package app.irmodels.properties.layout.position

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `inset-inline` property.
 *
 * ## CSS Property
 * **Syntax**: `inset-inline: <length-percentage> | auto{1,2}`
 *
 * ## Description
 * Shorthand for setting inset-inline-start and inset-inline-end. Defines the logical inline
 * start and end offsets (left and right in LTR, reversed in RTL) of an element.
 *
 * ## Examples
 * ```kotlin
 * // Both sides with length
 * InsetInlineProperty(
 *     start = InsetValue.Length(IRLength(10.0, LengthUnit.PX)),
 *     end = InsetValue.Length(IRLength(20.0, LengthUnit.PX))
 * )
 *
 * // Auto value
 * InsetInlineProperty.all(InsetValue.Auto)
 * ```
 *
 * @property start Inline-start inset (left in LTR, right in RTL)
 * @property end Inline-end inset (right in LTR, left in RTL)
 * @see [MDN inset-inline](https://developer.mozilla.org/en-US/docs/Web/CSS/inset-inline)
 */
@Serializable
data class InsetInlineProperty(
    val start: InsetValue,
    val end: InsetValue
) : IRProperty {
    override val propertyName = "inset-inline"

    companion object {
        fun all(value: InsetValue) = InsetInlineProperty(value, value)
    }
}
