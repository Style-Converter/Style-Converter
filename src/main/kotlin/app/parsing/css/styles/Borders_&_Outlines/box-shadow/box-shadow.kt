package app.parsing.css.styles

import app.PropertyIR
import app.parsing.parseColor
import app.parsing.parseSizeToken
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private data class ShadowTokens(
	val offsetX: String?,
	val offsetY: String?,
	val blur: String?,
	val spread: String?,
	val colorRaw: String?,
	val inset: Boolean
)

private fun splitBoxShadowList(input: String): List<String> {
    val parts = mutableListOf<String>()
    val sb = StringBuilder()
    var depth = 0
    input.forEach { ch ->
        when (ch) {
            '(' -> { depth += 1; sb.append(ch) }
            ')' -> { depth = (depth - 1).coerceAtLeast(0); sb.append(ch) }
            ',' -> {
                if (depth == 0) {
                    parts.add(sb.toString().trim())
                    sb.setLength(0)
                } else sb.append(ch)
            }
            else -> sb.append(ch)
        }
    }
    val last = sb.toString().trim()
    if (last.isNotEmpty()) parts.add(last)
    return parts
}

private fun tokenizeBoxShadow(input: String): ShadowTokens {
	val tokens = input.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
	var inset = false
	val parts = mutableListOf<String>()
	for (t in tokens) {
		if (t.equals("inset", ignoreCase = true)) inset = true else parts.add(t)
	}
	var idx = 0
	fun takeSize(): String? {
		val t = parts.getOrNull(idx) ?: return null
		val (n, _) = parseSizeToken(t)
		return if (n != null) { idx++; t } else null
	}
	val x = takeSize()
	val y = takeSize()
	val b = takeSize()
	val s = takeSize()
	val remaining = parts.drop(idx)
	val color = remaining.firstOrNull { parseColor(it) != null }
	return ShadowTokens(x, y, b, s, color, inset)
}

private fun define(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
    val parts = splitBoxShadowList(input)
    if (parts.isEmpty()) return acc
    for (segment in parts) {
        val t = tokenizeBoxShadow(segment)
        val color = t.colorRaw?.let { parseColor(it) }
        acc.add(
            PropertyIR(
                name = "box-shadow",
                stringSizeValue = segment.trim(),
                color = color,
                value = if (t.inset) "inset" else null
            )
        )
    }
    return acc
}

fun parseBoxShadow(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["box-shadow"]?.jsonPrimitive?.contentOrNull ?: return acc
	return define(raw, acc)
}