package app.irmodels.properties.borders

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `border-bottom` property.
 *
 * ## CSS Property
 * **Syntax**: `border-bottom: <border-width> || <border-style> || <color>`
 *
 * ## Description
 * Shorthand property for setting border-bottom-width, border-bottom-style, and border-bottom-color.
 * Sets all bottom border properties in one declaration.
 *
 * ## Examples
 * ```kotlin
 * BorderBottomProperty(
 *     width = BorderWidthValue.Length(IRLength(1.0, LengthUnit.PX)),
 *     style = BorderStyle.SOLID,
 *     color = IRColor.Hex(0xFF000000)
 * )
 * ```
 *
 * @property width Border width (optional)
 * @property style Border style (optional)
 * @property color Border color (optional)
 * @see [MDN border-bottom](https://developer.mozilla.org/en-US/docs/Web/CSS/border-bottom)
 */
@Serializable
data class BorderBottomProperty(
    val width: BorderWidthValue? = null,
    val style: BorderStyle? = null,
    val color: app.irmodels.IRColor? = null
) : IRProperty {
    override val propertyName = "border-bottom"
}
