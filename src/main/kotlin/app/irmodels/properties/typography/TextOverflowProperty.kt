package app.irmodels.properties.typography

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class TextOverflowProperty(
    val overflow: TextOverflow
) : IRProperty {
    override val propertyName = "text-overflow"

    enum class TextOverflow {
        CLIP, ELLIPSIS
    }
}
