package app.irmodels.properties.background

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class BackgroundSizeProperty(
    val sizes: List<BackgroundSize>
) : IRProperty {
    override val propertyName = "background-size"

    @Serializable
    sealed interface BackgroundSize {
        @Serializable
        data class Keyword(val value: SizeKeyword) : BackgroundSize

        @Serializable
        data class LengthValue(val width: IRLength, val height: IRLength?) : BackgroundSize

        @Serializable
        data class PercentageValue(val width: IRPercentage, val height: IRPercentage?) : BackgroundSize

        enum class SizeKeyword {
            COVER, CONTAIN, AUTO
        }
    }
}
