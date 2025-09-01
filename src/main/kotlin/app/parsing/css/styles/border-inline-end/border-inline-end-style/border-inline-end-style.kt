package app.parsing.css.styles.borderinlineend

import app.BaseIR
import app.BorderIR
import app.BorderSide
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object borderInlineEndStyle {
    fun applyBorderInlineEndStyle(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val style = value.jsonPrimitive.content.trim()
        val current = acc.border ?: BorderIR()
        val right = current.right ?: BorderSide()
        return acc.copy(border = current.copy(right = right.copy(style = style)))
    }
}

