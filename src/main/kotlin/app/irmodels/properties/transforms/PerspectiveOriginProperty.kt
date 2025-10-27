package app.irmodels.properties.transforms

import app.irmodels.*
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `perspective-origin` property.
 *
 * ## CSS Property
 * **Syntax**: `perspective-origin: <position>`
 *
 * ## Description
 * Determines the position at which the viewer is looking at 3D-transformed elements.
 *
 * @property x The horizontal position
 * @property y The vertical position
 * @see [MDN perspective-origin](https://developer.mozilla.org/en-US/docs/Web/CSS/perspective-origin)
 */
@Serializable
data class PerspectiveOriginProperty(
    val x: PositionValue,
    val y: PositionValue
) : IRProperty {
    override val propertyName = "perspective-origin"

    @Serializable
    sealed interface PositionValue {
        @Serializable
        data class LengthValue(val length: IRLength) : PositionValue

        @Serializable
        data class PercentageValue(val percentage: IRPercentage) : PositionValue

        @Serializable
        data class Keyword(val value: PositionKeyword) : PositionValue
    }

    enum class PositionKeyword {
        LEFT, CENTER, RIGHT, TOP, BOTTOM
    }
}
