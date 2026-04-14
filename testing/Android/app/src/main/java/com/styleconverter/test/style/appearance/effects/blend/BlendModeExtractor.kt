package com.styleconverter.test.style.appearance.effects.blend

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

/**
 * Extracts mix-blend-mode configuration from IR properties.
 */
object BlendModeExtractor {

    /**
     * Extract blend mode configuration from properties.
     *
     * @param properties List of (type, data) pairs
     * @return BlendModeConfig
     */
    fun extractBlendModeConfig(properties: List<Pair<String, JsonElement?>>): BlendModeConfig {
        for ((type, data) in properties) {
            if (type == "MixBlendMode" && data != null) {
                val value = when (data) {
                    is JsonPrimitive -> data.content
                    else -> data.jsonPrimitive.content
                }
                val blendMode = BlendModeMapping.fromCssValue(value)
                return BlendModeConfig(blendMode = blendMode)
            }
        }
        return BlendModeConfig()
    }

    /**
     * Check if property type is a blend mode property.
     */
    fun isBlendModeProperty(type: String): Boolean {
        return type == "MixBlendMode" || type == "BackgroundBlendMode"
    }
}
