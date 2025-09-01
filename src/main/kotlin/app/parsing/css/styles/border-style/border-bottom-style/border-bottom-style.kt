package app.parsing.css.styles.borderstyle

import app.BaseIR
import app.BorderIR
import app.BorderSide
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object borderBottomStyle {
    fun applyBorderBottomStyle(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val style = value.jsonPrimitive.content.trim()
        val current = acc.border ?: BorderIR()
        val bottom = current.bottom ?: BorderSide()
        return acc.copy(border = current.copy(bottom = bottom.copy(style = style)))
    }
}

