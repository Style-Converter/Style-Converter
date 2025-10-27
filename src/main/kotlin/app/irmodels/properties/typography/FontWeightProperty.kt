package app.irmodels.properties.typography

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class FontWeightProperty(
    val weight: FontWeight
) : IRProperty {
    override val propertyName = "font-weight"

    @Serializable
    sealed interface FontWeight {
        @Serializable
        data class Numeric(val value: Int) : FontWeight // 100-900

        @Serializable
        data class Keyword(val value: WeightKeyword) : FontWeight

        enum class WeightKeyword {
            NORMAL, BOLD, LIGHTER, BOLDER
        }
    }
}
