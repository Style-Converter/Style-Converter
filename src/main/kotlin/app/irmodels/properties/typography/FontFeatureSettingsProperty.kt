package app.irmodels.properties.typography

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class FontFeatureSettingsProperty(
    val settings: FeatureSettings
) : IRProperty {
    override val propertyName = "font-feature-settings"

    @Serializable
    sealed interface FeatureSettings {
        @Serializable
        data class Normal(val unit: kotlin.Unit = kotlin.Unit) : FeatureSettings

        @Serializable
        data class Features(val features: List<Feature>) : FeatureSettings
    }

    @Serializable
    data class Feature(
        val tag: String,
        val value: Int? = null
    )
}
