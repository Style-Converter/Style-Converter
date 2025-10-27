package app.logic.compose.generation

import app.logic.compose.analysis.*
import app.IRProperty

/**
 * Represents different types of layout wrappers
 */
sealed class WrapperConfig {
    data class Row(
        val horizontalArrangement: String? = null,
        val verticalAlignment: String? = null,
        val reverseLayout: Boolean = false
    ) : WrapperConfig()

    data class Column(
        val verticalArrangement: String? = null,
        val horizontalAlignment: String? = null,
        val reverseLayout: Boolean = false
    ) : WrapperConfig()

    data class FlowRow(
        val horizontalArrangement: String? = null,
        val verticalArrangement: String? = null,
        val maxItemsInEachRow: Int? = null
    ) : WrapperConfig()

    data class FlowColumn(
        val horizontalArrangement: String? = null,
        val verticalArrangement: String? = null,
        val maxItemsInEachColumn: Int? = null
    ) : WrapperConfig()

    data class Box(
        val contentAlignment: String? = null
    ) : WrapperConfig()

    data class ScrollableRow(
        val state: String = "rememberScrollState()",
        val smooth: Boolean = false
    ) : WrapperConfig()

    data class ScrollableColumn(
        val state: String = "rememberScrollState()",
        val smooth: Boolean = false
    ) : WrapperConfig()

    data class PositionedBox(
        val alignment: String,
        val offsetX: String? = null,
        val offsetY: String? = null
    ) : WrapperConfig()

    data class LazyVerticalGrid(
        val columns: Int = 2,
        val horizontalArrangement: String? = null,
        val verticalArrangement: String? = null
    ) : WrapperConfig()
}

/**
 * Represents state setup code
 */
data class StateSetup(
    val variableName: String,
    val type: String,
    val initialValue: String,
    val mutable: Boolean = true
)

/**
 * Represents animation setup
 */
data class AnimationSetup(
    val variableName: String,
    val targetValue: String,
    val animationSpec: String,
    val type: String = "Float"  // Float, Color, Dp, etc.
)

/**
 * Represents state-dependent modifiers
 */
data class StateDependentModifiers(
    val stateType: SelectorType,
    val modifiers: List<String>  // Modifiers to apply when in this state
)

/**
 * Represents responsive/media query modifiers
 */
data class ResponsiveModifiers(
    val minWidth: Int? = null,
    val maxWidth: Int? = null,
    val modifiers: List<String>  // Modifiers to apply when screen size matches
)

/**
 * Represents the complete structure of a generated component
 */
data class ComponentStructure(
    val name: String,
    val outerWrapper: WrapperConfig? = null,
    val innerWrapper: WrapperConfig? = null,
    val stateSetup: List<StateSetup> = emptyList(),
    val animationSetup: List<AnimationSetup> = emptyList(),
    val modifierChain: List<String> = emptyList(),
    val stateDependentModifiers: List<StateDependentModifiers> = emptyList(),
    val responsiveModifiers: List<ResponsiveModifiers> = emptyList(),
    val needsBoxWithConstraints: Boolean = false,
    val customDrawingCode: String? = null,
    val textConfig: TextConfig? = null,
    val containerTextStyle: TextConfig? = null,  // For non-text containers with text styling
    val needsContentParameter: Boolean = true,
    val contentScope: ContentScope = ContentScope.DEFAULT,
    val contentDefaultEmpty: Boolean = true,
    val additionalParameters: List<ComponentParameter> = emptyList(),
    val requiredImports: Set<String> = emptySet()
)

/**
 * Represents additional component parameters
 */
data class ComponentParameter(
    val name: String,
    val type: String,
    val defaultValue: String? = null
)

/**
 * Defines the scope type for the content parameter
 * This determines what capabilities children have access to
 */
enum class ContentScope {
    DEFAULT,        // @Composable () -> Unit - No special scope
    ROW_SCOPE,      // @Composable RowScope.() -> Unit - Children can use weight(), alignByBaseline()
    COLUMN_SCOPE,   // @Composable ColumnScope.() -> Unit - Children can use weight(), alignByBaseline()
    BOX_SCOPE,      // @Composable BoxScope.() -> Unit - Children can use align()
    FLOW_ROW_SCOPE, // @Composable FlowRowScope.() -> Unit - FlowRow children scope
    FLOW_COLUMN_SCOPE // @Composable FlowColumnScope.() -> Unit - FlowColumn children scope
}

/**
 * Text component configuration
 */
data class TextConfig(
    val fontSize: String? = null,
    val fontWeight: String? = null,
    val fontStyle: String? = null,
    val color: String? = null,
    val textAlign: String? = null,
    val textDecoration: String? = null,
    val lineHeight: String? = null,
    val letterSpacing: String? = null,
    val maxLines: Int? = null,
    val overflow: String? = null
)

/**
 * Builds the structure of a Compose component based on requirements analysis
 */
class ComponentStructureBuilder {

    fun build(name: String, requirements: ComponentRequirements): ComponentStructure {
        val imports = mutableSetOf<String>()
        imports.add("androidx.compose.runtime.Composable")
        imports.add("androidx.compose.ui.Modifier")

        // Determine wrapper hierarchy
        val outerWrapper = determineOuterWrapper(requirements, imports)
        val innerWrapper = determineInnerWrapper(requirements, outerWrapper, imports)

        // Build state and animation setup
        val stateSetup = buildStateSetup(requirements, imports)
        val animationSetup = buildAnimationSetup(requirements, imports)
        val stateDependentMods = buildStateDependentModifiers(requirements, imports)
        val responsiveMods = buildResponsiveModifiers(requirements, imports)

        // Determine additional parameters
        val additionalParams = buildAdditionalParameters(requirements)

        // Build text configuration if needed
        val textConfig = if (requirements.needsTextComponent) {
            buildTextConfig(requirements.textRequirements!!, imports)
        } else null

        // Build custom drawing code if needed
        val customDrawing = if (requirements.needsSvgDrawing) {
            buildSvgDrawingCode(requirements.svgRequirements!!, imports)
        } else null

        // Determine if content parameter is needed
        val needsContent = !requirements.needsTextComponent && customDrawing == null

        // Determine content scope based on the innermost wrapper
        val contentScope = determineContentScope(innerWrapper, outerWrapper)

        return ComponentStructure(
            name = name,
            outerWrapper = outerWrapper,
            innerWrapper = innerWrapper,
            stateSetup = stateSetup,
            animationSetup = animationSetup,
            stateDependentModifiers = stateDependentMods,
            responsiveModifiers = responsiveMods,
            needsBoxWithConstraints = requirements.needsResponsive,
            customDrawingCode = customDrawing,
            textConfig = textConfig,
            needsContentParameter = needsContent,
            contentScope = contentScope,
            contentDefaultEmpty = true,
            additionalParameters = additionalParams,
            requiredImports = imports
        )
    }

    // ============================================
    // Content Scope Determination
    // ============================================

    /**
     * Determines the appropriate content scope based on the wrapper hierarchy.
     * The innermost wrapper determines what scope children will have access to.
     */
    private fun determineContentScope(
        innerWrapper: WrapperConfig?,
        outerWrapper: WrapperConfig?
    ): ContentScope {
        // The innermost wrapper determines the scope
        val primaryWrapper = innerWrapper ?: outerWrapper

        return when (primaryWrapper) {
            is WrapperConfig.Row, is WrapperConfig.ScrollableRow -> ContentScope.ROW_SCOPE
            is WrapperConfig.Column, is WrapperConfig.ScrollableColumn -> ContentScope.COLUMN_SCOPE
            is WrapperConfig.Box, is WrapperConfig.PositionedBox -> ContentScope.BOX_SCOPE
            is WrapperConfig.FlowRow -> ContentScope.FLOW_ROW_SCOPE
            is WrapperConfig.FlowColumn -> ContentScope.FLOW_COLUMN_SCOPE
            is WrapperConfig.LazyVerticalGrid -> ContentScope.DEFAULT  // Grid items don't have special scope
            null -> ContentScope.DEFAULT
        }
    }

    // ============================================
    // Wrapper Determination
    // ============================================

    private fun determineOuterWrapper(
        requirements: ComponentRequirements,
        imports: MutableSet<String>
    ): WrapperConfig? {
        // Priority: Position > Scroll > Flex

        if (requirements.needsPositionWrapper && requirements.positionConfig?.type in listOf(PositionType.ABSOLUTE, PositionType.FIXED)) {
            imports.add("androidx.compose.foundation.layout.Box")
            imports.add("androidx.compose.foundation.layout.fillMaxSize")
            imports.add("androidx.compose.ui.Alignment")
            val config = requirements.positionConfig!!
            return WrapperConfig.PositionedBox(
                alignment = calculateAlignment(config),
                offsetX = config.left?.let { "${it}.dp" } ?: config.right?.let { "(-${it}).dp" },
                offsetY = config.top?.let { "${it}.dp" } ?: config.bottom?.let { "(-${it}).dp" }
            )
        }

        return null
    }

    private fun determineInnerWrapper(
        requirements: ComponentRequirements,
        outerWrapper: WrapperConfig?,
        imports: MutableSet<String>
    ): WrapperConfig? {
        // If we have grid, use LazyVerticalGrid
        if (requirements.needsGridWrapper && requirements.gridConfig != null) {
            imports.add("androidx.compose.foundation.lazy.grid.LazyVerticalGrid")
            imports.add("androidx.compose.foundation.lazy.grid.GridCells")
            imports.add("androidx.compose.foundation.layout.Arrangement")
            val gridConfig = requirements.gridConfig
            return WrapperConfig.LazyVerticalGrid(
                columns = gridConfig.columns ?: 2
            )
        }

        // If we have scroll, wrap the flex/box in scroll
        if (requirements.needsScrollWrapper) {
            val scrollConfig = requirements.scrollConfig!!

            if (requirements.needsFlexWrapper) {
                val flexConfig = requirements.flexConfig!!
                return when (flexConfig.direction) {
                    LayoutWrapperType.ROW, LayoutWrapperType.ROW_REVERSE -> {
                        imports.add("androidx.compose.foundation.layout.Row")
                        imports.add("androidx.compose.foundation.horizontalScroll")
                        imports.add("androidx.compose.foundation.rememberScrollState")
                        if (scrollConfig.horizontal) {
                            WrapperConfig.ScrollableRow(smooth = scrollConfig.smooth)
                        } else {
                            buildFlexWrapper(flexConfig, imports)
                        }
                    }
                    LayoutWrapperType.COLUMN, LayoutWrapperType.COLUMN_REVERSE -> {
                        imports.add("androidx.compose.foundation.layout.Column")
                        imports.add("androidx.compose.foundation.verticalScroll")
                        imports.add("androidx.compose.foundation.rememberScrollState")
                        if (scrollConfig.vertical) {
                            WrapperConfig.ScrollableColumn(smooth = scrollConfig.smooth)
                        } else {
                            buildFlexWrapper(flexConfig, imports)
                        }
                    }
                    else -> buildFlexWrapper(flexConfig, imports)
                }
            } else {
                // Just scroll, no flex
                return if (scrollConfig.vertical && scrollConfig.horizontal) {
                    imports.add("androidx.compose.foundation.layout.Box")
                    imports.add("androidx.compose.foundation.verticalScroll")
                    imports.add("androidx.compose.foundation.horizontalScroll")
                    imports.add("androidx.compose.foundation.rememberScrollState")
                    WrapperConfig.Box()
                } else if (scrollConfig.vertical) {
                    imports.add("androidx.compose.foundation.layout.Column")
                    imports.add("androidx.compose.foundation.verticalScroll")
                    imports.add("androidx.compose.foundation.rememberScrollState")
                    WrapperConfig.ScrollableColumn(smooth = scrollConfig.smooth)
                } else {
                    imports.add("androidx.compose.foundation.layout.Row")
                    imports.add("androidx.compose.foundation.horizontalScroll")
                    imports.add("androidx.compose.foundation.rememberScrollState")
                    WrapperConfig.ScrollableRow(smooth = scrollConfig.smooth)
                }
            }
        }

        // If we have flex without scroll
        if (requirements.needsFlexWrapper) {
            return buildFlexWrapper(requirements.flexConfig!!, imports)
        }

        // Default to Box if we need a wrapper for other reasons
        if (requirements.needsStateManagement || requirements.needsSvgDrawing || requirements.needsFilters) {
            imports.add("androidx.compose.foundation.layout.Box")
            return WrapperConfig.Box()
        }

        return null
    }

    private fun buildFlexWrapper(flexConfig: FlexConfig, imports: MutableSet<String>): WrapperConfig {
        imports.add("androidx.compose.foundation.layout.Arrangement")
        imports.add("androidx.compose.ui.Alignment")

        return when (flexConfig.direction) {
            LayoutWrapperType.ROW -> {
                imports.add("androidx.compose.foundation.layout.Row")
                WrapperConfig.Row(
                    horizontalArrangement = mapJustifyContent(flexConfig.justifyContent, flexConfig.gap ?: flexConfig.columnGap),
                    verticalAlignment = mapAlignItems(flexConfig.alignItems, isVertical = false),
                    reverseLayout = false
                )
            }
            LayoutWrapperType.ROW_REVERSE -> {
                imports.add("androidx.compose.foundation.layout.Row")
                WrapperConfig.Row(
                    horizontalArrangement = mapJustifyContent(flexConfig.justifyContent, flexConfig.gap ?: flexConfig.columnGap),
                    verticalAlignment = mapAlignItems(flexConfig.alignItems, isVertical = false),
                    reverseLayout = true
                )
            }
            LayoutWrapperType.COLUMN -> {
                imports.add("androidx.compose.foundation.layout.Column")
                WrapperConfig.Column(
                    verticalArrangement = mapJustifyContent(flexConfig.justifyContent, flexConfig.gap ?: flexConfig.rowGap),
                    horizontalAlignment = mapAlignItems(flexConfig.alignItems, isVertical = true),
                    reverseLayout = false
                )
            }
            LayoutWrapperType.COLUMN_REVERSE -> {
                imports.add("androidx.compose.foundation.layout.Column")
                WrapperConfig.Column(
                    verticalArrangement = mapJustifyContent(flexConfig.justifyContent, flexConfig.gap ?: flexConfig.rowGap),
                    horizontalAlignment = mapAlignItems(flexConfig.alignItems, isVertical = true),
                    reverseLayout = true
                )
            }
            LayoutWrapperType.FLOW_ROW -> {
                imports.add("androidx.compose.foundation.layout.FlowRow")
                WrapperConfig.FlowRow(
                    horizontalArrangement = mapJustifyContent(flexConfig.justifyContent, flexConfig.gap ?: flexConfig.columnGap),
                    verticalArrangement = mapJustifyContent(flexConfig.alignContent, flexConfig.gap ?: flexConfig.rowGap)
                )
            }
            LayoutWrapperType.FLOW_COLUMN -> {
                imports.add("androidx.compose.foundation.layout.FlowColumn")
                WrapperConfig.FlowColumn(
                    horizontalArrangement = mapJustifyContent(flexConfig.alignContent, flexConfig.gap ?: flexConfig.columnGap),
                    verticalArrangement = mapJustifyContent(flexConfig.justifyContent, flexConfig.gap ?: flexConfig.rowGap)
                )
            }
            else -> {
                imports.add("androidx.compose.foundation.layout.Box")
                WrapperConfig.Box()
            }
        }
    }

    // ============================================
    // State and Animation Setup
    // ============================================

    private fun buildStateSetup(
        requirements: ComponentRequirements,
        imports: MutableSet<String>
    ): List<StateSetup> {
        val setups = mutableListOf<StateSetup>()

        if (requirements.needsStateManagement) {
            val stateReq = requirements.stateRequirements!!

            if (stateReq.needsHoverState) {
                imports.add("androidx.compose.runtime.remember")
                imports.add("androidx.compose.runtime.mutableStateOf")
                imports.add("androidx.compose.runtime.getValue")
                imports.add("androidx.compose.runtime.setValue")
                setups.add(
                    StateSetup(
                        variableName = "isHovered",
                        type = "Boolean",
                        initialValue = "false",
                        mutable = true
                    )
                )
            }

            if (stateReq.needsActiveState) {
                imports.add("androidx.compose.runtime.remember")
                imports.add("androidx.compose.runtime.mutableStateOf")
                imports.add("androidx.compose.runtime.getValue")
                imports.add("androidx.compose.runtime.setValue")
                setups.add(
                    StateSetup(
                        variableName = "isPressed",
                        type = "Boolean",
                        initialValue = "false",
                        mutable = true
                    )
                )
            }

            if (stateReq.needsFocusState) {
                imports.add("androidx.compose.runtime.remember")
                imports.add("androidx.compose.runtime.mutableStateOf")
                imports.add("androidx.compose.runtime.getValue")
                imports.add("androidx.compose.runtime.setValue")
                setups.add(
                    StateSetup(
                        variableName = "isFocused",
                        type = "Boolean",
                        initialValue = "false",
                        mutable = true
                    )
                )
            }
        }

        return setups
    }

    private fun buildAnimationSetup(
        requirements: ComponentRequirements,
        imports: MutableSet<String>
    ): List<AnimationSetup> {
        val animations = mutableListOf<AnimationSetup>()

        if (requirements.needsStateManagement && requirements.stateRequirements != null) {
            val transitions = requirements.stateRequirements.transitions

            transitions.forEach { transition ->
                imports.add("androidx.compose.animation.core.animateFloatAsState")
                imports.add("androidx.compose.animation.core.tween")
                imports.add("androidx.compose.runtime.getValue")

                val animSpec = buildAnimationSpec(
                    duration = transition.duration,
                    timingFunction = transition.timingFunction
                )

                animations.add(
                    AnimationSetup(
                        variableName = "animatedAlpha",  // Example - would need to be determined by transition.property
                        targetValue = "if (isHovered) 1f else 0.5f",
                        animationSpec = animSpec,
                        type = "Float"
                    )
                )
            }
        }

        return animations
    }

    private fun buildAnimationSpec(duration: Int?, timingFunction: String?): String {
        val durationMs = duration ?: 300

        val easing = when (timingFunction) {
            "linear" -> "LinearEasing"
            "ease-in" -> "FastOutLinearInEasing"
            "ease-out" -> "LinearOutSlowInEasing"
            "ease-in-out" -> "FastOutSlowInEasing"
            else -> "FastOutSlowInEasing"
        }

        return "tween(durationMillis = $durationMs, easing = $easing)"
    }

    /**
     * Builds state-dependent modifiers from selector states
     */
    private fun buildStateDependentModifiers(
        requirements: ComponentRequirements,
        imports: MutableSet<String>
    ): List<StateDependentModifiers> {
        val stateMods = mutableListOf<StateDependentModifiers>()

        if (requirements.needsStateManagement && requirements.stateRequirements != null) {
            val selectorStates = requirements.stateRequirements.selectorStates

            // Add focus imports if we have focus states
            if (selectorStates.any { it.type == SelectorType.FOCUS }) {
                imports.add("androidx.compose.ui.focus.onFocusChanged")
                imports.add("androidx.compose.foundation.focusable")
                imports.add("androidx.compose.runtime.remember")
                imports.add("androidx.compose.runtime.mutableStateOf")
                imports.add("androidx.compose.runtime.getValue")
                imports.add("androidx.compose.runtime.setValue")
            }

            // Add pointer input imports if we have hover or active states
            if (selectorStates.any { it.type in listOf(SelectorType.HOVER, SelectorType.ACTIVE) }) {
                imports.add("androidx.compose.ui.input.pointer.pointerInput")
                imports.add("androidx.compose.ui.input.pointer.PointerEventType")
                imports.add("androidx.compose.foundation.gestures.awaitPointerEventScope")
                imports.add("androidx.compose.foundation.gestures.awaitPointerEvent")
                imports.add("androidx.compose.runtime.remember")
                imports.add("androidx.compose.runtime.mutableStateOf")
                imports.add("androidx.compose.runtime.getValue")
                imports.add("androidx.compose.runtime.setValue")
            }

            for (selectorState in selectorStates) {
                val modifiers = mutableListOf<String>()

                // Convert selector properties to modifiers
                for (prop in selectorState.properties) {
                    val modifier = propertyToModifier(prop, imports)
                    if (modifier != null) {
                        modifiers.add(modifier)
                    }
                }

                // Deduplicate modifiers (e.g., multiple border-*-color all create border())
                val uniqueModifiers = modifiers.distinct()

                if (uniqueModifiers.isNotEmpty()) {
                    stateMods.add(
                        StateDependentModifiers(
                            stateType = selectorState.type,
                            modifiers = uniqueModifiers
                        )
                    )
                }
            }
        }

        return stateMods
    }

    /**
     * Builds responsive modifiers from media queries
     */
    private fun buildResponsiveModifiers(
        requirements: ComponentRequirements,
        imports: MutableSet<String>
    ): List<ResponsiveModifiers> {
        val responsiveMods = mutableListOf<ResponsiveModifiers>()

        if (requirements.needsResponsive) {
            // Add BoxWithConstraints import
            imports.add("androidx.compose.foundation.layout.BoxWithConstraints")
            imports.add("androidx.compose.ui.unit.dp")

            for (mediaQuery in requirements.mediaQueries) {
                val modifiers = mutableListOf<String>()

                // Convert media query properties to modifiers
                for (prop in mediaQuery.properties) {
                    val modifier = propertyToModifier(prop, imports)
                    if (modifier != null) {
                        modifiers.add(modifier)
                    }
                }

                // Deduplicate modifiers
                val uniqueModifiers = modifiers.distinct()

                if (uniqueModifiers.isNotEmpty()) {
                    responsiveMods.add(
                        ResponsiveModifiers(
                            minWidth = mediaQuery.minWidth,
                            maxWidth = mediaQuery.maxWidth,
                            modifiers = uniqueModifiers
                        )
                    )
                }
            }
        }

        return responsiveMods
    }

    /**
     * Converts a single IR property to a Compose modifier string
     */
    private fun propertyToModifier(prop: IRProperty, imports: MutableSet<String>): String? {
        return when (prop.propertyName) {
            "opacity" -> {
                imports.add("androidx.compose.ui.draw.alpha")
                prop.lengths?.firstOrNull()?.value?.let { "alpha(${it}f)" }
            }
            "background-color" -> {
                imports.add("androidx.compose.foundation.background")
                imports.add("androidx.compose.ui.graphics.Color")
                prop.colors?.firstOrNull()?.raw?.let { color ->
                    val hex = color.removePrefix("#")
                    "background(Color(0xFF$hex))"
                }
            }
            "background", "background-image" -> {
                // Check if it's a gradient
                if (prop.raw?.contains("linear-gradient") == true || prop.raw?.contains("radial-gradient") == true) {
                    imports.add("androidx.compose.foundation.background")
                    imports.add("androidx.compose.ui.graphics.Brush")
                    imports.add("androidx.compose.ui.graphics.Color")
                    imports.add("androidx.compose.ui.geometry.Offset")
                    parseGradient(prop.raw)
                } else {
                    // Regular background color
                    imports.add("androidx.compose.foundation.background")
                    imports.add("androidx.compose.ui.graphics.Color")
                    prop.colors?.firstOrNull()?.raw?.let { color ->
                        val hex = color.removePrefix("#")
                        "background(Color(0xFF$hex))"
                    }
                }
            }
            "transform" -> {
                // Handle transform: scale(0.98)
                if (prop.raw?.contains("scale") == true) {
                    imports.add("androidx.compose.ui.draw.scale")
                    val scaleMatch = Regex("scale\\(([0-9.]+)\\)").find(prop.raw)
                    scaleMatch?.groupValues?.get(1)?.let { "scale(${it}f)" }
                } else null
            }
            "box-shadow" -> {
                imports.add("androidx.compose.ui.draw.shadow")
                imports.add("androidx.compose.ui.unit.dp")
                prop.shadows?.firstOrNull()?.let { shadow ->
                    val blur = shadow.blur?.value ?: 0.0
                    "shadow(${blur}.dp)"
                }
            }
            "border-color", "border-top-color", "border-right-color",
            "border-bottom-color", "border-left-color" -> {
                imports.add("androidx.compose.foundation.border")
                imports.add("androidx.compose.ui.graphics.Color")
                imports.add("androidx.compose.ui.unit.dp")
                prop.colors?.firstOrNull()?.raw?.let { color ->
                    val hex = color.removePrefix("#")
                    "border(1.dp, Color(0xFF$hex))"
                }
            }
            else -> null
        }
    }

    /**
     * Parses CSS gradient syntax and converts to Compose Brush
     */
    private fun parseGradient(gradientStr: String): String? {
        // Parse linear-gradient
        val linearMatch = Regex("""linear-gradient\((.*?)\)""").find(gradientStr)
        if (linearMatch != null) {
            val content = linearMatch.groupValues[1]
            val parts = content.split(",").map { it.trim() }

            // Extract direction (default to bottom)
            val direction = if (parts.firstOrNull()?.contains("deg") == true ||
                              parts.firstOrNull()?.let { it in listOf("to top", "to bottom", "to left", "to right") } == true) {
                parts.first()
            } else null

            // Extract colors
            val colorParts = if (direction != null) parts.drop(1) else parts
            val colors = colorParts.mapNotNull { part ->
                val colorMatch = Regex("""#[0-9A-Fa-f]{6}""").find(part)
                colorMatch?.value?.let { "Color(0xFF${it.removePrefix("#")})" }
            }

            if (colors.size >= 2) {
                // Determine gradient direction
                val (startOffset, endOffset) = when (direction) {
                    "to top", "0deg" -> "Offset(0f, Float.POSITIVE_INFINITY)" to "Offset(0f, 0f)"
                    "to bottom", "180deg", null -> "Offset(0f, 0f)" to "Offset(0f, Float.POSITIVE_INFINITY)"
                    "to left", "270deg" -> "Offset(Float.POSITIVE_INFINITY, 0f)" to "Offset(0f, 0f)"
                    "to right", "90deg" -> "Offset(0f, 0f)" to "Offset(Float.POSITIVE_INFINITY, 0f)"
                    else -> "Offset(0f, 0f)" to "Offset(0f, Float.POSITIVE_INFINITY)"
                }

                return "background(Brush.linearGradient(colors = listOf(${colors.joinToString(", ")}), start = $startOffset, end = $endOffset))"
            }
        }

        // Parse radial-gradient
        val radialMatch = Regex("""radial-gradient\((.*?)\)""").find(gradientStr)
        if (radialMatch != null) {
            val content = radialMatch.groupValues[1]
            val colorParts = content.split(",").map { it.trim() }
            val colors = colorParts.mapNotNull { part ->
                val colorMatch = Regex("""#[0-9A-Fa-f]{6}""").find(part)
                colorMatch?.value?.let { "Color(0xFF${it.removePrefix("#")})" }
            }

            if (colors.size >= 2) {
                return "background(Brush.radialGradient(colors = listOf(${colors.joinToString(", ")})))"
            }
        }

        return null
    }

    // ============================================
    // Text Configuration
    // ============================================

    private fun buildTextConfig(textReq: TextRequirements, imports: MutableSet<String>): TextConfig {
        imports.add("androidx.compose.material3.Text")
        imports.add("androidx.compose.ui.text.font.FontWeight")
        imports.add("androidx.compose.ui.text.font.FontStyle")
        imports.add("androidx.compose.ui.text.style.TextAlign")
        imports.add("androidx.compose.ui.text.style.TextDecoration")
        imports.add("androidx.compose.ui.text.style.TextOverflow")
        imports.add("androidx.compose.ui.unit.sp")
        imports.add("androidx.compose.ui.graphics.Color")

        return TextConfig(
            fontSize = textReq.fontSize?.let { "${it}.sp" },
            fontWeight = mapFontWeight(textReq.fontWeight),
            fontStyle = mapFontStyle(textReq.fontStyle),
            color = textReq.color?.let { "Color(0xFF${it.removePrefix("#")})" },
            textAlign = mapTextAlign(textReq.textAlign),
            textDecoration = mapTextDecoration(textReq.textDecoration),
            lineHeight = textReq.lineHeight?.let { "${it}.sp" },
            letterSpacing = textReq.letterSpacing?.let { "${it}.sp" },
            maxLines = textReq.maxLines,
            overflow = if (textReq.textOverflow == "ellipsis") "TextOverflow.Ellipsis" else null
        )
    }

    // ============================================
    // SVG Drawing
    // ============================================

    private fun buildSvgDrawingCode(svgReq: SvgRequirements, imports: MutableSet<String>): String {
        imports.add("androidx.compose.foundation.Canvas")
        imports.add("androidx.compose.ui.graphics.Color")
        imports.add("androidx.compose.ui.graphics.drawscope.Stroke")
        imports.add("androidx.compose.ui.geometry.Offset")

        return when (svgReq.shapeType) {
            SvgShape.CIRCLE -> buildCircleDrawing(svgReq)
            SvgShape.RECT -> buildRectDrawing(svgReq)
            SvgShape.ELLIPSE -> buildEllipseDrawing(svgReq)
            else -> "// SVG drawing not yet implemented for ${svgReq.shapeType}"
        }
    }

    private fun buildCircleDrawing(svg: SvgRequirements): String {
        val cx = svg.cx ?: 0.0
        val cy = svg.cy ?: 0.0
        val r = svg.r ?: 50.0
        val fill = svg.fill ?: "#000000"
        val stroke = svg.stroke
        val strokeWidth = svg.strokeWidth ?: 1.0

        return """
Canvas(modifier = modifier) {
    drawCircle(
        color = Color(0xFF${fill.removePrefix("#")}),
        radius = ${r}f.dp.toPx(),
        center = Offset(${cx}f.dp.toPx(), ${cy}f.dp.toPx())
    )
    ${if (stroke != null) """
    drawCircle(
        color = Color(0xFF${stroke.removePrefix("#")}),
        radius = ${r}f.dp.toPx(),
        center = Offset(${cx}f.dp.toPx(), ${cy}f.dp.toPx()),
        style = Stroke(width = ${strokeWidth}f.dp.toPx())
    )
    """ else ""}
}
        """.trimIndent()
    }

    private fun buildRectDrawing(svg: SvgRequirements): String {
        return "// Rectangle drawing not yet implemented"
    }

    private fun buildEllipseDrawing(svg: SvgRequirements): String {
        return "// Ellipse drawing not yet implemented"
    }

    // ============================================
    // Additional Parameters
    // ============================================

    private fun buildAdditionalParameters(requirements: ComponentRequirements): List<ComponentParameter> {
        val params = mutableListOf<ComponentParameter>()

        if (requirements.needsStateManagement) {
            requirements.stateRequirements?.animations?.forEach { anim ->
                if (anim.duration != null) {
                    params.add(
                        ComponentParameter(
                            name = "animationDuration",
                            type = "Int",
                            defaultValue = anim.duration.toString()
                        )
                    )
                }
            }
        }

        return params
    }

    // ============================================
    // Helper Mappers
    // ============================================

    private fun calculateAlignment(config: PositionConfig): String {
        val vertical = when {
            config.top != null -> "Top"
            config.bottom != null -> "Bottom"
            else -> "Center"
        }

        val horizontal = when {
            config.left != null -> "Start"
            config.right != null -> "End"
            else -> "Center"
        }

        return "Alignment.$vertical$horizontal"
    }

    private fun mapJustifyContent(value: String?, gap: Double?): String? {
        val base = when (value) {
            "flex-start", "start" -> "Arrangement.Start"
            "flex-end", "end" -> "Arrangement.End"
            "center" -> "Arrangement.Center"
            "space-between" -> "Arrangement.SpaceBetween"
            "space-around" -> "Arrangement.SpaceAround"
            "space-evenly" -> "Arrangement.SpaceEvenly"
            else -> null
        }

        return if (gap != null && base != null) {
            "$base with Arrangement.spacedBy(${gap}.dp)"
        } else if (gap != null) {
            "Arrangement.spacedBy(${gap}.dp)"
        } else {
            base
        }
    }

    private fun mapAlignItems(value: String?, isVertical: Boolean): String? {
        return when (value) {
            "flex-start", "start" -> if (isVertical) "Alignment.Start" else "Alignment.Top"
            "flex-end", "end" -> if (isVertical) "Alignment.End" else "Alignment.Bottom"
            "center" -> if (isVertical) "Alignment.CenterHorizontally" else "Alignment.CenterVertically"
            "stretch" -> null  // Default behavior
            else -> null
        }
    }

    private fun mapFontWeight(value: String?): String? {
        return when (value) {
            "bold", "700" -> "FontWeight.Bold"
            "normal", "400" -> "FontWeight.Normal"
            "100" -> "FontWeight.Thin"
            "200" -> "FontWeight.ExtraLight"
            "300" -> "FontWeight.Light"
            "500" -> "FontWeight.Medium"
            "600" -> "FontWeight.SemiBold"
            "800" -> "FontWeight.ExtraBold"
            "900" -> "FontWeight.Black"
            else -> null
        }
    }

    private fun mapFontStyle(value: String?): String? {
        return when (value) {
            "italic" -> "FontStyle.Italic"
            "normal" -> "FontStyle.Normal"
            else -> null
        }
    }

    private fun mapTextAlign(value: String?): String? {
        return when (value) {
            "left", "start" -> "TextAlign.Start"
            "right", "end" -> "TextAlign.End"
            "center" -> "TextAlign.Center"
            "justify" -> "TextAlign.Justify"
            else -> null
        }
    }

    private fun mapTextDecoration(value: String?): String? {
        return when (value) {
            "underline" -> "TextDecoration.Underline"
            "line-through" -> "TextDecoration.LineThrough"
            "none" -> "TextDecoration.None"
            else -> null
        }
    }
}
