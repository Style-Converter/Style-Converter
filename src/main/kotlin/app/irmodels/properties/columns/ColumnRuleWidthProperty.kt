package app.irmodels.properties.columns

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class ColumnRuleWidthProperty(
    val width: RuleWidth
) : IRProperty {
    override val propertyName = "column-rule-width"

    @Serializable
    sealed interface RuleWidth {
        @Serializable
        data class LengthValue(val length: IRLength) : RuleWidth

        @Serializable
        data class Keyword(val value: RuleWidthKeyword) : RuleWidth
    }

    enum class RuleWidthKeyword {
        THIN, MEDIUM, THICK
    }
}
