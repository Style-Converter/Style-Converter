package app.irmodels.properties.columns

import app.irmodels.IRProperty
import app.irmodels.IRColor
import app.irmodels.IRLength
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `column-rule` property.
 *
 * ## CSS Property
 * **Syntax**: `column-rule: <column-rule-width> || <column-rule-style> || <column-rule-color>`
 *
 * ## Description
 * Shorthand property for setting column-rule-width, column-rule-style, and column-rule-color.
 * Defines the rule (line) drawn between columns in a multi-column layout.
 *
 * ## Examples
 * ```kotlin
 * ColumnRuleProperty(
 *     width = IRLength(1.0, LengthUnit.PX),
 *     style = ColumnRuleStyle.SOLID,
 *     color = IRColor.Hex(0xFF000000)
 * )
 * ```
 *
 * @property width Column rule width (optional)
 * @property style Column rule style (optional)
 * @property color Column rule color (optional)
 * @see [MDN column-rule](https://developer.mozilla.org/en-US/docs/Web/CSS/column-rule)
 */
@Serializable
data class ColumnRuleProperty(
    val width: IRLength? = null,
    val style: ColumnRuleStyle? = null,
    val color: IRColor? = null
) : IRProperty {
    override val propertyName = "column-rule"
}

/**
 * Represents column-rule-style values (same as border-style).
 */
@Serializable
enum class ColumnRuleStyle {
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
