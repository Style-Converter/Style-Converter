package com.styleconverter.test.style.layout.spacing

import com.styleconverter.test.style.core.types.ValueExtractors
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Extracts margin trim configuration from IR properties.
 */
object MarginTrimExtractor {

    fun extractMarginTrimConfig(properties: List<Pair<String, JsonElement?>>): MarginTrimConfig {
        for ((type, data) in properties) {
            when (type) {
                "MarginTrim" -> return MarginTrimConfig(extractMarginTrim(data))
            }
        }
        return MarginTrimConfig()
    }

    private fun extractMarginTrim(data: JsonElement?): Set<MarginTrimValue> {
        if (data == null) return setOf(MarginTrimValue.NONE)

        val values = mutableSetOf<MarginTrimValue>()

        when (data) {
            is JsonPrimitive -> {
                val keyword = data.contentOrNull?.uppercase()?.replace("-", "_")
                parseMarginTrimKeyword(keyword)?.let { values.add(it) }
            }
            is JsonArray -> {
                data.forEach { element ->
                    val keyword = element.jsonPrimitive.contentOrNull?.uppercase()?.replace("-", "_")
                    parseMarginTrimKeyword(keyword)?.let { values.add(it) }
                }
            }
            else -> {}
        }

        return if (values.isEmpty()) setOf(MarginTrimValue.NONE) else values
    }

    private fun parseMarginTrimKeyword(keyword: String?): MarginTrimValue? {
        return when (keyword) {
            "NONE" -> MarginTrimValue.NONE
            "BLOCK" -> MarginTrimValue.BLOCK
            "INLINE" -> MarginTrimValue.INLINE
            "BLOCK_START" -> MarginTrimValue.BLOCK_START
            "BLOCK_END" -> MarginTrimValue.BLOCK_END
            "INLINE_START" -> MarginTrimValue.INLINE_START
            "INLINE_END" -> MarginTrimValue.INLINE_END
            else -> null
        }
    }

    fun isMarginTrimProperty(type: String): Boolean = type == "MarginTrim"
}
