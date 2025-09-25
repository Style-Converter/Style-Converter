package app.parsing.css.selectors

import app.SelectorIR
import app.PropertyIR
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun parseSelectors(arr: JsonArray?, baseParser: (JsonObject) -> MutableList<PropertyIR>): List<SelectorIR> {
	if (arr == null) return emptyList()
	return arr.mapNotNull { el ->
		val obj = el.jsonObject
		val whenStr = obj["when"]?.jsonPrimitive?.content ?: return@mapNotNull null
		val styles = obj["styles"]?.jsonObject ?: return@mapNotNull null
		SelectorIR(condition = whenStr, styles = baseParser(styles))
	}
}


