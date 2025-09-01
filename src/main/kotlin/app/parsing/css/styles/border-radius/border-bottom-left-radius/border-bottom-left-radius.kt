package app.parsing.css.styles.borderradius

import app.BaseIR
import app.BorderIR
import app.BorderRadius
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object borderBottomLeftRadius {
    fun applyBorderBottomLeftRadius(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val px = parsePx(value.jsonPrimitive.content) ?: return acc
        val current = acc.border ?: BorderIR()
        val radius = current.radius ?: BorderRadius()
        return acc.copy(border = current.copy(radius = radius.copy(bottomLeftPx = px)))
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

