package app.parsing.css.styles.borderblock

import app.BaseIR
import app.BorderIR
import app.BorderSide
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object borderBlockStyle {
    fun applyBorderBlockStyle(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val style = value.jsonPrimitive.content.trim()
        val current = acc.border ?: BorderIR()
        val top = (current.top ?: BorderSide()).copy(style = style)
        val bottom = (current.bottom ?: BorderSide()).copy(style = style)
        return acc.copy(border = current.copy(top = top, bottom = bottom))
    }
}

