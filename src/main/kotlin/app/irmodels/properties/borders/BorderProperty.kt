package app.irmodels.properties.borders

import app.irmodels.IRProperty
import app.irmodels.IRColor
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `border` property.
 *
 * ## CSS Property
 * **Syntax**: `border: <border-width> || <border-style> || <color>`
 *
 * ## Description
 * Shorthand property for setting border-width, border-style, and border-color
 * for all four sides of an element.
 *
 * ## Examples
 * ```kotlin
 * BorderProperty(
 *     width = BorderWidthValue.Length(IRLength(1.0, LengthUnit.PX)),
 *     style = BorderStyle.SOLID,
 *     color = IRColor.Hex(0xFF000000)
 * )
 * ```
 *
 * @property width Border width (optional)
 * @property style Border style (optional)
 * @property color Border color (optional)
 * @see [MDN border](https://developer.mozilla.org/en-US/docs/Web/CSS/border)
 */
@Serializable
data class BorderProperty(
    val width: BorderWidthValue? = null,
    val style: BorderStyle? = null,
    val color: IRColor? = null
) : IRProperty {
    override val propertyName = "border"
}
