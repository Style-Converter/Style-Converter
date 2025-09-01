package app.parsing.css.styles.background

import app.BaseIR
import app.ColorRgba
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object background {
	fun applyBackgroundColor(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
		val hex = value.jsonPrimitive.content
		val color = parseHexColor(hex) ?: return acc
		return acc.copy(backgroundColor = color)
	}

	private fun parseHexColor(hex: String): ColorRgba? {
		val cleaned = hex.trim()
		if (!cleaned.startsWith("#")) return null
		val s = cleaned.removePrefix("#")
		return when (s.length) {
			6 -> {
				val r = s.substring(0, 2).toInt(16)
				val g = s.substring(2, 4).toInt(16)
				val b = s.substring(4, 6).toInt(16)
				ColorRgba(r, g, b, 1.0)
			}
			8 -> {
				val a = s.substring(0, 2).toInt(16) / 255.0
				val r = s.substring(2, 4).toInt(16)
				val g = s.substring(4, 6).toInt(16)
				val b = s.substring(6, 8).toInt(16)
				ColorRgba(r, g, b, a)
			}
			else -> null
		}
	}
}


