package app.irmodels.properties.typography

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class TextIndentProperty(
    val indent: TextIndent
) : IRProperty {
    override val propertyName = "text-indent"

    @Serializable
    sealed interface TextIndent {
        @Serializable
        data class LengthValue(val length: IRLength) : TextIndent

        @Serializable
        data class PercentageValue(val percentage: IRPercentage) : TextIndent
    }
}
