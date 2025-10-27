package app.irmodels.properties.layout.position

import app.irmodels.IRProperty
import app.irmodels.IRLength
import app.irmodels.IRPercentage
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `inset-block` property.
 *
 * ## CSS Property
 * **Syntax**: `inset-block: <length-percentage> | auto{1,2}`
 *
 * ## Description
 * Shorthand for setting inset-block-start and inset-block-end. Defines the logical block
 * start and end offsets (top and bottom in horizontal-tb writing mode) of an element.
 *
 * ## Examples
 * ```kotlin
 * // Both sides with length
 * InsetBlockProperty(
 *     start = InsetValue.Length(IRLength(10.0, LengthUnit.PX)),
 *     end = InsetValue.Length(IRLength(20.0, LengthUnit.PX))
 * )
 *
 * // Auto value
 * InsetBlockProperty.all(InsetValue.Auto)
 * ```
 *
 * @property start Block-start inset (top in LTR)
 * @property end Block-end inset (bottom in LTR)
 * @see [MDN inset-block](https://developer.mozilla.org/en-US/docs/Web/CSS/inset-block)
 */
@Serializable
data class InsetBlockProperty(
    val start: InsetValue,
    val end: InsetValue
) : IRProperty {
    override val propertyName = "inset-block"

    companion object {
        fun all(value: InsetValue) = InsetBlockProperty(value, value)
    }
}
