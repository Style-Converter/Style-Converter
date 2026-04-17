package com.styleconverter.test.style.background

import com.styleconverter.test.style.core.types.ValueExtractors
import kotlinx.serialization.json.JsonElement

/**
 * Extracts background box configuration from IR properties.
 */
object BackgroundBoxExtractor {

    fun extractBackgroundBoxConfig(properties: List<Pair<String, JsonElement?>>): BackgroundBoxConfig {
        var backgroundClip = BackgroundBoxValue.BORDER_BOX
        var backgroundOrigin = BackgroundBoxValue.PADDING_BOX

        for ((type, data) in properties) {
            when (type) {
                "BackgroundClip" -> backgroundClip = extractBackgroundBox(data, BackgroundBoxValue.BORDER_BOX)
                "BackgroundOrigin" -> backgroundOrigin = extractBackgroundBox(data, BackgroundBoxValue.PADDING_BOX)
            }
        }

        return BackgroundBoxConfig(
            backgroundClip = backgroundClip,
            backgroundOrigin = backgroundOrigin
        )
    }

    private fun extractBackgroundBox(data: JsonElement?, default: BackgroundBoxValue): BackgroundBoxValue {
        val keyword = ValueExtractors.extractKeyword(data)?.uppercase()?.replace("-", "_")
            ?: return default

        return when (keyword) {
            "BORDER_BOX" -> BackgroundBoxValue.BORDER_BOX
            "PADDING_BOX" -> BackgroundBoxValue.PADDING_BOX
            "CONTENT_BOX" -> BackgroundBoxValue.CONTENT_BOX
            "TEXT" -> BackgroundBoxValue.TEXT
            else -> default
        }
    }

    fun isBackgroundBoxProperty(type: String): Boolean {
        return type in setOf("BackgroundClip", "BackgroundOrigin")
    }
}
