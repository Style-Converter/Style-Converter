package app.irmodels.properties.borders

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `border-left` property.
 *
 * ## CSS Property
 * **Syntax**: `border-left: <border-width> || <border-style> || <color>`
 *
 * ## Description
 * Shorthand property for setting border-left-width, border-left-style, and border-left-color.
 * Sets all left border properties in one declaration.
 *
 * ## Examples
 * ```kotlin
 * BorderLeftProperty(
 *     width = BorderWidthValue.Length(IRLength(1.0, LengthUnit.PX)),
 *     style = BorderStyle.SOLID,
 *     color = IRColor.Hex(0xFF000000)
 * )
 * ```
 *
 * @property width Border width (optional)
 * @property style Border style (optional)
 * @property color Border color (optional)
 * @see [MDN border-left](https://developer.mozilla.org/en-US/docs/Web/CSS/border-left)
 */
@Serializable
data class BorderLeftProperty(
    val width: BorderWidthValue? = null,
    val style: BorderStyle? = null,
    val color: app.irmodels.IRColor? = null
) : IRProperty {
    override val propertyName = "border-left"
}
