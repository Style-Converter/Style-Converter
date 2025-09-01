package app.parsing.css.styles.margin

import app.BaseIR
import app.SpacingIR
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object marginShorthand {
    fun applyMargin(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val spec = value.jsonPrimitive.content.trim()
        if (spec.isEmpty()) return acc
        val parts = spec.split(" ").filter { it.isNotBlank() }
        val px = parts.map { parsePx(it) }
        if (px.any { it == null }) return acc
        val t = px.getOrNull(0)
        val r = px.getOrNull(1) ?: t
        val b = px.getOrNull(2) ?: t
        val l = px.getOrNull(3) ?: r
        val spacing = acc.spacing ?: SpacingIR()
        return acc.copy(
            spacing = spacing.copy(
                marginTopPx = t,
                marginRightPx = r,
                marginBottomPx = b,
                marginLeftPx = l
            )
        )
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

