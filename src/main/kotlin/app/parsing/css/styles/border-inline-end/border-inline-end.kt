package app.parsing.css.styles.borderinlineend

import app.BaseIR
import app.BorderIR
import app.BorderSide
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object borderInlineEndShorthand {
    fun applyBorderInlineEnd(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val spec = value.jsonPrimitive.content.trim()
        val current = acc.border ?: BorderIR()
        val right = current.right ?: BorderSide()
        val token = spec
        val updatedRight = when {
            token.endsWith("px") || token.toDoubleOrNull() != null -> right.copy(widthPx = token.removeSuffix("px").toDoubleOrNull() ?: token.toDoubleOrNull())
            token.startsWith("#") -> right.copy()
            else -> right.copy(style = token)
        }
        return acc.copy(border = current.copy(right = updatedRight))
    }
}

