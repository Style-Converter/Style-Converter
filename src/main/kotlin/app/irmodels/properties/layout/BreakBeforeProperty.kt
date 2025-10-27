package app.irmodels.properties.layout

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `break-before` property.
 *
 * ## CSS Property
 * **Syntax**: `break-before: auto | avoid | always | all | avoid-page | page | left | right | recto | verso | avoid-column | column | avoid-region | region`
 *
 * ## Description
 * Defines how page, column, or region breaks should behave before an element.
 *
 * @property breakValue The break-before value
 * @see [MDN break-before](https://developer.mozilla.org/en-US/docs/Web/CSS/break-before)
 */
@Serializable
data class BreakBeforeProperty(
    val breakValue: BreakValue
) : IRProperty {
    override val propertyName = "break-before"

    enum class BreakValue {
        AUTO, AVOID, ALWAYS, ALL,
        AVOID_PAGE, PAGE, LEFT, RIGHT,
        RECTO, VERSO,
        AVOID_COLUMN, COLUMN,
        AVOID_REGION, REGION
    }
}
