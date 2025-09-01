package app.parsing.css.styles.borderwidth

import app.BaseIR
import app.BorderIR
import app.BorderSide
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object borderBottomWidth {
    fun applyBorderBottomWidth(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val px = parsePx(value.jsonPrimitive.content) ?: return acc
        val current = acc.border ?: BorderIR()
        val bottom = current.bottom ?: BorderSide()
        return acc.copy(border = current.copy(bottom = bottom.copy(widthPx = px)))
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

