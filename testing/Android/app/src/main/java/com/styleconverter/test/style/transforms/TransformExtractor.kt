package com.styleconverter.test.style.transforms

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.styleconverter.test.style.PropertyRegistry
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
 * Extracts transform-related configuration from IR properties.
 *
 * ## Supported Properties
 * - Transform: CSS transform with multiple functions
 * - TransformOrigin: Pivot point for transformations
 * - Rotate: Standalone rotation
 * - Scale: Standalone scale
 * - Translate: Standalone translation
 *
 * ## IR Formats
 *
 * ### Transform
 * ```json
 * {
 *   "type": "functions",
 *   "list": [
 *     { "fn": "translate", "x": { "px": 50 }, "y": { "px": 100 } },
 *     { "fn": "rotate", "angle": { "degrees": 45 } }
 *   ]
 * }
 * ```
 *
 * ### TransformOrigin
 * ```json
 * { "x": { "type": "percentage", "value": 50 }, "y": { "type": "percentage", "value": 50 } }
 * // or keyword: "center", "top left", etc.
 * ```
 *
 * ### Rotate
 * ```json
 * { "degrees": 45.0 }
 * // or: 45.0
 * ```
 *
 * ### Scale
 * ```json
 * { "x": 1.5, "y": 1.5 }
 * // or: 1.5
 * ```
 *
 * ### Translate
 * ```json
 * { "x": { "px": 50 }, "y": { "px": 100 } }
 * ```
 */
object TransformExtractor {

    init {
        // Phase 8 registration. Claim the 2D transform longhands + origin so the
        // legacy dispatch switch (and any future one) defers to this extractor.
        // TransformBox is listed here because it's a transform-family keyword
        // that conceptually belongs with Transform/TransformOrigin, even though
        // the runtime applier currently no-ops it (see TODO in TransformApplier).
        PropertyRegistry.migrated(
            "Transform",
            "TransformOrigin",
            "TransformBox",
            "Rotate",
            "Scale",
            "Translate",
            owner = "transforms"
        )
    }

    /**
     * Extract a complete TransformConfig from a list of property type/data pairs.
     *
     * @param properties List of (propertyType, data) pairs from IR
     * @return TransformConfig with all extracted values
     */
    fun extractTransformConfig(properties: List<Pair<String, JsonElement?>>): TransformConfig {
        var config = TransformConfig()

        properties.forEach { (type, data) ->
            config = when (type) {
                "Transform" -> config.copy(functions = extractTransformFunctions(data))
                "TransformOrigin" -> {
                    val origin = extractTransformOrigin(data)
                    config.copy(
                        originX = origin?.first ?: 0.5f,
                        originY = origin?.second ?: 0.5f
                    )
                }
                "Rotate" -> config.copy(rotate = ValueExtractors.extractDegrees(data))
                "RotateX" -> config.copy(rotateX = ValueExtractors.extractDegrees(data))
                "RotateY" -> config.copy(rotateY = ValueExtractors.extractDegrees(data))
                "Scale" -> {
                    val scaleData = extractScaleData(data)
                    config.copy(
                        scale = scaleData.uniform,
                        scaleX = scaleData.x,
                        scaleY = scaleData.y
                    )
                }
                "ScaleX" -> {
                    val x = extractFloat(data)
                    config.copy(scaleX = x)
                }
                "ScaleY" -> {
                    val y = extractFloat(data)
                    config.copy(scaleY = y)
                }
                "ScaleZ" -> {
                    val z = extractFloat(data)
                    config.copy(scaleZ = z)
                }
                "Translate" -> {
                    val translateData = extractTranslateData(data)
                    config.copy(
                        translateX = translateData.x,
                        translateY = translateData.y,
                        translateZ = translateData.z
                    )
                }
                "TranslateX" -> {
                    val x = data?.let { ValueExtractors.extractDp(it) }
                    config.copy(translateX = x)
                }
                "TranslateY" -> {
                    val y = data?.let { ValueExtractors.extractDp(it) }
                    config.copy(translateY = y)
                }
                "TranslateZ" -> {
                    val z = data?.let { ValueExtractors.extractDp(it) }
                    config.copy(translateZ = z)
                }
                "Perspective" -> {
                    val perspective = data?.let { ValueExtractors.extractDp(it) }
                    config.copy(perspective = perspective)
                }
                "SkewX" -> {
                    val skewX = ValueExtractors.extractDegrees(data)
                    config.copy(skewX = skewX)
                }
                "SkewY" -> {
                    val skewY = ValueExtractors.extractDegrees(data)
                    config.copy(skewY = skewY)
                }
                else -> config
            }
        }

        return config
    }

    /**
     * Extract a float value from JSON.
     */
    private fun extractFloat(data: JsonElement?): Float? {
        return when (data) {
            is JsonPrimitive -> data.floatOrNull
            is JsonObject -> data["value"]?.jsonPrimitive?.floatOrNull
            else -> null
        }
    }

    /**
     * Extract transform origin as normalized values (0-1).
     *
     * Handles formats:
     * - Object with x/y percentages: { "x": 50, "y": 50 }
     * - Keywords: "center", "top left", etc.
     *
     * @param data JSON element containing transform-origin data
     * @return Pair of (x, y) as fractions (0-1), or null if not extractable
     */
    fun extractTransformOrigin(data: JsonElement?): Pair<Float, Float>? {
        if (data == null) return null

        return when (data) {
            is JsonObject -> {
                // Extract x and y, handling various formats
                val x = extractOriginComponent(data["x"]) ?: 0.5f
                val y = extractOriginComponent(data["y"]) ?: 0.5f
                Pair(x, y)
            }
            is JsonPrimitive -> {
                // Handle keyword values
                when (data.contentOrNull?.lowercase()) {
                    "center" -> Pair(0.5f, 0.5f)
                    "top" -> Pair(0.5f, 0f)
                    "bottom" -> Pair(0.5f, 1f)
                    "left" -> Pair(0f, 0.5f)
                    "right" -> Pair(1f, 0.5f)
                    "top left" -> Pair(0f, 0f)
                    "top right" -> Pair(1f, 0f)
                    "bottom left" -> Pair(0f, 1f)
                    "bottom right" -> Pair(1f, 1f)
                    else -> null
                }
            }
            else -> null
        }
    }

    /**
     * Extract a single origin component (x or y) from various formats.
     */
    private fun extractOriginComponent(data: JsonElement?): Float? {
        if (data == null) return null

        return when (data) {
            is JsonPrimitive -> {
                // Direct percentage value
                data.floatOrNull?.let { it / 100f }
                    ?: when (data.contentOrNull?.uppercase()) {
                        "LEFT", "TOP" -> 0f
                        "CENTER" -> 0.5f
                        "RIGHT", "BOTTOM" -> 1f
                        else -> null
                    }
            }
            is JsonObject -> {
                // Object format: { "type": "percentage", "value": 50 }
                // or { "type": "keyword", "value": "CENTER" }
                val type = data["type"]?.jsonPrimitive?.contentOrNull
                val value = data["value"]

                when (type?.lowercase()) {
                    "percentage" -> value?.jsonPrimitive?.floatOrNull?.let { it / 100f }
                    "keyword" -> when (value?.jsonPrimitive?.contentOrNull?.uppercase()) {
                        "LEFT", "TOP" -> 0f
                        "CENTER" -> 0.5f
                        "RIGHT", "BOTTOM" -> 1f
                        else -> null
                    }
                    else -> null
                }
            }
            else -> null
        }
    }

    /**
     * Extract transform functions from IR data.
     *
     * IR format:
     * ```json
     * {
     *   "type": "functions",
     *   "list": [
     *     { "fn": "translate", "x": { "px": 50 }, "y": { "px": 100 } },
     *     { "fn": "rotate", "angle": { "degrees": 45 } }
     *   ]
     * }
     * ```
     *
     * @param data JSON element containing transform data
     * @return List of TransformFunction objects
     */
    fun extractTransformFunctions(data: JsonElement?): List<TransformFunction> {
        val obj = (data as? JsonObject) ?: return emptyList()

        // Check for expression type (calc, var) - cannot be parsed
        val type = obj["type"]?.jsonPrimitive?.contentOrNull
        if (type == "expression") {
            return emptyList()
        }

        // Check for "none" keyword
        if (type == "none") {
            return listOf(TransformFunction.None)
        }

        // Extract list of functions
        val listData = obj["list"] as? JsonArray ?: return emptyList()

        return listData.mapNotNull { element ->
            val fnObj = (element as? JsonObject) ?: return@mapNotNull null
            val fn = fnObj["fn"]?.jsonPrimitive?.contentOrNull

            when (fn?.lowercase()) {
                "translate" -> extractTranslateFunction(fnObj)
                "translatex" -> extractTranslateXFunction(fnObj)
                "translatey" -> extractTranslateYFunction(fnObj)
                "translatez" -> extractTranslateZFunction(fnObj)
                "translate3d" -> extractTranslate3dFunction(fnObj)
                "rotate" -> extractRotateFunction(fnObj)
                "rotatex" -> extractRotateXFunction(fnObj)
                "rotatey" -> extractRotateYFunction(fnObj)
                "rotatez" -> extractRotateZFunction(fnObj)
                "rotate3d" -> extractRotate3dFunction(fnObj)
                "scale" -> extractScaleFunction(fnObj)
                "scalex" -> extractScaleXFunction(fnObj)
                "scaley" -> extractScaleYFunction(fnObj)
                "scalez" -> extractScaleZFunction(fnObj)
                "scale3d" -> extractScale3dFunction(fnObj)
                "skew" -> extractSkewFunction(fnObj)
                "skewx" -> extractSkewXFunction(fnObj)
                "skewy" -> extractSkewYFunction(fnObj)
                "perspective" -> extractPerspectiveFunction(fnObj)
                "matrix" -> extractMatrixFunction(fnObj)
                "matrix3d" -> extractMatrix3dFunction(fnObj)
                "none" -> TransformFunction.None
                else -> null
            }
        }
    }

    // ==================== TRANSLATE FUNCTIONS ====================

    private fun extractTranslateFunction(obj: JsonObject): TransformFunction.Translate {
        val x = obj["x"]?.let { ValueExtractors.extractDp(it) } ?: 0.dp
        val y = obj["y"]?.let { ValueExtractors.extractDp(it) } ?: 0.dp
        return TransformFunction.Translate(x, y, 0.dp)
    }

    private fun extractTranslateXFunction(obj: JsonObject): TransformFunction.TranslateX {
        val x = obj["x"]?.let { ValueExtractors.extractDp(it) } ?: 0.dp
        return TransformFunction.TranslateX(x)
    }

    private fun extractTranslateYFunction(obj: JsonObject): TransformFunction.TranslateY {
        val y = obj["y"]?.let { ValueExtractors.extractDp(it) } ?: 0.dp
        return TransformFunction.TranslateY(y)
    }

    private fun extractTranslateZFunction(obj: JsonObject): TransformFunction.TranslateZ {
        val z = obj["z"]?.let { ValueExtractors.extractDp(it) } ?: 0.dp
        return TransformFunction.TranslateZ(z)
    }

    private fun extractTranslate3dFunction(obj: JsonObject): TransformFunction.Translate {
        val x = obj["x"]?.let { ValueExtractors.extractDp(it) } ?: 0.dp
        val y = obj["y"]?.let { ValueExtractors.extractDp(it) } ?: 0.dp
        val z = obj["z"]?.let { ValueExtractors.extractDp(it) } ?: 0.dp
        return TransformFunction.Translate(x, y, z)
    }

    // ==================== ROTATE FUNCTIONS ====================

    private fun extractRotateFunction(obj: JsonObject): TransformFunction.Rotate {
        val angle = obj["angle"]?.let { ValueExtractors.extractDegrees(it) } ?: 0f
        return TransformFunction.Rotate(angle)
    }

    private fun extractRotateXFunction(obj: JsonObject): TransformFunction.RotateX {
        val angle = obj["angle"]?.let { ValueExtractors.extractDegrees(it) } ?: 0f
        return TransformFunction.RotateX(angle)
    }

    private fun extractRotateYFunction(obj: JsonObject): TransformFunction.RotateY {
        val angle = obj["angle"]?.let { ValueExtractors.extractDegrees(it) } ?: 0f
        return TransformFunction.RotateY(angle)
    }

    private fun extractRotateZFunction(obj: JsonObject): TransformFunction.RotateZ {
        val angle = obj["angle"]?.let { ValueExtractors.extractDegrees(it) } ?: 0f
        return TransformFunction.RotateZ(angle)
    }

    private fun extractRotate3dFunction(obj: JsonObject): TransformFunction? {
        // rotate3d(x, y, z, angle) - rotation around an arbitrary axis
        // For simplicity, map to individual axis rotations based on which component is non-zero
        val angle = obj["angle"]?.let { ValueExtractors.extractDegrees(it) } ?: return null
        val x = obj["x"]?.jsonPrimitive?.floatOrNull ?: 0f
        val y = obj["y"]?.jsonPrimitive?.floatOrNull ?: 0f
        val z = obj["z"]?.jsonPrimitive?.floatOrNull ?: 0f

        // Simple heuristic: use the axis with the largest component
        return when {
            z >= x && z >= y -> TransformFunction.RotateZ(angle)
            y >= x -> TransformFunction.RotateY(angle)
            else -> TransformFunction.RotateX(angle)
        }
    }

    // ==================== SCALE FUNCTIONS ====================

    private fun extractScaleFunction(obj: JsonObject): TransformFunction.Scale {
        val x = obj["x"]?.jsonPrimitive?.floatOrNull ?: 1f
        val y = obj["y"]?.jsonPrimitive?.floatOrNull ?: x // Default y to x for uniform scale
        val z = obj["z"]?.jsonPrimitive?.floatOrNull ?: 1f
        return TransformFunction.Scale(x, y, z)
    }

    private fun extractScaleXFunction(obj: JsonObject): TransformFunction.ScaleX {
        val x = obj["x"]?.jsonPrimitive?.floatOrNull ?: 1f
        return TransformFunction.ScaleX(x)
    }

    private fun extractScaleYFunction(obj: JsonObject): TransformFunction.ScaleY {
        val y = obj["y"]?.jsonPrimitive?.floatOrNull ?: 1f
        return TransformFunction.ScaleY(y)
    }

    private fun extractScaleZFunction(obj: JsonObject): TransformFunction.ScaleZ {
        val z = obj["z"]?.jsonPrimitive?.floatOrNull ?: 1f
        return TransformFunction.ScaleZ(z)
    }

    private fun extractScale3dFunction(obj: JsonObject): TransformFunction.Scale {
        val x = obj["x"]?.jsonPrimitive?.floatOrNull ?: 1f
        val y = obj["y"]?.jsonPrimitive?.floatOrNull ?: 1f
        val z = obj["z"]?.jsonPrimitive?.floatOrNull ?: 1f
        return TransformFunction.Scale(x, y, z)
    }

    // ==================== SKEW FUNCTIONS ====================

    private fun extractSkewFunction(obj: JsonObject): TransformFunction.Skew {
        val x = obj["x"]?.let { ValueExtractors.extractDegrees(it) } ?: 0f
        val y = obj["y"]?.let { ValueExtractors.extractDegrees(it) } ?: 0f
        return TransformFunction.Skew(x, y)
    }

    private fun extractSkewXFunction(obj: JsonObject): TransformFunction.SkewX {
        val x = obj["x"]?.let { ValueExtractors.extractDegrees(it) }
            ?: obj["angle"]?.let { ValueExtractors.extractDegrees(it) }
            ?: 0f
        return TransformFunction.SkewX(x)
    }

    private fun extractSkewYFunction(obj: JsonObject): TransformFunction.SkewY {
        val y = obj["y"]?.let { ValueExtractors.extractDegrees(it) }
            ?: obj["angle"]?.let { ValueExtractors.extractDegrees(it) }
            ?: 0f
        return TransformFunction.SkewY(y)
    }

    // ==================== PERSPECTIVE FUNCTION ====================

    private fun extractPerspectiveFunction(obj: JsonObject): TransformFunction.Perspective {
        val d = obj["d"]?.let { ValueExtractors.extractDp(it) }
            ?: obj["distance"]?.let { ValueExtractors.extractDp(it) }
            ?: 1000.dp
        return TransformFunction.Perspective(d)
    }

    // ==================== MATRIX FUNCTIONS ====================

    private fun extractMatrixFunction(obj: JsonObject): TransformFunction.Matrix? {
        val values = obj["values"] as? JsonArray ?: return null
        if (values.size < 6) return null

        val floatValues = values.mapNotNull { it.jsonPrimitive.floatOrNull }
        if (floatValues.size < 6) return null

        return TransformFunction.Matrix(floatValues.take(6))
    }

    private fun extractMatrix3dFunction(obj: JsonObject): TransformFunction.Matrix3d? {
        val values = obj["values"] as? JsonArray ?: return null
        if (values.size < 16) return null

        val floatValues = values.mapNotNull { it.jsonPrimitive.floatOrNull }
        if (floatValues.size < 16) return null

        return TransformFunction.Matrix3d(floatValues.take(16))
    }

    // ==================== STANDALONE PROPERTY EXTRACTORS ====================

    /**
     * Helper class for scale extraction results.
     */
    private data class ScaleData(
        val uniform: Float? = null,
        val x: Float? = null,
        val y: Float? = null
    )

    /**
     * Extract scale data from standalone Scale property.
     */
    private fun extractScaleData(data: JsonElement?): ScaleData {
        if (data == null) return ScaleData()

        return when (data) {
            is JsonPrimitive -> {
                // Single number = uniform scale
                data.floatOrNull?.let { ScaleData(uniform = it) } ?: ScaleData()
            }
            is JsonObject -> {
                val x = data["x"]?.jsonPrimitive?.floatOrNull
                val y = data["y"]?.jsonPrimitive?.floatOrNull

                if (x != null && y != null && x == y) {
                    ScaleData(uniform = x)
                } else {
                    ScaleData(x = x, y = y)
                }
            }
            else -> ScaleData()
        }
    }

    /**
     * Helper class for translate extraction results.
     */
    private data class TranslateData(
        val x: Dp? = null,
        val y: Dp? = null,
        val z: Dp? = null
    )

    /**
     * Extract translate data from standalone Translate property.
     */
    private fun extractTranslateData(data: JsonElement?): TranslateData {
        if (data == null) return TranslateData()

        return when (data) {
            is JsonObject -> {
                val x = data["x"]?.let { ValueExtractors.extractDp(it) }
                val y = data["y"]?.let { ValueExtractors.extractDp(it) }
                val z = data["z"]?.let { ValueExtractors.extractDp(it) }
                TranslateData(x, y, z)
            }
            else -> TranslateData()
        }
    }
}
