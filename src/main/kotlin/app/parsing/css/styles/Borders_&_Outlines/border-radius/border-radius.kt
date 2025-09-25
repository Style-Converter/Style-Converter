package app.parsing.css.styles

import app.PropertyIR
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private fun splitTokensRespectParens(s: String): List<String> {
	val tokens = mutableListOf<String>()
	val sb = StringBuilder()
	var depth = 0
	var i = 0
	while (i < s.length) {
		val ch = s[i]
		when (ch) {
			'(' -> { depth++; sb.append(ch) }
			')' -> { depth = (depth - 1).coerceAtLeast(0); sb.append(ch) }
			' ', '\t', '\n', '\r' -> {
				if (depth == 0) {
					if (sb.isNotEmpty()) { tokens.add(sb.toString()); sb.setLength(0) }
				} else sb.append(ch)
			}
			else -> sb.append(ch)
		}
		i++
	}
	val last = sb.toString()
	if (last.isNotBlank()) tokens.add(last)
	return tokens
}

private fun splitOnTopLevelSlash(s: String): Pair<String, String?> {
	var depth = 0
	for (i in s.indices) {
		val ch = s[i]
		when (ch) {
			'(' -> depth++
			')' -> depth = (depth - 1).coerceAtLeast(0)
			'/' -> if (depth == 0) {
				val left = s.substring(0, i).trim()
				val right = s.substring(i + 1).trim()
				return left to right
			}
		}
	}
	return s.trim() to null
}

private fun refactor(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	// Split on top-level slash into horizontal and vertical radii lists
	val (mainH, mainVOpt) = splitOnTopLevelSlash(input)
	val hTokens = splitTokensRespectParens(mainH)
	val vTokens = mainVOpt?.let { splitTokensRespectParens(it) }
	if (hTokens.isEmpty()) return acc

	fun expand4(tokens: List<String>): List<String> {
		return when (tokens.size) {
			1 -> List(4) { tokens[0] }
			2 -> listOf(tokens[0], tokens[1], tokens[0], tokens[1])
			3 -> listOf(tokens[0], tokens[1], tokens[2], tokens[1])
			else -> listOf(tokens[0], tokens[1], tokens[2], tokens[3])
		}
	}

	val h4 = expand4(hTokens)
    val values = if (vTokens != null && vTokens.isNotEmpty()) {
		val v4 = expand4(vTokens)
		listOf(
            "${h4[0]} / ${v4[0]}",
            "${h4[1]} / ${v4[1]}",
            "${h4[2]} / ${v4[2]}",
            "${h4[3]} / ${v4[3]}"
		)
	} else h4

	val keys = listOf(
		"border-top-left-radius",
		"border-top-right-radius",
		"border-bottom-right-radius",
		"border-bottom-left-radius"
	)
	for (i in 0 until 4) {
		parseBaseStyles(buildJsonObject { put(keys[i], JsonPrimitive(values[i])) }, acc)
	}
	return acc
}

fun parseBorderRadius(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-radius"]?.jsonPrimitive?.contentOrNull ?: return acc
	return refactor(raw, acc)
}