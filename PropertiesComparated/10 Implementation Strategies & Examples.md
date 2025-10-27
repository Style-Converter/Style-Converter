11# Cross-Platform Style Properties Reference - Part 10

## Implementation Strategies

### For Individual Borders

**CSS:**
```css
border-left: 2px solid red;
border-right: 1px solid blue;
```

**Jetpack Compose (Generated):**
```kotlin
Box(
  modifier = Modifier.drawBehind {
    // Left border
    drawLine(
      color = Color(0xFFFF0000),
      start = Offset(0f, 0f),
      end = Offset(0f, size.height),
      strokeWidth = 2.dp.toPx()
    )
    // Right border
    drawLine(
      color = Color(0xFF0000FF),
      start = Offset(size.width, 0f),
      end = Offset(size.width, size.height),
      strokeWidth = 1.dp.toPx()
    )
  }
)
```

**SwiftUI (Generated):**
```swift
Rectangle()
  .fill(Color.clear)
  .overlay(
    Rectangle()
      .frame(width: 2)
      .foregroundColor(.red),
    alignment: .leading
  )
  .overlay(
    Rectangle()
      .frame(width: 1)
      .foregroundColor(.blue),
    alignment: .trailing
  )
```

---

### For CSS Grid

**CSS:**
```css
display: grid;
grid-template-columns: 1fr 2fr 1fr;
grid-gap: 10px;
```

**Jetpack Compose (Generated):**
```kotlin
Column(
  verticalArrangement = Arrangement.spacedBy(10.dp)
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Box(Modifier.weight(1f)) { /* item 1 */ }
    Box(Modifier.weight(2f)) { /* item 2 */ }
    Box(Modifier.weight(1f)) { /* item 3 */ }
  }
  // ... more rows
}
```

**SwiftUI (Generated):**
```swift
LazyVGrid(
  columns: [
    GridItem(.flexible(minimum: 0, maximum: .infinity)),
    GridItem(.flexible(minimum: 0, maximum: .infinity)),
    GridItem(.flexible(minimum: 0, maximum: .infinity))
  ],
  spacing: 10
) {
  // items
}
// Note: For 1fr:2fr:1fr ratio, would need custom implementation
```

---

### For Margin to Spacing Conversion

**CSS:**
```css
.child {
  margin-top: 10px;
  margin-bottom: 20px;
}
```

**Jetpack Compose (Generated):**
```kotlin
// Move margin to parent's arrangement
Column(
  verticalArrangement = Arrangement.spacedBy(10.dp)
) {
  ChildView()
  Spacer(Modifier.height(20.dp)) // Extra bottom margin
  NextChildView()
}
```

**SwiftUI (Generated):**
```swift
VStack(spacing: 10) {
  ChildView()
    .padding(.bottom, 10) // Additional spacing
  NextChildView()
}
```

---

### For Position Absolute

**CSS:**
```css
.container {
  position: relative;
}
.child {
  position: absolute;
  top: 20px;
  right: 10px;
}
```

**Jetpack Compose (Generated):**
```kotlin
Box(modifier = Modifier.fillMaxSize()) {
  // Container content
  ContainerContent()
  
  // Absolutely positioned child
  Box(
    modifier = Modifier
      .align(Alignment.TopEnd)
      .offset(x = (-10).dp, y = 20.dp)
  ) {
    ChildContent()
  }
}
```

**SwiftUI (Generated):**
```swift
ZStack(alignment: .topTrailing) {
  // Container content
  ContainerContent()
  
  // Absolutely positioned child
  ChildContent()
    .offset(x: -10, y: 20)
}
```

---

### For Advanced Filters

**CSS:**
```css
filter: grayscale(50%) brightness(1.2) contrast(1.5);
```

**Jetpack Compose (Generated):**
```kotlin
Image(
  painter = painterResource(R.drawable.image),
  contentDescription = null,
  modifier = Modifier.graphicsLayer {
    colorFilter = ColorFilter.colorMatrix(
      ColorMatrix().apply {
        // Combine multiple filters
        val grayscale = ColorMatrix().apply { setToSaturation(0.5f) }
        val brightness = ColorMatrix(floatArrayOf(
          1.2f, 0f, 0f, 0f, 0f,
          0f, 1.2f, 0f, 0f, 0f,
          0f, 0f, 1.2f, 0f, 0f,
          0f, 0f, 0f, 1f, 0f
        ))
        set(grayscale)
        // Apply additional filters...
      }
    )
  }
)
```

**SwiftUI (Generated):**
```swift
Image("image")
  .grayscale(0.5)
  .brightness(0.2) // Note: SwiftUI uses additive, not multiplicative
  .contrast(1.5)
```

---

## SDUI Parser Recommendations

### Tier 1: Safe to Use (Direct Support)

Use these properties freely in your SDUI schema - they map directly across all platforms:

- Basic dimensions (width, height, min/max, aspect-ratio)
- Padding (all variations including directional and shorthand)
- Background color and gradients (linear, radial, angular/conic)
- Border radius and basic borders
- Opacity and basic visibility
- Basic typography (size, weight, family, align, decoration)
- Basic transforms (translate, scale, rotate)
- Flexbox basics (Row/Column with spacing and alignment)
- Basic colors (RGBA, hex with extensions)
- Text overflow and line limits
- Basic shadows (with platform differences noted)
- Blend modes (all major modes supported)

### Tier 2: Requires Adaptation Layer

These need platform-specific code generation or transformation:

- **Individual border styling** → Generate custom drawing code with drawBehind/Canvas
- **CSS Grid** → Decompose to nested Row/Column structures or LazyGrid
- **Margin** → Convert to parent spacing with Arrangement.spacedBy
- **position: absolute** → Generate Box/ZStack with alignment and offset
- **Complex shadows** → Platform-specific implementations (elevation vs shadow parameters)
- **Advanced gradients** → Conic/angular gradients need different implementations
- **3D transforms** → Use graphicsLayer (Compose) or rotation3DEffect (SwiftUI)
- **Advanced filters** → ColorMatrix (Compose) vs dedicated modifiers (SwiftUI)
- **Backdrop filters** → Material backgrounds (SwiftUI only)
- **Masks** → drawWithContent (Compose) vs .mask() (SwiftUI)

### Tier 3: Avoid or Document Limitations

These have significant limitations or no support:

- **position: sticky** → Not supported on mobile (use scroll-aware positioning)
- **Multiple shadows** → Very limited support (requires view stacking)
- **float/clear** → CSS-only (use modern layout instead)
- **Pseudo-elements** → Add real views instead (no ::before/::after)
- **Individual word-spacing** → Not supported (use letter-spacing)
- **Print/pagination** → Web-specific (no mobile equivalent)
- **Shape wrapping** → No text wrapping around shapes
- **CSS Grid advanced features** → Template areas, auto-flow not supported
- **Cursor styles** → Mobile doesn't have cursor (use touch feedback)