package app.parsing.css.styles.borderstyle

import app.BaseIR
import app.BorderIR
import app.BorderSide
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object borderStyleShorthand {
    fun applyBorderStyle(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val spec = value.jsonPrimitive.content.trim()
        val current = acc.border ?: BorderIR()
        return acc.copy(
            border = current.copy(
                top = (current.top ?: BorderSide()).copy(style = spec),
                right = (current.right ?: BorderSide()).copy(style = spec),
                bottom = (current.bottom ?: BorderSide()).copy(style = spec),
                left = (current.left ?: BorderSide()).copy(style = spec)
            )
        )
    }
}

