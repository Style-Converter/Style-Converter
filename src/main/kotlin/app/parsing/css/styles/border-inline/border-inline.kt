package app.parsing.css.styles.borderinline

import app.BaseIR
import app.BorderIR
import app.BorderSide
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object borderInlineShorthand {
    fun applyBorderInline(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val spec = value.jsonPrimitive.content.trim()
        // For now, treat border-inline shorthand as applying the same style token to both inline edges
        // This is a simplification; full CSS parsing would support width style color ordering.
        val current = acc.border ?: BorderIR()
        val left = current.left ?: BorderSide()
        val right = current.right ?: BorderSide()
        val token = spec
        val updatedLeft = when {
            token.endsWith("px") || token.toDoubleOrNull() != null -> left.copy(widthPx = token.removeSuffix("px").toDoubleOrNull() ?: token.toDoubleOrNull())
            token.startsWith("#") -> left.copy()
            else -> left.copy(style = token)
        }
        val updatedRight = when {
            token.endsWith("px") || token.toDoubleOrNull() != null -> right.copy(widthPx = token.removeSuffix("px").toDoubleOrNull() ?: token.toDoubleOrNull())
            token.startsWith("#") -> right.copy()
            else -> right.copy(style = token)
        }
        return acc.copy(border = current.copy(left = updatedLeft, right = updatedRight))
    }
}

