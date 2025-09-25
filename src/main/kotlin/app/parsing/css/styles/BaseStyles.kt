package app.parsing.css.styles

import app.PropertyIR
import kotlinx.serialization.json.JsonObject

typealias CategoryParser = (styles: JsonObject, acc: MutableList<PropertyIR>) -> MutableList<PropertyIR>

fun parseBaseStyles(styles: JsonObject, acc: MutableList<PropertyIR> = mutableListOf()): MutableList<PropertyIR> {
	var next = acc
	val parsers: List<CategoryParser> = listOf(
        ::parseBordersAndOutlines,
        ::parseAccessibilityAndSemanticAdjustments
	)
	for (parser in parsers) {
		next = parser(styles, next)
	}
	return next
}