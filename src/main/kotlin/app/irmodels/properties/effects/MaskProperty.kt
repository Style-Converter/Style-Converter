package app.irmodels.properties.effects

import app.irmodels.IRProperty
import app.irmodels.IRUrl
import kotlinx.serialization.Serializable

@Serializable
data class MaskProperty(
    val masks: List<Mask>
) : IRProperty {
    override val propertyName = "mask"

    @Serializable
    sealed interface Mask {
        @Serializable
        data class None(val unit: Unit = Unit) : Mask

        @Serializable
        data class Image(val url: IRUrl) : Mask
    }
}
