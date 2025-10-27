package app.irmodels.properties.borders

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class BorderRightWidthProperty(
    val width: BorderWidth
) : IRProperty {
    override val propertyName = "border-right-width"

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
