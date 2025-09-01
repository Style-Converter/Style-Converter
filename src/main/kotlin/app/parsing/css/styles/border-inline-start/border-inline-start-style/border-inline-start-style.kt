package app.parsing.css.styles.borderinlinestart

import app.BaseIR
import app.BorderIR
import app.BorderSide
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object borderInlineStartStyle {
    fun applyBorderInlineStartStyle(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val style = value.jsonPrimitive.content.trim()
        val current = acc.border ?: BorderIR()
        val left = current.left ?: BorderSide()
        return acc.copy(border = current.copy(left = left.copy(style = style)))
    }
}

