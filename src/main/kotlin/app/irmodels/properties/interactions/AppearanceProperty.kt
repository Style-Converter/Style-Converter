package app.irmodels.properties.interactions

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `appearance` property.
 *
 * ## CSS Property
 * **Syntax**: `appearance: none | auto | <compat-auto>`
 *
 * ## Description
 * Controls the native appearance of UI controls.
 * Commonly used to remove platform-specific styling.
 *
 * @property value The appearance value
 * @see [MDN appearance](https://developer.mozilla.org/en-US/docs/Web/CSS/appearance)
 */
@Serializable
data class AppearanceProperty(
    val value: Appearance
) : IRProperty {
    override val propertyName = "appearance"

    enum class Appearance {
        NONE,
        AUTO,
        BUTTON,
        CHECKBOX,
        LISTBOX,
        MENULIST,
        METER,
        PROGRESS_BAR,
        PUSH_BUTTON,
        RADIO,
        SEARCHFIELD,
        SLIDER_HORIZONTAL,
        SQUARE_BUTTON,
        TEXTAREA,
        TEXTFIELD
    }
}
