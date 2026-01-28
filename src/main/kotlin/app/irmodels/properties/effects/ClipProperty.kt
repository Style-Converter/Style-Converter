package app.irmodels.properties.effects

import app.irmodels.IRLength
import app.irmodels.IRProperty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `clip` property (legacy).
 *
 * Deprecated in favor of clip-path, but still used in some contexts.
 *
 * Syntax: auto | rect(top, right, bottom, left)
 */
@Serializable
data class ClipProperty(val value: ClipValue) : IRProperty {
    override val propertyName: String = "clip"
}

@Serializable
sealed interface ClipValue {
    @Serializable
    @SerialName("auto")
    data object Auto : ClipValue

    @Serializable
    @SerialName("rect")
    data class Rect(
        val top: IRLength,
        val right: IRLength,
        val bottom: IRLength,
        val left: IRLength
    ) : ClipValue
}
