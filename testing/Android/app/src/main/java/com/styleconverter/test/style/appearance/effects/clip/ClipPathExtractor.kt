package com.styleconverter.test.style.appearance.effects.clip

import androidx.compose.ui.unit.dp
import com.styleconverter.test.style.core.types.ValueExtractors
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Extracts clip-path configuration from IR property JSON data.
 *
 * Handles the following IR property types:
 * - `ClipPath` - CSS `clip-path` property
 * - `Clip` - Legacy CSS `clip` property (deprecated but still used)
 *
 * ## IR Format Examples
 * ```json
 * // Circle
 * {
 *   "type": "ClipPath",
 *   "data": {
 *     "type": "circle",
 *     "radius": { "percentage": 50 },
 *     "x": 50,
 *     "y": 50
 *   }
 * }
 *
 * // Polygon
 * {
 *   "type": "ClipPath",
 *   "data": {
 *     "type": "polygon",
 *     "points": [
 *       { "x": 50, "y": 0 },
 *       { "x": 100, "y": 100 },
 *       { "x": 0, "y": 100 }
 *     ]
 *   }
 * }
 * ```
 */
object ClipPathExtractor {

    /**
     * Extract clip-path configuration from a list of property type/data pairs.
     *
     * @param properties List of pairs where first is the property type
     *                   and second is the JSON data for that property.
     * @return ClipPathConfig with extracted clip path shape.
     */
    fun extractClipPathConfig(properties: List<Pair<String, JsonElement?>>): ClipPathConfig {
        for ((type, data) in properties) {
            if (type == "ClipPath" && data != null) {
                return ClipPathConfig(shape = extractShape(data))
            }
        }
        return ClipPathConfig()
    }

    /**
     * Extract a clip shape from JSON data.
     */
    private fun extractShape(json: JsonElement): ClipShape? {
        if (json !is JsonObject) return null

        val type = json["type"]?.jsonPrimitive?.content?.lowercase()

        return when (type) {
            "circle" -> extractCircle(json)
            "ellipse" -> extractEllipse(json)
            "inset" -> extractInset(json)
            "polygon" -> extractPolygon(json)
            "path" -> extractPath(json)
            else -> null
        }
    }

    /**
     * Extract circle shape configuration.
     */
    private fun extractCircle(json: JsonObject): ClipShape.Circle {
        val radius = json["radius"]?.let { extractClipRadius(it) } ?: ClipRadius.ClosestSide
        val centerX = json["x"]?.jsonPrimitive?.floatOrNull ?: 50f
        val centerY = json["y"]?.jsonPrimitive?.floatOrNull ?: 50f
        return ClipShape.Circle(radius, centerX, centerY)
    }

    /**
     * Extract ellipse shape configuration.
     */
    private fun extractEllipse(json: JsonObject): ClipShape.Ellipse {
        val radiusX = json["rx"]?.let { extractClipRadius(it) } ?: ClipRadius.ClosestSide
        val radiusY = json["ry"]?.let { extractClipRadius(it) } ?: ClipRadius.ClosestSide
        val centerX = json["x"]?.jsonPrimitive?.floatOrNull ?: 50f
        val centerY = json["y"]?.jsonPrimitive?.floatOrNull ?: 50f
        return ClipShape.Ellipse(radiusX, radiusY, centerX, centerY)
    }

    /**
     * Extract inset shape configuration.
     */
    private fun extractInset(json: JsonObject): ClipShape.Inset {
        val top = ValueExtractors.extractDp(json["top"]) ?: 0.dp
        val right = ValueExtractors.extractDp(json["right"]) ?: 0.dp
        val bottom = ValueExtractors.extractDp(json["bottom"]) ?: 0.dp
        val left = ValueExtractors.extractDp(json["left"]) ?: 0.dp
        val radius = ValueExtractors.extractDp(json["round"]) ?: 0.dp
        return ClipShape.Inset(top, right, bottom, left, radius)
    }

    /**
     * Extract polygon shape configuration.
     */
    private fun extractPolygon(json: JsonObject): ClipShape.Polygon {
        val pointsArray = json["points"] as? JsonArray ?: return ClipShape.Polygon(emptyList())
        val points = pointsArray.mapNotNull { point ->
            if (point is JsonObject) {
                val x = point["x"]?.jsonPrimitive?.floatOrNull ?: return@mapNotNull null
                val y = point["y"]?.jsonPrimitive?.floatOrNull ?: return@mapNotNull null
                Pair(x, y)
            } else null
        }
        return ClipShape.Polygon(points)
    }

    /**
     * Extract SVG path shape configuration.
     */
    private fun extractPath(json: JsonObject): ClipShape.Path? {
        val d = json["d"]?.jsonPrimitive?.content ?: return null
        return ClipShape.Path(d)
    }

    /**
     * Extract a ClipRadius value from JSON.
     *
     * Handles:
     * - Fixed lengths: { "px": 50.0 }
     * - Percentages: { "percentage": 50 } or just a number
     * - Keywords: "closest-side", "farthest-side"
     */
    private fun extractClipRadius(json: JsonElement): ClipRadius {
        return when (json) {
            is JsonObject -> {
                // Check for pixel value
                json["px"]?.jsonPrimitive?.floatOrNull?.let {
                    return ClipRadius.Fixed(it.dp)
                }
                // Check for percentage
                json["percentage"]?.jsonPrimitive?.floatOrNull?.let {
                    return ClipRadius.Percentage(it)
                }
                ClipRadius.ClosestSide
            }
            is JsonPrimitive -> {
                // Check for keyword
                when (json.content.lowercase()) {
                    "closest-side" -> ClipRadius.ClosestSide
                    "farthest-side" -> ClipRadius.FarthestSide
                    else -> {
                        // Try as percentage number
                        json.floatOrNull?.let { ClipRadius.Percentage(it) }
                            ?: ClipRadius.ClosestSide
                    }
                }
            }
            else -> ClipRadius.ClosestSide
        }
    }

    /**
     * Check if a property type is a clip-path related property.
     *
     * @param type The property type string.
     * @return True if this is a clip path property.
     */
    fun isClipPathProperty(type: String): Boolean {
        return type in setOf("ClipPath", "Clip")
    }
}
