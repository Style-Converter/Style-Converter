package app.irmodels.properties.table

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class CaptionSideProperty(
    val side: CaptionSide
) : IRProperty {
    override val propertyName = "caption-side"

    enum class CaptionSide {
        TOP, BOTTOM
    }
}
