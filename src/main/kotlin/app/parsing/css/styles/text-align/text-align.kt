package app.parsing.css.styles.textalign

import app.BaseIR
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object textAlign {
    fun applyTextAlign(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val v = value.jsonPrimitive.content.trim()
        val other = (acc.other ?: emptyMap()).toMutableMap()
        other["text-align"] = v
        return acc.copy(other = other)
    }
}

