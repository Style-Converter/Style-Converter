package app.parsing.css.styles.font

import app.BaseIR
import app.TypographyIR
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object fontFamily {
    fun applyFontFamily(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val f = value.jsonPrimitive.content.trim()
        val t = acc.typography ?: TypographyIR()
        val other = (acc.other ?: emptyMap()).toMutableMap()
        other["font-family"] = f
        return acc.copy(typography = t, other = other)
    }
}

