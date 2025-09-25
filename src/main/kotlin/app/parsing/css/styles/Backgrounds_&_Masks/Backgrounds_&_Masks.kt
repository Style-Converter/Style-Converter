package app.parsing.css.styles

import app.PropertyIR
import kotlinx.serialization.json.JsonObject

fun parseBackgroundsAndMasks(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val parsers: List<(JsonObject, MutableList<PropertyIR>) -> MutableList<PropertyIR>> = listOf(
	)
	var next = acc
	for (p in parsers) next = p(styles, next)
	return next
}