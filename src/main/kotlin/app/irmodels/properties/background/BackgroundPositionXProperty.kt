package app.irmodels.properties.background

import app.irmodels.*
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `background-position-x` property.
 *
 * ## CSS Property
 * **Syntax**: `background-position-x: left | center | right | <length> | <percentage>`
 *
 * ## Description
 * Sets the horizontal position of a background image.
 *
 * @property position The horizontal position value
 * @see [MDN background-position-x](https://developer.mozilla.org/en-US/docs/Web/CSS/background-position-x)
 */
@Serializable
data class BackgroundPositionXProperty(
    val position: PositionX
) : IRProperty {
    override val propertyName = "background-position-x"

    @Serializable
    sealed interface PositionX {
        @Serializable
        data class LengthValue(val length: IRLength) : PositionX

        @Serializable
        data class PercentageValue(val percentage: IRPercentage) : PositionX

        @Serializable
        data class Keyword(val value: HorizontalKeyword) : PositionX
    }

    enum class HorizontalKeyword {
        LEFT, CENTER, RIGHT
    }
}
