package app.parsing.css.styles.width

import app.BaseIR
import app.SizeIR
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object width {
    fun applyWidth(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val raw = value.jsonPrimitive.content
        val px = parsePx(raw) ?: return acc
        val size = acc.size ?: SizeIR()
        return acc.copy(size = size.copy(widthPx = px))
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

