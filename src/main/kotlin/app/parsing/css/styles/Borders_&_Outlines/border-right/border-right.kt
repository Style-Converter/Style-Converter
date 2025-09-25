package app.parsing.css.styles

import app.PropertyIR
import app.parsing.parseSizeToken
import app.parsing.parseColor
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonPrimitive

private fun refactor(input: String, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
    val tokens = input.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    var sizeToken: String? = null
    var borderType: String? = null
    var colorToken: String? = null
    val allowed = setOf("none","hidden","dotted","dashed","solid","double","groove","ridge","inset","outset")
    for (t in tokens) {
        if (sizeToken == null) {
            val (num, _) = parseSizeToken(t)
            if (num != null) sizeToken = t
        }
        if (borderType == null && t.lowercase() in allowed) borderType = t
        if (colorToken == null && parseColor(t) != null) colorToken = t
    }
    val objs = buildList<JsonObject> {
        sizeToken?.let { add(buildJsonObject { put("border-right-width", JsonPrimitive(it)) }) }
        borderType?.let { add(buildJsonObject { put("border-right-style", JsonPrimitive(it)) }) }
        colorToken?.let { add(buildJsonObject { put("border-right-color", JsonPrimitive(it)) }) }
    }
    for (obj in objs) parseBaseStyles(obj, acc)
    return acc
}

fun parseBorderRight(styles: JsonObject, acc: MutableList<PropertyIR>): MutableList<PropertyIR> {
    val raw = styles["border-right"]?.jsonPrimitive?.contentOrNull ?: return acc
    return refactor(raw, acc)
}