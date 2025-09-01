package app.parsing.css.styles.borderwidth

import app.BaseIR
import app.BorderIR
import app.BorderSide
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object borderWidthShorthand {
    fun applyBorderWidth(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val spec = value.jsonPrimitive.content.trim()
        if (spec.isEmpty()) return acc
        val parts = spec.split(" ").filter { it.isNotBlank() }
        val px = parts.map { parsePx(it) }
        if (px.any { it == null }) return acc
        val top = px.getOrNull(0)
        val right = px.getOrNull(1) ?: top
        val bottom = px.getOrNull(2) ?: top
        val left = px.getOrNull(3) ?: right
        val current = acc.border ?: BorderIR()
        return acc.copy(
            border = current.copy(
                top = (current.top ?: BorderSide()).copy(widthPx = top),
                right = (current.right ?: BorderSide()).copy(widthPx = right),
                bottom = (current.bottom ?: BorderSide()).copy(widthPx = bottom),
                left = (current.left ?: BorderSide()).copy(widthPx = left)
            )
        )
    }

    private fun parsePx(input: String): Double? {
        val t = input.trim().lowercase()
        return when {
            t.endsWith("px") -> t.removeSuffix("px").toDoubleOrNull()
            t == "0" -> 0.0
            else -> t.toDoubleOrNull()
        }
    }
}

