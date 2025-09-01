package app.parsing.css.styles.borderradius

import app.BaseIR
import app.BorderIR
import app.BorderRadius
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object borderRadiusShorthand {
    fun applyBorderRadius(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val spec = value.jsonPrimitive.content.trim()
        if (spec.isEmpty()) return acc
        val parts = spec.split(" ").filter { it.isNotBlank() }
        val px = parts.map { parsePx(it) }
        if (px.any { it == null }) return acc
        val tl = px.getOrNull(0)
        val tr = px.getOrNull(1) ?: tl
        val br = px.getOrNull(2) ?: tl
        val bl = px.getOrNull(3) ?: tr
        val current = acc.border ?: BorderIR()
        val radius = current.radius ?: BorderRadius()
        return acc.copy(border = current.copy(radius = radius.copy(
            topLeftPx = tl,
            topRightPx = tr,
            bottomRightPx = br,
            bottomLeftPx = bl
        )))
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

