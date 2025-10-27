package app.irmodels.properties.typography

import app.irmodels.IRProperty
import app.irmodels.IRColor
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `text-decoration` property.
 *
 * ## CSS Property
 * **Syntax**: `text-decoration: <text-decoration-line> <text-decoration-color> <text-decoration-style> <text-decoration-thickness>`
 *
 * ## Description
 * Shorthand property for setting text-decoration-line, text-decoration-color,
 * text-decoration-style, and text-decoration-thickness.
 *
 * ## Examples
 * ```kotlin
 * TextDecorationProperty(
 *     line = "underline",
 *     color = IRColor.Hex(0xFF000000),
 *     style = "solid",
 *     thickness = "auto"
 * )
 * ```
 *
 * @property line Text decoration line (optional)
 * @property color Text decoration color (optional)
 * @property style Text decoration style (optional)
 * @property thickness Text decoration thickness (optional)
 * @see [MDN text-decoration](https://developer.mozilla.org/en-US/docs/Web/CSS/text-decoration)
 */
@Serializable
data class TextDecorationProperty(
    val line: String? = null,
    val color: IRColor? = null,
    val style: String? = null,
    val thickness: String? = null
) : IRProperty {
    override val propertyName = "text-decoration"
}
