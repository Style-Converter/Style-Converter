package app.irmodels.properties.typography

import app.irmodels.*
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `font-size` property.
 *
 * ## CSS Property
 * **Syntax**: `font-size: <length> | <percentage> | <absolute-size> | <relative-size>`
 *
 * ## Description
 * Sets the size of the font. Font size can be specified as an absolute length, percentage of parent font size,
 * absolute size keyword, or relative size keyword.
 *
 * ## Value Types
 * - **Length**: Absolute or relative length units (px, em, rem, pt, etc.)
 * - **Percentage**: Relative to parent element's font-size
 * - **Absolute Size Keywords**: Predefined size keywords (xx-small through xxx-large)
 * - **Relative Size Keywords**: Relative to parent's font-size (larger, smaller)
 *
 * ## Examples
 * ```kotlin
 * // Absolute length
 * FontSizeProperty(FontSize.Length(IRLength(16.0, LengthUnit.PX)))  // font-size: 16px
 *
 * // Relative length
 * FontSizeProperty(FontSize.Length(IRLength(1.5, LengthUnit.EM)))   // font-size: 1.5em
 *
 * // Percentage
 * FontSizeProperty(FontSize.Percentage(IRPercentage(120.0)))        // font-size: 120%
 *
 * // Absolute keyword
 * FontSizeProperty(FontSize.Keyword(AbsoluteSize.LARGE))            // font-size: large
 *
 * // Relative keyword
 * FontSizeProperty(FontSize.Relative(RelativeSize.LARGER))          // font-size: larger
 * ```
 *
 * ## Platform Support
 * - **CSS**: Full support for all value types
 * - **Jetpack Compose**: Use `fontSize` in `TextStyle` (convert to `.sp` for text)
 * - **SwiftUI**: Use `.font(.system(size: ...))` or font size modifiers
 *
 * @property size The font size value
 * @see [MDN font-size](https://developer.mozilla.org/en-US/docs/Web/CSS/font-size)
 */
@Serializable
data class FontSizeProperty(
    val size: FontSize
) : IRProperty {
    override val propertyName = "font-size"

    /**
     * Sealed interface representing all possible font-size value types.
     */
    @Serializable
    sealed interface FontSize {
        /**
         * Length value (px, em, rem, pt, etc.).
         * Most common way to specify font size.
         */
        @Serializable
        data class Length(val value: IRLength) : FontSize

        /**
         * Percentage relative to parent element's font-size.
         * Example: 120% = 1.2x parent's font size
         */
        @Serializable
        data class Percentage(val value: IRPercentage) : FontSize

        /**
         * Absolute size keyword.
         * Predefined size scale from xx-small to xxx-large.
         */
        @Serializable
        data class Keyword(val value: AbsoluteSize) : FontSize

        /**
         * Relative size keyword.
         * Larger or smaller than parent's font-size.
         */
        @Serializable
        data class Relative(val value: RelativeSize) : FontSize

        /**
         * Absolute size keywords.
         * Provides a predefined scale of font sizes.
         */
        enum class AbsoluteSize {
            /** Extra-extra-small: typically 9px */
            XX_SMALL,

            /** Extra-small: typically 10px */
            X_SMALL,

            /** Small: typically 13px */
            SMALL,

            /** Medium (default): typically 16px */
            MEDIUM,

            /** Large: typically 18px */
            LARGE,

            /** Extra-large: typically 24px */
            X_LARGE,

            /** Extra-extra-large: typically 32px */
            XX_LARGE,

            /** Extra-extra-extra-large: typically 48px */
            XXX_LARGE
        }

        /**
         * Relative size keywords.
         * Adjusts font size relative to parent element.
         */
        enum class RelativeSize {
            /** One size larger than parent's font-size */
            LARGER,

            /** One size smaller than parent's font-size */
            SMALLER
        }
    }
}
