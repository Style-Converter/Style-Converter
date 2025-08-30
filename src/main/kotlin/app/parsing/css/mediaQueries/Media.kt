package app.parsing.css.mediaQueries

import app.MediaIR
import app.BaseIR
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun parseMedia(arr: JsonArray?, baseParser: (JsonObject) -> BaseIR): List<MediaIR> {
	if (arr == null) return emptyList()
	return arr.mapNotNull { el ->
		val obj = el.jsonObject
		val query = obj["query"]?.jsonPrimitive?.content ?: return@mapNotNull null
		val styles = obj["styles"]?.jsonObject ?: return@mapNotNull null
		MediaIR(query = query, styles = baseParser(styles))
	}
}


