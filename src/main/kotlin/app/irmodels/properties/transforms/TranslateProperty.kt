package app.irmodels.properties.transforms

import app.irmodels.*
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `translate` property.
 *
 * ## CSS Property
 * **Syntax**: `translate: none | <length-percentage> [ <length-percentage> <length>? ]?`
 *
 * ## Description
 * Allows you to specify translation transforms individually (instead of using transform property).
 * Part of the individual transform properties.
 *
 * @property translation The translate value
 * @see [MDN translate](https://developer.mozilla.org/en-US/docs/Web/CSS/translate)
 */
@Serializable
data class TranslateProperty(
    val translation: Translate
) : IRProperty {
    override val propertyName = "translate"

    @Serializable
    sealed interface Translate {
        @Serializable
        data class None(val unit: Unit = Unit) : Translate

        @Serializable
        data class OneAxis(val x: LengthPercentage) : Translate

        @Serializable
        data class TwoAxis(val x: LengthPercentage, val y: LengthPercentage) : Translate

        @Serializable
        data class ThreeAxis(
            val x: LengthPercentage,
            val y: LengthPercentage,
            val z: IRLength
        ) : Translate
    }

    @Serializable
    sealed interface LengthPercentage {
        @Serializable
        data class LengthValue(val length: IRLength) : LengthPercentage

        @Serializable
        data class PercentageValue(val percentage: IRPercentage) : LengthPercentage
    }
}
