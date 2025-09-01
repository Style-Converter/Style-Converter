package app.parsing.css.styles.marginblock

import app.BaseIR
import app.SpacingIR
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object marginBlockStart {
    fun applyMarginBlockStart(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val raw = value.jsonPrimitive.content
        val px = parsePx(raw) ?: return acc
        val spacing = acc.spacing ?: SpacingIR()
        // For now map block-start to marginTopPx
        return acc.copy(spacing = spacing.copy(marginTopPx = px))
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

