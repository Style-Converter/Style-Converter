package app.irmodels.properties.borders

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class BoxShadowProperty(
    val shadows: List<Shadow>
) : IRProperty {
    override val propertyName = "box-shadow"

    @Serializable
    data class Shadow(
        val offsetX: IRLength,
        val offsetY: IRLength,
        val blurRadius: IRLength?,
        val spreadRadius: IRLength?,
        val color: IRColor?,
        val inset: Boolean = false
    )
}
