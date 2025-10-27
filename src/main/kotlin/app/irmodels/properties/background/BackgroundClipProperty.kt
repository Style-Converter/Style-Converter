package app.irmodels.properties.background

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class BackgroundClipProperty(
    val clips: List<Clip>
) : IRProperty {
    override val propertyName = "background-clip"

    enum class Clip {
        BORDER_BOX, PADDING_BOX, CONTENT_BOX, TEXT
    }
}
