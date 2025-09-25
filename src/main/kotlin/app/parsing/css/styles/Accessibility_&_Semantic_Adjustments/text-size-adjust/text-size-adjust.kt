package app.parsing.css.styles

import app.PropertyIR
import app.parsing.parseSizeToken
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private val GLOBAL_KEYWORDS = setOf("inherit","initial","unset","revert","revert-layer")

private fun define(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val v = input.trim().lowercase()
	val name = "text-size-adjust"
	val idx = acc.indexOfFirst { it.name == name }
	val existing = if (idx >= 0) acc[idx] else PropertyIR(name = name)
	if (v in GLOBAL_KEYWORDS) {
		val mergedGlobal = existing.copy(value = v)
		if (idx >= 0) acc[idx] = mergedGlobal else acc.add(mergedGlobal)
		return acc
	}
	val merged = when (v) {
		"none", "auto" -> existing.copy(value = v)
		else -> {
			// Accept percentages or unitless numbers as per spec
			val (num, unit) = parseSizeToken(input)
			if (unit == "%" || unit == null && num != null) {
				existing.copy(sizeName = unit, numericSizeValue = num, stringSizeValue = input)
			} else {
				return acc
			}
		}
	}
	if (idx >= 0) acc[idx] = merged else acc.add(merged)
	return acc
}

fun parseTextSizeAdjust(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["text-size-adjust"]?.jsonPrimitive?.contentOrNull ?: return acc
	return define(raw, acc)
}