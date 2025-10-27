package app.irmodels.properties.typography

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class WordBreakProperty(
    val wordBreak: WordBreak
) : IRProperty {
    override val propertyName = "word-break"

    enum class WordBreak {
        NORMAL, BREAK_ALL, KEEP_ALL, BREAK_WORD
    }
}
