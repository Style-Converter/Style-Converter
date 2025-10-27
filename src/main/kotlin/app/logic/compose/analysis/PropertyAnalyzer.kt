package app.logic.compose.analysis

import app.IRProperty
import app.IRKeyword
import app.IRSelector

/**
 * Types of layout wrappers that may be needed
 */
enum class LayoutWrapperType {
    ROW,                    // display: flex; flex-direction: row
    COLUMN,                 // display: flex; flex-direction: column
    ROW_REVERSE,            // flex-direction: row-reverse
    COLUMN_REVERSE,         // flex-direction: column-reverse
    FLOW_ROW,               // flex-wrap: wrap with row
    FLOW_COLUMN,            // flex-wrap: wrap with column
    BOX,                    // Default container
    LAZY_COLUMN,            // For large scrollable lists
    LAZY_ROW                // For large scrollable horizontal lists
}

/**
 * Types of positioning
 */
enum class PositionType {
    ABSOLUTE,   // position: absolute
    FIXED,      // position: fixed
    RELATIVE,   // position: relative
    STICKY      // position: sticky
}

/**
 * Scroll behavior
 */
data class ScrollConfig(
    val vertical: Boolean = false,
    val horizontal: Boolean = false,
    val smooth: Boolean = false
)

/**
 * Flexbox/Grid configuration
 */
data class FlexConfig(
    val direction: LayoutWrapperType = LayoutWrapperType.ROW,
    val justifyContent: String? = null,        // "start", "center", "space-between", etc.
    val alignItems: String? = null,            // "start", "center", "end", "stretch"
    val alignContent: String? = null,          // For multi-line flex
    val gap: Double? = null,
    val rowGap: Double? = null,
    val columnGap: Double? = null,
    val wrap: Boolean = false
)

/**
 * CSS Grid configuration
 */
data class GridConfig(
    val templateColumns: String? = null,      // "repeat(3, 1fr)", "100px 200px", etc.
    val templateRows: String? = null,
    val columnGap: Double? = null,
    val rowGap: Double? = null,
    val gap: Double? = null,
    val autoFlow: String? = null,             // "row", "column", "dense"
    val columns: Int? = null,                 // Number of columns (parsed from template)
    val rows: Int? = null                     // Number of rows (parsed from template)
)

/**
 * Position configuration
 */
data class PositionConfig(
    val type: PositionType,
    val top: Double? = null,
    val right: Double? = null,
    val bottom: Double? = null,
    val left: Double? = null,
    val zIndex: Float? = null
)

/**
 * Animation configuration
 */
data class AnimationConfig(
    val name: String? = null,
    val duration: Int? = null,              // milliseconds
    val timingFunction: String? = null,     // "ease", "linear", "ease-in-out"
    val delay: Int? = null,
    val iterationCount: String? = null,     // "infinite" or number
    val direction: String? = null,          // "normal", "reverse", "alternate"
    val fillMode: String? = null
)

/**
 * Transition configuration
 */
data class TransitionConfig(
    val property: String = "all",
    val duration: Int? = null,
    val timingFunction: String? = null,
    val delay: Int? = null
)

/**
 * Selector state configuration (hover, active, focus)
 */
data class SelectorState(
    val type: SelectorType,
    val properties: List<IRProperty>  // Properties to apply in this state
)

enum class SelectorType {
    HOVER,    // :hover
    ACTIVE,   // :active (pressed)
    FOCUS,    // :focus
    DISABLED  // :disabled
}

/**
 * State requirements
 */
data class StateRequirements(
    val animations: List<AnimationConfig> = emptyList(),
    val transitions: List<TransitionConfig> = emptyList(),
    val selectorStates: List<SelectorState> = emptyList(),  // States from :hover, :active, :focus
    val needsHoverState: Boolean = false,
    val needsActiveState: Boolean = false,
    val needsFocusState: Boolean = false,
    val needsClickable: Boolean = false
)

/**
 * Media query/responsive requirements
 */
data class MediaQueryState(
    val query: String,           // Original media query string
    val minWidth: Int? = null,   // Minimum width in dp
    val maxWidth: Int? = null,   // Maximum width in dp
    val properties: List<IRProperty>  // Properties to apply when query matches
)

/**
 * SVG drawing requirements
 */
data class SvgRequirements(
    val shapeType: SvgShape? = null,
    val cx: Double? = null,
    val cy: Double? = null,
    val r: Double? = null,
    val rx: Double? = null,
    val ry: Double? = null,
    val x: Double? = null,
    val y: Double? = null,
    val width: Double? = null,
    val height: Double? = null,
    val pathData: String? = null,
    val fill: String? = null,
    val stroke: String? = null,
    val strokeWidth: Double? = null,
    val strokeDasharray: String? = null,
    val strokeLinecap: String? = null,
    val strokeLinejoin: String? = null
)

enum class SvgShape {
    CIRCLE, RECT, ELLIPSE, PATH, LINE, POLYGON
}

/**
 * Filter/effect requirements
 */
data class FilterRequirements(
    val blur: Double? = null,
    val brightness: Double? = null,
    val contrast: Double? = null,
    val saturate: Double? = null,
    val blendMode: String? = null,
    val backdropBlur: Double? = null
)

/**
 * Text styling requirements
 */
data class TextRequirements(
    val fontSize: Double? = null,
    val fontWeight: String? = null,
    val fontStyle: String? = null,
    val fontFamily: String? = null,
    val color: String? = null,
    val textAlign: String? = null,
    val textDecoration: String? = null,
    val textTransform: String? = null,
    val lineHeight: Double? = null,
    val letterSpacing: Double? = null,
    val whiteSpace: String? = null,
    val textOverflow: String? = null,
    val maxLines: Int? = null
)

/**
 * Complete component requirements analysis
 */
data class ComponentRequirements(
    val needsFlexWrapper: Boolean = false,
    val flexConfig: FlexConfig? = null,
    val needsGridWrapper: Boolean = false,
    val gridConfig: GridConfig? = null,
    val needsPositionWrapper: Boolean = false,
    val positionConfig: PositionConfig? = null,
    val needsScrollWrapper: Boolean = false,
    val scrollConfig: ScrollConfig? = null,
    val needsStateManagement: Boolean = false,
    val stateRequirements: StateRequirements? = null,
    val needsSvgDrawing: Boolean = false,
    val svgRequirements: SvgRequirements? = null,
    val needsFilters: Boolean = false,
    val filterRequirements: FilterRequirements? = null,
    val needsTextComponent: Boolean = false,
    val textRequirements: TextRequirements? = null,
    val needsResponsive: Boolean = false,
    val mediaQueries: List<MediaQueryState> = emptyList()
)

/**
 * Analyzes CSS properties to determine what Compose components and wrappers are needed
 */
class PropertyAnalyzer {

    fun analyze(
        properties: List<IRProperty>,
        selectors: List<IRSelector> = emptyList(),
        mediaQueries: List<app.IRMedia> = emptyList()
    ): ComponentRequirements {
        val propMap = properties.associateBy { it.propertyName }
        val mediaQueryStates = parseMediaQueries(mediaQueries)

        return ComponentRequirements(
            needsFlexWrapper = needsFlexWrapper(propMap) && !needsGridWrapper(propMap),
            flexConfig = if (needsFlexWrapper(propMap) && !needsGridWrapper(propMap)) analyzeFlexConfig(propMap) else null,
            needsGridWrapper = needsGridWrapper(propMap),
            gridConfig = if (needsGridWrapper(propMap)) analyzeGridConfig(propMap) else null,
            needsPositionWrapper = needsPositionWrapper(propMap),
            positionConfig = if (needsPositionWrapper(propMap)) analyzePositionConfig(propMap) else null,
            needsScrollWrapper = needsScrollWrapper(propMap),
            scrollConfig = if (needsScrollWrapper(propMap)) analyzeScrollConfig(propMap) else null,
            needsStateManagement = needsStateManagement(propMap) || selectors.isNotEmpty() || mediaQueryStates.isNotEmpty(),
            stateRequirements = if (needsStateManagement(propMap) || selectors.isNotEmpty())
                analyzeStateRequirements(propMap, selectors) else null,
            needsSvgDrawing = needsSvgDrawing(propMap),
            svgRequirements = if (needsSvgDrawing(propMap)) analyzeSvgRequirements(propMap) else null,
            needsFilters = needsFilters(propMap),
            filterRequirements = if (needsFilters(propMap)) analyzeFilterRequirements(propMap) else null,
            needsTextComponent = needsTextComponent(propMap),
            textRequirements = if (needsTextComponent(propMap)) analyzeTextRequirements(propMap) else null,
            needsResponsive = mediaQueryStates.isNotEmpty(),
            mediaQueries = mediaQueryStates
        )
    }

    // ============================================
    // Flexbox Detection
    // ============================================

    private fun needsFlexWrapper(props: Map<String, IRProperty>): Boolean {
        return props["display"]?.keywords?.any { it.value == "flex" } == true ||
               props.keys.any { it.startsWith("flex-") } ||
               props.containsKey("justify-content") ||
               props.containsKey("align-items") ||
               props.containsKey("align-content") ||
               props.containsKey("gap") ||
               props.containsKey("row-gap") ||
               props.containsKey("column-gap")
    }

    private fun analyzeFlexConfig(props: Map<String, IRProperty>): FlexConfig {
        val direction = getFlexDirection(props)
        val wrap = props["flex-wrap"]?.keywords?.any { it.value == "wrap" } == true

        return FlexConfig(
            direction = if (wrap) {
                if (direction == LayoutWrapperType.ROW || direction == LayoutWrapperType.ROW_REVERSE)
                    LayoutWrapperType.FLOW_ROW
                else
                    LayoutWrapperType.FLOW_COLUMN
            } else {
                direction
            },
            justifyContent = props["justify-content"]?.keywords?.firstOrNull()?.value,
            alignItems = props["align-items"]?.keywords?.firstOrNull()?.value,
            alignContent = props["align-content"]?.keywords?.firstOrNull()?.value,
            gap = props["gap"]?.lengths?.firstOrNull()?.value,
            rowGap = props["row-gap"]?.lengths?.firstOrNull()?.value,
            columnGap = props["column-gap"]?.lengths?.firstOrNull()?.value,
            wrap = wrap
        )
    }

    private fun getFlexDirection(props: Map<String, IRProperty>): LayoutWrapperType {
        val direction = props["flex-direction"]?.keywords?.firstOrNull()?.value
        return when (direction) {
            "row", null -> LayoutWrapperType.ROW
            "row-reverse" -> LayoutWrapperType.ROW_REVERSE
            "column" -> LayoutWrapperType.COLUMN
            "column-reverse" -> LayoutWrapperType.COLUMN_REVERSE
            else -> LayoutWrapperType.ROW
        }
    }

    // ============================================
    // Grid Detection
    // ============================================

    private fun needsGridWrapper(props: Map<String, IRProperty>): Boolean {
        return props["display"]?.keywords?.any { it.value == "grid" } == true ||
               props.keys.any { it.startsWith("grid-") }
    }

    private fun analyzeGridConfig(props: Map<String, IRProperty>): GridConfig {
        val templateColumns = props["grid-template-columns"]?.raw
        val templateRows = props["grid-template-rows"]?.raw

        // Try to parse number of columns from template
        val columns = templateColumns?.let { template ->
            when {
                template.contains("repeat") -> {
                    val repeatMatch = Regex("""repeat\((\d+),""").find(template)
                    repeatMatch?.groupValues?.get(1)?.toIntOrNull()
                }
                else -> template.split(" ").size
            }
        }

        return GridConfig(
            templateColumns = templateColumns,
            templateRows = templateRows,
            columnGap = props["column-gap"]?.lengths?.firstOrNull()?.value
                ?: props["grid-column-gap"]?.lengths?.firstOrNull()?.value,
            rowGap = props["row-gap"]?.lengths?.firstOrNull()?.value
                ?: props["grid-row-gap"]?.lengths?.firstOrNull()?.value,
            gap = props["gap"]?.lengths?.firstOrNull()?.value
                ?: props["grid-gap"]?.lengths?.firstOrNull()?.value,
            autoFlow = props["grid-auto-flow"]?.keywords?.firstOrNull()?.value,
            columns = columns
        )
    }

    // ============================================
    // Position Detection
    // ============================================

    private fun needsPositionWrapper(props: Map<String, IRProperty>): Boolean {
        val position = props["position"]?.keywords?.firstOrNull()?.value
        return position in listOf("absolute", "fixed", "sticky") ||
               (position == "relative" && props.keys.any { it in listOf("top", "left", "right", "bottom") })
    }

    private fun analyzePositionConfig(props: Map<String, IRProperty>): PositionConfig {
        val positionType = when (props["position"]?.keywords?.firstOrNull()?.value) {
            "absolute" -> PositionType.ABSOLUTE
            "fixed" -> PositionType.FIXED
            "sticky" -> PositionType.STICKY
            else -> PositionType.RELATIVE
        }

        return PositionConfig(
            type = positionType,
            top = props["top"]?.lengths?.firstOrNull()?.value,
            right = props["right"]?.lengths?.firstOrNull()?.value,
            bottom = props["bottom"]?.lengths?.firstOrNull()?.value,
            left = props["left"]?.lengths?.firstOrNull()?.value,
            zIndex = props["z-index"]?.raw?.toFloatOrNull()
        )
    }

    // ============================================
    // Scroll Detection
    // ============================================

    private fun needsScrollWrapper(props: Map<String, IRProperty>): Boolean {
        val overflow = props["overflow"]?.keywords?.firstOrNull()?.value
        val overflowX = props["overflow-x"]?.keywords?.firstOrNull()?.value
        val overflowY = props["overflow-y"]?.keywords?.firstOrNull()?.value

        return overflow in listOf("scroll", "auto") ||
               overflowX in listOf("scroll", "auto") ||
               overflowY in listOf("scroll", "auto")
    }

    private fun analyzeScrollConfig(props: Map<String, IRProperty>): ScrollConfig {
        val overflow = props["overflow"]?.keywords?.firstOrNull()?.value
        val overflowX = props["overflow-x"]?.keywords?.firstOrNull()?.value
        val overflowY = props["overflow-y"]?.keywords?.firstOrNull()?.value
        val scrollBehavior = props["scroll-behavior"]?.keywords?.firstOrNull()?.value

        return ScrollConfig(
            vertical = overflow in listOf("scroll", "auto") || overflowY in listOf("scroll", "auto"),
            horizontal = overflow in listOf("scroll", "auto") || overflowX in listOf("scroll", "auto"),
            smooth = scrollBehavior == "smooth"
        )
    }

    // ============================================
    // State Management Detection
    // ============================================

    private fun needsStateManagement(props: Map<String, IRProperty>): Boolean {
        return props.keys.any { it.startsWith("animation-") || it == "animation" } ||
               props.keys.any { it.startsWith("transition-") || it == "transition" } ||
               props["cursor"]?.keywords?.any { it.value == "pointer" } == true
    }

    private fun analyzeStateRequirements(props: Map<String, IRProperty>, selectors: List<IRSelector>): StateRequirements {
        val animations = mutableListOf<AnimationConfig>()
        val transitions = mutableListOf<TransitionConfig>()
        val selectorStates = mutableListOf<SelectorState>()

        // Check for animation properties
        if (props.containsKey("animation") || props.keys.any { it.startsWith("animation-") }) {
            animations.add(
                AnimationConfig(
                    name = props["animation-name"]?.keywords?.firstOrNull()?.value,
                    duration = props["animation-duration"]?.raw?.removeSuffix("ms")?.toIntOrNull()
                        ?: props["animation-duration"]?.raw?.removeSuffix("s")?.toDoubleOrNull()?.times(1000)?.toInt(),
                    timingFunction = props["animation-timing-function"]?.keywords?.firstOrNull()?.value,
                    delay = props["animation-delay"]?.raw?.removeSuffix("ms")?.toIntOrNull(),
                    iterationCount = props["animation-iteration-count"]?.raw,
                    direction = props["animation-direction"]?.keywords?.firstOrNull()?.value,
                    fillMode = props["animation-fill-mode"]?.keywords?.firstOrNull()?.value
                )
            )
        }

        // Check for transition properties
        if (props.containsKey("transition") || props.keys.any { it.startsWith("transition-") }) {
            transitions.add(
                TransitionConfig(
                    property = props["transition-property"]?.keywords?.firstOrNull()?.value ?: "all",
                    duration = props["transition-duration"]?.raw?.removeSuffix("ms")?.toIntOrNull()
                        ?: props["transition-duration"]?.raw?.removeSuffix("s")?.toDoubleOrNull()?.times(1000)?.toInt(),
                    timingFunction = props["transition-timing-function"]?.keywords?.firstOrNull()?.value,
                    delay = props["transition-delay"]?.raw?.removeSuffix("ms")?.toIntOrNull()
                )
            )
        }

        // Analyze selectors
        for (selector in selectors) {
            val selectorType = when {
                selector.condition.contains("hover") -> SelectorType.HOVER
                selector.condition.contains("active") -> SelectorType.ACTIVE
                selector.condition.contains("focus") -> SelectorType.FOCUS
                selector.condition.contains("disabled") -> SelectorType.DISABLED
                else -> null
            }

            if (selectorType != null) {
                selectorStates.add(
                    SelectorState(
                        type = selectorType,
                        properties = selector.properties
                    )
                )
            }
        }

        return StateRequirements(
            animations = animations,
            transitions = transitions,
            selectorStates = selectorStates,
            needsHoverState = selectorStates.any { it.type == SelectorType.HOVER },
            needsActiveState = selectorStates.any { it.type == SelectorType.ACTIVE },
            needsFocusState = selectorStates.any { it.type == SelectorType.FOCUS },
            needsClickable = props["cursor"]?.keywords?.any { it.value == "pointer" } == true ||
                            selectorStates.any { it.type in listOf(SelectorType.HOVER, SelectorType.ACTIVE) }
        )
    }

    // ============================================
    // SVG Drawing Detection
    // ============================================

    private fun needsSvgDrawing(props: Map<String, IRProperty>): Boolean {
        return props.keys.any { it in listOf("cx", "cy", "r", "rx", "ry", "x", "y", "d") } ||
               (props.containsKey("fill") && props.containsKey("stroke"))
    }

    private fun analyzeSvgRequirements(props: Map<String, IRProperty>): SvgRequirements {
        val shapeType = when {
            props.containsKey("cx") && props.containsKey("cy") && props.containsKey("r") -> SvgShape.CIRCLE
            props.containsKey("cx") && props.containsKey("cy") && props.containsKey("rx") -> SvgShape.ELLIPSE
            props.containsKey("x") && props.containsKey("y") -> SvgShape.RECT
            props.containsKey("d") -> SvgShape.PATH
            else -> null
        }

        return SvgRequirements(
            shapeType = shapeType,
            cx = props["cx"]?.lengths?.firstOrNull()?.value,
            cy = props["cy"]?.lengths?.firstOrNull()?.value,
            r = props["r"]?.lengths?.firstOrNull()?.value,
            rx = props["rx"]?.lengths?.firstOrNull()?.value,
            ry = props["ry"]?.lengths?.firstOrNull()?.value,
            x = props["x"]?.lengths?.firstOrNull()?.value,
            y = props["y"]?.lengths?.firstOrNull()?.value,
            width = props["width"]?.lengths?.firstOrNull()?.value,
            height = props["height"]?.lengths?.firstOrNull()?.value,
            pathData = props["d"]?.raw,
            fill = props["fill"]?.colors?.firstOrNull()?.raw,
            stroke = props["stroke"]?.colors?.firstOrNull()?.raw,
            strokeWidth = props["stroke-width"]?.lengths?.firstOrNull()?.value,
            strokeDasharray = props["stroke-dasharray"]?.raw,
            strokeLinecap = props["stroke-linecap"]?.keywords?.firstOrNull()?.value,
            strokeLinejoin = props["stroke-linejoin"]?.keywords?.firstOrNull()?.value
        )
    }

    // ============================================
    // Filter Detection
    // ============================================

    private fun needsFilters(props: Map<String, IRProperty>): Boolean {
        return props.containsKey("filter") || props.containsKey("backdrop-filter") || props.containsKey("mix-blend-mode")
    }

    private fun analyzeFilterRequirements(props: Map<String, IRProperty>): FilterRequirements {
        // This is a simplified version - full filter parsing would be more complex
        return FilterRequirements(
            blendMode = props["mix-blend-mode"]?.keywords?.firstOrNull()?.value
        )
    }

    // ============================================
    // Text Component Detection
    // ============================================

    private fun needsTextComponent(props: Map<String, IRProperty>): Boolean {
        return props.keys.any {
            it in listOf(
                "font-size", "font-weight", "font-style", "font-family",
                "text-align", "text-decoration", "text-transform",
                "line-height", "letter-spacing", "text-overflow"
            )
        }
    }

    private fun analyzeTextRequirements(props: Map<String, IRProperty>): TextRequirements {
        return TextRequirements(
            fontSize = props["font-size"]?.lengths?.firstOrNull()?.value,
            fontWeight = props["font-weight"]?.keywords?.firstOrNull()?.value
                ?: props["font-weight"]?.raw,
            fontStyle = props["font-style"]?.keywords?.firstOrNull()?.value,
            fontFamily = props["font-family"]?.keywords?.firstOrNull()?.value
                ?: props["font-family"]?.raw,
            color = props["color"]?.colors?.firstOrNull()?.raw,
            textAlign = props["text-align"]?.keywords?.firstOrNull()?.value,
            textDecoration = props["text-decoration"]?.keywords?.firstOrNull()?.value,
            textTransform = props["text-transform"]?.keywords?.firstOrNull()?.value,
            lineHeight = props["line-height"]?.lengths?.firstOrNull()?.value,
            letterSpacing = props["letter-spacing"]?.lengths?.firstOrNull()?.value,
            whiteSpace = props["white-space"]?.keywords?.firstOrNull()?.value,
            textOverflow = props["text-overflow"]?.keywords?.firstOrNull()?.value,
            maxLines = if (props["white-space"]?.keywords?.any { it.value == "nowrap" } == true) 1 else null
        )
    }

    // ============================================
    // Media Query Parsing
    // ============================================

    private fun parseMediaQueries(mediaQueries: List<app.IRMedia>): List<MediaQueryState> {
        return mediaQueries.map { media ->
            val query = media.query
            // Parse min-width and max-width from query
            // Example: "(min-width: 768px)" or "(max-width: 1024px)"
            val minWidthMatch = Regex("""min-width:\s*(\d+)px""").find(query)
            val maxWidthMatch = Regex("""max-width:\s*(\d+)px""").find(query)

            MediaQueryState(
                query = query,
                minWidth = minWidthMatch?.groupValues?.get(1)?.toIntOrNull(),
                maxWidth = maxWidthMatch?.groupValues?.get(1)?.toIntOrNull(),
                properties = media.properties
            )
        }
    }
}
