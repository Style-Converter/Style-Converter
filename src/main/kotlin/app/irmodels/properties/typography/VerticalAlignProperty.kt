package app.irmodels.properties.typography

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class VerticalAlignProperty(
    val alignment: VerticalAlignment
) : IRProperty {
    override val propertyName = "vertical-align"

    @Serializable
    sealed interface VerticalAlignment {
        @Serializable
        data class Keyword(val value: AlignKeyword) : VerticalAlignment

        @Serializable
        data class LengthValue(val length: IRLength) : VerticalAlignment

        @Serializable
        data class PercentageValue(val percentage: IRPercentage) : VerticalAlignment

        enum class AlignKeyword {
            BASELINE, SUB, SUPER, TEXT_TOP, TEXT_BOTTOM,
            MIDDLE, TOP, BOTTOM
        }
    }
}
