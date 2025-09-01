package app.parsing.css.styles.font

import app.BaseIR
import app.TypographyIR
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object fontWeight {
    fun applyFontWeight(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val w = value.jsonPrimitive.content.trim()
        val t = acc.typography ?: TypographyIR()
        // Store as other for now; extend TypographyIR later if needed
        val other = (acc.other ?: emptyMap()).toMutableMap()
        other["font-weight"] = w
        return acc.copy(typography = t, other = other)
    }
}

