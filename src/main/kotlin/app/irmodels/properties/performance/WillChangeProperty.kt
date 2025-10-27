package app.irmodels.properties.performance

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class WillChangeProperty(
    val properties: List<WillChangeValue>
) : IRProperty {
    override val propertyName = "will-change"

    @Serializable
    sealed interface WillChangeValue {
        @Serializable
        data class Auto(val unit: kotlin.Unit = kotlin.Unit) : WillChangeValue

        @Serializable
        data class ScrollPosition(val unit: kotlin.Unit = kotlin.Unit) : WillChangeValue

        @Serializable
        data class Contents(val unit: kotlin.Unit = kotlin.Unit) : WillChangeValue

        @Serializable
        data class PropertyName(val name: String) : WillChangeValue
    }
}
