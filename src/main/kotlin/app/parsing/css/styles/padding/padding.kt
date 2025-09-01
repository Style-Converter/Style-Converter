package app.parsing.css.styles.padding

import app.BaseIR
import app.SpacingIR
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object paddingShorthand {
    fun applyPadding(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
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
                paddingTopPx = t,
                paddingRightPx = r,
                paddingBottomPx = b,
                paddingLeftPx = l
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

