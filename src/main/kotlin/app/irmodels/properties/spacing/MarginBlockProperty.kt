package app.irmodels.properties.spacing

import app.irmodels.IRProperty
import app.irmodels.IRLength
import app.irmodels.IRPercentage
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `margin-block` property.
 *
 * ## CSS Property
 * **Syntax**: `margin-block: <length-percentage> | auto{1,2}`
 *
 * ## Description
 * Shorthand for setting margin-block-start and margin-block-end. Defines the logical
 * block start and end margins (top and bottom in horizontal-tb writing mode).
 *
 * ## Examples
 * ```kotlin
 * // Both sides
 * MarginBlockProperty(
 *     start = MarginValue.Length(IRLength(10.0, LengthUnit.PX)),
 *     end = MarginValue.Length(IRLength(10.0, LengthUnit.PX))
 * )
 *
 * // Auto margins
 * MarginBlockProperty.all(MarginValue.Auto)
 * ```
 *
 * @property start Block-start margin (top in LTR)
 * @property end Block-end margin (bottom in LTR)
 * @see [MDN margin-block](https://developer.mozilla.org/en-US/docs/Web/CSS/margin-block)
 */
@Serializable
data class MarginBlockProperty(
    val start: MarginValue,
    val end: MarginValue
) : IRProperty {
    override val propertyName = "margin-block"

    companion object {
        fun all(margin: MarginValue) = MarginBlockProperty(margin, margin)
    }
}

/**
 * Represents a margin value (shared across all margin properties).
 */
@Serializable
sealed interface MarginValue {
    @Serializable
    data class Length(val value: IRLength) : MarginValue

    @Serializable
    data class Percentage(val value: IRPercentage) : MarginValue

    @Serializable
    data object Auto : MarginValue
}
