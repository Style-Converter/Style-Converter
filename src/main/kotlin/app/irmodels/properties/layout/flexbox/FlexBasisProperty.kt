package app.irmodels.properties.layout.flexbox

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class FlexBasisProperty(
    val value: FlexBasis
) : IRProperty {
    override val propertyName = "flex-basis"

    @Serializable
    sealed interface FlexBasis {
        @Serializable
        data class Auto(val unit: Unit = Unit) : FlexBasis

        @Serializable
        data class Content(val unit: Unit = Unit) : FlexBasis

        @Serializable
        data class LengthValue(val length: IRLength) : FlexBasis

        @Serializable
        data class PercentageValue(val percentage: IRPercentage) : FlexBasis
    }
}
