package app.parsing.css.styles.borderinline

import app.BaseIR
import app.BorderIR
import app.BorderSide
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object borderInlineStyle {
    fun applyBorderInlineStyle(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val style = value.jsonPrimitive.content.trim()
        val current = acc.border ?: BorderIR()
        val left = (current.left ?: BorderSide()).copy(style = style)
        val right = (current.right ?: BorderSide()).copy(style = style)
        return acc.copy(border = current.copy(left = left, right = right))
    }
}

