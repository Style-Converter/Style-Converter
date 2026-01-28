package com.styleconverter.test.style.layout.sizing

import com.styleconverter.test.style.core.types.ValueExtractors
import kotlinx.serialization.json.JsonElement

/**
 * Extracts sizing configuration from IR properties.
 *
 * Handles the following CSS properties:
 * - width, height
 * - min-width, max-width, min-height, max-height
 * - block-size, inline-size (logical properties)
 * - min-block-size, max-block-size, min-inline-size, max-inline-size
 */
object SizingExtractor {

    /**
     * Extract a complete SizingConfig from a list of property type/data pairs.
     *
     * @param properties List of (propertyType, data) pairs from IR properties
     * @return SizingConfig with all extracted sizing values
     */
    fun extractSizingConfig(properties: List<Pair<String, JsonElement?>>): SizingConfig {
        var config = SizingConfig()

        properties.forEach { (type, data) ->
            config = when (type) {
                // Physical sizing properties
                "Width" -> config.copy(width = extractSizeValue(data))
                "Height" -> config.copy(height = extractSizeValue(data))
                "MinWidth" -> config.copy(minWidth = ValueExtractors.extractDp(data))
                "MaxWidth" -> config.copy(maxWidth = ValueExtractors.extractDp(data))
                "MinHeight" -> config.copy(minHeight = ValueExtractors.extractDp(data))
                "MaxHeight" -> config.copy(maxHeight = ValueExtractors.extractDp(data))

                // Logical sizing properties (writing mode aware)
                "BlockSize" -> config.copy(blockSize = extractSizeValue(data))
                "InlineSize" -> config.copy(inlineSize = extractSizeValue(data))
                "MinBlockSize" -> config.copy(minBlockSize = ValueExtractors.extractDp(data))
                "MaxBlockSize" -> config.copy(maxBlockSize = ValueExtractors.extractDp(data))
                "MinInlineSize" -> config.copy(minInlineSize = ValueExtractors.extractDp(data))
                "MaxInlineSize" -> config.copy(maxInlineSize = ValueExtractors.extractDp(data))

                else -> config
            }
        }

        return config
    }

    /**
     * Extract a single SizeValue from JSON data.
     *
     * Handles:
     * - Fixed lengths: {"px": 100}
     * - Percentages: {"original": {"v": 50, "u": "PERCENT"}}
     * - Keywords: auto, max-content, min-content, fit-content, fill-available
     *
     * @param data The JSON element containing the size value
     * @return SizeValue or null if not extractable
     */
    fun extractSizeValue(data: JsonElement?): SizeValue? {
        if (data == null) return null

        // Check for keywords first
        val keyword = ValueExtractors.extractKeyword(data)
        when (keyword?.lowercase()) {
            "auto" -> return SizeValue.Auto
            "max-content", "fill-available", "-webkit-fill-available" -> return SizeValue.FillMax
            "min-content", "fit-content" -> return SizeValue.WrapContent
            "100%" -> return SizeValue.FillMax
        }

        // Check for length or percentage
        return when (val lop = ValueExtractors.extractLengthOrPercentage(data)) {
            is ValueExtractors.LengthOrPercentage.Length -> SizeValue.Fixed(lop.dp)
            is ValueExtractors.LengthOrPercentage.Percentage -> {
                // Handle 100% as FillMax for better Compose mapping
                if (lop.fraction >= 0.999f) {
                    SizeValue.FillMax
                } else {
                    SizeValue.Percentage(lop.fraction)
                }
            }
            is ValueExtractors.LengthOrPercentage.Auto -> SizeValue.Auto
            null -> null
        }
    }

    /**
     * Check if a property type is a sizing property.
     */
    fun isSizingProperty(propertyType: String): Boolean {
        return propertyType in SIZING_PROPERTIES
    }

    private val SIZING_PROPERTIES = setOf(
        "Width", "Height",
        "MinWidth", "MaxWidth", "MinHeight", "MaxHeight",
        "BlockSize", "InlineSize",
        "MinBlockSize", "MaxBlockSize", "MinInlineSize", "MaxInlineSize"
    )
}
