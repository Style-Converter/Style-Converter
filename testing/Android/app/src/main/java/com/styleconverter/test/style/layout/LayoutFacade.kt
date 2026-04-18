package com.styleconverter.test.style.layout

import androidx.compose.ui.Modifier
import com.styleconverter.test.style.layout.flexbox.FlexContainerConfig
import com.styleconverter.test.style.layout.flexbox.FlexExtractor
import com.styleconverter.test.style.layout.flexbox.FlexItemConfig
import com.styleconverter.test.style.layout.grid.GridConfig
import com.styleconverter.test.style.layout.grid.GridExtractor
import com.styleconverter.test.style.layout.position.PositionApplier
import com.styleconverter.test.style.layout.position.PositionConfig
import com.styleconverter.test.style.layout.position.PositionExtractor
import com.styleconverter.test.style.sizing.SizingApplier
import com.styleconverter.test.style.sizing.SizingConfig
import com.styleconverter.test.style.sizing.SizingExtractor
import com.styleconverter.test.style.spacing.GapConfig
import com.styleconverter.test.style.spacing.MarginConfig
import com.styleconverter.test.style.spacing.PaddingConfig
import com.styleconverter.test.style.spacing.SpacingApplier
import com.styleconverter.test.style.spacing.SpacingExtractor
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

        // Apply padding BEFORE sizing so that CSS box model is respected:
        // In CSS, padding expands the box outward. In Compose, Modifier.padding()
        // applied before sizing constraints means the constraints apply to the
        // outer box (padding + content), matching CSS behavior.
        result = SpacingApplier.applyPadding(result, config.padding)

        // Apply sizing (width, height, constraints) — constrains outer dimensions
        result = SizingApplier.applySizing(result, config.sizing)

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

    // --- Phase 7 step 1: style-engine LayoutConfig hook ---------------------
    //
    // The three methods below delegate to the new LayoutExtractor /
    // LayoutApplier scaffold. They are intentionally separate from the legacy
    // `extractConfig` / `applyToModifier` above — those remain untouched so
    // ComponentRenderer's existing code path works byte-identically.
    //
    // Later Phase 7 steps will migrate callers from the legacy pair to the
    // style-engine triplet, then (step 6) delete the legacy pair. Until then
    // both exist side by side.
    //
    // The method names deliberately don't collide with the legacy ones:
    //   extractConfig       -> legacy LayoutFacade.LayoutConfig
    //   extractLayoutConfig -> new style-engine LayoutConfig (top-level in package)

    /**
     * Phase 7 entrypoint — extract the style-engine [LayoutConfig] scaffold.
     *
     * Step 1: returns [LayoutConfig.Empty] (all nulls). See
     * [LayoutExtractor.extractLayoutConfig] for the contract.
     */
    fun extractLayoutConfig(
        properties: List<Pair<String, JsonElement?>>
    ): com.styleconverter.test.style.layout.LayoutConfig {
        return LayoutExtractor.extractLayoutConfig(properties)
    }

    /**
     * Phase 7 entrypoint — ask the style engine which Compose container this
     * component should render with. Step 1 always returns
     * [ContainerDecision.default] which signals "defer to the legacy renderer."
     */
    fun containerDecision(
        config: com.styleconverter.test.style.layout.LayoutConfig?
    ): ContainerDecision {
        // Null-safe so ComponentRenderer can pass null in failure paths.
        if (config == null) return ContainerDecision.default
        return LayoutApplier.containerDecision(config)
    }

    /**
     * Phase 7 entrypoint — child-level Modifier contribution (zIndex,
     * alignSelf, order, relative inset). Step 1 returns identity Modifier so
     * the legacy StyleApplier chain is unaffected.
     */
    fun childModifier(
        config: com.styleconverter.test.style.layout.LayoutConfig?
    ): Modifier {
        if (config == null) return Modifier
        return LayoutApplier.childModifier(config)
    }
}
