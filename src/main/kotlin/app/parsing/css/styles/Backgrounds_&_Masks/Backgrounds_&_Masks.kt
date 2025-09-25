package app.parsing.css.styles

import app.BaseIR
import kotlinx.serialization.json.JsonObject

fun parseBackgroundsAndMasks(styles: JsonObject, acc: BaseIR): BaseIR {
	val parsers: List<(JsonObject, BaseIR) -> BaseIR> = listOf(
	)
	var next = acc
	for (p in parsers) next = p(styles, next)
	return next
}