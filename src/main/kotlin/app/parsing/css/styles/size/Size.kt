package app.parsing.css.styles.size

import app.BaseIR
import app.SizeIR
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object size {
	fun applyWidth(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
		val px = parsePx(value.jsonPrimitive.content) ?: return acc
		val cur = acc.size ?: SizeIR()
		return acc.copy(size = cur.copy(widthPx = px))
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


