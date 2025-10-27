package app.irmodels.properties.background

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class BackgroundOriginProperty(
    val origins: List<Origin>
) : IRProperty {
    override val propertyName = "background-origin"

    enum class Origin {
        BORDER_BOX, PADDING_BOX, CONTENT_BOX
    }
}
