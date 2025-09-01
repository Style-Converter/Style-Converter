package app.parsing.css.styles.font

import app.BaseIR
import app.TypographyIR
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object fontSize {
    fun applyFontSize(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val px = parsePx(value.jsonPrimitive.content) ?: return acc
        val t = acc.typography ?: TypographyIR()
        return acc.copy(typography = t.copy(fontSizePx = px))
    }

    private fun parsePx(input: String): Double? {
        val s = input.trim().lowercase()
        return when {
            s.endsWith("px") -> s.removeSuffix("px").toDoubleOrNull()
            s == "0" -> 0.0
            else -> s.toDoubleOrNull()
        }
    }
}

