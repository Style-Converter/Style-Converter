package app.parsing.css.styles.background

import app.BaseIR
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object backgroundRepeat {
    fun applyBackgroundRepeat(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val v = value.jsonPrimitive.content.trim()
        val other = (acc.other ?: emptyMap()).toMutableMap()
        other["background-repeat"] = v
        return acc.copy(other = other)
    }
}

