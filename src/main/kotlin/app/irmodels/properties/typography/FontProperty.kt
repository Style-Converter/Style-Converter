package app.irmodels.properties.typography

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `font` property.
 *
 * ## CSS Property
 * **Syntax**: `font: <font-style> <font-variant> <font-weight> <font-size> / <line-height> <font-family>`
 *
 * ## Description
 * Shorthand property for setting font-style, font-variant, font-weight, font-size,
 * line-height, and font-family in one declaration.
 *
 * ## Examples
 * ```kotlin
 * FontProperty(
 *     style = "italic",
 *     variant = "normal",
 *     weight = "bold",
 *     size = "16px",
 *     lineHeight = "1.5",
 *     family = "Arial, sans-serif"
 * )
 * ```
 *
 * @property style Font style (optional)
 * @property variant Font variant (optional)
 * @property weight Font weight (optional)
 * @property size Font size (required for shorthand)
 * @property lineHeight Line height (optional)
 * @property family Font family (required for shorthand)
 * @see [MDN font](https://developer.mozilla.org/en-US/docs/Web/CSS/font)
 */
@Serializable
data class FontProperty(
    val style: String? = null,
    val variant: String? = null,
    val weight: String? = null,
    val size: String,
    val lineHeight: String? = null,
    val family: String
) : IRProperty {
    override val propertyName = "font"
}
