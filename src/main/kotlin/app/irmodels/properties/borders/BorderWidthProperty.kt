package app.irmodels.properties.borders

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class BorderWidthProperty(
    val values: BorderWidthValues
) : IRProperty {
    override val propertyName = "border-width"

    @Serializable
    sealed interface BorderWidthValues {
        @Serializable
        data class All(val value: LineWidth) : BorderWidthValues

        @Serializable
        data class VerticalHorizontal(
            val vertical: LineWidth,
            val horizontal: LineWidth
        ) : BorderWidthValues

        @Serializable
        data class FourSides(
            val top: LineWidth,
            val right: LineWidth,
            val bottom: LineWidth,
            val left: LineWidth
        ) : BorderWidthValues
    }

    @Serializable
    sealed interface LineWidth {
        @Serializable
        data class Keyword(val value: WidthKeyword) : LineWidth

        @Serializable
        data class LengthValue(val length: IRLength) : LineWidth

        enum class WidthKeyword {
            THIN, MEDIUM, THICK
        }
    }
}
