package app.parsing.css.styles

import app.PropertyIR
import app.parsing.parseSizeToken
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private fun refactor(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val (num, unit) = parseSizeToken(input)
	val n = num ?: return acc
	val token = if (unit != null) "$n$unit" else "$n"
    // expand to border-top/bottom and also logical inline start/end
	val objs = listOf(
        kotlinx.serialization.json.buildJsonObject { put("border-inline-start-width", kotlinx.serialization.json.JsonPrimitive(token)) },
        kotlinx.serialization.json.buildJsonObject { put("border-inline-end-width", kotlinx.serialization.json.JsonPrimitive(token)) }
	)
	for (obj in objs) parseBaseStyles(obj, acc)
	return acc
}

fun parseBorderBlockWidth(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-block-width"]?.jsonPrimitive?.contentOrNull ?: return acc
	return refactor(raw, acc)
}