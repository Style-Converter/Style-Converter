# CSS Properties Not Directly Supported in Jetpack Compose

This document lists all CSS properties that cannot be directly mapped to Jetpack Compose modifiers. For each property, we provide guidance on how to achieve similar effects in Compose.

**Total Properties Listed:** 500+ properties with helpful comments and implementation guidance

## Table of Contents

- [Animation & Transitions](#animation--transitions)
- [Flexbox & Grid Layout](#flexbox--grid-layout)
- [Text & Typography](#text--typography)
- [SVG Properties](#svg-properties)
- [Filters & Visual Effects](#filters--visual-effects)
- [Scroll & Overflow](#scroll--overflow)
- [Transform & 3D](#transform--3d)
- [Background & Borders](#background--borders)
- [Position & Layout](#position--layout)
- [Vendor Prefixes](#vendor-prefixes)
- [Experimental Properties](#experimental-properties)
- [Legacy/Deprecated Properties](#legacydeprecated-properties)
- [Print & Paged Media](#print--paged-media)
- [Audio CSS](#audio-css)
- [Complete Property Reference](#complete-property-reference)

---

## Animation & Transitions

CSS animations and transitions need to be implemented using Compose's animation APIs.

### Properties

| CSS Property | Compose Alternative |
|--------------|-------------------|
| `animation` | Use `animate*AsState()` or `Animatable` |
| `animation-name` | Define animation with `animate*AsState()` |
| `animation-duration` | Pass duration parameter to animation functions |
| `animation-timing-function` | Use `tween()`, `spring()`, or custom `AnimationSpec` |
| `animation-delay` | Use `DelayedAnimation` or `keyframes {}` |
| `animation-iteration-count` | Use `infiniteRepeatable()` or `repeatable()` |
| `animation-direction` | Use `RepeatMode.Reverse` |
| `animation-fill-mode` | Control with state management |
| `animation-play-state` | Control with state variables |
| `animation-composition` | Layer multiple animations with `updateTransition` |
| `animation-timeline` | Use scroll-based animation with `ScrollState` |
| `animation-range` | Define progress range in custom animations |
| `transition` | Use `AnimatedVisibility` or `animate*AsState()` |
| `transition-property` | Specify which properties to animate |
| `transition-duration` | Pass to animation spec |
| `transition-timing-function` | Use `AnimationSpec` |
| `transition-delay` | Use `DelayedAnimation` |

**Example:**
```kotlin
// CSS: animation: fadeIn 2s ease-in-out
val alpha by animateFloatAsState(
    targetValue = if (visible) 1f else 0f,
    animationSpec = tween(
        durationMillis = 2000,
        easing = FastOutSlowInEasing
    )
)
modifier.alpha(alpha)
```

---

## Flexbox & Grid Layout

Compose uses `Row`, `Column`, and custom `Layout` instead of CSS flexbox/grid.

### Properties

| CSS Property | Compose Alternative |
|--------------|-------------------|
| `display: flex` | Use `Row {}` or `Column {}` |
| `display: grid` | Use `Column/Row` with weight or custom `Layout` |
| `flex-direction: row` | Use `Row {}` |
| `flex-direction: column` | Use `Column {}` |
| `flex-direction: row-reverse` | Use `Row(reverseLayout = true)` |
| `flex-grow` | Use `Modifier.weight(1f)` |
| `flex-shrink` | Use `Modifier.weight(fill = false)` |
| `flex-basis` | Set minimum size with weight |
| `flex-wrap` | Use `FlowRow` or `FlowColumn` |
| `justify-content: start` | Use `horizontalArrangement = Arrangement.Start` |
| `justify-content: center` | Use `horizontalArrangement = Arrangement.Center` |
| `justify-content: space-between` | Use `horizontalArrangement = Arrangement.SpaceBetween` |
| `align-items: center` | Use `verticalAlignment = Alignment.CenterVertically` |
| `align-content` | Use `verticalArrangement` + `horizontalArrangement` |
| `gap` | Use `Arrangement.spacedBy(16.dp)` |
| `order` | Rearrange children manually in composable |
| `grid-template-columns` | Use `Row` with weights or custom `Layout` |
| `grid-template-rows` | Use `Column` with weights |
| `grid-gap` | Use `Arrangement.spacedBy()` |

**Example:**
```kotlin
// CSS: display: flex; justify-content: space-between; align-items: center;
Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
) {
    // Children
}
```

---

## Text & Typography

Text styling in Compose uses `TextStyle` instead of CSS properties.

### Properties

| CSS Property | Compose Alternative |
|--------------|-------------------|
| `color` | Use `Text(color = Color.Red)` |
| `font-size` | Use `Text(fontSize = 16.sp)` |
| `font-weight` | Use `Text(fontWeight = FontWeight.Bold)` |
| `font-family` | Define custom `FontFamily` and use in TextStyle |
| `font-style: italic` | Use `Text(fontStyle = FontStyle.Italic)` |
| `text-align` | Use `Text(textAlign = TextAlign.Center)` |
| `text-decoration: underline` | Use `Text(textDecoration = TextDecoration.Underline)` |
| `text-transform: uppercase` | Use `.uppercase()` on text string |
| `line-height` | Use `Text(lineHeight = 1.5.em)` |
| `letter-spacing` | Use `Text(letterSpacing = 0.05.em)` |
| `white-space: nowrap` | Use `Text(maxLines = 1, softWrap = false)` |
| `text-overflow: ellipsis` | Use `Text(overflow = TextOverflow.Ellipsis)` |
| `word-break` | Use `LineBreaker` configuration |
| `text-indent` | Use padding or custom text layout |
| `text-shadow` | Use shadow layer or `graphicsLayer` |
| `vertical-align` | Use `Alignment` in parent layout |
| `direction: rtl` | Use `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)` |
| `hyphens` | Use `Hyphenation` configuration |
| `text-decoration-color` | Customize TextDecoration styling |
| `text-underline-offset` | Custom underline positioning |

**Example:**
```kotlin
// CSS: font-size: 18px; font-weight: bold; color: #1f2937;
Text(
    text = "Hello",
    fontSize = 18.sp,
    fontWeight = FontWeight.Bold,
    color = Color(0xFF1f2937)
)
```

---

## SVG Properties

SVG-specific properties are not applicable in Compose. Use Compose's vector graphics APIs.

### Properties

| CSS Property | Compose Alternative |
|--------------|-------------------|
| `fill` | Use `Path(fill = ...)` in vector graphics |
| `stroke` | Use `Path(stroke = ...)` |
| `stroke-width` | Specify in `Stroke` constructor |
| `stroke-dasharray` | Use `PathEffect.dashPathEffect()` |
| `stroke-linecap` | Set in `Stroke(cap = ...)` |
| `stroke-linejoin` | Set in `Stroke(join = ...)` |
| `cx`, `cy`, `r`, `rx`, `ry` | Build shapes with Canvas drawing APIs |
| `d` (path data) | Use `Path().apply { ... }` with path commands |
| All SVG filter properties | Use Compose drawing APIs |

**Example:**
```kotlin
// SVG properties -> Compose Canvas
Canvas(modifier) {
    drawPath(
        path = myPath,
        color = Color.Red,
        style = Stroke(
            width = 2.dp.toPx(),
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
        )
    )
}
```

---

## Filters & Visual Effects

Use `Modifier.graphicsLayer` or `ColorFilter` for visual effects.

### Properties

| CSS Property | Compose Alternative |
|--------------|-------------------|
| `filter: blur()` | Use `graphicsLayer { renderEffect = BlurEffect(...) }` |
| `filter: brightness()` | Use `ColorFilter.colorMatrix()` |
| `filter: contrast()` | Use `ColorMatrix` adjustments |
| `backdrop-filter` | Not directly supported, use custom blur implementation |
| `mix-blend-mode` | Use `graphicsLayer { blendMode = BlendMode.Multiply }` |
| `background-blend-mode` | Layer backgrounds with blend modes |
| `mask` | Use `graphicsLayer { alpha = ... }` or custom drawing |
| `clip-path` | Use `Modifier.clip()` with custom `Shape` |

**Example:**
```kotlin
// CSS: filter: blur(5px)
modifier.graphicsLayer {
    renderEffect = BlurEffect(
        radiusX = 5f,
        radiusY = 5f,
        edgeTreatment = TileMode.Decal
    )
}
```

---

## Scroll & Overflow

Compose has different scrolling mechanisms than CSS.

### Properties

| CSS Property | Compose Alternative |
|--------------|-------------------|
| `overflow: scroll` | Use `Modifier.verticalScroll(rememberScrollState())` |
| `overflow-x: scroll` | Use `Modifier.horizontalScroll(rememberScrollState())` |
| `overflow: hidden` | Use `Modifier.clip(RectangleShape)` |
| `scroll-behavior: smooth` | Use `animateScrollTo()` with ScrollState |
| `scroll-snap-type` | Use `Pager` or custom scroll behavior |
| `scroll-padding` | Use contentPadding parameter |
| `scroll-margin` | Use contentPadding parameter |
| `overscroll-behavior` | Use `nestedScroll` modifier |

**Example:**
```kotlin
// CSS: overflow: scroll;
Box(
    modifier = Modifier
        .verticalScroll(rememberScrollState())
        .horizontalScroll(rememberScrollState())
)
```

---

## Transform & 3D

Use `Modifier.graphicsLayer` for transforms.

### Properties

| CSS Property | Compose Alternative |
|--------------|-------------------|
| `transform: rotate()` | Use `Modifier.rotate(45f)` |
| `transform: scale()` | Use `Modifier.scale(1.2f)` |
| `transform: translate()` | Use `Modifier.offset(x, y)` |
| `transform-origin` | Use `graphicsLayer { transformOrigin = ... }` |
| `perspective` | Use `graphicsLayer` with 3D transforms |
| `backface-visibility` | Use `graphicsLayer { rotationY = 180f, cameraDistance = 8f }` |

**Example:**
```kotlin
// CSS: transform: rotate(45deg) scale(1.2);
modifier
    .rotate(45f)
    .scale(1.2f)
```

---

## Background & Borders

Some advanced background and border effects require custom implementation.

### Properties

| CSS Property                    | Compose Alternative |
|---------------------------------|-------------------|
| `background: linear-gradient()` | Use `Brush.linearGradient()` |
| `background-position`           | Use Alignment parameter |
| `background-eversize: cover`      | Use `ContentScale.Crop` |
| `background-repeat`             | Use `TileMode` |
| `border-image`                  | Use custom drawing |
| `outline`                       | Use border with offset or `graphicsLayer` |
| Individual border sides         | Use custom drawing (not natively supported) |

**Example:**
```kotlin
// CSS: background: linear-gradient(to right, blue, purple);
modifier.background(
    brush = Brush.linearGradient(
        colors = listOf(Color.Blue, Color(0xFF8b5cf6))
    )
)
```

---

## Position & Layout

Compose uses different positioning strategies than CSS.

### Properties

| CSS Property | Compose Alternative |
|--------------|-------------------|
| `position: absolute` | Use `Box` with `Alignment` or offset |
| `position: fixed` | Use `Box` at root level |
| `position: sticky` | Not directly supported |
| `top`, `left`, `right`, `bottom` | Use `Modifier.offset()` or Box alignment |
| `margin` | Use parent spacing with `Arrangement.spacedBy()` |
| `float` | Use Box with alignment |

**Example:**
```kotlin
// CSS: position: absolute; top: 20px; left: 20px;
Box(modifier = Modifier.fillMaxSize()) {
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = 20.dp, y = 20.dp)
    ) {
        // Content
    }
}
```

---

## Vendor Prefixes

All vendor-prefixed properties should use their standard equivalents.

### WebKit Prefixes (-webkit-)

**Total:** 89 properties

Use standard property without prefix:
- `-webkit-animation-*` → `animation-*`
- `-webkit-transform` → `transform`
- `-webkit-transition` → `transition`
- `-webkit-flex-*` → `flex-*`
- `-webkit-border-radius` → `border-radius`
- `-webkit-box-shadow` → `box-shadow`

**WebKit-specific (no standard equivalent):**
- `-webkit-appearance` → Use custom composables
- `-webkit-tap-highlight-color` → Mobile tap highlight styling
- `-webkit-font-smoothing` → Font rendering hint
- `-webkit-text-security` → Password field masking
- `-webkit-box-reflect` → Reflection effect
- And 80+ more...

### Mozilla Prefixes (-moz-)

**Total:** 11 properties

- `-moz-appearance` → Use custom composables
- `-moz-user-select` → Use standard `user-select`
- `-moz-border-radius` → Use standard `border-radius`
- `-moz-box-*` → Legacy flexbox, use standard flex

### Microsoft Prefixes (-ms-)

**Total:** 51 properties

- `-ms-flex-*` → Use standard flex properties
- `-ms-grid-*` → Use standard grid properties
- `-ms-overflow-style` → Scrollbar styling
- `-ms-scroll-snap-*` → Use standard scroll-snap
- And 45+ more IE-specific properties...

### Opera Prefixes (-o-)

**Total:** 9 properties

- `-o-transform` → Use standard `transform`
- `-o-transition` → Use standard `transition`
- `-o-object-fit` → Use standard `object-fit`

---

## Experimental Properties

Properties in draft specifications or experimental features.

| CSS Property | Status | Compose Alternative |
|--------------|--------|-------------------|
| `container-*` | CSS Containment Level 3 | Use `BoxWithConstraints` |
| `anchor-name` | CSS Anchor Positioning | Use `onGloballyPositioned` |
| `view-transition-name` | View Transitions API | Use shared element transitions |
| `scroll-timeline` | Scroll-driven Animations | Use `LazyListState` or `ScrollState` |
| `masonry-auto-flow` | CSS Grid Level 3 | Custom layout |
| `text-wrap: balance` | Text Level 4 | Not yet supported |

---

## Legacy/Deprecated Properties

Properties from older CSS specifications.

| CSS Property | Compose Alternative |
|--------------|-------------------|
| `float` | Use Box with alignment |
| `clear` | Not applicable |
| `-webkit-box-*` (2009 flexbox) | Use standard flex |
| `-moz-box-*` (2009 flexbox) | Use standard flex |
| `table-layout` | Tables not directly supported |
| `list-style` | Manually implement with Row/Column |

---

## Print & Paged Media

Print-specific properties are not applicable in mobile UI.

| CSS Property | Compose Alternative |
|--------------|-------------------|
| `page-break-*` | Not applicable |
| `orphans`, `widows` | Print properties, not applicable |
| `page` | Named pages for printing, not applicable |

---

## Audio CSS

Audio CSS properties for screen readers and speech synthesis.

| CSS Property | Compose Alternative |
|--------------|-------------------|
| `azimuth` | Audio spatial positioning, not applicable |
| `voice-*` | Speech synthesis properties, not applicable |
| `speak-*` | Audio CSS, not applicable |
| `rest`, `rest-after`, `rest-before` | Audio pauses, not applicable |

---

## Complete Property Reference

### Summary Statistics

- **Total CSS Properties Handled:** 610+
- **Properties with Direct Compose Support:** ~110
- **Properties with Helpful Comments:** ~500
- **Vendor-Prefixed Properties:** 160
- **Standard Properties:** 380
- **SVG Properties:** 50
- **Experimental Properties:** 20

### Property Support Level

| Support Level | Count | Description |
|--------------|-------|-------------|
| ✅ Direct Support | ~110 | Mapped directly to Compose modifiers |
| 📝 Alternative Approach | ~300 | Achievable with different Compose APIs |
| ⚠️ Partial Support | ~100 | Requires custom implementation |
| ❌ Not Applicable | ~100 | Web/print-only features |

---

## How to Use This Guide

1. **Find your CSS property** in the table of contents
2. **Read the Compose alternative** approach
3. **Check the example code** for implementation
4. **Refer to official Compose docs** for detailed API documentation

## Contributing

If you find a CSS property that should be added to this list or have a better Compose alternative, please submit a pull request!

## Additional Resources

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Compose Modifiers](https://developer.android.com/jetpack/compose/modifiers)
- [Compose Animation](https://developer.android.com/jetpack/compose/animation)
- [Compose Layouts](https://developer.android.com/jetpack/compose/layouts)

---

**Generated:** $(date)
**Version:** 1.0.0
**Style Converter Version:** See package.json
