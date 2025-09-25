package app.parsing.css.styles

import app.PropertyIR
import app.parsing.parseSizeToken
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

fun parseBorderTopWidth(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
	val raw = styles["border-top-width"]?.jsonPrimitive?.contentOrNull ?: return acc
    val (num, unit) = parseSizeToken(raw)
    val name = "border-top"
    val idx = acc.indexOfFirst { it.name == name }
    val existing = if (idx >= 0) acc[idx] else PropertyIR(name = name)
    val merged = if (num != null) existing.copy(sizeName = unit ?: existing.sizeName, numericSizeValue = num) else existing.copy(stringSizeValue = raw)
    if (idx >= 0) acc[idx] = merged else acc.add(merged)
	return acc
}


