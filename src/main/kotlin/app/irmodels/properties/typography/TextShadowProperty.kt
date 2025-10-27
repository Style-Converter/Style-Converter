package app.irmodels.properties.typography

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class TextShadowProperty(
    val shadows: List<Shadow>
) : IRProperty {
    override val propertyName = "text-shadow"

    @Serializable
    data class Shadow(
        val offsetX: IRLength,
        val offsetY: IRLength,
        val blurRadius: IRLength?,
        val color: IRColor?
    )
}
