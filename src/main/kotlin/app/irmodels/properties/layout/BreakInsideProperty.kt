package app.irmodels.properties.layout

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `break-inside` property.
 *
 * ## CSS Property
 * **Syntax**: `break-inside: auto | avoid | avoid-page | avoid-column | avoid-region`
 *
 * ## Description
 * Defines how page, column, or region breaks should behave inside an element.
 *
 * @property breakValue The break-inside value
 * @see [MDN break-inside](https://developer.mozilla.org/en-US/docs/Web/CSS/break-inside)
 */
@Serializable
data class BreakInsideProperty(
    val breakValue: BreakValue
) : IRProperty {
    override val propertyName = "break-inside"

    enum class BreakValue {
        AUTO, AVOID,
        AVOID_PAGE, AVOID_COLUMN, AVOID_REGION
    }
}
