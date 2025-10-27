package app.irmodels.properties.spacing

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class MaxWidthProperty(
    val maxWidth: MaxValue
) : IRProperty {
    override val propertyName = "max-width"

    @Serializable
    sealed interface MaxValue {
        @Serializable
        data class None(val unit: Unit = Unit) : MaxValue

        @Serializable
        data class LengthValue(val length: IRLength) : MaxValue

        @Serializable
        data class PercentageValue(val percentage: IRPercentage) : MaxValue

        @Serializable
        data class MinContent(val unit: Unit = Unit) : MaxValue

        @Serializable
        data class MaxContent(val unit: Unit = Unit) : MaxValue

        @Serializable
        data class FitContent(val maxSize: IRLength?) : MaxValue
    }
}
