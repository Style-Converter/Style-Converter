package app.irmodels.properties.layout.position

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class TopProperty(
    val value: InsetValue
) : IRProperty {
    override val propertyName = "top"
}

@Serializable
sealed interface InsetValue {
    @Serializable
    data class Auto(val unit: Unit = Unit) : InsetValue

    @Serializable
    data class LengthValue(val length: IRLength) : InsetValue

    @Serializable
    data class PercentageValue(val percentage: IRPercentage) : InsetValue
}
