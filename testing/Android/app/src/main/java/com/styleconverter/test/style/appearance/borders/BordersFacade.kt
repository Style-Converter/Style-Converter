package com.styleconverter.test.style.appearance.borders

import androidx.compose.ui.Modifier
import com.styleconverter.test.style.appearance.borders.outline.OutlineApplier
import com.styleconverter.test.style.appearance.borders.outline.OutlineConfig
import com.styleconverter.test.style.appearance.borders.outline.OutlineExtractor
import com.styleconverter.test.style.appearance.borders.radius.BorderRadiusApplier
import com.styleconverter.test.style.appearance.borders.radius.BorderRadiusConfig
import com.styleconverter.test.style.appearance.borders.radius.BorderRadiusExtractor
import com.styleconverter.test.style.appearance.borders.sides.AllBordersConfig
import com.styleconverter.test.style.appearance.borders.sides.BorderSideApplier
import com.styleconverter.test.style.appearance.borders.sides.BorderSideExtractor
import kotlinx.serialization.json.JsonElement

/**
 * Facade for all border-related property handling.
 *
 * This provides a unified interface for extracting and applying:
 * - Border sides (width, color, style for each side)
 * - Border radius (all four corners)
 * - Outline (outline-width, outline-style, outline-color, outline-offset)
 *
 * ## Usage
 * ```kotlin
 * val properties: List<Pair<String, JsonElement?>> = ...
 * val config = BordersFacade.extractConfig(properties)
 * val modifier = BordersFacade.apply(Modifier, config)
 * ```
 *
 * ## Order of Operations
 * 1. Border radius (clip) is applied first to establish shape
 * 2. Border sides are drawn after to respect the clipped shape
 */
object BordersFacade {

    /**
     * Combined configuration for all border properties.
     */
    data class BordersConfig(
        val sides: AllBordersConfig,
        val radius: BorderRadiusConfig,
        val outline: OutlineConfig = OutlineConfig()
    ) {
        /**
         * Check if there are any border properties to apply.
         */
        val hasBorders: Boolean get() = sides.hasBorders || radius.hasRadius || outline.hasOutline
    }

    /**
     * Extract all border configurations from a list of property type/data pairs.
     *
     * @param properties List of pairs where first is the property type (e.g., "BorderTopWidth")
     *                   and second is the JSON data for that property.
     * @return BordersConfig containing configurations for sides and radius.
     */
    fun extractConfig(properties: List<Pair<String, JsonElement?>>): BordersConfig {
        return BordersConfig(
            sides = BorderSideExtractor.extractBorderConfig(properties),
            radius = BorderRadiusExtractor.extractRadiusConfig(properties),
            outline = OutlineExtractor.extractOutlineConfig(properties)
        )
    }

    /**
     * Apply all border styling to a modifier.
     *
     * @param modifier The modifier to apply borders to.
     * @param config The combined border configuration.
     * @return Modified modifier with borders and radius applied.
     */
    fun apply(modifier: Modifier, config: BordersConfig): Modifier {
        var result = modifier

        // Apply radius first (clip)
        result = BorderRadiusApplier.applyRadius(result, config.radius)

        // Then apply borders
        result = BorderSideApplier.applyBorders(result, config.sides)

        // Apply outline (drawn outside the element)
        result = OutlineApplier.applyOutline(result, config.outline)

        return result
    }

    /**
     * Check if a property type is a border-related property.
     *
     * @param type The property type string.
     * @return True if this is a border side or radius property.
     */
    fun isBorderProperty(type: String): Boolean {
        // Handle shorthand properties
        if (type == "BorderWidth" || type == "BorderStyle" || type == "BorderColor") return true

        // Handle outline properties
        if (OutlineExtractor.isOutlineProperty(type)) return true

        // Handle individual side properties and radius
        return type.startsWith("Border") && (
            type.contains("Width") ||
            type.contains("Color") ||
            type.contains("Style") ||
            type.contains("Radius")
        ) && !type.contains("Image") && !type.contains("Spacing")
    }
}
