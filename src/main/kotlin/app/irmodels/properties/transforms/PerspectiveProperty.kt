package app.irmodels.properties.transforms

import app.irmodels.*
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `perspective` property.
 *
 * ## CSS Property
 * **Syntax**: `perspective: none | <length>`
 *
 * ## Description
 * Determines the distance between the z=0 plane and the user in order to give a 3D-positioned element some perspective.
 *
 * @property value The perspective value
 * @see [MDN perspective](https://developer.mozilla.org/en-US/docs/Web/CSS/perspective)
 */
@Serializable
data class PerspectiveProperty(
    val value: Perspective
) : IRProperty {
    override val propertyName = "perspective"

    @Serializable
    sealed interface Perspective {
        @Serializable
        data class None(val unit: Unit = Unit) : Perspective

        @Serializable
        data class LengthValue(val length: IRLength) : Perspective
    }
}
