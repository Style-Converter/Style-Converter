package app.parsing.css.styles.paddingblock

import app.BaseIR
import app.SpacingIR
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object paddingBlockEnd {
	fun applyPaddingBlockEnd(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
		val raw = value.jsonPrimitive.content
		val px = parsePx(raw) ?: return acc
		val spacing = acc.spacing ?: SpacingIR()
		return acc.copy(spacing = spacing.copy(paddingBottomPx = px))
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

