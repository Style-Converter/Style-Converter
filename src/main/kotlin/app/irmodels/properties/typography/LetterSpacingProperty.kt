package app.irmodels.properties.typography

import app.irmodels.IRLength
import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class LetterSpacingProperty(
    val spacing: LetterSpacing
) : IRProperty {
    override val propertyName = "letter-spacing"

    @Serializable
    sealed interface LetterSpacing {
        @Serializable
        data class Normal(val unit: Unit = Unit) : LetterSpacing

        @Serializable
        data class LengthValue(val length: IRLength) : LetterSpacing
    }
}
