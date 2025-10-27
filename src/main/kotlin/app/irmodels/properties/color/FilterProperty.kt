package app.irmodels.properties.color

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class FilterProperty(
    val filters: FilterValue
) : IRProperty {
    override val propertyName = "filter"

    @Serializable
    sealed interface FilterValue {
        @Serializable
        data class None(val unit: Unit = Unit) : FilterValue

        @Serializable
        data class FilterList(val functions: List<FilterFunction>) : FilterValue
    }
}

@Serializable
sealed interface FilterFunction {
    @Serializable
    data class Blur(val radius: IRLength) : FilterFunction

    @Serializable
    data class Brightness(val amount: IRPercentage) : FilterFunction

    @Serializable
    data class Contrast(val amount: IRPercentage) : FilterFunction

    @Serializable
    data class Grayscale(val amount: IRPercentage) : FilterFunction

    @Serializable
    data class HueRotate(val angle: IRAngle) : FilterFunction

    @Serializable
    data class Invert(val amount: IRPercentage) : FilterFunction

    @Serializable
    data class Saturate(val amount: IRPercentage) : FilterFunction

    @Serializable
    data class Sepia(val amount: IRPercentage) : FilterFunction

    @Serializable
    data class DropShadow(
        val offsetX: IRLength,
        val offsetY: IRLength,
        val blurRadius: IRLength?,
        val color: IRColor?
    ) : FilterFunction
}
