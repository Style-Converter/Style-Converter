package app.irmodels.properties.typography

import app.irmodels.IRLength
import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class WordSpacingProperty(
    val spacing: WordSpacing
) : IRProperty {
    override val propertyName = "word-spacing"

    @Serializable
    sealed interface WordSpacing {
        @Serializable
        data class Normal(val unit: Unit = Unit) : WordSpacing

        @Serializable
        data class LengthValue(val length: IRLength) : WordSpacing
    }
}
