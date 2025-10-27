package app.irmodels.properties.background

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class BackgroundAttachmentProperty(
    val attachments: List<Attachment>
) : IRProperty {
    override val propertyName = "background-attachment"

    enum class Attachment {
        SCROLL, FIXED, LOCAL
    }
}
