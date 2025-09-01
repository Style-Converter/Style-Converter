package app.parsing.css.styles.margininline

import app.BaseIR
import app.SpacingIR
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object marginInlineShorthand {
    fun applyMarginInline(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val spec = value.jsonPrimitive.content.trim()
        if (spec.isEmpty()) return acc
        val parts = spec.split(" ").filter { it.isNotBlank() }
        val px = parts.map { parsePx(it) }
        if (px.any { it == null }) return acc
        val start = px.getOrNull(0)
        val end = px.getOrNull(1) ?: start
        val spacing = acc.spacing ?: SpacingIR()
        // Map inline to left/right
        return acc.copy(spacing = spacing.copy(marginLeftPx = start, marginRightPx = end))
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

