package com.styleconverter.test.style.layout.flex

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Applies CSS flexbox properties to Compose layouts.
 *
 * ## CSS Properties
 * ```css
 * .flex-container {
 *     display: flex;
 *     flex-direction: row;
 *     flex-wrap: wrap;
 *     justify-content: space-between;
 *     align-items: center;
 *     gap: 16px;
 * }
 *
 * .flex-item {
 *     flex-grow: 1;
 *     flex-shrink: 0;
 *     flex-basis: 200px;
 *     align-self: flex-start;
 *     order: 1;
 * }
 * ```
 *
 * ## Compose Mapping
 *
 * | CSS Property | Compose Equivalent |
 * |--------------|-------------------|
 * | display: flex | Row (row) / Column (column) |
 * | flex-direction: row | Row |
 * | flex-direction: column | Column |
 * | flex-wrap: wrap | FlowRow / FlowColumn |
 * | justify-content | Arrangement.Horizontal / Vertical |
 * | align-items | Alignment.Vertical / Horizontal |
 * | gap | Arrangement.spacedBy() |
 * | flex-grow | Modifier.weight() |
 * | align-self | Alignment override |
 *
 * ## Limitations
 *
 * - **flex-basis**: Partial support via width/height modifiers
 * - **flex-shrink**: Not directly supported in Compose
 * - **order**: Requires manual reordering of children
 * - **flex-wrap: wrap-reverse**: Limited support
 * - **align-content**: Limited support for multi-line
 *
 * ## Usage
 * ```kotlin
 * FlexApplier.FlexContainer(
 *     config = flexContainerConfig,
 *     gap = 16.dp,
 *     modifier = Modifier.fillMaxWidth()
 * ) {
 *     FlexApplier.FlexItem(
 *         config = flexItemConfig,
 *         modifier = Modifier
 *     ) {
 *         Text("Item 1")
 *     }
 * }
 * ```
 */
object FlexApplier {

    /**
     * CompositionLocal to pass flex container config to children.
     */
    val LocalFlexContainer = compositionLocalOf<FlexContainerConfig?> { null }

    // =========================================================================
    // CONTAINER COMPOSABLES
    // =========================================================================

    /**
     * A flexible container that renders as Row, Column, FlowRow, or FlowColumn
     * based on the flex configuration.
     *
     * @param config Flex container configuration
     * @param gap Gap between items (maps to Arrangement.spacedBy)
     * @param modifier Modifier for the container
     * @param content Child items to render
     */
    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun FlexContainer(
        config: FlexContainerConfig,
        gap: Dp = 0.dp,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        if (!config.isFlex) {
            // Not a flex container, render as Box
            Box(modifier = modifier) {
                content()
            }
            return
        }

        CompositionLocalProvider(LocalFlexContainer provides config) {
            when {
                // Wrapping flex containers
                config.wrap != FlexWrap.NO_WRAP && config.isRow -> {
                    FlowRow(
                        modifier = modifier,
                        horizontalArrangement = getHorizontalArrangement(config.justifyContent, gap),
                        verticalArrangement = getVerticalArrangement(config.alignContent, 0.dp),
                        content = { content() }
                    )
                }
                config.wrap != FlexWrap.NO_WRAP && !config.isRow -> {
                    FlowColumn(
                        modifier = modifier,
                        verticalArrangement = getVerticalArrangement(config.justifyContent.toAlignContent(), gap),
                        horizontalArrangement = getHorizontalArrangement(config.alignContent.toJustifyContent(), 0.dp),
                        content = { content() }
                    )
                }
                // Non-wrapping flex containers
                config.isRow -> {
                    FlexRow(
                        config = config,
                        gap = gap,
                        modifier = modifier,
                        content = content
                    )
                }
                else -> {
                    FlexColumn(
                        config = config,
                        gap = gap,
                        modifier = modifier,
                        content = content
                    )
                }
            }
        }
    }

    /**
     * Flex container with row direction.
     */
    @Composable
    fun FlexRow(
        config: FlexContainerConfig,
        gap: Dp = 0.dp,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        val arrangement = getHorizontalArrangement(config.justifyContent, gap)
        val alignment = getVerticalAlignment(config.alignItems)

        // Handle reverse
        val rowContent: @Composable RowScope.() -> Unit = {
            content()
        }

        Row(
            modifier = modifier,
            horizontalArrangement = if (config.isReverse) {
                Arrangement.End
            } else {
                arrangement
            },
            verticalAlignment = alignment
        ) {
            rowContent()
        }
    }

    /**
     * Flex container with column direction.
     */
    @Composable
    fun FlexColumn(
        config: FlexContainerConfig,
        gap: Dp = 0.dp,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        val arrangement = getVerticalArrangement(config.justifyContent.toAlignContent(), gap)
        val alignment = getHorizontalAlignment(config.alignItems)

        Column(
            modifier = modifier,
            verticalArrangement = if (config.isReverse) {
                Arrangement.Bottom
            } else {
                arrangement
            },
            horizontalAlignment = alignment
        ) {
            content()
        }
    }

    // =========================================================================
    // ITEM COMPOSABLES
    // =========================================================================

    /**
     * A flex item that applies flex item properties.
     *
     * Note: For flex-grow to work, this must be used inside a Row/Column
     * with RowScope/ColumnScope.
     *
     * @param config Flex item configuration
     * @param modifier Base modifier
     * @param content Item content
     */
    @Composable
    fun FlexItem(
        config: FlexItemConfig,
        modifier: Modifier = Modifier,
        content: @Composable BoxScope.() -> Unit
    ) {
        val containerConfig = LocalFlexContainer.current
        val itemModifier = applyFlexItemModifier(modifier, config, containerConfig)

        Box(
            modifier = itemModifier,
            contentAlignment = getFlexItemAlignment(config, containerConfig),
            content = content
        )
    }

    /**
     * Apply flex item properties to a modifier.
     *
     * @param modifier Base modifier
     * @param config Flex item configuration
     * @param containerConfig Parent flex container config (optional)
     * @return Modified modifier with flex properties
     */
    fun applyFlexItemModifier(
        modifier: Modifier,
        config: FlexItemConfig,
        containerConfig: FlexContainerConfig? = null
    ): Modifier {
        var result = modifier

        // Apply flex-basis as size constraint
        result = when (val basis = config.flexBasis) {
            is FlexBasis.Length -> {
                if (containerConfig?.isRow == true) {
                    result.width(basis.dp.dp)
                } else {
                    result.height(basis.dp.dp)
                }
            }
            is FlexBasis.Percentage -> {
                if (containerConfig?.isRow == true) {
                    result.fillMaxWidth(basis.fraction)
                } else {
                    result.fillMaxHeight(basis.fraction)
                }
            }
            is FlexBasis.Auto, is FlexBasis.Content -> {
                if (containerConfig?.isRow == true) {
                    result.wrapContentWidth()
                } else {
                    result.wrapContentHeight()
                }
            }
        }

        return result
    }

    // =========================================================================
    // SCOPE EXTENSIONS FOR FLEX-GROW
    // =========================================================================

    /**
     * Apply flex-grow weight in a RowScope.
     *
     * @param flexGrow The flex-grow value (0 = no grow, 1+ = proportional grow)
     * @return Modifier with weight applied if flexGrow > 0
     */
    fun RowScope.flexGrow(flexGrow: Float): Modifier {
        return if (flexGrow > 0f) {
            Modifier.weight(flexGrow)
        } else {
            Modifier
        }
    }

    /**
     * Apply flex-grow weight in a ColumnScope.
     *
     * @param flexGrow The flex-grow value (0 = no grow, 1+ = proportional grow)
     * @return Modifier with weight applied if flexGrow > 0
     */
    fun ColumnScope.flexGrow(flexGrow: Float): Modifier {
        return if (flexGrow > 0f) {
            Modifier.weight(flexGrow)
        } else {
            Modifier
        }
    }

    // =========================================================================
    // ARRANGEMENT MAPPERS
    // =========================================================================

    /**
     * Map justify-content to Compose horizontal Arrangement.
     */
    fun getHorizontalArrangement(
        justifyContent: JustifyContent,
        gap: Dp = 0.dp
    ): Arrangement.Horizontal {
        return when (justifyContent) {
            JustifyContent.FLEX_START -> {
                if (gap > 0.dp) Arrangement.spacedBy(gap, Alignment.Start)
                else Arrangement.Start
            }
            JustifyContent.FLEX_END -> {
                if (gap > 0.dp) Arrangement.spacedBy(gap, Alignment.End)
                else Arrangement.End
            }
            JustifyContent.CENTER -> {
                if (gap > 0.dp) Arrangement.spacedBy(gap, Alignment.CenterHorizontally)
                else Arrangement.Center
            }
            JustifyContent.SPACE_BETWEEN -> Arrangement.SpaceBetween
            JustifyContent.SPACE_AROUND -> Arrangement.SpaceAround
            JustifyContent.SPACE_EVENLY -> Arrangement.SpaceEvenly
        }
    }

    /**
     * Map align-content to Compose vertical Arrangement.
     */
    fun getVerticalArrangement(
        alignContent: AlignContent,
        gap: Dp = 0.dp
    ): Arrangement.Vertical {
        return when (alignContent) {
            AlignContent.FLEX_START -> {
                if (gap > 0.dp) Arrangement.spacedBy(gap, Alignment.Top)
                else Arrangement.Top
            }
            AlignContent.FLEX_END -> {
                if (gap > 0.dp) Arrangement.spacedBy(gap, Alignment.Bottom)
                else Arrangement.Bottom
            }
            AlignContent.CENTER -> {
                if (gap > 0.dp) Arrangement.spacedBy(gap, Alignment.CenterVertically)
                else Arrangement.Center
            }
            AlignContent.SPACE_BETWEEN -> Arrangement.SpaceBetween
            AlignContent.SPACE_AROUND -> Arrangement.SpaceAround
            AlignContent.STRETCH -> {
                if (gap > 0.dp) Arrangement.spacedBy(gap)
                else Arrangement.Top
            }
        }
    }

    // =========================================================================
    // ALIGNMENT MAPPERS
    // =========================================================================

    /**
     * Map align-items to Compose vertical Alignment (for Row).
     */
    fun getVerticalAlignment(alignItems: AlignItems): Alignment.Vertical {
        return when (alignItems) {
            AlignItems.FLEX_START -> Alignment.Top
            AlignItems.FLEX_END -> Alignment.Bottom
            AlignItems.CENTER -> Alignment.CenterVertically
            AlignItems.BASELINE -> Alignment.Top // Baseline not directly supported
            AlignItems.STRETCH -> Alignment.CenterVertically
        }
    }

    /**
     * Map align-items to Compose horizontal Alignment (for Column).
     */
    fun getHorizontalAlignment(alignItems: AlignItems): Alignment.Horizontal {
        return when (alignItems) {
            AlignItems.FLEX_START -> Alignment.Start
            AlignItems.FLEX_END -> Alignment.End
            AlignItems.CENTER -> Alignment.CenterHorizontally
            AlignItems.BASELINE -> Alignment.Start // Baseline not directly supported
            AlignItems.STRETCH -> Alignment.CenterHorizontally
        }
    }

    /**
     * Get alignment for a flex item based on align-self and container config.
     */
    fun getFlexItemAlignment(
        config: FlexItemConfig,
        containerConfig: FlexContainerConfig?
    ): Alignment {
        val alignSelf = config.alignSelf
        val containerAlignItems = containerConfig?.alignItems ?: AlignItems.STRETCH

        val effectiveAlign = if (alignSelf == AlignSelf.AUTO) {
            containerAlignItems
        } else {
            alignSelf.toAlignItems()
        }

        return if (containerConfig?.isRow == true) {
            // Row: cross-axis is vertical
            when (effectiveAlign) {
                AlignItems.FLEX_START -> Alignment.TopStart
                AlignItems.FLEX_END -> Alignment.BottomStart
                AlignItems.CENTER -> Alignment.CenterStart
                AlignItems.BASELINE -> Alignment.TopStart
                AlignItems.STRETCH -> Alignment.CenterStart
            }
        } else {
            // Column: cross-axis is horizontal
            when (effectiveAlign) {
                AlignItems.FLEX_START -> Alignment.TopStart
                AlignItems.FLEX_END -> Alignment.TopEnd
                AlignItems.CENTER -> Alignment.TopCenter
                AlignItems.BASELINE -> Alignment.TopStart
                AlignItems.STRETCH -> Alignment.TopCenter
            }
        }
    }

    // =========================================================================
    // EXTENSION CONVERSIONS
    // =========================================================================

    private fun JustifyContent.toAlignContent(): AlignContent = when (this) {
        JustifyContent.FLEX_START -> AlignContent.FLEX_START
        JustifyContent.FLEX_END -> AlignContent.FLEX_END
        JustifyContent.CENTER -> AlignContent.CENTER
        JustifyContent.SPACE_BETWEEN -> AlignContent.SPACE_BETWEEN
        JustifyContent.SPACE_AROUND -> AlignContent.SPACE_AROUND
        JustifyContent.SPACE_EVENLY -> AlignContent.CENTER // No direct equivalent
    }

    private fun AlignContent.toJustifyContent(): JustifyContent = when (this) {
        AlignContent.FLEX_START -> JustifyContent.FLEX_START
        AlignContent.FLEX_END -> JustifyContent.FLEX_END
        AlignContent.CENTER -> JustifyContent.CENTER
        AlignContent.SPACE_BETWEEN -> JustifyContent.SPACE_BETWEEN
        AlignContent.SPACE_AROUND -> JustifyContent.SPACE_AROUND
        AlignContent.STRETCH -> JustifyContent.FLEX_START
    }

    private fun AlignSelf.toAlignItems(): AlignItems = when (this) {
        AlignSelf.AUTO -> AlignItems.STRETCH
        AlignSelf.FLEX_START -> AlignItems.FLEX_START
        AlignSelf.FLEX_END -> AlignItems.FLEX_END
        AlignSelf.CENTER -> AlignItems.CENTER
        AlignSelf.BASELINE -> AlignItems.BASELINE
        AlignSelf.STRETCH -> AlignItems.STRETCH
    }

    // =========================================================================
    // NOTES
    // =========================================================================

    object Notes {
        const val FLEX_GROW = """
            CSS flex-grow maps to Compose Modifier.weight() but requires scope access.
            Use the flexGrow extension functions inside Row/Column:

            Row {
                Box(modifier = Modifier.then(flexGrow(1f))) { ... }
                Box(modifier = Modifier.then(flexGrow(2f))) { ... }
            }
        """

        const val FLEX_SHRINK = """
            CSS flex-shrink has no direct Compose equivalent.
            Compose layouts don't shrink children below their minimum size.
            Consider using:
            - Modifier.width(IntrinsicSize.Min) for minimum sizing
            - Modifier.sizeIn(maxWidth = ...) for maximum constraints
        """

        const val FLEX_BASIS = """
            CSS flex-basis is approximated using width/height modifiers:
            - flex-basis: <length> → width(x.dp) or height(x.dp)
            - flex-basis: <percentage> → fillMaxWidth(fraction) or fillMaxHeight(fraction)
            - flex-basis: auto → wrapContentWidth() or wrapContentHeight()
        """

        const val ORDER = """
            CSS order requires manual reordering of children.
            In SDUI, sort children by their order property before rendering:

            children.sortedBy { it.order }.forEach { child ->
                DynamicComponent(child)
            }
        """

        const val WRAP = """
            CSS flex-wrap: wrap maps to FlowRow/FlowColumn.
            Requires ExperimentalLayoutApi from Compose Foundation 1.4+.

            flex-wrap: wrap-reverse is partially supported - items wrap but
            not in reverse order.
        """
    }
}
