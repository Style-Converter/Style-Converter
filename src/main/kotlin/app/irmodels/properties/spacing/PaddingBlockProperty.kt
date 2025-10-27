package app.irmodels.properties.spacing

import app.irmodels.IRProperty
import app.irmodels.IRLength
import app.irmodels.IRPercentage
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `padding-block` property.
 *
 * ## CSS Property
 * **Syntax**: `padding-block: <length-percentage>{1,2}`
 *
 * ## Description
 * Shorthand for setting padding-block-start and padding-block-end. Defines the logical
 * block start and end padding (top and bottom in horizontal-tb writing mode).
 *
 * ## Examples
 * ```kotlin
 * // Both sides
 * PaddingBlockProperty(
 *     start = PaddingValue.Length(IRLength(10.0, LengthUnit.PX)),
 *     end = PaddingValue.Length(IRLength(10.0, LengthUnit.PX))
 * )
 * ```
 *
 * @property start Block-start padding (top in LTR)
 * @property end Block-end padding (bottom in LTR)
 * @see [MDN padding-block](https://developer.mozilla.org/en-US/docs/Web/CSS/padding-block)
 */
@Serializable
data class PaddingBlockProperty(
    val start: PaddingValue,
    val end: PaddingValue
) : IRProperty {
    override val propertyName = "padding-block"

    companion object {
        fun all(padding: PaddingValue) = PaddingBlockProperty(padding, padding)
    }
}

/**
 * Represents a padding value (shared across all padding properties).
 */
@Serializable
sealed interface PaddingValue {
    @Serializable
    data class Length(val value: IRLength) : PaddingValue

    @Serializable
    data class Percentage(val value: IRPercentage) : PaddingValue
}
