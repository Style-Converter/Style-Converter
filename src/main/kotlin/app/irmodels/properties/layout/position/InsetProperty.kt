package app.irmodels.properties.layout.position

import app.irmodels.*
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `inset` property.
 *
 * ## CSS Property
 * **Syntax**: `inset: <top> <right> <bottom> <left>` (shorthand for top/right/bottom/left)
 *
 * ## Description
 * Shorthand property for top, right, bottom, and left positioning properties.
 * Defines the logical block and inline offsets of an element.
 *
 * @property values The inset values (1-4 values like margin/padding)
 * @see [MDN inset](https://developer.mozilla.org/en-US/docs/Web/CSS/inset)
 */
@Serializable
data class InsetProperty(
    val values: InsetValues
) : IRProperty {
    override val propertyName = "inset"

    @Serializable
    sealed interface InsetValues {
        @Serializable
        data class All(val value: LengthPercentageOrAuto) : InsetValues

        @Serializable
        data class VerticalHorizontal(
            val vertical: LengthPercentageOrAuto,
            val horizontal: LengthPercentageOrAuto
        ) : InsetValues

        @Serializable
        data class TopHorizontalBottom(
            val top: LengthPercentageOrAuto,
            val horizontal: LengthPercentageOrAuto,
            val bottom: LengthPercentageOrAuto
        ) : InsetValues

        @Serializable
        data class FourSides(
            val top: LengthPercentageOrAuto,
            val right: LengthPercentageOrAuto,
            val bottom: LengthPercentageOrAuto,
            val left: LengthPercentageOrAuto
        ) : InsetValues
    }

    @Serializable
    sealed interface LengthPercentageOrAuto {
        @Serializable
        data class Auto(val unit: Unit = Unit) : LengthPercentageOrAuto

        @Serializable
        data class LengthValue(val length: IRLength) : LengthPercentageOrAuto

        @Serializable
        data class PercentageValue(val percentage: IRPercentage) : LengthPercentageOrAuto
    }
}
