package app.irmodels.properties.borders

import app.irmodels.IRProperty
import app.irmodels.IRColor
import app.irmodels.IRLength
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `outline` property.
 *
 * ## CSS Property
 * **Syntax**: `outline: <outline-width> || <outline-style> || <outline-color>`
 *
 * ## Description
 * Shorthand property for setting outline-width, outline-style, and outline-color.
 * Outlines differ from borders in that they don't take up space and can be non-rectangular.
 *
 * ## Examples
 * ```kotlin
 * OutlineProperty(
 *     width = IRLength(2.0, LengthUnit.PX),
 *     style = OutlineStyle.SOLID,
 *     color = IRColor.Hex(0xFF0000FF)
 * )
 * ```
 *
 * @property width Outline width (optional)
 * @property style Outline style (optional)
 * @property color Outline color (optional)
 * @see [MDN outline](https://developer.mozilla.org/en-US/docs/Web/CSS/outline)
 */
@Serializable
data class OutlineProperty(
    val width: IRLength? = null,
    val style: OutlineStyle? = null,
    val color: IRColor? = null
) : IRProperty {
    override val propertyName = "outline"
}

/**
 * Represents outline-style values (same as border-style).
 */
@Serializable
enum class OutlineStyle {
    NONE,
    HIDDEN,
    DOTTED,
    DASHED,
    SOLID,
    DOUBLE,
    GROOVE,
    RIDGE,
    INSET,
    OUTSET
}
