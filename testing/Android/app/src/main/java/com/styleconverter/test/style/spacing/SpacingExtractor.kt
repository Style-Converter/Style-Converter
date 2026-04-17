package com.styleconverter.test.style.spacing

import androidx.compose.ui.unit.Dp
import com.styleconverter.test.style.core.types.ValueExtractors
import kotlinx.serialization.json.JsonElement

object SpacingExtractor {

    fun extractPaddingConfig(properties: List<Pair<String, JsonElement?>>): PaddingConfig {
        var config = PaddingConfig()

        properties.forEach { (type, data) ->
            val dp = ValueExtractors.extractDp(data)
            config = when (type) {
                "PaddingTop" -> config.copy(top = dp)
                "PaddingRight" -> config.copy(end = dp)
                "PaddingBottom" -> config.copy(bottom = dp)
                "PaddingLeft" -> config.copy(start = dp)
                "PaddingBlockStart" -> config.copy(blockStart = dp)
                "PaddingBlockEnd" -> config.copy(blockEnd = dp)
                "PaddingInlineStart" -> config.copy(inlineStart = dp)
                "PaddingInlineEnd" -> config.copy(inlineEnd = dp)
                else -> config
            }
        }

        return config
    }

    fun extractMarginConfig(properties: List<Pair<String, JsonElement?>>): MarginConfig {
        var config = MarginConfig()

        properties.forEach { (type, data) ->
            val dp = ValueExtractors.extractDp(data)
            config = when (type) {
                "MarginTop" -> config.copy(top = dp)
                "MarginRight" -> config.copy(end = dp)
                "MarginBottom" -> config.copy(bottom = dp)
                "MarginLeft" -> config.copy(start = dp)
                "MarginBlockStart" -> config.copy(blockStart = dp)
                "MarginBlockEnd" -> config.copy(blockEnd = dp)
                "MarginInlineStart" -> config.copy(inlineStart = dp)
                "MarginInlineEnd" -> config.copy(inlineEnd = dp)
                else -> config
            }
        }

        return config
    }

    fun extractGapConfig(properties: List<Pair<String, JsonElement?>>): GapConfig {
        var rowGap: Dp? = null
        var columnGap: Dp? = null

        properties.forEach { (type, data) ->
            when (type) {
                "Gap" -> {
                    val dp = ValueExtractors.extractDp(data)
                    rowGap = dp
                    columnGap = dp
                }
                "RowGap" -> rowGap = ValueExtractors.extractDp(data)
                "ColumnGap" -> columnGap = ValueExtractors.extractDp(data)
            }
        }

        return GapConfig(rowGap, columnGap)
    }

    /**
     * Check if a property type is a spacing-related property.
     */
    fun isSpacingProperty(propertyType: String): Boolean {
        return propertyType in SPACING_PROPERTIES
    }

    private val SPACING_PROPERTIES = setOf(
        "PaddingTop", "PaddingRight", "PaddingBottom", "PaddingLeft",
        "PaddingBlockStart", "PaddingBlockEnd", "PaddingInlineStart", "PaddingInlineEnd",
        "MarginTop", "MarginRight", "MarginBottom", "MarginLeft",
        "MarginBlockStart", "MarginBlockEnd", "MarginInlineStart", "MarginInlineEnd",
        "Gap", "RowGap", "ColumnGap"
    )
}
