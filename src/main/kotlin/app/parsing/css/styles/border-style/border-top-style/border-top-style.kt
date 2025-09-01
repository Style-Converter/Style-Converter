package app.parsing.css.styles.borderstyle

import app.BaseIR
import app.BorderIR
import app.BorderSide
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object borderTopStyle {
    fun applyBorderTopStyle(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val style = value.jsonPrimitive.content.trim()
        val current = acc.border ?: BorderIR()
        val top = current.top ?: BorderSide()
        return acc.copy(border = current.copy(top = top.copy(style = style)))
    }
}

