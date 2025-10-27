package app.irmodels.properties.layout.grid

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `place-self` property.
 *
 * ## CSS Property
 * **Syntax**: `place-self: <align-self> <justify-self>?`
 *
 * ## Description
 * Shorthand for align-self and justify-self.
 * Controls self-alignment in both axes.
 *
 * @property alignSelf The align-self value
 * @property justifySelf The justify-self value (optional, defaults to alignSelf)
 * @see [MDN place-self](https://developer.mozilla.org/en-US/docs/Web/CSS/place-self)
 */
@Serializable
data class PlaceSelfProperty(
    val alignSelf: SelfAlignment,
    val justifySelf: SelfAlignment? = null
) : IRProperty {
    override val propertyName = "place-self"

    enum class SelfAlignment {
        AUTO, START, END, CENTER, STRETCH,
        BASELINE, FIRST_BASELINE, LAST_BASELINE,
        SELF_START, SELF_END
    }
}
