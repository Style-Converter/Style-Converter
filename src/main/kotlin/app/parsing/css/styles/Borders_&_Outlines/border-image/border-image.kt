package app.parsing.css.styles

import app.PropertyIR
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private fun refactor(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	// border-image: <source> || <slice> [ / <width> [ / <outset> ] ] || <repeat>
	// We will detect tokens by simple heuristics and dispatch to specific props
	val s = input.trim()
	// split on '/' to capture slice/width/outset sections
	val slashParts = s.split("/").map { it.trim() }
	// First, attempt to find source (url(...) or none)
	val sourceRegex = Regex("^url\\(.*\\)|none$", RegexOption.IGNORE_CASE)
	val tokens = s.split(Regex("\\s+")).filter { it.isNotEmpty() }
	val sourceTok = tokens.firstOrNull { sourceRegex.containsMatchIn(it) }
	sourceTok?.let { parseBaseStyles(buildJsonObject { put("border-image-source", JsonPrimitive(it)) }, acc) }
	// Slice is before first '/'
	val slicePart = slashParts.getOrNull(0)
	if (!slicePart.isNullOrEmpty()) {
		// If we consumed source as a single token, keep slicePart anyway (browsers allow orders)
		parseBaseStyles(buildJsonObject { put("border-image-slice", JsonPrimitive(slicePart)) }, acc)
	}
	// Width is second part if present
	val widthPart = slashParts.getOrNull(1)
	if (!widthPart.isNullOrEmpty()) parseBaseStyles(buildJsonObject { put("border-image-width", JsonPrimitive(widthPart)) }, acc)
	// Outset is third part if present
	val outsetPart = slashParts.getOrNull(2)
	if (!outsetPart.isNullOrEmpty()) parseBaseStyles(buildJsonObject { put("border-image-outset", JsonPrimitive(outsetPart)) }, acc)
	// Repeat keywords may appear anywhere
	val repeatKeywords = setOf("stretch","repeat","round","space")
	val repeatTok = tokens.lastOrNull { repeatKeywords.contains(it.lowercase()) }
	repeatTok?.let { parseBaseStyles(buildJsonObject { put("border-image-repeat", JsonPrimitive(it)) }, acc) }
	return acc
}

fun parseBorderImage(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-image"]?.jsonPrimitive?.contentOrNull ?: return acc
	return refactor(raw, acc)
}