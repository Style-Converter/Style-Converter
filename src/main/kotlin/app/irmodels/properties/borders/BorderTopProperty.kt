package app.irmodels.properties.borders

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `border-top` property.
 *
 * ## CSS Property
 * **Syntax**: `border-top: <border-width> || <border-style> || <color>`
 *
 * ## Description
 * Shorthand property for setting border-top-width, border-top-style, and border-top-color.
 * Sets all top border properties in one declaration.
 *
 * ## Examples
 * ```kotlin
 * BorderTopProperty(
 *     width = BorderWidthValue.Length(IRLength(1.0, LengthUnit.PX)),
 *     style = BorderStyle.SOLID,
 *     color = IRColor.Hex(0xFF000000)
 * )
 * ```
 *
 * @property width Border width (optional)
 * @property style Border style (optional)
 * @property color Border color (optional)
 * @see [MDN border-top](https://developer.mozilla.org/en-US/docs/Web/CSS/border-top)
 */
@Serializable
data class BorderTopProperty(
    val width: BorderWidthValue? = null,
    val style: BorderStyle? = null,
    val color: app.irmodels.IRColor? = null
) : IRProperty {
    override val propertyName = "border-top"
}
