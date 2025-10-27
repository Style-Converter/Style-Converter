package app.irmodels.properties.effects

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class BackdropFilterProperty(
    val filters: List<FilterFunction>
) : IRProperty {
    override val propertyName = "backdrop-filter"

    @Serializable
    sealed interface FilterFunction {
        @Serializable
        data class None(val unit: kotlin.Unit = kotlin.Unit) : FilterFunction

        @Serializable
        data class Blur(val radius: IRLength) : FilterFunction

        @Serializable
        data class Brightness(val amount: IRPercentage) : FilterFunction

        @Serializable
        data class Contrast(val amount: IRPercentage) : FilterFunction

        @Serializable
        data class Grayscale(val amount: IRPercentage) : FilterFunction

        @Serializable
        data class Saturate(val amount: IRPercentage) : FilterFunction

        @Serializable
        data class Sepia(val amount: IRPercentage) : FilterFunction

        @Serializable
        data class Invert(val amount: IRPercentage) : FilterFunction

        @Serializable
        data class Opacity(val amount: IRPercentage) : FilterFunction

        @Serializable
        data class HueRotate(val angle: IRAngle) : FilterFunction
    }
}
