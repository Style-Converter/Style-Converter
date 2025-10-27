package app.irmodels.properties.sizing

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class MaxBlockSizeProperty(
    val size: Size
) : IRProperty {
    override val propertyName = "max-block-size"

    @Serializable
    sealed interface Size {
        @Serializable
        data class LengthValue(val length: IRLength) : Size

        @Serializable
        data class PercentageValue(val percentage: IRPercentage) : Size

        @Serializable
        data class None(val unit: kotlin.Unit = kotlin.Unit) : Size
    }
}
