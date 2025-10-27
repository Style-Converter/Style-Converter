# Full CSS Property Implementation Plan

## Goal
Implement EVERY CSS property (except "not applicable") as actual Compose code instead of comments.

## Architecture Overview

### Component Generation Pipeline

```
CSS Properties
    ↓
Property Analysis (classify by type)
    ↓
Dependency Resolution (determine wrapper needs)
    ↓
Component Structure Generation
    ↓
Code Generation (6 passes)
```

## Property Classification

### Category 1: Simple Modifiers (✅ Already Working)
**Count:** ~110 properties
**Examples:** `padding`, `margin`, `width`, `height`, `background-color`, `border-radius`
**Implementation:** Direct modifier mapping
**Status:** Keep as-is

### Category 2: Layout Wrappers
**Count:** ~150 properties
**Examples:** `display: flex`, `justify-content`, `position: absolute`, `overflow: scroll`

**Subcategories:**

#### 2.1 Flexbox/Grid Layout
```kotlin
// CSS: display: flex; justify-content: space-between; align-items: center;
Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
) {
    content()
}
```

**Properties Handled:**
- `display: flex` → `Row {}` or `Column {}`
- `flex-direction: row` → `Row {}`
- `flex-direction: column` → `Column {}`
- `flex-direction: row-reverse` → `Row(reverseLayout = true)`
- `justify-content: start/center/end/space-between/space-around/space-evenly`
- `align-items: start/center/end/stretch`
- `align-content` (for multi-line flex)
- `gap`, `row-gap`, `column-gap` → `Arrangement.spacedBy()`
- `flex-grow` → `Modifier.weight(1f)`
- `flex-shrink` → `Modifier.weight(fill = false)`
- `flex-wrap` → `FlowRow {}` or `FlowColumn {}`

**Decision Logic:**
```kotlin
fun needsFlexWrapper(properties: Map<String, Property>): Boolean {
    return properties.containsKey("display") &&
           properties["display"]?.value == "flex" ||
           properties.keys.any { it.startsWith("flex-") ||
                                  it in listOf("justify-content", "align-items", "gap") }
}

fun getFlexDirection(properties: Map<String, Property>): FlexDirection {
    return when (properties["flex-direction"]?.value) {
        "row", null -> FlexDirection.ROW
        "row-reverse" -> FlexDirection.ROW_REVERSE
        "column" -> FlexDirection.COLUMN
        "column-reverse" -> FlexDirection.COLUMN_REVERSE
        else -> FlexDirection.ROW
    }
}
```

#### 2.2 Positioned Layout
```kotlin
// CSS: position: absolute; top: 20px; left: 20px;
Box(modifier = Modifier.fillMaxSize()) {
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = 20.dp, y = 20.dp)
    ) {
        content()
    }
}
```

**Properties Handled:**
- `position: absolute` → `Box` with alignment + offset
- `position: fixed` → `Box` at root level
- `position: relative` → No wrapper (use `Modifier.offset()`)
- `position: sticky` → Custom implementation with scroll state
- `top`, `right`, `bottom`, `left` → Calculate `offset()` and `Alignment`
- `z-index` → `Modifier.zIndex()`

**Decision Logic:**
```kotlin
fun needsPositionWrapper(properties: Map<String, Property>): PositionType? {
    val position = properties["position"]?.value
    return when (position) {
        "absolute" -> PositionType.ABSOLUTE
        "fixed" -> PositionType.FIXED
        "sticky" -> PositionType.STICKY
        "relative" -> if (properties.keys.any { it in listOf("top", "left", "right", "bottom") })
                        PositionType.RELATIVE else null
        else -> null
    }
}
```

#### 2.3 Scrollable Layout
```kotlin
// CSS: overflow: scroll;
Column(
    modifier = Modifier.verticalScroll(rememberScrollState())
) {
    content()
}

// CSS: overflow-x: scroll;
Row(
    modifier = Modifier.horizontalScroll(rememberScrollState())
) {
    content()
}
```

**Properties Handled:**
- `overflow: scroll/auto` → `verticalScroll()` + `horizontalScroll()`
- `overflow-x: scroll` → `horizontalScroll()`
- `overflow-y: scroll` → `verticalScroll()`
- `overflow: hidden` → `Modifier.clip(RectangleShape)`
- `scroll-behavior: smooth` → Use `animateScrollTo()`
- `overscroll-behavior` → `nestedScroll` modifier

### Category 3: State Management
**Count:** ~80 properties
**Examples:** `animation-*`, `transition-*`, `:hover`, `:active`

#### 3.1 Animations
```kotlin
@Composable
fun AnimatedComponent(
    modifier: Modifier = Modifier,
    animationSpec: AnimationSpec<Float> = tween(2000, easing = FastOutSlowInEasing),
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(true) }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = animationSpec
    )

    Box(modifier = modifier.alpha(alpha)) {
        content()
    }
}
```

**Properties Handled:**
- `animation`, `animation-name` → Component name/state setup
- `animation-duration` → `tween(durationMillis = ...)`
- `animation-timing-function` → `tween(easing = ...)` or `spring()`
- `animation-delay` → `keyframes {}` or custom delay
- `animation-iteration-count` → `infiniteRepeatable()` or `repeatable(n)`
- `animation-direction` → `RepeatMode.Reverse`
- `transition` → `animateFloatAsState()` or `updateTransition()`
- `transition-property` → Which properties to animate
- `transition-duration` → Animation spec duration
- `transition-timing-function` → Easing function

#### 3.2 Interaction States
```kotlin
@Composable
fun InteractiveComponent(
    modifier: Modifier = Modifier,
    hoverColor: Color = Color.Blue,
    normalColor: Color = Color.Gray,
    content: @Composable () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    val backgroundColor by animateColorAsState(
        when {
            isPressed -> hoverColor.copy(alpha = 0.8f)
            isHovered -> hoverColor
            else -> normalColor
        }
    )

    Box(
        modifier = modifier
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { isPressed = true; tryAwaitRelease(); isPressed = false },
                )
            }
            .hoverable(interactionSource = remember { MutableInteractionSource() })
    ) {
        content()
    }
}
```

**Properties Handled:**
- `:hover` pseudo-class → State management + `hoverable()`
- `:active` pseudo-class → `detectTapGestures(onPress = ...)`
- `:focus` pseudo-class → `focusable()` + focus state
- `cursor: pointer` → `clickable {}`
- `cursor: *` → Pointer icon (if supported)

### Category 4: Custom Drawing
**Count:** ~60 properties
**Examples:** SVG properties, filters, complex gradients

#### 4.1 SVG Properties
```kotlin
@Composable
fun SvgCircle(
    cx: Dp,
    cy: Dp,
    r: Dp,
    fill: Color,
    stroke: Color? = null,
    strokeWidth: Dp = 1.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawCircle(
            color = fill,
            radius = r.toPx(),
            center = Offset(cx.toPx(), cy.toPx())
        )

        stroke?.let {
            drawCircle(
                color = it,
                radius = r.toPx(),
                center = Offset(cx.toPx(), cy.toPx()),
                style = Stroke(width = strokeWidth.toPx())
            )
        }
    }
}
```

**Properties Handled:**
- `cx`, `cy`, `r` → Circle drawing
- `rx`, `ry` → Ellipse/rounded rect
- `x`, `y`, `width`, `height` → Rectangle
- `d` (path data) → Parse SVG path commands to Compose Path
- `fill` → `drawPath(color = ...)`
- `stroke` → `drawPath(style = Stroke(...))`
- `stroke-width` → `Stroke(width = ...)`
- `stroke-dasharray` → `PathEffect.dashPathEffect()`
- `stroke-linecap` → `Stroke(cap = StrokeCap.*)`
- `stroke-linejoin` → `Stroke(join = StrokeJoin.*)`

#### 4.2 Filters
```kotlin
@Composable
fun BlurredComponent(
    blurRadius: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.graphicsLayer {
            renderEffect = BlurEffect(
                radiusX = blurRadius.toPx(),
                radiusY = blurRadius.toPx(),
                edgeTreatment = TileMode.Decal
            )
        }
    ) {
        content()
    }
}
```

**Properties Handled:**
- `filter: blur()` → `BlurEffect`
- `filter: brightness()` → `ColorMatrix` adjustments
- `filter: contrast()` → `ColorMatrix` adjustments
- `filter: saturate()` → `ColorMatrix` adjustments
- `backdrop-filter` → Custom blur implementation
- `mix-blend-mode` → `graphicsLayer { blendMode = BlendMode.* }`

#### 4.3 Complex Gradients
```kotlin
@Composable
fun GradientComponent(
    gradient: Brush,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.background(gradient)
    ) {
        content()
    }
}

// Usage:
// CSS: background-image: linear-gradient(to right, #3b82f6, #8b5cf6)
GradientComponent(
    gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF3b82f6), Color(0xFF8b5cf6)),
        start = Offset.Zero,
        end = Offset(Float.POSITIVE_INFINITY, 0f)
    )
)
```

### Category 5: Text Properties
**Count:** ~40 properties
**Examples:** `font-size`, `font-weight`, `color`, `text-align`

**Implementation:** Detect text properties and extract text content, then wrap in `Text()`:

```kotlin
@Composable
fun TextComponent(
    text: String,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle = FontStyle.Normal,
    color: Color = Color.Black,
    textAlign: TextAlign = TextAlign.Start,
    textDecoration: TextDecoration? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = fontWeight,
        fontStyle = fontStyle,
        color = color,
        textAlign = textAlign,
        textDecoration = textDecoration,
        lineHeight = lineHeight,
        letterSpacing = letterSpacing,
        modifier = modifier
    )
}
```

## Implementation Phases

### Phase 1: Refactor IR Structure ✓
**Status:** Already complete
**What:** ParsedStyle contains all necessary property data

### Phase 2: Property Analyzer
**New Class:** `PropertyAnalyzer.kt`

```kotlin
data class ComponentRequirements(
    val needsFlexWrapper: Boolean = false,
    val flexDirection: FlexDirection? = null,
    val needsPositionWrapper: PositionType? = null,
    val needsScrollWrapper: ScrollType? = null,
    val needsStateManagement: StateRequirements? = null,
    val needsCustomDrawing: DrawingRequirements? = null,
    val needsTextExtraction: Boolean = false,
    val simpleModifiers: List<String> = emptyList()
)

class PropertyAnalyzer {
    fun analyze(properties: Map<String, ParsedProperty>): ComponentRequirements {
        // Analyze all properties and determine requirements
    }
}
```

### Phase 3: Component Structure Builder
**New Class:** `ComponentStructureBuilder.kt`

```kotlin
data class ComponentStructure(
    val outerWrapper: WrapperType? = null,
    val innerWrapper: WrapperType? = null,
    val stateSetup: List<String> = emptyList(),
    val modifierChain: List<String> = emptyList(),
    val customDrawing: String? = null,
    val textComponent: TextConfig? = null
)

class ComponentStructureBuilder {
    fun build(requirements: ComponentRequirements): ComponentStructure {
        // Build component structure based on requirements
    }
}
```

### Phase 4: Code Generator Refactor
**Refactor:** `SimpleComposeBuilder.kt`

```kotlin
class AdvancedComposeBuilder {
    private val analyzer = PropertyAnalyzer()
    private val structureBuilder = ComponentStructureBuilder()

    fun generateComponent(style: ParsedStyle): ComponentCode {
        // 1. Analyze properties
        val requirements = analyzer.analyze(style.properties)

        // 2. Build structure
        val structure = structureBuilder.build(requirements)

        // 3. Generate code in 6 passes
        return generateCode(structure)
    }

    private fun generateCode(structure: ComponentStructure): ComponentCode {
        val code = StringBuilder()

        // Pass 1: Function signature
        code.append("@Composable\nfun ${structure.name}(")
        code.append("modifier: Modifier = Modifier")
        if (structure.stateSetup.isNotEmpty()) {
            // Add state parameters
        }
        code.append(", content: @Composable () -> Unit = {}")
        code.append(") {\n")

        // Pass 2: State management
        structure.stateSetup.forEach { code.append("    $it\n") }

        // Pass 3: Outer wrapper
        structure.outerWrapper?.let {
            code.append("    ${it.open}\n")
        }

        // Pass 4: Custom drawing
        structure.customDrawing?.let {
            code.append("    $it\n")
        }

        // Pass 5: Inner wrapper + modifiers
        structure.innerWrapper?.let {
            code.append("    ${it.open}(\n")
            code.append("        modifier = modifier\n")
            structure.modifierChain.forEach {
                code.append("            .$it\n")
            }
            code.append("    ) {\n")
        }

        // Pass 6: Text or content
        if (structure.textComponent != null) {
            code.append("        Text(...)\n")
        } else {
            code.append("        content()\n")
        }

        // Close all wrappers
        structure.innerWrapper?.let { code.append("    }\n") }
        structure.outerWrapper?.let { code.append("    }\n") }

        code.append("}\n")

        return ComponentCode(code.toString())
    }
}
```

### Phase 5: Testing
Create comprehensive test cases for each category.

### Phase 6: Documentation
Update all documentation with new capabilities.

## Priority Order

1. **Week 1:** Layout wrappers (flex, position, scroll) - Highest impact
2. **Week 2:** State management (animations, interactions)
3. **Week 3:** Custom drawing (SVG, filters)
4. **Week 4:** Text extraction and typography
5. **Week 5:** Edge cases and refinements
6. **Week 6:** Testing and documentation

## Open Questions

1. **Children content:** How do we extract and reorganize children in the IR?
2. **Text extraction:** How do we know which children are text vs. nested elements?
3. **Performance:** Will deeply nested wrappers impact performance?
4. **API design:** Should we expose all animation parameters, or use sensible defaults?
5. **Vendor prefixes:** Should we implement these separately or map to standard properties?

## Success Metrics

- [ ] 90%+ of standard CSS properties generate working Compose code
- [ ] All flexbox/grid layouts work correctly
- [ ] All position types work correctly
- [ ] Basic animations work
- [ ] SVG shapes render correctly
- [ ] Build time remains < 10 seconds
- [ ] Generated code is readable and idiomatic Kotlin
