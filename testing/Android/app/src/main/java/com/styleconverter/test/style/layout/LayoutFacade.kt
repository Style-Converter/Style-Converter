package com.styleconverter.test.style.layout

import androidx.compose.ui.Modifier
import com.styleconverter.test.style.layout.flex.FlexContainerConfig
import com.styleconverter.test.style.layout.flex.FlexExtractor
import com.styleconverter.test.style.layout.flex.FlexItemConfig
import com.styleconverter.test.style.layout.grid.GridConfig
import com.styleconverter.test.style.layout.grid.GridExtractor
import com.styleconverter.test.style.layout.position.PositionApplier
import com.styleconverter.test.style.layout.position.PositionConfig
import com.styleconverter.test.style.layout.position.PositionExtractor
import com.styleconverter.test.style.layout.sizing.SizingApplier
import com.styleconverter.test.style.layout.sizing.SizingConfig
import com.styleconverter.test.style.layout.sizing.SizingExtractor
import com.styleconverter.test.style.layout.spacing.GapConfig
import com.styleconverter.test.style.layout.spacing.MarginConfig
import com.styleconverter.test.style.layout.spacing.PaddingConfig
import com.styleconverter.test.style.layout.spacing.SpacingApplier
import com.styleconverter.test.style.layout.spacing.SpacingExtractor
import kotlinx.serialization.json.JsonElement

/**
 * Facade for all layout-related property handling.
 *
 * This provides a unified interface for extracting and applying:
 * - Sizing (width, height, min/max constraints)
 * - Spacing (padding, margin, gap)
 * - Position (position type, top/right/bottom/left, z-index)
 * - Flex (container and item properties)
 * - Grid (grid container and item properties)
 *
 * ## Usage
 * ```kotlin
 * val properties: List<Pair<String, JsonElement?>> = ...
 * val config = LayoutFacade.extractConfig(properties)
 * val modifier = LayoutFacade.applyToModifier(Modifier, config)
 * ```
 *
 * ## Order of Operations
 * 1. Sizing is applied first to establish dimensions
 * 2. Padding is applied next (inside the borders)
 * 3. Margin is applied as offset
 * 4. Position (offset and z-index) is applied last
 *
 * Note: Flex container/item configurations are extracted but not applied
 * directly to modifiers - they are used by the container rendering logic.
 */
object LayoutFacade {

    /**
     * Combined configuration for all layout properties.
     */
    data class LayoutConfig(
        val sizing: SizingConfig = SizingConfig(),
        val padding: PaddingConfig = PaddingConfig(),
        val margin: MarginConfig = MarginConfig(),
        val gap: GapConfig = GapConfig(),
        val position: PositionConfig = PositionConfig(),
        val flexContainer: FlexContainerConfig = FlexContainerConfig(),
        val flexItem: FlexItemConfig = FlexItemConfig(),
        val grid: GridConfig = GridConfig()
    ) {
        /**
         * Check if there are any layout properties to apply.
         */
        val hasLayout: Boolean
            get() = sizing.hasSizing ||
                padding.hasPadding ||
                margin.hasMargin ||
                gap.hasGap ||
                position.hasPosition ||
                grid.hasGrid

        /**
         * Check if this is a flex container.
         */
        val isFlexContainer: Boolean
            get() = flexContainer.isFlex

        /**
         * Check if this is a grid container.
         */
        val isGridContainer: Boolean
            get() = grid.hasGrid

        /**
         * Check if this element is absolutely positioned.
         */
        val isAbsolutelyPositioned: Boolean
            get() = position.isAbsolutelyPositioned
    }

    /**
     * Extract all layout configurations from a list of property type/data pairs.
     *
     * @param properties List of pairs where first is the property type (e.g., "Width")
     *                   and second is the JSON data for that property.
     * @return LayoutConfig containing all extracted layout configurations.
     */
    fun extractConfig(properties: List<Pair<String, JsonElement?>>): LayoutConfig {
        return LayoutConfig(
            sizing = SizingExtractor.extractSizingConfig(properties),
            padding = SpacingExtractor.extractPaddingConfig(properties),
            margin = SpacingExtractor.extractMarginConfig(properties),
            gap = SpacingExtractor.extractGapConfig(properties),
            position = PositionExtractor.extractPositionConfig(properties),
            flexContainer = FlexExtractor.extractContainerConfig(properties),
            flexItem = FlexExtractor.extractItemConfig(properties),
            grid = GridExtractor.extractGridConfig(properties)
        )
    }

    /**
     * Apply layout configurations to a modifier.
     *
     * Note: This applies sizing, spacing, and position modifiers.
     * Flex container/item properties need to be handled by the container
     * rendering logic (Row, Column, Box, etc.).
     *
     * @param modifier The modifier to apply layout to.
     * @param config The combined layout configuration.
     * @return Modified modifier with layout applied.
     */
    fun applyToModifier(modifier: Modifier, config: LayoutConfig): Modifier {
        var result = modifier

        // Apply sizing (width, height, constraints)
        result = SizingApplier.applySizing(result, config.sizing)

        // Apply padding (internal spacing)
        result = SpacingApplier.applyPadding(result, config.padding)

        // Apply margin as offset (Compose doesn't have margin)
        result = SpacingApplier.applyMargin(result, config.margin)

        // Apply position (offset and z-index)
        result = PositionApplier.applyPosition(result, config.position)

        return result
    }

    /**
     * Apply only position-related modifiers (z-index and offset).
     * Useful when sizing/spacing is handled separately.
     *
     * @param modifier The base modifier
     * @param config The layout configuration
     * @return Modifier with only position applied
     */
    fun applyPositionOnly(modifier: Modifier, config: LayoutConfig): Modifier {
        return PositionApplier.applyPosition(modifier, config.position)
    }

    /**
     * Apply only sizing and spacing modifiers (no position).
     * Useful when position is handled by container logic.
     *
     * @param modifier The base modifier
     * @param config The layout configuration
     * @return Modifier with sizing and spacing applied
     */
    fun applySizingAndSpacing(modifier: Modifier, config: LayoutConfig): Modifier {
        var result = modifier
        result = SizingApplier.applySizing(result, config.sizing)
        result = SpacingApplier.applyPadding(result, config.padding)
        result = SpacingApplier.applyMargin(result, config.margin)
        return result
    }

    /**
     * Check if a property type is a layout-related property.
     *
     * @param type The property type string.
     * @return True if this is a layout property.
     */
    fun isLayoutProperty(type: String): Boolean {
        return SizingExtractor.isSizingProperty(type) ||
            SpacingExtractor.isSpacingProperty(type) ||
            PositionExtractor.isPositionProperty(type) ||
            isFlexProperty(type) ||
            GridExtractor.isGridProperty(type)
    }

    /**
     * Check if a property type is a flex-related property.
     */
    private fun isFlexProperty(type: String): Boolean {
        return type in FLEX_PROPERTIES
    }

    private val FLEX_PROPERTIES = setOf(
        "Display",
        "FlexDirection", "FlexWrap",
        "JustifyContent", "AlignItems", "AlignContent",
        "FlexGrow", "FlexShrink", "FlexBasis",
        "AlignSelf", "Order"
    )
}
