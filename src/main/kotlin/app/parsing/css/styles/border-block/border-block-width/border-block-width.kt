package app.parsing.css.styles.borderblock

import app.BaseIR
import app.BorderIR
import app.BorderSide
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object borderBlockWidth {
    fun applyBorderBlockWidth(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val px = parsePx(value.jsonPrimitive.content) ?: return acc
        val current = acc.border ?: BorderIR()
        val top = (current.top ?: BorderSide()).copy(widthPx = px)
        val bottom = (current.bottom ?: BorderSide()).copy(widthPx = px)
        return acc.copy(border = current.copy(top = top, bottom = bottom))
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

