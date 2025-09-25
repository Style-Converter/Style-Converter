package app.parsing.css.styles

import app.PropertyIR
import app.parsing.parseColor
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private fun refactor(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val color = parseColor(input) ?: return acc
    val objs = listOf(
        kotlinx.serialization.json.buildJsonObject { put("border-inline-start-color", kotlinx.serialization.json.JsonPrimitive(input)) },
        kotlinx.serialization.json.buildJsonObject { put("border-inline-end-color", kotlinx.serialization.json.JsonPrimitive(input)) }
    )
	for (obj in objs) parseBaseStyles(obj, acc)
    // ensure color applied
    for (name in listOf("border-inline-start","border-inline-end")) {
		val idx = acc.indexOfFirst { it.name == name }
		if (idx >= 0) acc[idx] = acc[idx].copy(color = color)
	}
	return acc
}

fun parseBorderBlockColor(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-block-color"]?.jsonPrimitive?.contentOrNull ?: return acc
	return refactor(raw, acc)
}