package app.irmodels.properties.layout.grid

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `place-content` property.
 *
 * ## CSS Property
 * **Syntax**: `place-content: <align-content> <justify-content>?`
 *
 * ## Description
 * Shorthand for align-content and justify-content.
 * Controls both block and inline axis alignment.
 *
 * @property alignContent The align-content value
 * @property justifyContent The justify-content value (optional, defaults to alignContent)
 * @see [MDN place-content](https://developer.mozilla.org/en-US/docs/Web/CSS/place-content)
 */
@Serializable
data class PlaceContentProperty(
    val alignContent: ContentAlignment,
    val justifyContent: ContentAlignment? = null
) : IRProperty {
    override val propertyName = "place-content"

    enum class ContentAlignment {
        START, END, CENTER, STRETCH,
        SPACE_BETWEEN, SPACE_AROUND, SPACE_EVENLY,
        BASELINE, FIRST_BASELINE, LAST_BASELINE
    }
}
