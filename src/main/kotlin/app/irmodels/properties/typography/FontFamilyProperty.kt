package app.irmodels.properties.typography

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class FontFamilyProperty(
    val families: List<FontFamily>
) : IRProperty {
    override val propertyName = "font-family"

    @Serializable
    sealed interface FontFamily {
        @Serializable
        data class Named(val name: String) : FontFamily

        @Serializable
        data class Generic(val type: GenericFamily) : FontFamily

        enum class GenericFamily {
            SERIF, SANS_SERIF, MONOSPACE, CURSIVE, FANTASY,
            SYSTEM_UI, UI_SERIF, UI_SANS_SERIF, UI_MONOSPACE,
            UI_ROUNDED, EMOJI, MATH, FANGSONG
        }
    }
}
