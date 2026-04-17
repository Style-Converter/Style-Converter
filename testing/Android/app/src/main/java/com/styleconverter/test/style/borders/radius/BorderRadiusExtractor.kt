package com.styleconverter.test.style.borders.radius

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.styleconverter.test.style.core.types.ValueExtractors
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.doubleOrNull

/**
 * Extracts border radius configurations from IR properties.
 * Handles both physical (top-left) and logical (start-start) property names.
 * Supports pixel values and percentage-based radius (resolved from component dimensions).
 */
object BorderRadiusExtractor {

    /**
     * Extract border radius configuration from a list of property type/data pairs.
     *
     * @param properties List of pairs where first is the property type (e.g., "BorderTopLeftRadius")
     *                   and second is the JSON data for that property.
     * @param componentWidth Optional width for resolving percentage-based radius.
     * @param componentHeight Optional height for resolving percentage-based radius.
     * @return BorderRadiusConfig with extracted values for all corners.
     */
    fun extractRadiusConfig(
        properties: List<Pair<String, JsonElement?>>,
        componentWidth: Dp? = null,
        componentHeight: Dp? = null
    ): BorderRadiusConfig {
        var topStart = 0.dp
        var topEnd = 0.dp
        var bottomEnd = 0.dp
        var bottomStart = 0.dp

        properties.forEach { (type, data) ->
            val dp = extractRadiusDp(data, componentWidth, componentHeight) ?: return@forEach
            when (type) {
                // Physical properties (LTR assumed for mapping)
                "BorderTopLeftRadius", "BorderStartStartRadius" -> topStart = dp
                "BorderTopRightRadius", "BorderStartEndRadius" -> topEnd = dp
                "BorderBottomRightRadius", "BorderEndEndRadius" -> bottomEnd = dp
                "BorderBottomLeftRadius", "BorderEndStartRadius" -> bottomStart = dp
            }
        }

        return BorderRadiusConfig(topStart, topEnd, bottomEnd, bottomStart)
    }

    /**
     * Extract a radius value, handling both px and percentage formats.
     * For percentage: resolves against the smaller of width/height (CSS spec for border-radius %).
     * Format: {"original": {"v": 50.0, "u": "PERCENT"}} or {"px": 10.0}
     */
    private fun extractRadiusDp(
        data: JsonElement?,
        componentWidth: Dp?,
        componentHeight: Dp?
    ): Dp? {
        if (data == null) return null

        // Try standard px extraction first
        val pxValue = ValueExtractors.extractDp(data)
        if (pxValue != null) return pxValue

        // Check for percentage in original: {v: 50.0, u: "PERCENT"}
        if (data is JsonObject) {
            val original = data["original"]
            if (original is JsonObject) {
                val v = original["v"]?.jsonPrimitive?.doubleOrNull
                val u = original["u"]?.jsonPrimitive?.content?.uppercase()
                if (v != null && u == "PERCENT") {
                    // CSS border-radius % resolves against the corresponding dimension
                    // Use the smaller dimension as a reasonable approximation
                    val refSize = when {
                        componentWidth != null && componentHeight != null ->
                            minOf(componentWidth, componentHeight)
                        componentWidth != null -> componentWidth
                        componentHeight != null -> componentHeight
                        else -> return null
                    }
                    return refSize * (v.toFloat() / 100f)
                }
            }
        }

        return null
    }
}
