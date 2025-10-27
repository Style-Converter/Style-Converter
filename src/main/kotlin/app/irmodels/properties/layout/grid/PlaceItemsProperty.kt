package app.irmodels.properties.layout.grid

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `place-items` property.
 *
 * ## CSS Property
 * **Syntax**: `place-items: <align-items> <justify-items>?`
 *
 * ## Description
 * Shorthand for align-items and justify-items.
 * Controls item alignment in both axes.
 *
 * @property alignItems The align-items value
 * @property justifyItems The justify-items value (optional, defaults to alignItems)
 * @see [MDN place-items](https://developer.mozilla.org/en-US/docs/Web/CSS/place-items)
 */
@Serializable
data class PlaceItemsProperty(
    val alignItems: ItemAlignment,
    val justifyItems: ItemAlignment? = null
) : IRProperty {
    override val propertyName = "place-items"

    enum class ItemAlignment {
        START, END, CENTER, STRETCH,
        BASELINE, FIRST_BASELINE, LAST_BASELINE,
        SELF_START, SELF_END
    }
}
