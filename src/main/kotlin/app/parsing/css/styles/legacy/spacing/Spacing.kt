package app.parsing.css.styles.spacing

import app.BaseIR
import app.SpacingIR
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object spacing {
	fun applyMarginTop(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
		val px = parsePx(value.jsonPrimitive.content) ?: return acc
		val cur = acc.spacing ?: SpacingIR()
		return acc.copy(spacing = cur.copy(marginTopPx = px))
	}

	private fun parsePx(v: String): Double? {
		val t = v.trim().lowercase()
		return when {
			t.endsWith("px") -> t.removeSuffix("px").toDoubleOrNull()
			t == "0" -> 0.0
			else -> t.toDoubleOrNull()
		}
	}
}


