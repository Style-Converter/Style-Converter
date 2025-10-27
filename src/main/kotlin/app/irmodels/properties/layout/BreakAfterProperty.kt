package app.irmodels.properties.layout

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `break-after` property.
 *
 * ## CSS Property
 * **Syntax**: `break-after: auto | avoid | always | all | avoid-page | page | left | right | recto | verso | avoid-column | column | avoid-region | region`
 *
 * ## Description
 * Defines how page, column, or region breaks should behave after an element.
 *
 * @property breakValue The break-after value
 * @see [MDN break-after](https://developer.mozilla.org/en-US/docs/Web/CSS/break-after)
 */
@Serializable
data class BreakAfterProperty(
    val breakValue: BreakValue
) : IRProperty {
    override val propertyName = "break-after"

    enum class BreakValue {
        AUTO, AVOID, ALWAYS, ALL,
        AVOID_PAGE, PAGE, LEFT, RIGHT,
        RECTO, VERSO,
        AVOID_COLUMN, COLUMN,
        AVOID_REGION, REGION
    }
}
