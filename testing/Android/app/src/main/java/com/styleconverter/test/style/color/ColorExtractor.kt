package com.styleconverter.test.style.color

import androidx.compose.ui.graphics.Color
import com.styleconverter.test.style.core.types.ValueExtractors
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Extracts color-related configuration from IR properties.
 *
 * ## Supported Properties
 * - BackgroundColor: Solid background color (IRColor format)
 * - Opacity: Alpha transparency (number or object with alpha field)
 * - BackgroundImage: Gradients and image URLs (array of gradient objects)
 *
 * ## IR Formats
 *
 * ### BackgroundColor
 * ```json
 * { "srgb": { "r": 1.0, "g": 0.0, "b": 0.0, "a": 1.0 }, "original": "red" }
 * ```
 *
 * ### Opacity
 * ```json
 * { "alpha": 0.5, "original": { "type": "number", "value": 0.5 } }
 * // or just: 0.5
 * ```
 *
 * ### BackgroundImage (Linear Gradient)
 * ```json
 * [{
 *   "type": "linear-gradient",
 *   "angle": { "deg": 90.0 },
 *   "stops": [
 *     { "color": { "srgb": { "r": 1, "g": 0, "b": 0 } }, "position": 0.0 },
 *     { "color": { "srgb": { "r": 0, "g": 0, "b": 1 } }, "position": 100.0 }
 *   ]
 * }]
 * ```
 */
object ColorExtractor {

    /**
     * Extract a complete ColorConfig from a list of property type/data pairs.
     *
     * @param properties List of (propertyType, data) pairs from IR
     * @return ColorConfig with all extracted values
     */
    fun extractColorConfig(properties: List<Pair<String, JsonElement?>>): ColorConfig {
        var backgroundColor: Color? = null
        var opacity: Float? = null
        var backgroundImages: List<BackgroundImageConfig> = emptyList()
        var backgroundPosition = BackgroundPositionConfig()
        var backgroundSize: BackgroundSizeConfig = BackgroundSizeConfig.Auto
        var backgroundRepeat = BackgroundRepeatConfig.REPEAT
        var backgroundAttachment = BackgroundAttachment.SCROLL

        properties.forEach { (type, data) ->
            when (type) {
                "BackgroundColor" -> backgroundColor = ValueExtractors.extractColor(data)
                "Opacity" -> opacity = extractOpacity(data)
                "BackgroundImage" -> backgroundImages = extractBackgroundImages(data)
                "BackgroundPosition", "BackgroundPositionX", "BackgroundPositionY" -> {
                    backgroundPosition = extractBackgroundPosition(data, backgroundPosition, type)
                }
                "BackgroundSize" -> backgroundSize = extractBackgroundSize(data)
                "BackgroundRepeat" -> backgroundRepeat = extractBackgroundRepeat(data)
                "BackgroundAttachment" -> backgroundAttachment = extractBackgroundAttachment(data)
            }
        }

        return ColorConfig(
            backgroundColor = backgroundColor,
            opacity = opacity,
            backgroundImages = backgroundImages,
            backgroundPosition = backgroundPosition,
            backgroundSize = backgroundSize,
            backgroundRepeat = backgroundRepeat,
            backgroundAttachment = backgroundAttachment
        )
    }

    /**
     * Extract opacity value from IR data.
     *
     * Handles formats:
     * - Direct number: `0.5`
     * - Object with alpha: `{ "alpha": 0.5 }`
     * - Object with value: `{ "value": 0.5 }`
     *
     * @param data JSON element containing opacity data
     * @return Float opacity value (0.0-1.0), or null if not extractable
     */
    fun extractOpacity(data: JsonElement?): Float? {
        if (data == null) return null
        return when (data) {
            is JsonPrimitive -> data.floatOrNull
            is JsonObject -> {
                data["alpha"]?.jsonPrimitive?.floatOrNull
                    ?: data["value"]?.jsonPrimitive?.floatOrNull
            }
            else -> null
        }
    }

    /**
     * Extract background images (gradients, URLs) from IR data.
     *
     * @param data JSON array of background image objects
     * @return List of BackgroundImageConfig objects
     */
    fun extractBackgroundImages(data: JsonElement?): List<BackgroundImageConfig> {
        val array = (data as? JsonArray) ?: return emptyList()

        return array.mapNotNull { element ->
            val obj = (element as? JsonObject) ?: return@mapNotNull null
            val type = obj["type"]?.jsonPrimitive?.contentOrNull

            when (type) {
                "linear-gradient" -> extractLinearGradient(obj, repeating = false)
                "repeating-linear-gradient" -> extractLinearGradient(obj, repeating = true)
                "radial-gradient" -> extractRadialGradient(obj, repeating = false)
                "repeating-radial-gradient" -> extractRadialGradient(obj, repeating = true)
                "conic-gradient" -> extractConicGradient(obj, repeating = false)
                "repeating-conic-gradient" -> extractConicGradient(obj, repeating = true)
                "url" -> BackgroundImageConfig.Url(obj["url"]?.jsonPrimitive?.contentOrNull ?: "")
                "none" -> BackgroundImageConfig.None
                else -> null
            }
        }
    }

    /**
     * Extract linear gradient configuration.
     *
     * IR format:
     * ```json
     * {
     *   "type": "linear-gradient",
     *   "angle": { "deg": 180.0 },
     *   "stops": [...]
     * }
     * ```
     */
    private fun extractLinearGradient(obj: JsonObject, repeating: Boolean): BackgroundImageConfig.LinearGradient {
        val angle = obj["angle"]?.jsonObject?.get("deg")?.jsonPrimitive?.floatOrNull ?: 180f
        val stops = extractColorStops(obj["stops"] as? JsonArray)
        return BackgroundImageConfig.LinearGradient(angle, stops, repeating)
    }

    /**
     * Extract radial gradient configuration.
     *
     * IR format:
     * ```json
     * {
     *   "type": "radial-gradient",
     *   "position": { "x": 50.0, "y": 50.0 },
     *   "stops": [...]
     * }
     * ```
     */
    private fun extractRadialGradient(obj: JsonObject, repeating: Boolean): BackgroundImageConfig.RadialGradient {
        val position = obj["position"]?.jsonObject
        val centerX = position?.get("x")?.jsonPrimitive?.floatOrNull?.div(100f) ?: 0.5f
        val centerY = position?.get("y")?.jsonPrimitive?.floatOrNull?.div(100f) ?: 0.5f
        val stops = extractColorStops(obj["stops"] as? JsonArray)
        return BackgroundImageConfig.RadialGradient(centerX, centerY, stops, repeating)
    }

    /**
     * Extract conic gradient configuration.
     *
     * IR format:
     * ```json
     * {
     *   "type": "conic-gradient",
     *   "position": { "x": 50.0, "y": 50.0 },
     *   "fromAngle": { "deg": 0.0 },
     *   "stops": [...]
     * }
     * ```
     */
    private fun extractConicGradient(obj: JsonObject, repeating: Boolean): BackgroundImageConfig.ConicGradient {
        val position = obj["position"]?.jsonObject
        val centerX = position?.get("x")?.jsonPrimitive?.floatOrNull?.div(100f) ?: 0.5f
        val centerY = position?.get("y")?.jsonPrimitive?.floatOrNull?.div(100f) ?: 0.5f
        val angle = obj["fromAngle"]?.jsonObject?.get("deg")?.jsonPrimitive?.floatOrNull ?: 0f
        val stops = extractColorStops(obj["stops"] as? JsonArray)
        return BackgroundImageConfig.ConicGradient(centerX, centerY, angle, stops, repeating)
    }

    /**
     * Extract color stops from a JSON array.
     *
     * IR format:
     * ```json
     * [
     *   { "color": { "srgb": { "r": 1, "g": 0, "b": 0 } }, "position": 0.0 },
     *   { "color": { "srgb": { "r": 0, "g": 0, "b": 1 } }, "position": 100.0 }
     * ]
     * ```
     *
     * Note: Position is in percentage (0-100) in IR, converted to fraction (0-1) here.
     */
    private fun extractColorStops(array: JsonArray?): List<ColorStop> {
        if (array == null) return emptyList()

        return array.mapIndexedNotNull { index, element ->
            val obj = (element as? JsonObject) ?: return@mapIndexedNotNull null
            val color = obj["color"]?.let { ValueExtractors.extractColor(it) } ?: return@mapIndexedNotNull null

            // Position is in percentage (0-100), convert to fraction (0-1)
            // If no position, distribute evenly
            val position = obj["position"]?.jsonPrimitive?.floatOrNull?.let { it / 100f }
                ?: (index.toFloat() / (array.size - 1).coerceAtLeast(1))

            ColorStop(color, position)
        }
    }

    /**
     * Extract background position from IR data.
     */
    private fun extractBackgroundPosition(
        data: JsonElement?,
        current: BackgroundPositionConfig,
        type: String
    ): BackgroundPositionConfig {
        if (data == null) return current

        when (data) {
            is JsonPrimitive -> {
                val keyword = data.contentOrNull?.lowercase()
                return when (keyword) {
                    "center" -> BackgroundPositionConfig.CENTER
                    "top" -> BackgroundPositionConfig.TOP_CENTER
                    "bottom" -> BackgroundPositionConfig.BOTTOM_CENTER
                    "left" -> BackgroundPositionConfig.CENTER_LEFT
                    "right" -> BackgroundPositionConfig.CENTER_RIGHT
                    else -> {
                        // Try to parse as percentage
                        val percent = keyword?.replace("%", "")?.toFloatOrNull()?.div(100f)
                        if (percent != null) {
                            when (type) {
                                "BackgroundPositionX" -> current.copy(x = percent)
                                "BackgroundPositionY" -> current.copy(y = percent)
                                else -> BackgroundPositionConfig(percent, percent)
                            }
                        } else current
                    }
                }
            }
            is JsonObject -> {
                val x = data["x"]?.jsonPrimitive?.floatOrNull?.div(100f) ?: current.x
                val y = data["y"]?.jsonPrimitive?.floatOrNull?.div(100f) ?: current.y
                return BackgroundPositionConfig(x, y)
            }
            else -> return current
        }
    }

    /**
     * Extract background size from IR data.
     */
    private fun extractBackgroundSize(data: JsonElement?): BackgroundSizeConfig {
        if (data == null) return BackgroundSizeConfig.Auto

        when (data) {
            is JsonPrimitive -> {
                return when (data.contentOrNull?.lowercase()) {
                    "cover" -> BackgroundSizeConfig.Cover
                    "contain" -> BackgroundSizeConfig.Contain
                    "auto" -> BackgroundSizeConfig.Auto
                    else -> BackgroundSizeConfig.Auto
                }
            }
            is JsonObject -> {
                val type = data["type"]?.jsonPrimitive?.contentOrNull?.lowercase()
                when (type) {
                    "cover" -> return BackgroundSizeConfig.Cover
                    "contain" -> return BackgroundSizeConfig.Contain
                    "auto" -> return BackgroundSizeConfig.Auto
                }

                val width = ValueExtractors.extractDp(data["width"])
                val height = ValueExtractors.extractDp(data["height"])
                val widthPct = data["widthPercent"]?.jsonPrimitive?.floatOrNull?.div(100f)
                val heightPct = data["heightPercent"]?.jsonPrimitive?.floatOrNull?.div(100f)

                return if (width != null || height != null || widthPct != null || heightPct != null) {
                    BackgroundSizeConfig.Dimensions(width, height, widthPct, heightPct)
                } else {
                    BackgroundSizeConfig.Auto
                }
            }
            else -> return BackgroundSizeConfig.Auto
        }
    }

    /**
     * Extract background repeat from IR data.
     */
    private fun extractBackgroundRepeat(data: JsonElement?): BackgroundRepeatConfig {
        val keyword = when (data) {
            is JsonPrimitive -> data.contentOrNull?.lowercase()?.replace("-", "_")
            is JsonObject -> data["type"]?.jsonPrimitive?.contentOrNull?.lowercase()?.replace("-", "_")
            else -> null
        } ?: return BackgroundRepeatConfig.REPEAT

        return when (keyword) {
            "repeat" -> BackgroundRepeatConfig.REPEAT
            "repeat_x" -> BackgroundRepeatConfig.REPEAT_X
            "repeat_y" -> BackgroundRepeatConfig.REPEAT_Y
            "no_repeat" -> BackgroundRepeatConfig.NO_REPEAT
            "space" -> BackgroundRepeatConfig.SPACE
            "round" -> BackgroundRepeatConfig.ROUND
            else -> BackgroundRepeatConfig.REPEAT
        }
    }

    /**
     * Extract background attachment from IR data.
     */
    private fun extractBackgroundAttachment(data: JsonElement?): BackgroundAttachment {
        val keyword = when (data) {
            is JsonPrimitive -> data.contentOrNull?.lowercase()
            is JsonObject -> data["type"]?.jsonPrimitive?.contentOrNull?.lowercase()
            else -> null
        } ?: return BackgroundAttachment.SCROLL

        return when (keyword) {
            "fixed" -> BackgroundAttachment.FIXED
            "local" -> BackgroundAttachment.LOCAL
            "scroll" -> BackgroundAttachment.SCROLL
            else -> BackgroundAttachment.SCROLL
        }
    }
}
