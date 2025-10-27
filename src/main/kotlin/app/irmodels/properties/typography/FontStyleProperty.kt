package app.irmodels.properties.typography

import app.irmodels.IRAngle
import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class FontStyleProperty(
    val style: FontStyle
) : IRProperty {
    override val propertyName = "font-style"

    @Serializable
    sealed interface FontStyle {
        @Serializable
        data class Normal(val unit: Unit = Unit) : FontStyle

        @Serializable
        data class Italic(val unit: Unit = Unit) : FontStyle

        @Serializable
        data class Oblique(val angle: IRAngle? = null) : FontStyle
    }
}
