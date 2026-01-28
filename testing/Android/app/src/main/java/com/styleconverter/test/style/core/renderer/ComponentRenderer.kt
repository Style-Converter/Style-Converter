package com.styleconverter.test.style.core.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.styleconverter.test.style.core.ir.IRComponent
import com.styleconverter.test.style.core.ir.IRProperty
import com.styleconverter.test.style.core.types.ValueExtractors
import com.styleconverter.test.style.StyleApplier
import com.styleconverter.test.style.layout.overflow.OverflowExtractor
import kotlinx.serialization.json.jsonPrimitive
import com.styleconverter.test.style.content.lists.ListStyleExtractor
import com.styleconverter.test.style.content.lists.ListStyleApplier as StyleListApplier
import com.styleconverter.test.style.typography.TextStyleApplier
import com.styleconverter.test.style.interactive.animations.AnimationExtractor
import com.styleconverter.test.style.interactive.animations.animatedModifier
import com.styleconverter.test.style.layout.container.ContainerQueryApplier
import com.styleconverter.test.style.layout.container.ContainerQueryExtractor
import com.styleconverter.test.style.layout.columns.MultiColumnApplier
import com.styleconverter.test.style.layout.columns.MultiColumnExtractor
import com.styleconverter.test.style.content.tables.TableApplier
import com.styleconverter.test.style.content.tables.TableApplier.TableCell
import com.styleconverter.test.style.content.tables.TableExtractor
import com.styleconverter.test.style.appearance.borders.image.BorderImageApplier
import com.styleconverter.test.style.appearance.borders.image.BorderImageExtractor
import com.styleconverter.test.style.interactive.forms.FormStylingApplier
import com.styleconverter.test.style.interactive.forms.FormStylingExtractor
import com.styleconverter.test.style.content.ContentApplier
import com.styleconverter.test.style.content.ContentExtractor
import com.styleconverter.test.style.content.CounterStateProvider
import com.styleconverter.test.style.core.media.MediaQueryApplier

/**
 * SDUI Component Renderer.
 *
 * Renders IRComponents as Compose UI at runtime.
 *
 * ## Rendering Strategy
 * 1. Extract display type to determine container (Box, Row, Column)
 * 2. Extract flex properties for layout configuration
 * 3. Apply all modifier properties via PropertyApplier
 * 4. Apply text styles via TextStyleApplier
 * 5. Render placeholder content or children
 */
object ComponentRenderer {

    /**
     * Render a single IR component.
     */
    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun RenderComponent(component: IRComponent) {
        // Apply media queries to get effective properties based on screen size
        val effectiveProperties = if (component.media.isNotEmpty()) {
            MediaQueryApplier.applyMediaQueries(
                baseProperties = component.properties,
                mediaQueries = component.media
            )
        } else {
            component.properties
        }

        // Extract property pairs for extractors
        val propertyPairs = effectiveProperties.map { it.type to it.data }

        // Extract data outside composable scope with error handling
        val baseModifier = try {
            StyleApplier.applyProperties(effectiveProperties)
        } catch (e: Exception) {
            Modifier
        }

        // Extract animation config and apply animated modifier
        val animationConfig = try {
            AnimationExtractor.extractAnimationConfig(propertyPairs)
        } catch (e: Exception) {
            null
        }

        val transitionConfig = try {
            AnimationExtractor.extractTransitionConfig(propertyPairs)
        } catch (e: Exception) {
            null
        }

        // Apply animations to modifier if present
        val modifier = if (animationConfig?.hasAnimations == true) {
            animatedModifier(baseModifier, animationConfig, transitionConfig ?: com.styleconverter.test.style.interactive.animations.TransitionConfig())
        } else {
            baseModifier
        }

        val displayConfig = try {
            extractDisplayConfig(effectiveProperties)
        } catch (e: Exception) {
            DisplayConfig(DisplayType.BLOCK, FlexDirection.ROW, JustifyContent.FLEX_START, AlignItems.STRETCH, AlignContent.STRETCH, FlexWrap.NOWRAP, 0.dp, 0.dp)
        }

        val textColor = try {
            TextStyleApplier.extractTextColor(effectiveProperties)
        } catch (e: Exception) {
            null
        }

        // Extract text direction for layout
        val direction = TextStyleApplier.extractDirection(effectiveProperties)
        val layoutDirection = when (direction) {
            TextStyleApplier.DirectionMode.RTL -> LayoutDirection.Rtl
            TextStyleApplier.DirectionMode.LTR -> LayoutDirection.Ltr
        }

        // Check if this is a container query container
        val containerConfig = try {
            ContainerQueryExtractor.extractContainerQueryConfig(propertyPairs)
        } catch (e: Exception) {
            null
        }

        // Check for border image
        val borderImageConfig = try {
            BorderImageExtractor.extractBorderImageConfig(propertyPairs)
        } catch (e: Exception) {
            null
        }

        // Extract form styling configuration
        val formStylingConfig = try {
            FormStylingExtractor.extractFormConfig(propertyPairs)
        } catch (e: Exception) {
            null
        }

        // Extract before/after pseudo-element configuration from selectors
        val beforeAfterConfig = try {
            ContentApplier.extractBeforeAfterConfig(component.selectors)
        } catch (e: Exception) {
            null
        }

        // Extract counter configuration for nested content
        val counterConfig = try {
            ContentExtractor.extractCounterConfig(propertyPairs)
        } catch (e: Exception) {
            null
        }

        // Wrap content with direction if not default LTR
        val content: @Composable () -> Unit = {
            // Wrap in BorderImageBox if border image is configured
            if (borderImageConfig?.hasBorderImage == true) {
                BorderImageApplier.BorderImageBox(
                    config = borderImageConfig,
                    modifier = modifier
                ) {
                    RenderComponentContent(component, Modifier, displayConfig, textColor)
                }
            } else {
                RenderComponentContent(component, modifier, displayConfig, textColor)
            }
        }

        // Apply container query wrapper if needed
        val containerWrappedContent: @Composable () -> Unit = if (containerConfig?.hasContainerQuery == true) {
            {
                ContainerQueryApplier.QueryContainer(
                    config = containerConfig,
                    modifier = Modifier
                ) {
                    content()
                }
            }
        } else {
            content
        }

        // Apply form styling wrapper if needed
        val formWrappedContent: @Composable () -> Unit = if (formStylingConfig?.hasFormStyling == true) {
            {
                FormStylingApplier.FormStylingProvider(config = formStylingConfig) {
                    containerWrappedContent()
                }
            }
        } else {
            containerWrappedContent
        }

        // Apply counter state wrapper if counters are defined
        val counterWrappedContent: @Composable () -> Unit = if (counterConfig?.hasCounters == true) {
            {
                CounterStateProvider(config = counterConfig) {
                    formWrappedContent()
                }
            }
        } else {
            formWrappedContent
        }

        // Apply before/after pseudo-element wrapper if defined
        val wrappedContent: @Composable () -> Unit = if (beforeAfterConfig?.hasBeforeAfter == true) {
            {
                ContentApplier.ContentWithPseudoElements(config = beforeAfterConfig) {
                    counterWrappedContent()
                }
            }
        } else {
            counterWrappedContent
        }

        if (direction == TextStyleApplier.DirectionMode.RTL) {
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                wrappedContent()
            }
        } else {
            wrappedContent()
        }
    }

    /**
     * Render the actual component content (separated for direction wrapping).
     */
    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun RenderComponentContent(
        component: IRComponent,
        modifier: Modifier,
        displayConfig: DisplayConfig,
        textColor: Color?
    ) {
        when (displayConfig.type) {
            DisplayType.NONE -> {
                // Don't render anything
            }
            DisplayType.FLEX_ROW -> {
                // Check for overflow scroll
                val overflowConfig = OverflowExtractor.extractOverflowConfig(
                    component.properties.map { it.type to it.data }
                )

                if (displayConfig.flexWrap == FlexWrap.WRAP || displayConfig.flexWrap == FlexWrap.WRAP_REVERSE) {
                    // Use FlowRow for wrapping flex layouts
                    FlowRow(
                        modifier = modifier,
                        horizontalArrangement = displayConfig.toRowArrangement(),
                        verticalArrangement = Arrangement.spacedBy(displayConfig.rowGap)
                    ) {
                        RenderContent(component, textColor, displayConfig)
                    }
                } else {
                    // Apply horizontal scroll if overflow-x is scroll
                    val rowModifier = if (overflowConfig.isScrollableX) {
                        modifier.horizontalScroll(rememberScrollState())
                    } else {
                        modifier
                    }
                    Row(
                        modifier = rowModifier,
                        horizontalArrangement = displayConfig.toRowArrangement(),
                        verticalAlignment = displayConfig.alignItems.toRowAlignment()
                    ) {
                        RenderRowContent(component, textColor)
                    }
                }
            }
            DisplayType.FLEX_COLUMN -> {
                // Check for overflow scroll
                val overflowConfig = OverflowExtractor.extractOverflowConfig(
                    component.properties.map { it.type to it.data }
                )

                if (displayConfig.flexWrap == FlexWrap.WRAP || displayConfig.flexWrap == FlexWrap.WRAP_REVERSE) {
                    // Use FlowColumn for wrapping flex layouts
                    FlowColumn(
                        modifier = modifier,
                        verticalArrangement = displayConfig.toColumnArrangement(),
                        horizontalArrangement = Arrangement.spacedBy(displayConfig.columnGap)
                    ) {
                        RenderContent(component, textColor, displayConfig)
                    }
                } else {
                    // Apply vertical scroll if overflow-y is scroll
                    val columnModifier = if (overflowConfig.isScrollableY) {
                        modifier.verticalScroll(rememberScrollState())
                    } else {
                        modifier
                    }
                    Column(
                        modifier = columnModifier,
                        verticalArrangement = displayConfig.toColumnArrangement(),
                        horizontalAlignment = displayConfig.alignItems.toColumnAlignment()
                    ) {
                        RenderColumnContent(component, textColor)
                    }
                }
            }
            DisplayType.GRID -> {
                // Grid handled separately by GridApplier
                GridRenderer.RenderGrid(
                    component = component,
                    modifier = modifier,
                    displayConfig = displayConfig,
                    textColor = textColor
                )
            }
            DisplayType.TABLE -> {
                // Table layout
                val tableConfig = TableExtractor.extractTableConfig(
                    component.properties.map { it.type to it.data }
                )
                TableApplier.Table(
                    config = tableConfig,
                    modifier = modifier
                ) {
                    RenderTableContent(component, textColor)
                }
            }
            DisplayType.MULTI_COLUMN -> {
                // Multi-column layout
                val columnConfig = MultiColumnExtractor.extractMultiColumnConfig(
                    component.properties.map { it.type to it.data }
                )
                MultiColumnApplier.MultiColumnLayout(
                    config = columnConfig,
                    modifier = modifier
                ) {
                    RenderContent(component, textColor, displayConfig)
                }
            }
            DisplayType.INLINE -> {
                Row(
                    modifier = modifier,
                    horizontalArrangement = displayConfig.toRowArrangement()
                ) {
                    RenderContent(component, textColor, displayConfig)
                }
            }
            else -> {
                Box(
                    modifier = modifier,
                    contentAlignment = displayConfig.alignItems.toBoxAlignment()
                ) {
                    RenderContent(component, textColor, displayConfig)
                }
            }
        }
    }

    /**
     * Render table content - rows and cells.
     */
    @Composable
    private fun RenderTableContent(component: IRComponent, textColor: Color?) {
        if (!component.children.isNullOrEmpty()) {
            component.children.forEach { rowComponent ->
                // Each child is a table row
                TableApplier.TableRow {
                    if (!rowComponent.children.isNullOrEmpty()) {
                        rowComponent.children.forEach { cellComponent ->
                            // Each grandchild is a table cell
                            TableCell {
                                RenderComponent(cellComponent)
                            }
                        }
                    } else {
                        // Single cell with row content
                        TableCell {
                            PlaceholderContent(rowComponent.name, textColor, rowComponent.properties)
                        }
                    }
                }
            }
        } else {
            // No children - show placeholder
            TableApplier.TableRow {
                TableCell {
                    PlaceholderContent(component.name, textColor, component.properties)
                }
            }
        }
    }

    /**
     * Render content - children or placeholder.
     * Handles position:absolute/fixed children with proper stacking.
     */
    @Composable
    private fun RenderContent(component: IRComponent, textColor: Color?, displayConfig: DisplayConfig? = null) {
        if (!component.children.isNullOrEmpty()) {
            // Check if this is a positioned container (position: relative)
            val isPositionedContainer = extractPositionType(component.properties) == PositionType.RELATIVE

            if (isPositionedContainer) {
                // Render with Box to support absolute positioning
                Box(modifier = Modifier.fillMaxSize()) {
                    component.children.forEach { child ->
                        val childPosition = extractPositionType(child.properties)
                        if (childPosition == PositionType.ABSOLUTE || childPosition == PositionType.FIXED) {
                            // Render absolutely positioned child with offset
                            RenderAbsoluteChild(child)
                        } else {
                            RenderComponent(child)
                        }
                    }
                }
            } else {
                component.children.forEach { child ->
                    RenderComponent(child)
                }
            }
        } else {
            PlaceholderContent(component.name, textColor, component.properties)
        }
    }

    /**
     * Render an absolutely positioned child.
     */
    @Composable
    private fun RenderAbsoluteChild(child: IRComponent) {
        val positionOffset = extractPositionOffsets(child.properties)
        val zIndexValue = extractZIndex(child.properties)

        // Build offset modifier based on top/right/bottom/left
        var offsetModifier: Modifier = Modifier

        // Apply z-index
        if (zIndexValue != 0f) {
            offsetModifier = offsetModifier.zIndex(zIndexValue)
        }

        // Apply position offsets
        if (positionOffset.top != null || positionOffset.left != null) {
            offsetModifier = offsetModifier.offset(
                x = positionOffset.left ?: 0.dp,
                y = positionOffset.top ?: 0.dp
            )
        }

        // Wrap in Box with offset
        Box(modifier = offsetModifier) {
            RenderComponent(child)
        }
    }

    /**
     * Position type enum.
     */
    enum class PositionType {
        STATIC, RELATIVE, ABSOLUTE, FIXED, STICKY
    }

    /**
     * Position offsets data class.
     */
    data class PositionOffsets(
        val top: Dp? = null,
        val right: Dp? = null,
        val bottom: Dp? = null,
        val left: Dp? = null
    )

    /**
     * Extract position type from properties.
     */
    private fun extractPositionType(properties: List<IRProperty>): PositionType {
        properties.forEach { prop ->
            if (prop.type == "Position") {
                val keyword = ValueExtractors.extractKeyword(prop.data)?.uppercase()
                return when (keyword) {
                    "RELATIVE" -> PositionType.RELATIVE
                    "ABSOLUTE" -> PositionType.ABSOLUTE
                    "FIXED" -> PositionType.FIXED
                    "STICKY" -> PositionType.STICKY
                    else -> PositionType.STATIC
                }
            }
        }
        return PositionType.STATIC
    }

    /**
     * Extract position offsets (top, right, bottom, left) from properties.
     */
    private fun extractPositionOffsets(properties: List<IRProperty>): PositionOffsets {
        var top: Dp? = null
        var right: Dp? = null
        var bottom: Dp? = null
        var left: Dp? = null

        properties.forEach { prop ->
            when (prop.type) {
                "Top", "InsetBlockStart" -> top = ValueExtractors.extractDp(prop.data)
                "Right", "InsetInlineEnd" -> right = ValueExtractors.extractDp(prop.data)
                "Bottom", "InsetBlockEnd" -> bottom = ValueExtractors.extractDp(prop.data)
                "Left", "InsetInlineStart" -> left = ValueExtractors.extractDp(prop.data)
            }
        }

        return PositionOffsets(top, right, bottom, left)
    }

    /**
     * Extract z-index from properties.
     */
    private fun extractZIndex(properties: List<IRProperty>): Float {
        properties.forEach { prop ->
            if (prop.type == "ZIndex") {
                return ValueExtractors.extractFloat(prop.data) ?: 0f
            }
        }
        return 0f
    }

    /**
     * Render content in Row scope with AlignSelf, FlexGrow, and Order support.
     */
    @Composable
    fun RowScope.RenderRowContent(component: IRComponent, textColor: Color?) {
        if (!component.children.isNullOrEmpty()) {
            // Sort children by order property
            val sortedChildren = sortByOrder(component.children)
            sortedChildren.forEach { child ->
                val alignSelf = extractAlignSelf(child.properties)
                val flexGrow = extractFlexGrow(child.properties)

                // Build modifier with align and weight
                var childModifier: Modifier = Modifier
                childModifier = when (alignSelf) {
                    AlignSelf.FLEX_START -> childModifier.align(Alignment.Top)
                    AlignSelf.FLEX_END -> childModifier.align(Alignment.Bottom)
                    AlignSelf.CENTER -> childModifier.align(Alignment.CenterVertically)
                    else -> childModifier
                }

                // Apply flex-grow as weight
                if (flexGrow > 0f) {
                    childModifier = childModifier.weight(flexGrow)
                }

                Box(modifier = childModifier) {
                    RenderComponent(child)
                }
            }
        } else {
            PlaceholderContent(component.name, textColor, component.properties)
        }
    }

    /**
     * Render content in Column scope with AlignSelf, FlexGrow, and Order support.
     */
    @Composable
    fun ColumnScope.RenderColumnContent(component: IRComponent, textColor: Color?) {
        if (!component.children.isNullOrEmpty()) {
            // Sort children by order property
            val sortedChildren = sortByOrder(component.children)
            sortedChildren.forEach { child ->
                val alignSelf = extractAlignSelf(child.properties)
                val flexGrow = extractFlexGrow(child.properties)

                // Build modifier with align and weight
                var childModifier: Modifier = Modifier
                childModifier = when (alignSelf) {
                    AlignSelf.FLEX_START -> childModifier.align(Alignment.Start)
                    AlignSelf.FLEX_END -> childModifier.align(Alignment.End)
                    AlignSelf.CENTER -> childModifier.align(Alignment.CenterHorizontally)
                    else -> childModifier
                }

                // Apply flex-grow as weight
                if (flexGrow > 0f) {
                    childModifier = childModifier.weight(flexGrow)
                }

                Box(modifier = childModifier) {
                    RenderComponent(child)
                }
            }
        } else {
            PlaceholderContent(component.name, textColor, component.properties)
        }
    }

    /**
     * Extract flex-grow value from properties.
     */
    private fun extractFlexGrow(properties: List<IRProperty>): Float {
        properties.forEach { prop ->
            if (prop.type == "FlexGrow") {
                return ValueExtractors.extractFloat(prop.data) ?: 0f
            }
        }
        return 0f
    }

    /**
     * Extract align-self value from properties.
     */
    private fun extractAlignSelf(properties: List<IRProperty>): AlignSelf {
        properties.forEach { prop ->
            if (prop.type == "AlignSelf") {
                val keyword = ValueExtractors.extractKeyword(prop.data)?.uppercase()
                return when (keyword) {
                    "FLEX_START", "FLEX-START", "START" -> AlignSelf.FLEX_START
                    "FLEX_END", "FLEX-END", "END" -> AlignSelf.FLEX_END
                    "CENTER" -> AlignSelf.CENTER
                    "STRETCH" -> AlignSelf.STRETCH
                    "BASELINE" -> AlignSelf.BASELINE
                    else -> AlignSelf.AUTO
                }
            }
        }
        return AlignSelf.AUTO
    }

    enum class AlignSelf {
        AUTO, FLEX_START, FLEX_END, CENTER, STRETCH, BASELINE
    }

    /**
     * Justify-self values for grid item alignment along the inline (row) axis.
     */
    enum class JustifySelf {
        AUTO, NORMAL, START, END, CENTER, STRETCH, FLEX_START, FLEX_END, SELF_START, SELF_END, LEFT, RIGHT, BASELINE
    }

    /**
     * Extract order value from properties.
     * CSS order: integer (default 0), lower values appear first.
     */
    fun extractOrder(properties: List<IRProperty>): Int {
        properties.forEach { prop ->
            if (prop.type == "Order") {
                return ValueExtractors.extractInt(prop.data) ?: 0
            }
        }
        return 0
    }

    /**
     * Extract justify-self value from properties.
     */
    fun extractJustifySelf(properties: List<IRProperty>): JustifySelf {
        properties.forEach { prop ->
            if (prop.type == "JustifySelf") {
                val keyword = ValueExtractors.extractKeyword(prop.data)?.uppercase()
                    ?: ValueExtractors.extractKeywordFromObject(prop.data)?.uppercase()
                return when (keyword) {
                    "AUTO" -> JustifySelf.AUTO
                    "NORMAL" -> JustifySelf.NORMAL
                    "START", "SELF_START", "SELF-START" -> JustifySelf.START
                    "END", "SELF_END", "SELF-END" -> JustifySelf.END
                    "CENTER" -> JustifySelf.CENTER
                    "STRETCH" -> JustifySelf.STRETCH
                    "FLEX_START", "FLEX-START" -> JustifySelf.FLEX_START
                    "FLEX_END", "FLEX-END" -> JustifySelf.FLEX_END
                    "LEFT" -> JustifySelf.LEFT
                    "RIGHT" -> JustifySelf.RIGHT
                    "BASELINE" -> JustifySelf.BASELINE
                    else -> JustifySelf.AUTO
                }
            }
        }
        return JustifySelf.AUTO
    }

    /**
     * Sort children by their order property.
     * Items with lower order values appear first.
     * Items with same order maintain their source order (stable sort).
     */
    fun sortByOrder(children: List<IRComponent>): List<IRComponent> {
        return children.mapIndexed { index, child -> IndexedChild(index, child) }
            .sortedWith(compareBy(
                { extractOrder(it.child.properties) },
                { it.originalIndex }
            ))
            .map { it.child }
    }

    private data class IndexedChild(val originalIndex: Int, val child: IRComponent)

    /**
     * Placeholder content showing component name with text styling support.
     *
     * @param name The component name to display
     * @param textColor Optional text color override
     * @param properties IR properties for styling
     * @param itemIndex Index for numbered list markers (default 0)
     */
    @Composable
    private fun PlaceholderContent(
        name: String,
        textColor: Color?,
        properties: List<IRProperty> = emptyList(),
        itemIndex: Int = 0
    ) {
        val textTransform = TextStyleApplier.extractTextTransform(properties)
        var displayText = TextStyleApplier.applyTextTransform(
            name.replace("_", " "),
            textTransform
        )

        // Extract tab-size and process tabs in text
        val tabConfig = TextStyleApplier.extractTabSize(properties)
        displayText = TextStyleApplier.applyTabSize(displayText, tabConfig)

        // Extract list style configuration and prepend marker if applicable
        val listStyleConfig = ListStyleExtractor.extractListStyleConfig(properties.map { it.type to it.data })
        if (listStyleConfig.hasListStyle) {
            val marker = StyleListApplier.getMarker(itemIndex, listStyleConfig)
            displayText = marker + displayText
        }

        // Extract line-clamp and text-overflow
        val maxLines = TextStyleApplier.extractMaxLines(properties) ?: 2
        val textOverflow = TextStyleApplier.extractTextOverflow(properties)

        // Extract text wrap configuration (word-break, overflow-wrap, white-space)
        val wrapConfig = TextStyleApplier.extractTextWrapConfig(properties)

        // Determine max lines based on white-space mode
        // pre and nowrap should not wrap, so use Int.MAX_VALUE for no line limit
        val effectiveMaxLines = when (wrapConfig.softWrap) {
            false -> Int.MAX_VALUE  // No wrapping for nowrap/pre
            true -> maxLines
        }

        // Extract additional text style properties
        val textStyle = TextStyleApplier.extractTextStyle(properties)

        // Build final text style with all extracted properties
        // For list-style-position: outside, we'd need padding on the left,
        // but for simplicity we include the marker inline (like "inside")
        val effectiveFontSize = if (textStyle.fontSize != TextUnit.Unspecified) textStyle.fontSize else 11.sp
        val effectiveColor = textColor ?: if (textStyle.color != Color.Unspecified) textStyle.color else Color(0xFF888888)
        val effectiveTextAlign = if (textStyle.textAlign != TextAlign.Unspecified) textStyle.textAlign else TextAlign.Center

        val finalTextStyle = TextStyle(
            fontSize = effectiveFontSize,
            color = effectiveColor,
            textAlign = effectiveTextAlign,
            letterSpacing = textStyle.letterSpacing,
            lineHeight = textStyle.lineHeight,
            fontWeight = textStyle.fontWeight,
            fontStyle = textStyle.fontStyle,
            fontFamily = textStyle.fontFamily,
            textDecoration = textStyle.textDecoration,
            shadow = textStyle.shadow,
            baselineShift = textStyle.baselineShift,
            textIndent = textStyle.textIndent,
            textGeometricTransform = textStyle.textGeometricTransform
        )

        Text(
            text = displayText,
            style = finalTextStyle,
            maxLines = effectiveMaxLines,
            overflow = textOverflow,
            softWrap = wrapConfig.softWrap,
            modifier = Modifier.padding(4.dp)
        )
    }

    /**
     * Extract display configuration from properties.
     */
    private fun extractDisplayConfig(properties: List<IRProperty>): DisplayConfig {
        var displayType = DisplayType.BLOCK
        var flexDirection = FlexDirection.ROW
        var justifyContent = JustifyContent.FLEX_START
        var alignItems = AlignItems.STRETCH
        var flexWrap = FlexWrap.NOWRAP
        var rowGap = 0.dp
        var columnGap = 0.dp
        var alignContent = AlignContent.STRETCH

        properties.forEach { prop ->
            when (prop.type) {
                "Display" -> {
                    val keyword = ValueExtractors.extractKeyword(prop.data)?.uppercase()
                    displayType = when (keyword) {
                        "FLEX" -> DisplayType.FLEX_ROW // Default flex is row
                        "INLINE_FLEX", "INLINE-FLEX" -> DisplayType.FLEX_ROW
                        "GRID" -> DisplayType.GRID
                        "TABLE", "TABLE_ROW", "TABLE-ROW", "TABLE_CELL", "TABLE-CELL" -> DisplayType.TABLE
                        "INLINE", "INLINE_BLOCK", "INLINE-BLOCK" -> DisplayType.INLINE
                        "NONE" -> DisplayType.NONE
                        else -> DisplayType.BLOCK
                    }
                }
                // Check for multi-column layout (column-count or column-width)
                "ColumnCount", "ColumnWidth" -> {
                    if (displayType == DisplayType.BLOCK) {
                        displayType = DisplayType.MULTI_COLUMN
                    }
                }
                "FlexDirection" -> {
                    val keyword = ValueExtractors.extractKeyword(prop.data)?.uppercase()
                    flexDirection = when (keyword) {
                        "COLUMN", "COLUMN_REVERSE", "COLUMN-REVERSE" -> FlexDirection.COLUMN
                        else -> FlexDirection.ROW
                    }
                    // Update display type based on flex direction
                    if (displayType == DisplayType.FLEX_ROW && flexDirection == FlexDirection.COLUMN) {
                        displayType = DisplayType.FLEX_COLUMN
                    }
                }
                "JustifyContent" -> {
                    val keyword = ValueExtractors.extractKeyword(prop.data)?.uppercase()
                    justifyContent = when (keyword) {
                        "CENTER" -> JustifyContent.CENTER
                        "FLEX_END", "FLEX-END", "END" -> JustifyContent.FLEX_END
                        "SPACE_BETWEEN", "SPACE-BETWEEN" -> JustifyContent.SPACE_BETWEEN
                        "SPACE_AROUND", "SPACE-AROUND" -> JustifyContent.SPACE_AROUND
                        "SPACE_EVENLY", "SPACE-EVENLY" -> JustifyContent.SPACE_EVENLY
                        else -> JustifyContent.FLEX_START
                    }
                }
                "AlignItems" -> {
                    val keyword = ValueExtractors.extractKeyword(prop.data)?.uppercase()
                    alignItems = when (keyword) {
                        "CENTER" -> AlignItems.CENTER
                        "FLEX_START", "FLEX-START", "START" -> AlignItems.FLEX_START
                        "FLEX_END", "FLEX-END", "END" -> AlignItems.FLEX_END
                        "BASELINE" -> AlignItems.BASELINE
                        else -> AlignItems.STRETCH
                    }
                }
                "AlignContent" -> {
                    val keyword = ValueExtractors.extractKeyword(prop.data)?.uppercase()
                    alignContent = when (keyword) {
                        "CENTER" -> AlignContent.CENTER
                        "FLEX_START", "FLEX-START", "START" -> AlignContent.FLEX_START
                        "FLEX_END", "FLEX-END", "END" -> AlignContent.FLEX_END
                        "SPACE_BETWEEN", "SPACE-BETWEEN" -> AlignContent.SPACE_BETWEEN
                        "SPACE_AROUND", "SPACE-AROUND" -> AlignContent.SPACE_AROUND
                        "SPACE_EVENLY", "SPACE-EVENLY" -> AlignContent.SPACE_EVENLY
                        else -> AlignContent.STRETCH
                    }
                }
                "FlexWrap" -> {
                    val keyword = ValueExtractors.extractKeyword(prop.data)?.uppercase()
                    flexWrap = when (keyword) {
                        "WRAP" -> FlexWrap.WRAP
                        "WRAP_REVERSE", "WRAP-REVERSE" -> FlexWrap.WRAP_REVERSE
                        else -> FlexWrap.NOWRAP
                    }
                }
                "Gap" -> {
                    ValueExtractors.extractDp(prop.data)?.let {
                        rowGap = it
                        columnGap = it
                    }
                }
                "RowGap" -> {
                    ValueExtractors.extractDp(prop.data)?.let { rowGap = it }
                }
                "ColumnGap" -> {
                    ValueExtractors.extractDp(prop.data)?.let { columnGap = it }
                }
            }
        }

        return DisplayConfig(
            type = displayType,
            flexDirection = flexDirection,
            justifyContent = justifyContent,
            alignItems = alignItems,
            alignContent = alignContent,
            flexWrap = flexWrap,
            rowGap = rowGap,
            columnGap = columnGap
        )
    }

    // ==================== CONFIG TYPES ====================

    data class DisplayConfig(
        val type: DisplayType,
        val flexDirection: FlexDirection,
        val justifyContent: JustifyContent,
        val alignItems: AlignItems,
        val alignContent: AlignContent = AlignContent.STRETCH,
        val flexWrap: FlexWrap,
        val rowGap: androidx.compose.ui.unit.Dp = 0.dp,
        val columnGap: androidx.compose.ui.unit.Dp = 0.dp
    ) {
        // Legacy support - get single gap value
        val gap: androidx.compose.ui.unit.Dp get() = maxOf(rowGap, columnGap)
    }

    enum class DisplayType {
        BLOCK, INLINE, FLEX_ROW, FLEX_COLUMN, GRID, TABLE, MULTI_COLUMN, NONE
    }

    enum class FlexDirection {
        ROW, COLUMN
    }

    enum class JustifyContent {
        FLEX_START, FLEX_END, CENTER, SPACE_BETWEEN, SPACE_AROUND, SPACE_EVENLY
    }

    enum class AlignItems {
        STRETCH, FLEX_START, FLEX_END, CENTER, BASELINE
    }

    enum class AlignContent {
        STRETCH, FLEX_START, FLEX_END, CENTER, SPACE_BETWEEN, SPACE_AROUND, SPACE_EVENLY
    }

    enum class FlexWrap {
        NOWRAP, WRAP, WRAP_REVERSE
    }

    // ==================== ARRANGEMENT/ALIGNMENT CONVERTERS ====================

    /**
     * Convert DisplayConfig to horizontal arrangement with gap support.
     */
    private fun DisplayConfig.toRowArrangement(): Arrangement.Horizontal {
        val spacing = columnGap
        return when (justifyContent) {
            JustifyContent.FLEX_START -> if (spacing > 0.dp) Arrangement.spacedBy(spacing, Alignment.Start) else Arrangement.Start
            JustifyContent.FLEX_END -> if (spacing > 0.dp) Arrangement.spacedBy(spacing, Alignment.End) else Arrangement.End
            JustifyContent.CENTER -> if (spacing > 0.dp) Arrangement.spacedBy(spacing, Alignment.CenterHorizontally) else Arrangement.Center
            JustifyContent.SPACE_BETWEEN -> Arrangement.SpaceBetween
            JustifyContent.SPACE_AROUND -> Arrangement.SpaceAround
            JustifyContent.SPACE_EVENLY -> Arrangement.SpaceEvenly
        }
    }

    /**
     * Convert DisplayConfig to vertical arrangement with gap support.
     */
    private fun DisplayConfig.toColumnArrangement(): Arrangement.Vertical {
        val spacing = rowGap
        return when (justifyContent) {
            JustifyContent.FLEX_START -> if (spacing > 0.dp) Arrangement.spacedBy(spacing, Alignment.Top) else Arrangement.Top
            JustifyContent.FLEX_END -> if (spacing > 0.dp) Arrangement.spacedBy(spacing, Alignment.Bottom) else Arrangement.Bottom
            JustifyContent.CENTER -> if (spacing > 0.dp) Arrangement.spacedBy(spacing, Alignment.CenterVertically) else Arrangement.Center
            JustifyContent.SPACE_BETWEEN -> Arrangement.SpaceBetween
            JustifyContent.SPACE_AROUND -> Arrangement.SpaceAround
            JustifyContent.SPACE_EVENLY -> Arrangement.SpaceEvenly
        }
    }

    private fun AlignItems.toRowAlignment(): Alignment.Vertical = when (this) {
        AlignItems.FLEX_START -> Alignment.Top
        AlignItems.FLEX_END -> Alignment.Bottom
        AlignItems.CENTER -> Alignment.CenterVertically
        AlignItems.BASELINE -> Alignment.CenterVertically // Baseline not directly supported
        AlignItems.STRETCH -> Alignment.CenterVertically
    }

    private fun AlignItems.toColumnAlignment(): Alignment.Horizontal = when (this) {
        AlignItems.FLEX_START -> Alignment.Start
        AlignItems.FLEX_END -> Alignment.End
        AlignItems.CENTER -> Alignment.CenterHorizontally
        AlignItems.BASELINE -> Alignment.Start
        AlignItems.STRETCH -> Alignment.CenterHorizontally
    }

    private fun AlignItems.toBoxAlignment(): Alignment = when (this) {
        AlignItems.FLEX_START -> Alignment.TopStart
        AlignItems.FLEX_END -> Alignment.BottomEnd
        AlignItems.CENTER -> Alignment.Center
        AlignItems.BASELINE -> Alignment.TopStart
        AlignItems.STRETCH -> Alignment.Center
    }
}
