package app.irmodels.properties.background

import app.irmodels.*
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `background-position-y` property.
 *
 * ## CSS Property
 * **Syntax**: `background-position-y: top | center | bottom | <length> | <percentage>`
 *
 * ## Description
 * Sets the vertical position of a background image.
 *
 * @property position The vertical position value
 * @see [MDN background-position-y](https://developer.mozilla.org/en-US/docs/Web/CSS/background-position-y)
 */
@Serializable
data class BackgroundPositionYProperty(
    val position: PositionY
) : IRProperty {
    override val propertyName = "background-position-y"

    @Serializable
    sealed interface PositionY {
        @Serializable
        data class LengthValue(val length: IRLength) : PositionX

        @Serializable
        data class PercentageValue(val percentage: IRPercentage) : PositionY

        @Serializable
        data class Keyword(val value: VerticalKeyword) : PositionY
    }

    enum class VerticalKeyword {
        TOP, CENTER, BOTTOM
    }
}
