package app.parsing.css.styles.margininline

import app.BaseIR
import app.SpacingIR
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object marginInlineEnd {
    fun applyMarginInlineEnd(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val raw = value.jsonPrimitive.content
        val px = parsePx(raw) ?: return acc
        val spacing = acc.spacing ?: SpacingIR()
        // Map inline-end to marginRightPx for now
        return acc.copy(spacing = spacing.copy(marginRightPx = px))
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

