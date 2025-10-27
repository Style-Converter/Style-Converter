package app.irmodels.properties.typography

import app.irmodels.*
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `font-stretch` property.
 *
 * ## CSS Property
 * **Syntax**: `font-stretch: normal | ultra-condensed | extra-condensed | condensed | semi-condensed | semi-expanded | expanded | extra-expanded | ultra-expanded | <percentage>`
 *
 * ## Description
 * Selects a normal, condensed, or expanded face from a font.
 *
 * @property stretch The font stretch value
 * @see [MDN font-stretch](https://developer.mozilla.org/en-US/docs/Web/CSS/font-stretch)
 */
@Serializable
data class FontStretchProperty(
    val stretch: FontStretch
) : IRProperty {
    override val propertyName = "font-stretch"

    @Serializable
    sealed interface FontStretch {
        @Serializable
        data class Keyword(val value: StretchKeyword) : FontStretch

        @Serializable
        data class PercentageValue(val percentage: IRPercentage) : FontStretch
    }

    enum class StretchKeyword {
        ULTRA_CONDENSED,
        EXTRA_CONDENSED,
        CONDENSED,
        SEMI_CONDENSED,
        NORMAL,
        SEMI_EXPANDED,
        EXPANDED,
        EXTRA_EXPANDED,
        ULTRA_EXPANDED
    }
}
