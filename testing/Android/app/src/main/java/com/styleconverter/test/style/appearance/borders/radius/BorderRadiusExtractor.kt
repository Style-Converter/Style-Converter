package com.styleconverter.test.style.appearance.borders.radius

import androidx.compose.ui.unit.dp
import com.styleconverter.test.style.core.types.ValueExtractors
import kotlinx.serialization.json.JsonElement

/**
 * Extracts border radius configurations from IR properties.
 * Handles both physical (top-left) and logical (start-start) property names.
 */
object BorderRadiusExtractor {

    /**
     * Extract border radius configuration from a list of property type/data pairs.
     *
     * @param properties List of pairs where first is the property type (e.g., "BorderTopLeftRadius")
     *                   and second is the JSON data for that property.
     * @return BorderRadiusConfig with extracted values for all corners.
     */
    fun extractRadiusConfig(properties: List<Pair<String, JsonElement?>>): BorderRadiusConfig {
        var topStart = 0.dp
        var topEnd = 0.dp
        var bottomEnd = 0.dp
        var bottomStart = 0.dp

        properties.forEach { (type, data) ->
            val dp = ValueExtractors.extractDp(data) ?: return@forEach
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
}
