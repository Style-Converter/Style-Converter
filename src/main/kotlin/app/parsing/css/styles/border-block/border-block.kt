package app.parsing.css.styles.borderblock

import app.BaseIR
import app.BorderIR
import app.BorderSide
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object borderBlockShorthand {
    fun applyBorderBlock(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val spec = value.jsonPrimitive.content.trim()
        val current = acc.border ?: BorderIR()
        val top = current.top ?: BorderSide()
        val bottom = current.bottom ?: BorderSide()
        val token = spec
        val updatedTop = when {
            token.endsWith("px") || token.toDoubleOrNull() != null -> top.copy(widthPx = token.removeSuffix("px").toDoubleOrNull() ?: token.toDoubleOrNull())
            token.startsWith("#") -> top.copy()
            else -> top.copy(style = token)
        }
        val updatedBottom = when {
            token.endsWith("px") || token.toDoubleOrNull() != null -> bottom.copy(widthPx = token.removeSuffix("px").toDoubleOrNull() ?: token.toDoubleOrNull())
            token.startsWith("#") -> bottom.copy()
            else -> bottom.copy(style = token)
        }
        return acc.copy(border = current.copy(top = updatedTop, bottom = updatedBottom))
    }
}

