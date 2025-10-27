package app.irmodels.properties.borders

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class BorderBottomWidthProperty(
    val width: BorderWidth
) : IRProperty {
    override val propertyName = "border-bottom-width"

    @Serializable
    sealed interface BorderWidth {
        @Serializable
        data class LengthValue(val length: IRLength) : BorderWidth

        @Serializable
        data class Keyword(val value: BorderWidthKeyword) : BorderWidth
    }

    enum class BorderWidthKeyword {
        THIN, MEDIUM, THICK
    }
}
