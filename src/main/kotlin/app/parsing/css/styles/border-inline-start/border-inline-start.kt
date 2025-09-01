package app.parsing.css.styles.borderinlinestart

import app.BaseIR
import app.BorderIR
import app.BorderSide
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object borderInlineStartShorthand {
    fun applyBorderInlineStart(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val spec = value.jsonPrimitive.content.trim()
        val current = acc.border ?: BorderIR()
        val left = current.left ?: BorderSide()
        val token = spec
        val updatedLeft = when {
            token.endsWith("px") || token.toDoubleOrNull() != null -> left.copy(widthPx = token.removeSuffix("px").toDoubleOrNull() ?: token.toDoubleOrNull())
            token.startsWith("#") -> left.copy()
            else -> left.copy(style = token)
        }
        return acc.copy(border = current.copy(left = updatedLeft))
    }
}

