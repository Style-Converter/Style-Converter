package app.irmodels.properties.background

import app.irmodels.IRProperty
import app.irmodels.IRColor
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `background` property.
 *
 * ## CSS Property
 * **Syntax**: `background: <bg-color> <bg-image> <bg-position> / <bg-size> <bg-repeat> <bg-attachment> <bg-origin> <bg-clip>`
 *
 * ## Description
 * Shorthand property for setting all background properties in one declaration:
 * background-color, background-image, background-position, background-size,
 * background-repeat, background-attachment, background-origin, background-clip.
 *
 * ## Examples
 * ```kotlin
 * BackgroundProperty(
 *     color = IRColor.Hex(0xFFFFFFFF),
 *     image = "url(image.png)",
 *     position = "center",
 *     size = "cover",
 *     repeat = "no-repeat",
 *     attachment = "scroll",
 *     origin = "padding-box",
 *     clip = "border-box"
 * )
 * ```
 *
 * @property color Background color (optional)
 * @property image Background image (optional)
 * @property position Background position (optional)
 * @property size Background size (optional)
 * @property repeat Background repeat (optional)
 * @property attachment Background attachment (optional)
 * @property origin Background origin (optional)
 * @property clip Background clip (optional)
 * @see [MDN background](https://developer.mozilla.org/en-US/docs/Web/CSS/background)
 */
@Serializable
data class BackgroundProperty(
    val color: IRColor? = null,
    val image: String? = null,
    val position: String? = null,
    val size: String? = null,
    val repeat: String? = null,
    val attachment: String? = null,
    val origin: String? = null,
    val clip: String? = null
) : IRProperty {
    override val propertyName = "background"
}
