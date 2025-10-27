package app.irmodels.properties.borders

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `border-right` property.
 *
 * ## CSS Property
 * **Syntax**: `border-right: <border-width> || <border-style> || <color>`
 *
 * ## Description
 * Shorthand property for setting border-right-width, border-right-style, and border-right-color.
 * Sets all right border properties in one declaration.
 *
 * ## Examples
 * ```kotlin
 * BorderRightProperty(
 *     width = BorderWidthValue.Length(IRLength(1.0, LengthUnit.PX)),
 *     style = BorderStyle.SOLID,
 *     color = IRColor.Hex(0xFF000000)
 * )
 * ```
 *
 * @property width Border width (optional)
 * @property style Border style (optional)
 * @property color Border color (optional)
 * @see [MDN border-right](https://developer.mozilla.org/en-US/docs/Web/CSS/border-right)
 */
@Serializable
data class BorderRightProperty(
    val width: BorderWidthValue? = null,
    val style: BorderStyle? = null,
    val color: app.irmodels.IRColor? = null
) : IRProperty {
    override val propertyName = "border-right"
}
