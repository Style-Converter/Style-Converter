package app.logic.compose

import app.irmodels.IRDocument
import app.irmodels.IRComponent
import app.irmodels.IRProperty
import app.irmodels.IRSelector
import app.irmodels.IRMedia
import app.irmodels.IRLength
import app.irmodels.IRColor
import app.logic.compose.analysis.PropertyAnalyzer
import app.logic.compose.generation.ComponentStructureBuilder
import app.logic.compose.generation.CodeGenerator
import app.logic.compose.converters.PropertyValueExtractor
import app.logic.compose.converters.*

/**
 * Holds text styling properties for container components
 */
data class TextStyleProperties(
    val color: String? = null,
    val fontSize: String? = null,
    val fontWeight: String? = null,
    val textAlign: String? = null,
    val lineHeight: String? = null,
    val letterSpacing: String? = null
) {
    fun hasAnyProperty(): Boolean = color != null || fontSize != null || fontWeight != null ||
                                     textAlign != null || lineHeight != null || letterSpacing != null
}

/**
 * Simplified Compose builder that works directly with IR models
 */
object SimpleComposeBuilder {

    fun buildComponents(irDocument: IRDocument): ComposeDocument {
        val components = irDocument.components.map { buildComponent(it) }
        val imports = generateImports()

        return ComposeDocument(
            components = components,
            imports = imports
        )
    }

    private fun buildComponent(irComponent: IRComponent): ComposeComponent {
        val componentName = sanitizeName(irComponent.name)

        // NEW: Analyze if component needs advanced wrapper generation
        val analyzer = PropertyAnalyzer()
        val requirements = analyzer.analyze(irComponent.properties, irComponent.selectors, irComponent.media)

        // Check if we need wrapper generation (flex, grid, position, scroll, state management, media queries, etc.)
        val needsWrapperGeneration = requirements.needsFlexWrapper ||
                                     requirements.needsGridWrapper ||
                                     requirements.needsPositionWrapper ||
                                     requirements.needsScrollWrapper ||
                                     requirements.needsSvgDrawing ||
                                     requirements.needsStateManagement ||
                                     requirements.needsResponsive

        if (needsWrapperGeneration) {
            // Use NEW architecture for components that need wrappers or state management
            return buildComponentWithWrapper(irComponent, componentName, requirements)
        } else {
            // Keep existing simple modifier generation for simple components
            return buildComponentSimple(irComponent, componentName)
        }
    }

    /**
     * NEW: Build component using wrapper generation architecture
     */
    private fun buildComponentWithWrapper(
        irComponent: IRComponent,
        componentName: String,
        requirements: app.logic.compose.analysis.ComponentRequirements
    ): ComposeComponent {
        // Filter out properties that will be handled by wrappers
        val propsHandledByWrapper = getPropertiesHandledByWrapper(requirements)
        val remainingProps = irComponent.properties.filterNot { prop ->
            propsHandledByWrapper.contains(prop.propertyName)
        }

        // Build simple modifiers ONLY for properties not handled by wrappers
        val simpleModifiers = buildModifiers(remainingProps)

        // Build component structure with wrappers
        val structureBuilder = ComponentStructureBuilder()
        val structure = structureBuilder.build(componentName, requirements)

        // Generate code
        val codeGenerator = CodeGenerator()
        val composableCode = codeGenerator.generate(structure, simpleModifiers)

        // Build states and responsive (same as before)
        val states = irComponent.selectors.map { buildState(it) }
        val responsive = irComponent.media.map { buildResponsive(it) }

        return ComposeComponent(
            name = componentName,
            composableCode = composableCode,
            baseModifiers = simpleModifiers, // Keep for reference
            states = states,
            responsive = responsive
        )
    }

    /**
     * Returns property names that are handled by wrappers (flex, position, scroll, etc.)
     */
    private fun getPropertiesHandledByWrapper(requirements: app.logic.compose.analysis.ComponentRequirements): Set<String> {
        val handled = mutableSetOf<String>()

        // Flexbox properties
        if (requirements.needsFlexWrapper) {
            handled.addAll(listOf(
                "display", "flex-direction", "flex-wrap",
                "justify-content", "align-items", "align-content",
                "gap", "row-gap", "column-gap",
                "flex", "flex-grow", "flex-shrink", "flex-basis", "order",
                // Column layout properties (multi-column layout)
                "columns", "column-count", "column-fill", "column-rule",
                "column-rule-color", "column-rule-style", "column-rule-width",
                "column-span", "column-width"
            ))
        }

        // Position properties
        if (requirements.needsPositionWrapper) {
            handled.addAll(listOf(
                "position", "top", "right", "bottom", "left", "z-index"
            ))
        }

        // Scroll properties
        if (requirements.needsScrollWrapper) {
            handled.addAll(listOf(
                "overflow", "overflow-x", "overflow-y",
                "scroll-behavior",
                "scroll-margin", "scroll-margin-top", "scroll-margin-right", "scroll-margin-bottom", "scroll-margin-left",
                "scroll-padding", "scroll-padding-top", "scroll-padding-right", "scroll-padding-bottom", "scroll-padding-left",
                "scroll-snap-type", "scroll-snap-align",
                "overscroll-behavior", "overscroll-behavior-x", "overscroll-behavior-y"
            ))
        }

        // SVG properties
        if (requirements.needsSvgDrawing) {
            handled.addAll(listOf(
                "cx", "cy", "r", "rx", "ry", "x", "y", "d",
                "fill", "stroke", "stroke-width", "stroke-dasharray",
                "stroke-linecap", "stroke-linejoin"
            ))
        }

        // Text properties (should not appear as modifier comments in wrappers)
        // These are text styling properties, not layout modifiers
        handled.addAll(listOf(
            "color", "font-size", "font-weight", "font-family", "font-style",
            "text-align", "text-decoration", "text-transform",
            "line-height", "letter-spacing", "text-overflow", "white-space",
            "word-spacing", "text-indent", "text-shadow"
        ))

        return handled
    }

    /**
     * Build component using simple modifier generation (existing logic)
     */
    private fun buildComponentSimple(
        irComponent: IRComponent,
        componentName: String
    ): ComposeComponent {
        val baseModifiers = buildModifiers(irComponent.properties)
        val textProperties = extractTextProperties(irComponent.properties)
        val states = irComponent.selectors.map { buildState(it) }
        val responsive = irComponent.media.map { buildResponsive(it) }

        val composableCode = generateComposableCode(
            componentName,
            baseModifiers,
            states,
            responsive,
            textProperties
        )

        return ComposeComponent(
            name = componentName,
            composableCode = composableCode,
            baseModifiers = baseModifiers,
            states = states,
            responsive = responsive
        )
    }

    private fun sanitizeName(name: String): String {
        return name.split("-", "_", " ")
            .filter { it.isNotBlank() }
            .joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
    }

    /**
     * Extract text styling properties that should be applied to child Text composables
     */
    private fun extractTextProperties(properties: List<IRProperty>): TextStyleProperties {
        val color = properties.find { it.propertyName == "color" }
            ?.let { PropertyValueExtractor.getColor(it) }
            ?.let { convertColor(it) }
        val fontSize = properties.find { it.propertyName == "font-size" }
            ?.let { PropertyValueExtractor.getLength(it) }
            ?.let { convertLength(it, useSp = true) }
        val fontWeight = properties.find { it.propertyName == "font-weight" }
            ?.let { PropertyValueExtractor.getKeyword(it) }
        val textAlign = properties.find { it.propertyName == "text-align" }
            ?.let { PropertyValueExtractor.getKeyword(it) }
        val lineHeight = properties.find { it.propertyName == "line-height" }
            ?.let { PropertyValueExtractor.getLength(it) }
            ?.let { convertLength(it, useSp = true) }
        val letterSpacing = properties.find { it.propertyName == "letter-spacing" }
            ?.let { PropertyValueExtractor.getLength(it) }
            ?.let { convertLength(it, useSp = true) }

        return TextStyleProperties(
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = textAlign,
            lineHeight = lineHeight,
            letterSpacing = letterSpacing
        )
    }

    private fun buildModifiers(properties: List<IRProperty>): List<String> {
        val modifiers = mutableListOf<String>()

        // Collect border properties for combined border modifier
        val borderWidth = properties.find { it.propertyName == "border-width" }?.let { PropertyValueExtractor.getLength(it) }
        val borderColor = properties.find { it.propertyName == "border-color" }?.let { PropertyValueExtractor.getColor(it) }
        val borderStyle = properties.find { it.propertyName == "border-style" }?.let { PropertyValueExtractor.getKeyword(it) }

        // Individual border sides
        val borderTopWidth = properties.find { it.propertyName == "border-top-width" }?.let { PropertyValueExtractor.getLength(it) }
        val borderTopColor = properties.find { it.propertyName == "border-top-color" }?.let { PropertyValueExtractor.getColor(it) }
        val borderBottomWidth = properties.find { it.propertyName == "border-bottom-width" }?.let { PropertyValueExtractor.getLength(it) }
        val borderBottomColor = properties.find { it.propertyName == "border-bottom-color" }?.let { PropertyValueExtractor.getColor(it) }
        val borderLeftWidth = properties.find { it.propertyName == "border-left-width" }?.let { PropertyValueExtractor.getLength(it) }
        val borderLeftColor = properties.find { it.propertyName == "border-left-color" }?.let { PropertyValueExtractor.getColor(it) }
        val borderRightWidth = properties.find { it.propertyName == "border-right-width" }?.let { PropertyValueExtractor.getLength(it) }
        val borderRightColor = properties.find { it.propertyName == "border-right-color" }?.let { PropertyValueExtractor.getColor(it) }

        // Collect border radius properties for combined modifier
        val topLeftRadius = properties.find { it.propertyName == "border-top-left-radius" }?.let { PropertyValueExtractor.getLength(it) }
        val topRightRadius = properties.find { it.propertyName == "border-top-right-radius" }?.let { PropertyValueExtractor.getLength(it) }
        val bottomLeftRadius = properties.find { it.propertyName == "border-bottom-left-radius" }?.let { PropertyValueExtractor.getLength(it) }
        val bottomRightRadius = properties.find { it.propertyName == "border-bottom-right-radius" }?.let { PropertyValueExtractor.getLength(it) }
        val borderRadius = properties.find { it.propertyName == "border-radius" }?.let { PropertyValueExtractor.getLength(it) }

        // Collect padding properties for combined modifier
        val paddingTop = properties.find { it.propertyName == "padding-top" }?.let { PropertyValueExtractor.getLength(it) }
        val paddingRight = properties.find { it.propertyName == "padding-right" }?.let { PropertyValueExtractor.getLength(it) }
        val paddingBottom = properties.find { it.propertyName == "padding-bottom" }?.let { PropertyValueExtractor.getLength(it) }
        val paddingLeft = properties.find { it.propertyName == "padding-left" }?.let { PropertyValueExtractor.getLength(it) }
        val padding = properties.find { it.propertyName == "padding" }?.let { PropertyValueExtractor.getLength(it) }

        // Track which properties we've already processed
        val processedProps = mutableSetOf<String>()

        // Store border drawing to add AFTER background (using drawWithContent)
        var borderDrawing: String? = null

        // Build individual border sides using drawWithContent (draws ON TOP of background)
        if (borderTopWidth != null || borderBottomWidth != null || borderLeftWidth != null || borderRightWidth != null) {
            val borderParts = mutableListOf<String>()

            // Build the drawWithContent block
            borderTopWidth?.let { width ->
                val color = borderTopColor?.let { PropertyValueExtractor.colorToHexString(it) } ?: "000000"
                val widthDp = convertLength(width)
                borderParts.add("drawLine(Color(0xFF$color), Offset(0f, ${widthDp}.toPx() / 2), Offset(size.width, ${widthDp}.toPx() / 2), ${widthDp}.toPx())")
            }
            borderBottomWidth?.let { width ->
                val color = borderBottomColor?.let { PropertyValueExtractor.colorToHexString(it) } ?: "000000"
                val widthDp = convertLength(width)
                borderParts.add("drawLine(Color(0xFF$color), Offset(0f, size.height - ${widthDp}.toPx() / 2), Offset(size.width, size.height - ${widthDp}.toPx() / 2), ${widthDp}.toPx())")
            }
            borderLeftWidth?.let { width ->
                val color = borderLeftColor?.let { PropertyValueExtractor.colorToHexString(it) } ?: "000000"
                val widthDp = convertLength(width)
                borderParts.add("drawLine(Color(0xFF$color), Offset(${widthDp}.toPx() / 2, 0f), Offset(${widthDp}.toPx() / 2, size.height), ${widthDp}.toPx())")
            }
            borderRightWidth?.let { width ->
                val color = borderRightColor?.let { PropertyValueExtractor.colorToHexString(it) } ?: "000000"
                val widthDp = convertLength(width)
                borderParts.add("drawLine(Color(0xFF$color), Offset(size.width - ${widthDp}.toPx() / 2, 0f), Offset(size.width - ${widthDp}.toPx() / 2, size.height), ${widthDp}.toPx())")
            }

            if (borderParts.isNotEmpty()) {
                borderDrawing = "drawWithContent { drawContent(); ${borderParts.joinToString("; ")} }"
                processedProps.addAll(listOf(
                    "border-top-width", "border-top-color", "border-top-style",
                    "border-bottom-width", "border-bottom-color", "border-bottom-style",
                    "border-left-width", "border-left-color", "border-left-style",
                    "border-right-width", "border-right-color", "border-right-style"
                ))
            }
        }

        // STEP 1: Add combined border radius modifier (clip) - defines the shape
        if (topLeftRadius != null || topRightRadius != null || bottomLeftRadius != null || bottomRightRadius != null) {
            val parts = mutableListOf<String>()
            topLeftRadius?.let { parts.add("topStart = ${convertLength(it)}") }
            topRightRadius?.let { parts.add("topEnd = ${convertLength(it)}") }
            bottomRightRadius?.let { parts.add("bottomEnd = ${convertLength(it)}") }
            bottomLeftRadius?.let { parts.add("bottomStart = ${convertLength(it)}") }

            if (parts.isNotEmpty()) {
                modifiers.add("clip(RoundedCornerShape(${parts.joinToString(", ")}))")
                processedProps.add("border-top-left-radius")
                processedProps.add("border-top-right-radius")
                processedProps.add("border-bottom-left-radius")
                processedProps.add("border-bottom-right-radius")
            }
        }

        // STEP 2: Add background color (fills the clipped area)
        val backgroundColor = properties.find { it.propertyName == "background-color" }?.colors?.firstOrNull()
        if (backgroundColor != null) {
            modifiers.add("background(${convertColor(backgroundColor)})")
            processedProps.add("background-color")
        }

        // STEP 3: Add border drawing (individual borders drawn ON TOP of background)
        borderDrawing?.let { modifiers.add(it) }

        // STEP 3b: Add regular border if present (also after background)
        if (borderWidth != null && borderColor != null) {
            val width = convertLength(borderWidth)
            val color = convertColor(borderColor)
            modifiers.add("border($width, $color)")
            processedProps.add("border-width")
            processedProps.add("border-color")
            processedProps.add("border-style")
            processedProps.add("border")
        }

        // STEP 4: Add combined padding modifier (creates internal space)
        if (paddingTop != null || paddingRight != null || paddingBottom != null || paddingLeft != null) {
            val parts = mutableListOf<String>()
            paddingTop?.let { parts.add("top = ${convertLength(it)}") }
            paddingRight?.let { parts.add("end = ${convertLength(it)}") }
            paddingBottom?.let { parts.add("bottom = ${convertLength(it)}") }
            paddingLeft?.let { parts.add("start = ${convertLength(it)}") }

            if (parts.isNotEmpty()) {
                modifiers.add("padding(${parts.joinToString(", ")})")
                processedProps.add("padding-top")
                processedProps.add("padding-right")
                processedProps.add("padding-bottom")
                processedProps.add("padding-left")
            }
        }

        properties.forEach { prop ->
            // Skip if already processed
            if (processedProps.contains(prop.propertyName)) {
                return@forEach
            }

            val modifier = when (prop.propertyName) {
                // Layout - Size
                "width" -> prop.lengths.firstOrNull()?.let {
                    // Handle percentage widths properly
                    if (it.unit == app.irmodels.IRLength.LengthUnit.PERCENT) {
                        if (it.value == 100.0) {
                            "fillMaxWidth()"
                        } else {
                            "fillMaxWidth(${it.value / 100}f)"
                        }
                    } else {
                        "width(${convertLength(it)})"
                    }
                }
                "height" -> prop.lengths.firstOrNull()?.let {
                    if (it.unit == app.irmodels.IRLength.LengthUnit.PERCENT) {
                        if (it.value == 100.0) {
                            "fillMaxHeight()"
                        } else {
                            "fillMaxHeight(${it.value / 100}f)"
                        }
                    } else {
                        "height(${convertLength(it)})"
                    }
                }
                "min-width" -> prop.lengths.firstOrNull()?.let { "widthIn(min = ${convertLength(it)})" }
                "max-width" -> prop.lengths.firstOrNull()?.let { "widthIn(max = ${convertLength(it)})" }
                "min-height" -> prop.lengths.firstOrNull()?.let { "heightIn(min = ${convertLength(it)})" }
                "max-height" -> prop.lengths.firstOrNull()?.let { "heightIn(max = ${convertLength(it)})" }

                // Layout - Padding (only if not combined above)
                "padding" -> prop.lengths.firstOrNull()?.let { "padding(${convertLength(it)})" }
                "padding-top", "padding-bottom", "padding-left", "padding-start", "padding-right", "padding-end" -> null

                // Layout - Margin (commented as Compose handles differently)
                "margin" -> prop.lengths.firstOrNull()?.let { "/* margin: ${convertLength(it)} - use parent spacing */" }
                "margin-top" -> prop.lengths.firstOrNull()?.let { "/* margin-top: ${convertLength(it)} */" }
                "margin-bottom" -> prop.lengths.firstOrNull()?.let { "/* margin-bottom: ${convertLength(it)} */" }
                "margin-left", "margin-start" -> prop.lengths.firstOrNull()?.let { "/* margin-start: ${convertLength(it)} */" }
                "margin-right", "margin-end" -> prop.lengths.firstOrNull()?.let { "/* margin-end: ${convertLength(it)} */" }

                // Appearance - Background
                "background-color" -> prop.colors.firstOrNull()?.let { "background(${convertColor(it)})" }
                "background" -> {
                    // Check if it's a gradient or solid color
                    if (prop.raw?.contains("gradient") == true) {
                        "/* background: ${prop.raw} - use Brush.linearGradient() or Brush.radialGradient() */"
                    } else {
                        prop.colors.firstOrNull()?.let { "background(${convertColor(it)})" }
                    }
                }
                "background-image" -> {
                    if (prop.raw?.contains("gradient") == true) {
                        "/* background-image: ${prop.raw} - use Brush.linearGradient() or Brush.radialGradient() */"
                    } else if (prop.urls.isNotEmpty()) {
                        "/* background-image: url - use painterResource() with Image */"
                    } else {
                        null
                    }
                }

                // Appearance - Border
                "border-width" -> {
                    // Only add border if we have complete border info
                    if (borderWidth != null && borderColor != null) {
                        val width = convertLength(borderWidth)
                        val color = convertColor(borderColor)
                        "border($width, $color)"
                    } else {
                        null
                    }
                }

                // Border radius (only if not combined above)
                "border-radius" -> {
                    // Only use if individual corners weren't specified
                    if (topLeftRadius == null && topRightRadius == null && bottomLeftRadius == null && bottomRightRadius == null) {
                        prop.lengths.firstOrNull()?.let {
                            "clip(RoundedCornerShape(${convertLength(it)}))"
                        }
                    } else {
                        null
                    }
                }
                "border-top-left-radius", "border-top-right-radius", "border-bottom-left-radius", "border-bottom-right-radius" -> null

                // Appearance - Shadow
                "box-shadow" -> prop.shadows.firstOrNull()?.let { shadow ->
                    convertShadow(shadow)
                }

                // Appearance - Opacity
                "opacity" -> PropertyValueExtractor.getOpacity(prop)?.let { alpha ->
                    if (alpha != 1.0) "alpha(${alpha}f)" else null
                }

                // Text properties (now handled via ProvideTextStyle wrapper in generateComposableCode)
                "color" -> null
                "font-size" -> null
                "font-weight" -> null
                "text-align" -> null
                "line-height" -> null
                "letter-spacing" -> null

                // Display & Flexbox
                "display" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "flex" -> "/* Use Row or Column */"
                        "none" -> "/* display: none - use visibility or conditional rendering */"
                        else -> null
                    }
                }
                "flex-direction" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "row" -> "/* flex-direction: row - use Row { } */"
                        "column" -> "/* flex-direction: column - use Column { } */"
                        "row-reverse" -> "/* flex-direction: row-reverse - use Row(reverseLayout = true) */"
                        "column-reverse" -> "/* flex-direction: column-reverse - use Column(reverseLayout = true) */"
                        else -> null
                    }
                }
                "justify-content" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "flex-start", "start" -> "/* justify-content: start - use Arrangement.Start */"
                        "flex-end", "end" -> "/* justify-content: end - use Arrangement.End */"
                        "center" -> "/* justify-content: center - use Arrangement.Center */"
                        "space-between" -> "/* justify-content: space-between - use Arrangement.SpaceBetween */"
                        "space-around" -> "/* justify-content: space-around - use Arrangement.SpaceAround */"
                        "space-evenly" -> "/* justify-content: space-evenly - use Arrangement.SpaceEvenly */"
                        else -> null
                    }
                }
                "align-items" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "flex-start", "start" -> "/* align-items: start - use Alignment.Start */"
                        "flex-end", "end" -> "/* align-items: end - use Alignment.End */"
                        "center" -> "/* align-items: center - use Alignment.CenterVertically/CenterHorizontally */"
                        "baseline" -> "/* align-items: baseline - not supported in basic layouts */"
                        "stretch" -> "/* align-items: stretch - children fill space */"
                        else -> null
                    }
                }
                "align-self" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "flex-start", "start" -> "align(Alignment.Start)"
                        "flex-end", "end" -> "align(Alignment.End)"
                        "center" -> "align(Alignment.CenterVertically)"
                        else -> null
                    }
                }
                "gap" -> prop.lengths.firstOrNull()?.let {
                    "/* gap: ${convertLength(it)} - use Arrangement.spacedBy(${convertLength(it)}) */"
                }
                "row-gap" -> prop.lengths.firstOrNull()?.let {
                    "/* row-gap: ${convertLength(it)} - use Arrangement.spacedBy(${convertLength(it)}) */"
                }
                "column-gap" -> prop.lengths.firstOrNull()?.let {
                    "/* column-gap: ${convertLength(it)} - use Arrangement.spacedBy(${convertLength(it)}) */"
                }
                "flex-grow" -> prop.lengths.firstOrNull()?.let {
                    val grow = it.value ?: 1.0
                    if (grow > 0.0) "weight(${grow}f)" else null
                }
                "flex-shrink" -> prop.lengths.firstOrNull()?.let {
                    "/* flex-shrink: ${it.value} - use weight(fill = false) */"
                }

                // Overflow
                "overflow" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "hidden" -> "clip(RectangleShape)"
                        "scroll" -> "verticalScroll(rememberScrollState())"
                        else -> null
                    }
                }

                // Z-index
                "z-index" -> prop.lengths.firstOrNull()?.let {
                    val z = it.value ?: 0.0
                    if (z != 0.0) "zIndex(${z}f)" else null
                }

                // Transform properties
                "transform" -> {
                    // Handle transform: scale(0.98)
                    val rawValue = prop.raw
                    if (rawValue?.contains("scale") == true) {
                        val scaleMatch = Regex("scale\\(([0-9.]+)\\)").find(rawValue)
                        scaleMatch?.groupValues?.get(1)?.let { "scale(${it}f)" }
                    } else {
                        // Other transforms not yet supported
                        rawValue?.let { "/* transform: $it - parse and use rotate(), scale(), offset() */" }
                    }
                }
                "rotate" -> prop.lengths.firstOrNull()?.let {
                    val degrees = it.value ?: 0.0
                    if (degrees != 0.0) "rotate(${degrees}f)" else null
                }

                // Position properties
                "position" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "absolute" -> "/* position: absolute - use Box with Alignment or offset */"
                        "relative" -> "/* position: relative - default in Compose */"
                        "fixed" -> "/* position: fixed - use Box at root level */"
                        "sticky" -> "/* position: sticky - not directly supported */"
                        else -> null
                    }
                }
                "top" -> prop.lengths.firstOrNull()?.let {
                    "/* top: ${convertLength(it)} - use offset or Box alignment */"
                }
                "left" -> prop.lengths.firstOrNull()?.let {
                    "/* left: ${convertLength(it)} - use offset or Box alignment */"
                }
                "right" -> prop.lengths.firstOrNull()?.let {
                    "/* right: ${convertLength(it)} - use offset or Box alignment */"
                }
                "bottom" -> prop.lengths.firstOrNull()?.let {
                    "/* bottom: ${convertLength(it)} - use offset or Box alignment */"
                }

                // Aspect ratio
                "aspect-ratio" -> prop.lengths.firstOrNull()?.let {
                    val ratio = it.value ?: 1.0
                    if (ratio > 0.0) "aspectRatio(${ratio}f)" else null
                }

                // Visibility
                "visibility" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "hidden" -> "alpha(0f)"
                        "visible" -> null // default
                        else -> null
                    }
                }

                // Text decoration
                "text-decoration" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "underline" -> "/* text-decoration: underline - use TextDecoration.Underline */"
                        "line-through" -> "/* text-decoration: line-through - use TextDecoration.LineThrough */"
                        "none" -> null
                        else -> "/* text-decoration: ${it.value} */"
                    }
                }
                "text-decoration-line" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "underline" -> "/* text-decoration: underline - use TextDecoration.Underline */"
                        "line-through" -> "/* text-decoration: line-through - use TextDecoration.LineThrough */"
                        else -> null
                    }
                }
                "font-style" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "italic" -> "/* font-style: italic - use FontStyle.Italic */"
                        "normal" -> null
                        else -> null
                    }
                }
                "font-family" -> prop.keywords.firstOrNull()?.let {
                    "/* font-family: ${it.value} - define custom FontFamily */"
                }
                "text-transform" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "uppercase" -> "/* text-transform: uppercase - use .uppercase() on text */"
                        "lowercase" -> "/* text-transform: lowercase - use .lowercase() on text */"
                        "capitalize" -> "/* text-transform: capitalize - use .capitalize() on text */"
                        else -> null
                    }
                }

                // Cursor
                "cursor" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "pointer" -> "/* cursor: pointer - use Modifier.clickable { } or .pointerInput { } */"
                        "not-allowed" -> "/* cursor: not-allowed - disable interaction */"
                        else -> "/* cursor: ${it.value} */"
                    }
                }

                // Animation & Transition
                "animation", "animation-name", "animation-duration", "animation-timing-function",
                "animation-delay", "animation-iteration-count", "animation-direction",
                "animation-fill-mode", "animation-play-state" -> {
                    "/* animation: ${prop.raw} - use animate*AsState() or Animatable */"
                }
                "transition", "transition-property", "transition-duration", "transition-timing-function", "transition-delay" -> {
                    "/* transition: ${prop.raw} - use animate*AsState() or AnimatedVisibility */"
                }

                // Filter Effects
                "filter" -> prop.raw?.let { "/* filter: $it - use graphicsLayer or ColorFilter */" }
                "backdrop-filter" -> prop.raw?.let { "/* backdrop-filter: $it - not directly supported */" }
                "blur" -> prop.lengths.firstOrNull()?.let { "/* blur: ${convertLength(it)} - use graphicsLayer { renderEffect } */" }

                // Overflow variants
                "overflow-x" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "hidden" -> "clip(RectangleShape)"
                        "scroll" -> "horizontalScroll(rememberScrollState())"
                        "auto" -> "/* overflow-x: auto - use horizontalScroll conditionally */"
                        else -> null
                    }
                }
                "overflow-y" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "hidden" -> "clip(RectangleShape)"
                        "scroll" -> "verticalScroll(rememberScrollState())"
                        "auto" -> "/* overflow-y: auto - use verticalScroll conditionally */"
                        else -> null
                    }
                }
                "overflow-wrap", "word-wrap" -> prop.keywords.firstOrNull()?.let {
                    "/* overflow-wrap: ${it.value} - use Text(overflow = TextOverflow) */"
                }
                "text-overflow" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "ellipsis" -> "/* text-overflow: ellipsis - use Text(overflow = TextOverflow.Ellipsis) */"
                        "clip" -> "/* text-overflow: clip - use Text(overflow = TextOverflow.Clip) */"
                        else -> null
                    }
                }

                // Flex properties (additional)
                "flex" -> prop.raw?.let { "/* flex: $it - split into flex-grow, flex-shrink, flex-basis */" }
                "flex-basis" -> prop.lengths.firstOrNull()?.let { "/* flex-basis: ${convertLength(it)} - use weight with minimum size */" }
                "flex-wrap" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "wrap" -> "/* flex-wrap: wrap - use FlowRow or FlowColumn */"
                        "nowrap" -> null // default
                        "wrap-reverse" -> "/* flex-wrap: wrap-reverse - use FlowRow/FlowColumn with reverse */"
                        else -> null
                    }
                }
                "order" -> prop.lengths.firstOrNull()?.let { "/* order: ${it.value} - rearrange children manually */" }

                // Grid Layout
                "grid", "grid-template-columns", "grid-template-rows", "grid-template-areas",
                "grid-template", "grid-column", "grid-row", "grid-area", "grid-column-start",
                "grid-column-end", "grid-row-start", "grid-row-end", "grid-auto-rows",
                "grid-auto-columns", "grid-auto-flow", "grid-gap", "grid-column-gap", "grid-row-gap" -> {
                    "/* grid: ${prop.raw} - use Column/Row with weight or custom Layout */"
                }

                // Table properties
                "table-layout", "border-collapse", "border-spacing", "caption-side", "empty-cells" -> {
                    "/* table: ${prop.raw} - tables not directly supported */"
                }

                // List styling
                "list-style", "list-style-type", "list-style-position", "list-style-image" -> {
                    "/* list-style: ${prop.raw} - manually implement with Row/Column */"
                }

                // Content & Quotes
                "content" -> prop.raw?.let { "/* content: $it - use Text composable */" }
                "quotes" -> prop.raw?.let { "/* quotes: $it - handle in Text content */" }

                // Outline (different from border)
                "outline", "outline-width", "outline-style", "outline-color", "outline-offset" -> {
                    // In CSS, outline is like border but doesn't affect layout
                    // In Compose, we can ignore "outline: none" as there's no default outline
                    when (prop.raw?.trim()) {
                        "none", "0", "0px" -> null  // No outline needed
                        else -> null  // Other outline values not commonly used, can be ignored
                    }
                }

                // Pointer events
                "pointer-events" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "none" -> "/* pointer-events: none - use Modifier.pointerInput {} with consumeDownChange = false */"
                        "auto" -> null // default
                        else -> null
                    }
                }
                "user-select" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "none" -> "/* user-select: none - use BasicTextField(readOnly = true) */"
                        else -> null
                    }
                }

                // Clipping & Masking
                "clip-path" -> prop.raw?.let { "/* clip-path: $it - use Modifier.clip() with custom Shape */" }
                "mask", "mask-image", "mask-mode", "mask-repeat", "mask-position", "mask-size" -> {
                    "/* mask: ${prop.raw} - use graphicsLayer { alpha = ... } or custom drawing */"
                }

                // Scroll behavior
                "scroll-behavior" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "smooth" -> "/* scroll-behavior: smooth - use animateScrollTo() */"
                        else -> null
                    }
                }
                "scroll-margin", "scroll-margin-top", "scroll-margin-bottom", "scroll-margin-left", "scroll-margin-right" -> {
                    prop.lengths.firstOrNull()?.let { "/* scroll-margin: ${convertLength(it)} - use contentPadding */" }
                }
                "scroll-padding", "scroll-padding-top", "scroll-padding-bottom", "scroll-padding-left", "scroll-padding-right" -> {
                    prop.lengths.firstOrNull()?.let { "/* scroll-padding: ${convertLength(it)} - use contentPadding */" }
                }

                // Text styling (additional)
                "white-space" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "nowrap" -> "/* white-space: nowrap - use Text(maxLines = 1, softWrap = false) */"
                        "pre" -> "/* white-space: pre - preserve whitespace in text */"
                        "pre-wrap" -> "/* white-space: pre-wrap - preserve whitespace with wrapping */"
                        else -> null
                    }
                }
                "word-spacing" -> prop.lengths.firstOrNull()?.let { "/* word-spacing: ${convertLength(it, useSp = true)} - not directly supported */" }
                "text-indent" -> prop.lengths.firstOrNull()?.let { "/* text-indent: ${convertLength(it)} - use padding or custom text layout */" }
                "text-shadow" -> prop.shadows.firstOrNull()?.let { "/* text-shadow: ${prop.raw} - use shadow layer or graphicsLayer */" }
                "text-justify" -> prop.keywords.firstOrNull()?.let { "/* text-justify: ${it.value} - use TextAlign.Justify */" }
                "vertical-align" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "top" -> "/* vertical-align: top - use Alignment.Top */"
                        "middle", "center" -> "/* vertical-align: middle - use Alignment.CenterVertically */"
                        "bottom" -> "/* vertical-align: bottom - use Alignment.Bottom */"
                        "baseline" -> "/* vertical-align: baseline - use AlignmentLine */"
                        else -> "/* vertical-align: ${it.value} */"
                    }
                }
                "direction" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "rtl" -> "/* direction: rtl - use CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) */"
                        "ltr" -> null // default
                        else -> null
                    }
                }
                "writing-mode" -> prop.keywords.firstOrNull()?.let { "/* writing-mode: ${it.value} - not supported */" }
                "unicode-bidi" -> prop.keywords.firstOrNull()?.let { "/* unicode-bidi: ${it.value} - handled automatically */" }

                // Background (additional)
                "background-attachment" -> prop.keywords.firstOrNull()?.let { "/* background-attachment: ${it.value} - not applicable */" }
                "background-clip" -> prop.keywords.firstOrNull()?.let { "/* background-clip: ${it.value} - use clip modifier */" }
                "background-origin" -> prop.keywords.firstOrNull()?.let { "/* background-origin: ${it.value} - adjust padding */" }
                "background-position" -> prop.raw?.let { "/* background-position: $it - use Alignment in background */" }
                "background-repeat" -> prop.keywords.firstOrNull()?.let { "/* background-repeat: ${it.value} - use TileMode */" }
                "background-size" -> prop.raw?.let { "/* background-size: $it - use ContentScale */" }
                "background-blend-mode" -> prop.keywords.firstOrNull()?.let { "/* background-blend-mode: ${it.value} - use BlendMode */" }

                // Border styles (additional)
                "border-style" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "solid" -> null // default
                        "dashed" -> "/* border-style: dashed - use DashedBorder custom Shape */"
                        "dotted" -> "/* border-style: dotted - use custom drawing */"
                        "none", "hidden" -> null
                        else -> "/* border-style: ${it.value} - use custom drawing */"
                    }
                }
                "border-color" -> null // handled with border-width
                "border-image", "border-image-source", "border-image-slice", "border-image-width",
                "border-image-outset", "border-image-repeat" -> {
                    "/* border-image: ${prop.raw} - use custom drawing */"
                }

                // Box model (additional)
                "box-sizing" -> prop.keywords.firstOrNull()?.let { "/* box-sizing: ${it.value} - Compose uses border-box by default */" }
                "object-fit" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "contain" -> "/* object-fit: contain - use ContentScale.Fit */"
                        "cover" -> "/* object-fit: cover - use ContentScale.Crop */"
                        "fill" -> "/* object-fit: fill - use ContentScale.FillBounds */"
                        "none" -> "/* object-fit: none - use ContentScale.None */"
                        "scale-down" -> "/* object-fit: scale-down - use ContentScale.Inside */"
                        else -> null
                    }
                }
                "object-position" -> prop.raw?.let { "/* object-position: $it - use Alignment */" }
                "resize" -> prop.keywords.firstOrNull()?.let { "/* resize: ${it.value} - not applicable in Compose */" }

                // Column layout
                "columns", "column-count", "column-fill", "column-gap", "column-rule",
                "column-rule-color", "column-rule-style", "column-rule-width", "column-span", "column-width" -> {
                    "/* columns: ${prop.raw} - use Row with weight or FlowRow */"
                }

                // Break properties
                "break-before", "break-after", "break-inside", "page-break-before",
                "page-break-after", "page-break-inside" -> {
                    "/* page-break: ${prop.raw} - not applicable in Compose */"
                }

                // Miscellaneous
                "float" -> prop.keywords.firstOrNull()?.let { "/* float: ${it.value} - use Box with alignment */" }
                "clear" -> prop.keywords.firstOrNull()?.let { "/* clear: ${it.value} - not applicable */" }
                "all" -> prop.keywords.firstOrNull()?.let { "/* all: ${it.value} - handle each property individually */" }
                "appearance" -> prop.keywords.firstOrNull()?.let { "/* appearance: ${it.value} - use custom composables */" }
                "will-change" -> prop.raw?.let { "/* will-change: $it - performance hint, not needed in Compose */" }
                "isolation" -> prop.keywords.firstOrNull()?.let { "/* isolation: ${it.value} - use graphicsLayer */" }
                "mix-blend-mode" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "normal" -> null
                        else -> "/* mix-blend-mode: ${it.value} - use graphicsLayer { blendMode = BlendMode.${it.value.replaceFirstChar { c -> c.uppercase() }} } */"
                    }
                }

                // Scroll snap
                "scroll-snap-type", "scroll-snap-align", "scroll-snap-stop" -> {
                    "/* scroll-snap: ${prop.raw} - use pager or custom scroll behavior */"
                }

                // Touch & interaction
                "touch-action" -> prop.keywords.firstOrNull()?.let { "/* touch-action: ${it.value} - use pointerInput */" }
                "overscroll-behavior", "overscroll-behavior-x", "overscroll-behavior-y" -> {
                    prop.keywords.firstOrNull()?.let { "/* overscroll-behavior: ${it.value} - use nestedScroll */" }
                }

                // Caret & Selection
                "caret-color" -> prop.colors.firstOrNull()?.let { "/* caret-color: ${convertColor(it)} - use TextFieldColors */" }
                "accent-color" -> prop.colors.firstOrNull()?.let { "/* accent-color: ${convertColor(it)} - use component colors */" }

                // Hyphens & line break
                "hyphens" -> prop.keywords.firstOrNull()?.let { "/* hyphens: ${it.value} - use Hyphenation */" }
                "line-break" -> prop.keywords.firstOrNull()?.let { "/* line-break: ${it.value} - use LineBreaker */" }
                "word-break" -> prop.keywords.firstOrNull()?.let { "/* word-break: ${it.value} - use LineBreaker */" }
                "tab-size" -> prop.lengths.firstOrNull()?.let { "/* tab-size: ${it.value} - not supported */" }

                // CSS Variables
                "--*" -> prop.raw?.let { "/* CSS variable: $it - handle at theme level */" }

                // Ink overflow
                "ink-overflow" -> prop.keywords.firstOrNull()?.let { "/* ink-overflow: ${it.value} - not applicable */" }

                // Logical Properties (writing-mode aware)
                "block-size" -> prop.lengths.firstOrNull()?.let {
                    if (it.unit == IRLength.LengthUnit.PERCENT) {
                        if (it.value == 100.0) "fillMaxHeight()" else "fillMaxHeight(${it.value / 100}f)"
                    } else {
                        "height(${convertLength(it)})" // Maps to height in LTR
                    }
                }
                "inline-size" -> prop.lengths.firstOrNull()?.let {
                    if (it.unit == IRLength.LengthUnit.PERCENT) {
                        if (it.value == 100.0) "fillMaxWidth()" else "fillMaxWidth(${it.value / 100}f)"
                    } else {
                        "width(${convertLength(it)})" // Maps to width in LTR
                    }
                }
                "min-block-size" -> prop.lengths.firstOrNull()?.let { "heightIn(min = ${convertLength(it)})" }
                "max-block-size" -> prop.lengths.firstOrNull()?.let { "heightIn(max = ${convertLength(it)})" }
                "min-inline-size" -> prop.lengths.firstOrNull()?.let { "widthIn(min = ${convertLength(it)})" }
                "max-inline-size" -> prop.lengths.firstOrNull()?.let { "widthIn(max = ${convertLength(it)})" }

                // Logical margin properties
                "margin-block", "margin-block-start", "margin-block-end" -> {
                    prop.lengths.firstOrNull()?.let { "/* margin-block: ${convertLength(it)} - use parent spacing */" }
                }
                "margin-inline", "margin-inline-start", "margin-inline-end" -> {
                    prop.lengths.firstOrNull()?.let { "/* margin-inline: ${convertLength(it)} - use parent spacing */" }
                }

                // Logical padding properties
                "padding-block", "padding-block-start", "padding-block-end" -> {
                    prop.lengths.firstOrNull()?.let { "padding(vertical = ${convertLength(it)})" }
                }
                "padding-inline", "padding-inline-start", "padding-inline-end" -> {
                    prop.lengths.firstOrNull()?.let { "padding(horizontal = ${convertLength(it)})" }
                }

                // Logical inset properties
                "inset" -> prop.lengths.firstOrNull()?.let { "/* inset: ${convertLength(it)} - use offset in all directions */" }
                "inset-block", "inset-block-start", "inset-block-end" -> {
                    prop.lengths.firstOrNull()?.let { "/* inset-block: ${convertLength(it)} - use offset vertically */" }
                }
                "inset-inline", "inset-inline-start", "inset-inline-end" -> {
                    prop.lengths.firstOrNull()?.let { "/* inset-inline: ${convertLength(it)} - use offset horizontally */" }
                }

                // Logical border properties
                "border-block", "border-block-start", "border-block-end",
                "border-block-start-width", "border-block-end-width",
                "border-block-start-color", "border-block-end-color",
                "border-block-start-style", "border-block-end-style" -> {
                    "/* border-block: ${prop.raw} - use border (top/bottom in LTR) */"
                }
                "border-inline", "border-inline-start", "border-inline-end",
                "border-inline-start-width", "border-inline-end-width",
                "border-inline-start-color", "border-inline-end-color",
                "border-inline-start-style", "border-inline-end-style" -> {
                    "/* border-inline: ${prop.raw} - use border (left/right in LTR) */"
                }

                // Border radius logical properties
                "border-start-start-radius", "border-start-end-radius",
                "border-end-start-radius", "border-end-end-radius" -> {
                    prop.lengths.firstOrNull()?.let { "/* border-radius logical: ${convertLength(it)} - maps to corner */" }
                }

                // Place properties (alignment shorthands)
                "place-content" -> prop.keywords.firstOrNull()?.let {
                    "/* place-content: ${it.value} - use verticalArrangement + horizontalArrangement */"
                }
                "place-items" -> prop.keywords.firstOrNull()?.let {
                    "/* place-items: ${it.value} - use Alignment in parent */"
                }
                "place-self" -> prop.keywords.firstOrNull()?.let {
                    "/* place-self: ${it.value} - use align() modifier */"
                }

                // Contain property (performance)
                "contain" -> prop.keywords.firstOrNull()?.let {
                    "/* contain: ${it.value} - performance hint, Compose handles automatically */"
                }
                "contain-intrinsic-size", "contain-intrinsic-width", "contain-intrinsic-height",
                "contain-intrinsic-block-size", "contain-intrinsic-inline-size" -> {
                    prop.lengths.firstOrNull()?.let { "/* contain-intrinsic-size: ${convertLength(it)} - use explicit size */" }
                }
                "content-visibility" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "auto" -> "/* content-visibility: auto - Compose handles visibility automatically */"
                        "hidden" -> "/* content-visibility: hidden - use conditional rendering */"
                        "visible" -> null
                        else -> null
                    }
                }

                // Offset properties (motion path)
                "offset" -> prop.raw?.let { "/* offset: $it - use animated offset() or graphicsLayer */" }
                "offset-anchor" -> prop.raw?.let { "/* offset-anchor: $it - use transformOrigin */" }
                "offset-distance" -> prop.lengths.firstOrNull()?.let { "/* offset-distance: ${convertLength(it)} - animate along path */" }
                "offset-path" -> prop.raw?.let { "/* offset-path: $it - use custom animation path */" }
                "offset-position" -> prop.raw?.let { "/* offset-position: $it - use offset modifier */" }
                "offset-rotate" -> prop.raw?.let { "/* offset-rotate: $it - use rotate with path */" }

                // Container Queries
                "container", "container-name", "container-type" -> {
                    "/* container: ${prop.raw} - use BoxWithConstraints or custom measurement */"
                }

                // Scroll timeline (scroll-driven animations)
                "scroll-timeline", "scroll-timeline-name", "scroll-timeline-axis" -> {
                    "/* scroll-timeline: ${prop.raw} - use LazyListState or ScrollState */"
                }
                "animation-timeline" -> prop.raw?.let { "/* animation-timeline: $it - use scroll-based animation */" }
                "animation-range", "animation-range-start", "animation-range-end" -> {
                    prop.raw?.let { "/* animation-range: $it - define animation progress range */" }
                }

                // View timeline
                "view-timeline", "view-timeline-name", "view-timeline-axis",
                "view-timeline-inset" -> {
                    "/* view-timeline: ${prop.raw} - use visibility tracking */"
                }

                // View transitions
                "view-transition-name" -> prop.keywords.firstOrNull()?.let {
                    "/* view-transition-name: ${it.value} - use shared element transitions */"
                }
                "view-transition-class" -> prop.keywords.firstOrNull()?.let {
                    "/* view-transition-class: ${it.value} - use AnimatedContent */"
                }
                "view-transition-group" -> prop.keywords.firstOrNull()?.let {
                    "/* view-transition-group: ${it.value} - group shared element transitions */"
                }

                // Anchor positioning (CSS Anchor Positioning)
                "anchor-name" -> prop.keywords.firstOrNull()?.let {
                    "/* anchor-name: ${it.value} - use onGloballyPositioned for anchor */"
                }
                "anchor-scope" -> prop.keywords.firstOrNull()?.let {
                    "/* anchor-scope: ${it.value} - define anchor scope */"
                }
                "position-anchor" -> prop.keywords.firstOrNull()?.let {
                    "/* position-anchor: ${it.value} - use offset based on anchor */"
                }
                "position-area" -> prop.keywords.firstOrNull()?.let {
                    "/* position-area: ${it.value} - position relative to anchor */"
                }
                "position-try", "position-try-fallbacks", "position-try-order" -> {
                    "/* position-try: ${prop.raw} - fallback positioning */"
                }

                // Text emphasis
                "text-emphasis", "text-emphasis-style", "text-emphasis-color", "text-emphasis-position" -> {
                    "/* text-emphasis: ${prop.raw} - not directly supported */"
                }

                // Ruby (East Asian typography)
                "ruby-position", "ruby-align", "ruby-overhang", "ruby-merge" -> {
                    "/* ruby: ${prop.raw} - not supported in Compose */"
                }

                // Text spacing
                "text-autospace" -> prop.keywords.firstOrNull()?.let {
                    "/* text-autospace: ${it.value} - handle spacing in Text */"
                }
                "text-spacing-trim" -> prop.keywords.firstOrNull()?.let {
                    "/* text-spacing-trim: ${it.value} - not supported */"
                }

                // Text box properties
                "text-box", "text-box-trim", "text-box-edge" -> {
                    "/* text-box: ${prop.raw} - control text box metrics */"
                }

                // Text wrap
                "text-wrap" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "wrap" -> null // default
                        "nowrap" -> "/* text-wrap: nowrap - use softWrap = false */"
                        "balance" -> "/* text-wrap: balance - experimental, not supported */"
                        "pretty" -> "/* text-wrap: pretty - experimental, not supported */"
                        "stable" -> "/* text-wrap: stable - experimental, not supported */"
                        else -> null
                    }
                }
                "text-wrap-mode", "text-wrap-style" -> {
                    "/* text-wrap: ${prop.raw} - use Text softWrap and overflow */"
                }

                // Math/calc functions (handled at parse level, but document)
                "math-depth", "math-shift", "math-style" -> {
                    "/* math: ${prop.raw} - not applicable in Compose */"
                }

                // Field sizing (form controls)
                "field-sizing" -> prop.keywords.firstOrNull()?.let {
                    "/* field-sizing: ${it.value} - use TextField with appropriate size */"
                }

                // Interpolate size (animation)
                "interpolate-size" -> prop.keywords.firstOrNull()?.let {
                    "/* interpolate-size: ${it.value} - Compose animates sizes by default */"
                }

                // Reading flow/order
                "reading-flow", "reading-order" -> {
                    "/* reading-order: ${prop.raw} - rearrange composables for accessibility */"
                }

                // Interactivity
                "interactivity" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "inert" -> "/* interactivity: inert - disable all interactions */"
                        "auto" -> null
                        else -> null
                    }
                }

                // Caret properties (cursor in text fields)
                "caret-animation" -> prop.keywords.firstOrNull()?.let {
                    "/* caret-animation: ${it.value} - use TextFieldColors */"
                }
                "caret-shape" -> prop.keywords.firstOrNull()?.let {
                    "/* caret-shape: ${it.value} - customize cursor in TextField */"
                }

                // Font properties (additional)
                "font-synthesis", "font-synthesis-weight", "font-synthesis-style",
                "font-synthesis-small-caps", "font-synthesis-position" -> {
                    "/* font-synthesis: ${prop.raw} - font fallback behavior */"
                }
                "font-variant-emoji" -> prop.keywords.firstOrNull()?.let {
                    "/* font-variant-emoji: ${it.value} - emoji rendering style */"
                }
                "font-variant-alternates", "font-variant-caps", "font-variant-east-asian",
                "font-variant-ligatures", "font-variant-numeric", "font-variant-position" -> {
                    "/* font-variant: ${prop.raw} - advanced typography features */"
                }
                "font-feature-settings" -> prop.raw?.let {
                    "/* font-feature-settings: $it - OpenType features */"
                }
                "font-variation-settings" -> prop.raw?.let {
                    "/* font-variation-settings: $it - variable font axes */"
                }
                "font-optical-sizing" -> prop.keywords.firstOrNull()?.let {
                    "/* font-optical-sizing: ${it.value} - font rendering optimization */"
                }
                "font-palette" -> prop.keywords.firstOrNull()?.let {
                    "/* font-palette: ${it.value} - color font palette */"
                }
                "font-size-adjust" -> prop.lengths.firstOrNull()?.let {
                    "/* font-size-adjust: ${it.value} - preserve x-height */"
                }
                "font-stretch" -> prop.keywords.firstOrNull()?.let {
                    "/* font-stretch: ${it.value} - font width/condensed */"
                }
                "font-kerning" -> prop.keywords.firstOrNull()?.let {
                    "/* font-kerning: ${it.value} - letter pair spacing */"
                }
                "font-language-override" -> prop.keywords.firstOrNull()?.let {
                    "/* font-language-override: ${it.value} - language-specific glyphs */"
                }

                // Color schemes and properties
                "color-scheme" -> prop.keywords.firstOrNull()?.let {
                    "/* color-scheme: ${it.value} - use isSystemInDarkTheme() */"
                }
                "forced-color-adjust" -> prop.keywords.firstOrNull()?.let {
                    "/* forced-color-adjust: ${it.value} - high contrast mode */"
                }
                "print-color-adjust" -> prop.keywords.firstOrNull()?.let {
                    "/* print-color-adjust: ${it.value} - not applicable in Compose */"
                }

                // Image rendering
                "image-rendering" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "auto" -> null
                        "crisp-edges" -> "/* image-rendering: crisp-edges - use FilterQuality.None */"
                        "pixelated" -> "/* image-rendering: pixelated - use FilterQuality.None */"
                        else -> null
                    }
                }
                "image-orientation" -> prop.keywords.firstOrNull()?.let {
                    "/* image-orientation: ${it.value} - rotate image */"
                }

                // Counter properties
                "counter-increment", "counter-reset", "counter-set" -> {
                    "/* counter: ${prop.raw} - implement with state */"
                }

                // Zoom
                "zoom" -> prop.lengths.firstOrNull()?.let {
                    val zoom = it.value ?: 1.0
                    if (zoom != 1.0) "scale(${zoom}f)" else null
                }

                // Orphans and widows (print)
                "orphans", "widows" -> {
                    prop.lengths.firstOrNull()?.let { "/* ${prop.propertyName}: ${it.value} - print property, not applicable */" }
                }

                // Scroll marker (experimental)
                "scroll-marker-group", "scroll-initial-target", "scroll-target", "scroll-target-group" -> {
                    "/* scroll-marker: ${prop.raw} - experimental, not supported */"
                }

                // Corner shape (experimental)
                "corner-shape", "corner-top-left-shape", "corner-top-right-shape",
                "corner-bottom-left-shape", "corner-bottom-right-shape" -> {
                    "/* corner-shape: ${prop.raw} - experimental, use RoundedCornerShape */"
                }

                // Dynamic range
                "dynamic-range-limit" -> prop.keywords.firstOrNull()?.let {
                    "/* dynamic-range-limit: ${it.value} - HDR control */"
                }

                // Line clamp (webkit)
                "-webkit-line-clamp" -> prop.lengths.firstOrNull()?.let {
                    "/* -webkit-line-clamp: ${it.value} - use Text(maxLines = ${it.value?.toInt()}) */"
                }

                // Alignment baseline (SVG)
                "alignment-baseline", "baseline-shift", "baseline-source" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - SVG/baseline alignment */"
                }

                // Azimuth (audio CSS)
                "azimuth" -> prop.raw?.let { "/* azimuth: $it - audio spatial positioning */" }

                // Backface visibility (3D transforms)
                "backface-visibility" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "visible" -> null // default
                        "hidden" -> "/* backface-visibility: hidden - use graphicsLayer { rotationY = 180f, cameraDistance = 8f } */"
                        else -> null
                    }
                }

                // Block ellipsis (experimental)
                "block-ellipsis" -> prop.keywords.firstOrNull()?.let {
                    "/* block-ellipsis: ${it.value} - use Text with maxLines and overflow */"
                }

                // Block step properties (experimental)
                "block-step", "block-step-align", "block-step-insert", "block-step-round", "block-step-size" -> {
                    "/* block-step: ${prop.raw} - not supported */"
                }

                // Bookmark properties (paged media)
                "bookmark-label", "bookmark-level", "bookmark-state" -> {
                    "/* bookmark: ${prop.raw} - paged media, not applicable */"
                }

                // Border radius logical (additional)
                "border-bottom-radius" -> prop.lengths.firstOrNull()?.let {
                    "/* border-bottom-radius: ${convertLength(it)} - use bottomStart/bottomEnd */"
                }
                "border-top-radius" -> prop.lengths.firstOrNull()?.let {
                    "/* border-top-radius: ${convertLength(it)} - use topStart/topEnd */"
                }
                "border-left-radius" -> prop.lengths.firstOrNull()?.let {
                    "/* border-left-radius: ${convertLength(it)} - use topStart/bottomStart */"
                }
                "border-right-radius" -> prop.lengths.firstOrNull()?.let {
                    "/* border-right-radius: ${convertLength(it)} - use topEnd/bottomEnd */"
                }

                // Box decoration break
                "box-decoration-break" -> prop.keywords.firstOrNull()?.let {
                    "/* box-decoration-break: ${it.value} - not applicable in Compose */"
                }

                // Caret (additional)
                "caret" -> prop.raw?.let { "/* caret: $it - use TextFieldColors */" }

                // Caption side (table)
                "caption-side" -> prop.keywords.firstOrNull()?.let {
                    "/* caption-side: ${it.value} - tables not supported */"
                }

                // Color adjust
                "color-adjust" -> prop.keywords.firstOrNull()?.let {
                    "/* color-adjust: ${it.value} - print optimization */"
                }

                // Column fill
                "column-fill" -> prop.keywords.firstOrNull()?.let {
                    "/* column-fill: ${it.value} - use FlowRow */"
                }

                // Continue (fragmentation)
                "continue" -> prop.keywords.firstOrNull()?.let {
                    "/* continue: ${it.value} - fragmentation not supported */"
                }

                // Dominant baseline
                "dominant-baseline" -> prop.keywords.firstOrNull()?.let {
                    "/* dominant-baseline: ${it.value} - SVG text alignment */"
                }

                // Empty cells (table)
                "empty-cells" -> prop.keywords.firstOrNull()?.let {
                    "/* empty-cells: ${it.value} - tables not supported */"
                }

                // Fill and stroke (SVG)
                "fill", "fill-opacity", "fill-rule" -> {
                    prop.colors.firstOrNull()?.let { "/* fill: ${convertColor(it)} - SVG property */" }
                        ?: "/* fill: ${prop.raw} - SVG property */"
                }
                "stroke", "stroke-dasharray", "stroke-dashoffset", "stroke-linecap",
                "stroke-linejoin", "stroke-miterlimit", "stroke-opacity", "stroke-width" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - SVG stroke property */"
                }

                // Flood (SVG filter)
                "flood-color", "flood-opacity" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - SVG filter property */"
                }

                // Font display
                "font-display" -> prop.keywords.firstOrNull()?.let {
                    "/* font-display: ${it.value} - font loading behavior */"
                }

                // Glyph orientation (SVG)
                "glyph-orientation-horizontal", "glyph-orientation-vertical" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - SVG text orientation */"
                }

                // Hanging punctuation
                "hanging-punctuation" -> prop.keywords.firstOrNull()?.let {
                    "/* hanging-punctuation: ${it.value} - not supported */"
                }

                // Hyphenate properties
                "hyphenate-character", "hyphenate-limit-chars" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - hyphenation control */"
                }

                // Initial letter (drop cap)
                "initial-letter", "initial-letter-align", "initial-letter-wrap" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - drop cap styling */"
                }

                // Inset shadow (experimental)
                "inset-shadow" -> prop.raw?.let { "/* inset-shadow: $it - use custom drawing */" }

                // Justify self/content/items (additional)
                "justify-self" -> prop.keywords.firstOrNull()?.let {
                    "/* justify-self: ${it.value} - use align() modifier */"
                }

                // Leading trim (experimental)
                "leading-trim" -> prop.keywords.firstOrNull()?.let {
                    "/* leading-trim: ${it.value} - text box trimming */"
                }

                // Lighting color (SVG filter)
                "lighting-color" -> prop.colors.firstOrNull()?.let {
                    "/* lighting-color: ${convertColor(it)} - SVG filter */"
                }

                // Line grid
                "line-grid", "line-snap" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - line grid alignment */"
                }

                // Line height step
                "line-height-step" -> prop.lengths.firstOrNull()?.let {
                    "/* line-height-step: ${convertLength(it, useSp = true)} - experimental */"
                }

                // Margin trim
                "margin-trim" -> prop.keywords.firstOrNull()?.let {
                    "/* margin-trim: ${it.value} - experimental, not supported */"
                }

                // Marker (SVG/list)
                "marker", "marker-end", "marker-mid", "marker-start" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - SVG marker */"
                }

                // Mask border
                "mask-border", "mask-border-mode", "mask-border-outset", "mask-border-repeat",
                "mask-border-slice", "mask-border-source", "mask-border-width" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - mask border */"
                }

                // Mask clip/composite/origin/type
                "mask-clip", "mask-composite", "mask-origin", "mask-type" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - mask property */"
                }

                // Max lines
                "max-lines" -> prop.lengths.firstOrNull()?.let {
                    "/* max-lines: ${it.value} - use Text(maxLines = ${it.value?.toInt()}) */"
                }

                // Nav index/up/down/left/right (deprecated)
                "nav-down", "nav-index", "nav-left", "nav-right", "nav-up" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - deprecated navigation */"
                }

                // Overlay (experimental)
                "overlay" -> prop.keywords.firstOrNull()?.let {
                    "/* overlay: ${it.value} - top layer control */"
                }

                // Perspective
                "perspective", "perspective-origin" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - use graphicsLayer 3D transforms */"
                }

                // Position fallback
                "position-fallback" -> prop.keywords.firstOrNull()?.let {
                    "/* position-fallback: ${it.value} - experimental positioning */"
                }

                // Position visibility
                "position-visibility" -> prop.keywords.firstOrNull()?.let {
                    "/* position-visibility: ${it.value} - anchor positioning visibility */"
                }

                // Regions (experimental)
                "region-fragment" -> prop.keywords.firstOrNull()?.let {
                    "/* region-fragment: ${it.value} - CSS regions not supported */"
                }

                // Scale
                "scale" -> prop.lengths.firstOrNull()?.let {
                    val scale = it.value ?: 1.0
                    if (scale != 1.0) "scale(${scale}f)" else null
                }

                // Scrollbar color/width/gutter
                "scrollbar-color", "scrollbar-gutter", "scrollbar-width" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - scrollbar styling */"
                }

                // Shape (SVG/clipping)
                "shape", "shape-image-threshold", "shape-margin", "shape-outside" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - shape wrapping */"
                }

                // Shape rendering (SVG)
                "shape-rendering" -> prop.keywords.firstOrNull()?.let {
                    "/* shape-rendering: ${it.value} - SVG rendering hint */"
                }

                // Speak (audio CSS)
                "speak", "speak-as" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - audio CSS */"
                }

                // Stop color/opacity (SVG gradient)
                "stop-color", "stop-opacity" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - SVG gradient stop */"
                }

                // String set (generated content)
                "string-set" -> prop.raw?.let { "/* string-set: $it - generated content */" }

                // Tab size (already handled above)

                // Text align all/last
                "text-align-all", "text-align-last" -> {
                    prop.keywords.firstOrNull()?.let { "/* ${prop.propertyName}: ${it.value} - use TextAlign */" }
                }

                // Text combine upright
                "text-combine-upright" -> prop.keywords.firstOrNull()?.let {
                    "/* text-combine-upright: ${it.value} - vertical text */"
                }

                // Text decoration (additional variants)
                "text-decoration-color", "text-decoration-skip", "text-decoration-skip-ink",
                "text-decoration-style", "text-decoration-thickness" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - text decoration styling */"
                }

                // Text orientation
                "text-orientation" -> prop.keywords.firstOrNull()?.let {
                    "/* text-orientation: ${it.value} - vertical text orientation */"
                }

                // Text rendering
                "text-rendering" -> prop.keywords.firstOrNull()?.let {
                    "/* text-rendering: ${it.value} - text optimization hint */"
                }

                // Text size adjust
                "text-size-adjust" -> prop.raw?.let {
                    "/* text-size-adjust: $it - mobile text scaling */"
                }

                // Text underline offset/position
                "text-underline-offset", "text-underline-position" -> {
                    prop.raw?.let { "/* ${prop.propertyName}: $it - underline positioning */" }
                }

                // Transform box/origin/style
                "transform-box" -> prop.keywords.firstOrNull()?.let {
                    "/* transform-box: ${it.value} - use graphicsLayer */"
                }
                "transform-origin" -> prop.raw?.let {
                    "/* transform-origin: $it - use graphicsLayer { transformOrigin = ... } */"
                }
                "transform-style" -> prop.keywords.firstOrNull()?.let {
                    "/* transform-style: ${it.value} - 3D transform context */"
                }

                // Translate
                "translate" -> prop.raw?.let {
                    "/* translate: $it - use offset() modifier */"
                }

                // Vector effect (SVG)
                "vector-effect" -> prop.keywords.firstOrNull()?.let {
                    "/* vector-effect: ${it.value} - SVG scaling behavior */"
                }

                // Voice (audio CSS)
                "voice-balance", "voice-duration", "voice-family", "voice-pitch",
                "voice-range", "voice-rate", "voice-stress", "voice-volume" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - audio CSS speech */"
                }

                // Webkit text (additional)
                "-webkit-text-fill-color" -> prop.colors.firstOrNull()?.let {
                    "/* -webkit-text-fill-color: ${convertColor(it)} - use Brush in TextStyle */"
                }
                "-webkit-text-stroke", "-webkit-text-stroke-color", "-webkit-text-stroke-width" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - text stroke effect */"
                }

                // Border individual sides (additional coverage)
                "border-left", "border-right", "border-top", "border-bottom" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - individual border side */"
                }
                "border-top-style", "border-right-style", "border-bottom-style", "border-left-style" -> {
                    prop.keywords.firstOrNull()?.let { "/* border-*-style: ${it.value} - property not yet mapped to Compose */" }
                }
                "border-top-color", "border-right-color", "border-bottom-color", "border-left-color" -> {
                    prop.colors.firstOrNull()?.let {
                        val color = convertColor(it)
                        "/* ${prop.propertyName}: $color - individual side colors in states require drawWithContent with remember { mutableStateOf() } for dynamic colors */"
                    }
                }

                // Background position (variants)
                "background-position-x", "background-position-y" -> {
                    prop.lengths.firstOrNull()?.let { "/* ${prop.propertyName}: ${convertLength(it)} - property not yet mapped to Compose */" }
                }
                "background-position-inline", "background-position-block" -> {
                    prop.lengths.firstOrNull()?.let { "/* ${prop.propertyName}: ${convertLength(it)} - logical background position */" }
                }

                // Align content (grid/flex)
                "align-content" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "flex-start", "start" -> "/* align-content: start - use verticalArrangement = Arrangement.Top */"
                        "flex-end", "end" -> "/* align-content: end - use verticalArrangement = Arrangement.Bottom */"
                        "center" -> "/* align-content: center - use verticalArrangement = Arrangement.Center */"
                        "space-between" -> "/* align-content: space-between - use verticalArrangement = Arrangement.SpaceBetween */"
                        "space-around" -> "/* align-content: space-around - use verticalArrangement = Arrangement.SpaceAround */"
                        "space-evenly" -> "/* align-content: space-evenly - use verticalArrangement = Arrangement.SpaceEvenly */"
                        "stretch" -> "/* align-content: stretch - children fill space */"
                        else -> null
                    }
                }

                // Align tracks (CSS Grid Level 3)
                "align-tracks" -> prop.keywords.firstOrNull()?.let {
                    "/* align-tracks: ${it.value} - experimental grid property */"
                }
                "justify-tracks" -> prop.keywords.firstOrNull()?.let {
                    "/* justify-tracks: ${it.value} - experimental grid property */"
                }

                // Animation composition
                "animation-composition" -> prop.keywords.firstOrNull()?.let {
                    "/* animation-composition: ${it.value} - use Animatable composition */"
                }

                // Animation trigger (scroll-driven animations)
                "animation-trigger" -> prop.keywords.firstOrNull()?.let {
                    "/* animation-trigger: ${it.value} - experimental scroll trigger */"
                }

                // Border boundary/clip (experimental)
                "border-boundary" -> prop.keywords.firstOrNull()?.let {
                    "/* border-boundary: ${it.value} - experimental */"
                }
                "border-clip" -> prop.keywords.firstOrNull()?.let {
                    "/* border-clip: ${it.value} - experimental border clipping */"
                }

                // Box snap (CSS Scroll Snap)
                "box-snap" -> prop.keywords.firstOrNull()?.let {
                    "/* box-snap: ${it.value} - use scroll snap alignment */"
                }

                // Color interpolation (SVG)
                "color-interpolation", "color-interpolation-filters" -> {
                    prop.keywords.firstOrNull()?.let { "/* ${prop.propertyName}: ${it.value} - SVG color interpolation */" }
                }

                // SVG geometry properties
                "cx", "cy", "r", "rx", "ry", "x", "y" -> {
                    prop.lengths.firstOrNull()?.let { "/* ${prop.propertyName}: ${convertLength(it)} - SVG geometry */" }
                }
                "d" -> prop.raw?.let { "/* d: $it - SVG path data */" }

                // SVG rendering
                "enable-background" -> prop.keywords.firstOrNull()?.let {
                    "/* enable-background: ${it.value} - SVG filter region */"
                }

                // Font shorthand
                "font" -> prop.raw?.let { "/* font: $it - split into font-size, font-family, etc. */" }
                "font-variant" -> prop.keywords.firstOrNull()?.let {
                    "/* font-variant: ${it.value} - use font variant properties */"
                }

                // Footnote properties (paged media)
                "footnote-display", "footnote-policy" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - paged media footnotes */"
                }

                // Grid auto position
                "grid-auto-position" -> prop.keywords.firstOrNull()?.let {
                    "/* grid-auto-position: ${it.value} - experimental grid property */"
                }

                // Initial letters (drop cap variant)
                "initial-letters" -> prop.raw?.let { "/* initial-letters: $it - drop cap sizing */" }

                // Layout order
                "layout-order" -> prop.lengths.firstOrNull()?.let {
                    "/* layout-order: ${it.value} - rearrange composables */"
                }

                // Marker side (list styling)
                "marker-side" -> prop.keywords.firstOrNull()?.let {
                    "/* marker-side: ${it.value} - list marker positioning */"
                }

                // Masonry layout (experimental)
                "masonry-auto-flow" -> prop.keywords.firstOrNull()?.let {
                    "/* masonry-auto-flow: ${it.value} - experimental masonry grid */"
                }

                // Moz appearance
                "-moz-appearance" -> prop.keywords.firstOrNull()?.let {
                    "/* -moz-appearance: ${it.value} - Firefox vendor prefix */"
                }

                // Overflow variants (additional)
                "overflow-anchor" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "none" -> "/* overflow-anchor: none - disable scroll anchoring */"
                        "auto" -> null // default
                        else -> null
                    }
                }
                "overflow-block", "overflow-inline" -> {
                    prop.keywords.firstOrNull()?.let { "/* ${prop.propertyName}: ${it.value} - logical overflow */" }
                }

                // Page properties (paged media)
                "page" -> prop.keywords.firstOrNull()?.let { "/* page: ${it.value} - named page for printing */" }
                "page-orientation" -> prop.keywords.firstOrNull()?.let {
                    "/* page-orientation: ${it.value} - print orientation */"
                }

                // Resolution (media query)
                "resolution" -> prop.raw?.let { "/* resolution: $it - use density-specific resources */" }

                // Rest (audio CSS)
                "rest", "rest-after", "rest-before" -> {
                    prop.raw?.let { "/* ${prop.propertyName}: $it - audio CSS pause */" }
                }

                // Scroll start (new scroll positioning)
                "scroll-start", "scroll-start-block", "scroll-start-inline",
                "scroll-start-target", "scroll-start-target-block", "scroll-start-target-inline" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - initial scroll position */"
                }

                // Scrollbar colors (IE/Edge legacy)
                "scrollbar-3dlight-color", "scrollbar-arrow-color", "scrollbar-base-color",
                "scrollbar-darkshadow-color", "scrollbar-face-color", "scrollbar-highlight-color",
                "scrollbar-shadow-color", "scrollbar-track-color" -> {
                    prop.colors.firstOrNull()?.let { "/* ${prop.propertyName}: ${convertColor(it)} - legacy IE scrollbar colors */" }
                        ?: "/* ${prop.propertyName}: ${prop.raw} - legacy IE scrollbar colors */"
                }

                // Spatial navigation (experimental)
                "spatial-navigation-action", "spatial-navigation-contain", "spatial-navigation-function" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - experimental spatial navigation */"
                }

                // Text space (experimental)
                "text-space-collapse", "text-space-trim" -> {
                    prop.keywords.firstOrNull()?.let { "/* ${prop.propertyName}: ${it.value} - experimental text spacing */" }
                }
                "text-group-align" -> prop.keywords.firstOrNull()?.let {
                    "/* text-group-align: ${it.value} - align text groups */"
                }

                // Unicode range (font loading)
                "unicode-range" -> prop.raw?.let { "/* unicode-range: $it - font unicode subset */" }

                // User modify (contenteditable)
                "user-modify" -> prop.keywords.firstOrNull()?.let {
                    "/* user-modify: ${it.value} - use TextField for editable content */"
                }

                // White space variants (experimental)
                "white-space-collapse", "white-space-trim" -> {
                    prop.keywords.firstOrNull()?.let { "/* ${prop.propertyName}: ${it.value} - experimental white-space control */" }
                }

                // Wrap properties (CSS Regions/Exclusions)
                "wrap-after", "wrap-before", "wrap-inside" -> {
                    prop.keywords.firstOrNull()?.let { "/* ${prop.propertyName}: ${it.value} - CSS Regions wrapping */" }
                }
                "wrap-flow", "wrap-through" -> {
                    prop.keywords.firstOrNull()?.let { "/* ${prop.propertyName}: ${it.value} - CSS Exclusions wrapping */" }
                }

                // ============================================
                // VENDOR-PREFIXED PROPERTIES (-webkit-, -moz-, -ms-, -o-)
                // ============================================

                // WebKit vendor prefixes (standardized properties with webkit prefix for compatibility)
                "-webkit-align-content", "-webkit-align-items", "-webkit-align-self" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - use standard property without prefix */"
                }
                "-webkit-animation", "-webkit-animation-delay", "-webkit-animation-direction",
                "-webkit-animation-duration", "-webkit-animation-fill-mode", "-webkit-animation-iteration-count",
                "-webkit-animation-name", "-webkit-animation-play-state", "-webkit-animation-timing-function" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - use standard animation property */"
                }
                "-webkit-backface-visibility" -> prop.keywords.firstOrNull()?.let {
                    "/* -webkit-backface-visibility: ${it.value} - use standard backface-visibility */"
                }
                "-webkit-background-clip", "-webkit-background-origin", "-webkit-background-size" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - use standard property without prefix */"
                }
                "-webkit-border-radius", "-webkit-border-top-left-radius", "-webkit-border-top-right-radius",
                "-webkit-border-bottom-left-radius", "-webkit-border-bottom-right-radius" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - use standard border-radius */"
                }
                "-webkit-box-shadow" -> prop.shadows.firstOrNull()?.let {
                    "/* -webkit-box-shadow: ${prop.raw} - use standard box-shadow */"
                }
                "-webkit-box-sizing" -> prop.keywords.firstOrNull()?.let {
                    "/* -webkit-box-sizing: ${it.value} - use standard box-sizing */"
                }
                "-webkit-flex", "-webkit-flex-basis", "-webkit-flex-direction", "-webkit-flex-flow",
                "-webkit-flex-grow", "-webkit-flex-shrink", "-webkit-flex-wrap" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - use standard flex property */"
                }
                "-webkit-filter" -> prop.raw?.let { "/* -webkit-filter: $it - use standard filter */" }
                "-webkit-justify-content" -> prop.keywords.firstOrNull()?.let {
                    "/* -webkit-justify-content: ${it.value} - use standard justify-content */"
                }
                "-webkit-transform", "-webkit-transform-origin", "-webkit-transform-style" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - use standard transform property */"
                }
                "-webkit-transition", "-webkit-transition-delay", "-webkit-transition-duration",
                "-webkit-transition-property", "-webkit-transition-timing-function" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - use standard transition property */"
                }
                "-webkit-user-select" -> prop.keywords.firstOrNull()?.let {
                    "/* -webkit-user-select: ${it.value} - use standard user-select */"
                }

                // WebKit-specific (non-standard) properties
                "-webkit-appearance" -> prop.keywords.firstOrNull()?.let {
                    "/* -webkit-appearance: ${it.value} - use custom composables */"
                }
                "-webkit-app-region" -> prop.keywords.firstOrNull()?.let {
                    "/* -webkit-app-region: ${it.value} - Electron frameless window dragging */"
                }
                "-webkit-aspect-ratio" -> prop.raw?.let {
                    "/* -webkit-aspect-ratio: $it - use standard aspect-ratio */"
                }
                "-webkit-backdrop-filter" -> prop.raw?.let {
                    "/* -webkit-backdrop-filter: $it - use standard backdrop-filter */"
                }
                "-webkit-background-composite" -> prop.keywords.firstOrNull()?.let {
                    "/* -webkit-background-composite: ${it.value} - WebKit-specific compositing */"
                }
                "-webkit-border-fit" -> prop.keywords.firstOrNull()?.let {
                    "/* -webkit-border-fit: ${it.value} - WebKit-specific border fitting */"
                }
                "-webkit-border-horizontal-spacing", "-webkit-border-vertical-spacing" -> {
                    prop.lengths.firstOrNull()?.let { "/* ${prop.propertyName}: ${convertLength(it)} - WebKit table border spacing */" }
                }
                "-webkit-box-align", "-webkit-box-orient", "-webkit-box-pack" -> {
                    prop.keywords.firstOrNull()?.let { "/* ${prop.propertyName}: ${it.value} - legacy flexbox, use standard flex */" }
                }
                "-webkit-box-flex" -> prop.lengths.firstOrNull()?.let {
                    "/* -webkit-box-flex: ${it.value} - legacy flexbox, use flex-grow */"
                }
                "-webkit-box-ordinal-group" -> prop.lengths.firstOrNull()?.let {
                    "/* -webkit-box-ordinal-group: ${it.value} - legacy flexbox, use order */"
                }
                "-webkit-box-reflect" -> prop.raw?.let {
                    "/* -webkit-box-reflect: $it - WebKit reflection effect */"
                }
                "-webkit-column-break-after", "-webkit-column-break-before", "-webkit-column-break-inside" -> {
                    prop.keywords.firstOrNull()?.let { "/* ${prop.propertyName}: ${it.value} - use standard break properties */" }
                }
                "-webkit-cursor-visibility" -> prop.keywords.firstOrNull()?.let {
                    "/* -webkit-cursor-visibility: ${it.value} - WebKit-specific */"
                }
                "-webkit-dashboard-region" -> prop.raw?.let {
                    "/* -webkit-dashboard-region: $it - macOS Dashboard widget */"
                }
                "-webkit-font-size-delta" -> prop.lengths.firstOrNull()?.let {
                    "/* -webkit-font-size-delta: ${convertLength(it, useSp = true)} - WebKit font adjustment */"
                }
                "-webkit-font-smoothing" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "antialiased" -> "/* -webkit-font-smoothing: antialiased - font rendering hint */"
                        "subpixel-antialiased" -> "/* -webkit-font-smoothing: subpixel-antialiased - font rendering hint */"
                        else -> "/* -webkit-font-smoothing: ${it.value} - font rendering hint */"
                    }
                }
                "-webkit-highlight" -> prop.colors.firstOrNull()?.let {
                    "/* -webkit-highlight: ${convertColor(it)} - text highlight color */"
                }
                "-webkit-line-break" -> prop.keywords.firstOrNull()?.let {
                    "/* -webkit-line-break: ${it.value} - use standard line-break */"
                }
                "-webkit-mask", "-webkit-mask-image", "-webkit-mask-clip", "-webkit-mask-composite",
                "-webkit-mask-origin", "-webkit-mask-position", "-webkit-mask-repeat", "-webkit-mask-size" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - WebKit masking */"
                }
                "-webkit-mask-box-image", "-webkit-mask-box-image-outset", "-webkit-mask-box-image-repeat",
                "-webkit-mask-box-image-slice", "-webkit-mask-box-image-source", "-webkit-mask-box-image-width" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - WebKit border-image-style masking */"
                }
                "-webkit-perspective", "-webkit-perspective-origin" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - use standard perspective */"
                }
                "-webkit-perspective-origin-x", "-webkit-perspective-origin-y" -> {
                    prop.lengths.firstOrNull()?.let { "/* ${prop.propertyName}: ${convertLength(it)} - use perspective-origin */" }
                }
                "-webkit-tap-highlight-color" -> prop.colors.firstOrNull()?.let {
                    "/* -webkit-tap-highlight-color: ${convertColor(it)} - mobile tap highlight */"
                }
                "-webkit-text-security" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "none" -> null
                        "disc", "circle", "square" -> "/* -webkit-text-security: ${it.value} - password field masking */"
                        else -> "/* -webkit-text-security: ${it.value} - password field masking */"
                    }
                }
                "-webkit-user-drag" -> prop.keywords.firstOrNull()?.let {
                    "/* -webkit-user-drag: ${it.value} - drag behavior control */"
                }
                "-webkit-user-modify" -> prop.keywords.firstOrNull()?.let {
                    "/* -webkit-user-modify: ${it.value} - contenteditable control */"
                }

                // Mozilla (Firefox) vendor prefixes
                "-moz-binding" -> prop.raw?.let {
                    "/* -moz-binding: $it - XBL binding (deprecated) */"
                }
                "-moz-border-radius" -> prop.lengths.firstOrNull()?.let {
                    "/* -moz-border-radius: ${convertLength(it)} - use standard border-radius */"
                }
                "-moz-box-align", "-moz-box-direction", "-moz-box-flex", "-moz-box-ordinal-group",
                "-moz-box-orient", "-moz-box-pack" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - legacy flexbox, use standard flex */"
                }
                "-moz-box-sizing" -> prop.keywords.firstOrNull()?.let {
                    "/* -moz-box-sizing: ${it.value} - use standard box-sizing */"
                }
                "-moz-user-select" -> prop.keywords.firstOrNull()?.let {
                    "/* -moz-user-select: ${it.value} - use standard user-select */"
                }

                // Microsoft (IE/Edge) vendor prefixes
                "-ms-accelerator" -> prop.keywords.firstOrNull()?.let {
                    "/* -ms-accelerator: ${it.value} - IE keyboard shortcut underline */"
                }
                "-ms-block-progression" -> prop.keywords.firstOrNull()?.let {
                    "/* -ms-block-progression: ${it.value} - writing mode */"
                }
                "-ms-content-zoom-chaining", "-ms-content-zooming" -> {
                    prop.keywords.firstOrNull()?.let { "/* ${prop.propertyName}: ${it.value} - IE touch zoom */" }
                }
                "-ms-content-zoom-limit", "-ms-content-zoom-limit-max", "-ms-content-zoom-limit-min" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - IE zoom limits */"
                }
                "-ms-content-zoom-snap", "-ms-content-zoom-snap-points", "-ms-content-zoom-snap-type" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - IE zoom snap points */"
                }
                "-ms-filter" -> prop.raw?.let {
                    "/* -ms-filter: $it - IE legacy filter effects */"
                }
                "-ms-flex", "-ms-flex-align", "-ms-flex-direction", "-ms-flex-order",
                "-ms-flex-pack", "-ms-flex-wrap" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - IE flexbox, use standard flex */"
                }
                "-ms-flow-from", "-ms-flow-into" -> {
                    prop.keywords.firstOrNull()?.let { "/* ${prop.propertyName}: ${it.value} - IE CSS Regions */" }
                }
                "-ms-grid-column", "-ms-grid-column-align", "-ms-grid-column-span", "-ms-grid-columns",
                "-ms-grid-row", "-ms-grid-row-align", "-ms-grid-row-span", "-ms-grid-rows" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - IE grid, use standard grid */"
                }
                "-ms-high-contrast-adjust" -> prop.keywords.firstOrNull()?.let {
                    "/* -ms-high-contrast-adjust: ${it.value} - IE high contrast mode */"
                }
                "-ms-hyphenate-limit-chars", "-ms-hyphenate-limit-lines", "-ms-hyphenate-limit-zone" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - IE hyphenation limits */"
                }
                "-ms-hyphens" -> prop.keywords.firstOrNull()?.let {
                    "/* -ms-hyphens: ${it.value} - use standard hyphens */"
                }
                "-ms-ime-align" -> prop.keywords.firstOrNull()?.let {
                    "/* -ms-ime-align: ${it.value} - IE IME alignment */"
                }
                "-ms-interpolation-mode" -> prop.keywords.firstOrNull()?.let {
                    "/* -ms-interpolation-mode: ${it.value} - IE image scaling */"
                }
                "-ms-overflow-style" -> prop.keywords.firstOrNull()?.let {
                    when (it.value) {
                        "none" -> "/* -ms-overflow-style: none - hide scrollbar */"
                        "scrollbar" -> "/* -ms-overflow-style: scrollbar - default scrollbar */"
                        "-ms-autohiding-scrollbar" -> "/* -ms-overflow-style: -ms-autohiding-scrollbar - auto-hide scrollbar */"
                        else -> "/* -ms-overflow-style: ${it.value} - scrollbar style */"
                    }
                }
                "-ms-scroll-chaining", "-ms-scroll-rails", "-ms-scroll-translation" -> {
                    prop.keywords.firstOrNull()?.let { "/* ${prop.propertyName}: ${it.value} - IE scroll behavior */" }
                }
                "-ms-scroll-limit", "-ms-scroll-limit-x-max", "-ms-scroll-limit-x-min",
                "-ms-scroll-limit-y-max", "-ms-scroll-limit-y-min" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - IE scroll limits */"
                }
                "-ms-scroll-snap-points-x", "-ms-scroll-snap-points-y", "-ms-scroll-snap-type",
                "-ms-scroll-snap-x", "-ms-scroll-snap-y" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - IE scroll snap, use standard scroll-snap */"
                }
                "-ms-text-autospace" -> prop.keywords.firstOrNull()?.let {
                    "/* -ms-text-autospace: ${it.value} - IE text spacing */"
                }
                "-ms-touch-action" -> prop.keywords.firstOrNull()?.let {
                    "/* -ms-touch-action: ${it.value} - use standard touch-action */"
                }
                "-ms-touch-select" -> prop.keywords.firstOrNull()?.let {
                    "/* -ms-touch-select: ${it.value} - IE touch selection */"
                }
                "-ms-user-select" -> prop.keywords.firstOrNull()?.let {
                    "/* -ms-user-select: ${it.value} - use standard user-select */"
                }
                "-ms-wrap-flow", "-ms-wrap-margin", "-ms-wrap-through" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - IE CSS Exclusions */"
                }

                // Opera vendor prefixes
                "-o-object-fit" -> prop.keywords.firstOrNull()?.let {
                    "/* -o-object-fit: ${it.value} - use standard object-fit */"
                }
                "-o-object-position" -> prop.raw?.let {
                    "/* -o-object-position: $it - use standard object-position */"
                }
                "-o-transform", "-o-transform-origin" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - use standard transform */"
                }
                "-o-transition", "-o-transition-delay", "-o-transition-duration",
                "-o-transition-property", "-o-transition-timing-function" -> {
                    "/* ${prop.propertyName}: ${prop.raw} - use standard transition */"
                }

                // Default case for any unhandled property
                else -> {
                    // For any unhandled property, add a comment with the raw value
                    val rawValue = prop.raw
                    if (rawValue != null && rawValue.isNotBlank()) {
                        "/* ${prop.propertyName}: $rawValue - property not yet mapped to Compose */"
                    } else {
                        null
                    }
                }
            }

            modifier?.let { modifiers.add(it) }
        }

        return modifiers
    }

    private fun buildState(selector: IRSelector): ComposeState {
        val stateName = when (selector.condition) {
            "hover" -> "hover"
            "active" -> "pressed"
            "focus" -> "focused"
            else -> selector.condition
        }

        val modifiers = buildModifiers(selector.properties)

        return ComposeState(
            name = stateName,
            modifiers = modifiers
        )
    }

    private fun buildResponsive(media: IRMedia): ComposeResponsive {
        val modifiers = buildModifiers(media.properties)

        return ComposeResponsive(
            condition = media.query,
            modifiers = modifiers
        )
    }

    private fun convertLength(length: IRLength, useSp: Boolean = false): String {
        val value = length.value
        val unit = length.unit

        return when (unit) {
            IRLength.LengthUnit.PX, IRLength.LengthUnit.DP -> "${value.toInt()}.${if (useSp) "sp" else "dp"}"
            IRLength.LengthUnit.SP -> "${value.toInt()}.sp"
            IRLength.LengthUnit.REM, IRLength.LengthUnit.EM -> "${value}.em"
            IRLength.LengthUnit.PERCENT -> "${(value / 100)}f"
            else -> "${value.toInt()}.${if (useSp) "sp" else "dp"}"
        }
    }

    private fun convertColor(color: IRColor): String {
        // Use normalized sRGB if available (handles all static color formats)
        color.srgb?.let { srgb ->
            val a = (srgb.a * 255).toInt().coerceIn(0, 255)
            val r = (srgb.r * 255).toInt().coerceIn(0, 255)
            val g = (srgb.g * 255).toInt().coerceIn(0, 255)
            val b = (srgb.b * 255).toInt().coerceIn(0, 255)
            return "Color(0x%02X%02X%02X%02X)".format(a, r, g, b)
        }

        // Fallback for dynamic colors that couldn't be normalized
        return when (val repr = color.representation) {
            is IRColor.ColorRepresentation.Transparent -> "Color.Transparent"
            is IRColor.ColorRepresentation.CurrentColor -> "LocalContentColor.current"
            is IRColor.ColorRepresentation.ColorMix -> "/* color-mix() - runtime evaluation needed */"
            is IRColor.ColorRepresentation.LightDark -> "/* light-dark() - use MaterialTheme.colorScheme */"
            else -> "/* dynamic color: ${color.raw ?: "unknown"} */"
        }
    }

    private fun convertShadow(shadow: Any): String {
        return when (shadow) {
            is app.irmodels.properties.borders.BoxShadowProperty.Shadow -> {
                val elevation = shadow.blurRadius?.value ?: shadow.offsetY.value
                // Use simplified color conversion with normalized sRGB
                val color = shadow.color?.let { irColor ->
                    "Color(0x${PropertyValueExtractor.colorToHexStringWithAlpha(irColor)})"
                } ?: "Color.Black"
                // Compose shadow() uses elevation (blur radius approximation) and ambientColor/spotColor
                "shadow(elevation = ${elevation.toInt()}.dp, ambientColor = $color, spotColor = $color)"
            }
            else -> "/* box-shadow - unsupported format */"
        }
    }

    private fun generateComposableCode(
        name: String,
        baseModifiers: List<String>,
        states: List<ComposeState>,
        responsive: List<ComposeResponsive>,
        textProperties: TextStyleProperties
    ): String {
        return buildString {
            appendLine("@Composable")
            appendLine("fun $name(modifier: Modifier = Modifier) {")

            // If we have text properties, wrap with ProvideTextStyle
            if (textProperties.hasAnyProperty()) {
                appendLine("    ProvideTextStyle(")
                appendLine("        value = LocalTextStyle.current.copy(")

                val styleProps = mutableListOf<String>()
                textProperties.color?.let { styleProps.add("color = $it") }
                textProperties.fontSize?.let { styleProps.add("fontSize = $it") }
                textProperties.fontWeight?.let {
                    val weight = when (it.lowercase()) {
                        "bold", "700" -> "FontWeight.Bold"
                        "normal", "400" -> "FontWeight.Normal"
                        "light", "300" -> "FontWeight.Light"
                        "medium", "500" -> "FontWeight.Medium"
                        "semibold", "600" -> "FontWeight.SemiBold"
                        "thin", "100" -> "FontWeight.Thin"
                        "extralight", "200" -> "FontWeight.ExtraLight"
                        "extrabold", "800" -> "FontWeight.ExtraBold"
                        "black", "900" -> "FontWeight.Black"
                        else -> it.toIntOrNull()?.let { num ->
                            when {
                                num <= 100 -> "FontWeight.Thin"
                                num <= 200 -> "FontWeight.ExtraLight"
                                num <= 300 -> "FontWeight.Light"
                                num <= 400 -> "FontWeight.Normal"
                                num <= 500 -> "FontWeight.Medium"
                                num <= 600 -> "FontWeight.SemiBold"
                                num <= 700 -> "FontWeight.Bold"
                                num <= 800 -> "FontWeight.ExtraBold"
                                else -> "FontWeight.Black"
                            }
                        } ?: "FontWeight.Normal"
                    }
                    styleProps.add("fontWeight = $weight")
                }
                textProperties.textAlign?.let {
                    val align = when (it.lowercase()) {
                        "center" -> "TextAlign.Center"
                        "left" -> "TextAlign.Left"
                        "right" -> "TextAlign.Right"
                        "justify" -> "TextAlign.Justify"
                        "start" -> "TextAlign.Start"
                        "end" -> "TextAlign.End"
                        else -> "TextAlign.Start"
                    }
                    styleProps.add("textAlign = $align")
                }
                textProperties.lineHeight?.let { styleProps.add("lineHeight = $it") }
                textProperties.letterSpacing?.let { styleProps.add("letterSpacing = $it") }

                styleProps.forEachIndexed { index, prop ->
                    append("            $prop")
                    if (index < styleProps.size - 1) appendLine(",")
                    else appendLine()
                }

                appendLine("        )")
                appendLine("    ) {")
                appendLine("        Box(")
                append("            modifier = modifier")
                baseModifiers.forEach { mod ->
                    if (!mod.startsWith("/*")) {
                        appendLine()
                        append("                .$mod")
                    }
                }
                appendLine()
                appendLine("        )")
                appendLine("    }")
            } else {
                // No text properties, use simple Box
                appendLine("    Box(")
                append("        modifier = modifier")
                baseModifiers.forEach { mod ->
                    if (!mod.startsWith("/*")) {
                        appendLine()
                        append("            .$mod")
                    }
                }
                appendLine()
                appendLine("    )")
            }

            appendLine("}")
        }
    }

    private fun generateImports(): List<String> {
        return listOf(
            "androidx.compose.runtime.Composable",
            "androidx.compose.ui.Modifier",
            "androidx.compose.foundation.layout.*",
            "androidx.compose.foundation.background",
            "androidx.compose.foundation.border",
            "androidx.compose.foundation.shape.RoundedCornerShape",
            "androidx.compose.foundation.shape.RectangleShape",
            "androidx.compose.ui.draw.clip",
            "androidx.compose.ui.draw.alpha",
            "androidx.compose.ui.draw.shadow",
            "androidx.compose.ui.draw.rotate",
            "androidx.compose.ui.draw.scale",
            "androidx.compose.ui.graphics.Color",
            "androidx.compose.ui.graphics.Brush",
            "androidx.compose.ui.unit.dp",
            "androidx.compose.ui.unit.sp",
            "androidx.compose.ui.text.font.FontStyle",
            "androidx.compose.ui.text.font.FontWeight",
            "androidx.compose.ui.text.style.TextDecoration",
            "androidx.compose.ui.text.style.TextAlign",
            "androidx.compose.foundation.verticalScroll",
            "androidx.compose.foundation.horizontalScroll",
            "androidx.compose.foundation.rememberScrollState",
            "androidx.compose.ui.zIndex",
            "androidx.compose.ui.Alignment",
            "androidx.compose.foundation.layout.Arrangement",
            "androidx.compose.material3.LocalTextStyle",
            "androidx.compose.material3.ProvideTextStyle"
        )
    }
}
