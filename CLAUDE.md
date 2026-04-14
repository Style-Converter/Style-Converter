# Style-Converter

CSS → IR → Compose/SwiftUI/CSS converter with Android SDUI runtime renderer.

## Quick Start

```bash
# Convert CSS to IR
./gradlew run --args="convert --from css --to compose -i examples/visual-test.json -o out"

# Full Android test (convert → build → install → screenshot → logcat)
./test-android.sh examples/visual-test.json
```

## Architecture

```
JSON Input → CSS Parser → IR Model → Generator → Platform Code
                              ↓
                    Android SDUI Runtime
                    (interprets IR at runtime)
```

### Project Structure

```
src/main/kotlin/app/
├── irmodels/                    # IR data models (567 property files)
│   ├── IRDocument.kt            # IRDocument, IRComponent
│   ├── IRProperty.kt            # Base interface
│   ├── ValueTypes.kt            # IRLength, IRColor, IRAngle, IRTime
│   └── properties/              # One file per CSS property
├── parsing/css/
│   ├── CssParsing.kt
│   └── properties/
│       ├── longhands/           # Property parsers + registry
│       ├── shorthands/          # Shorthand expanders
│       └── primitiveParsers/    # Color, Length, Angle parsers
└── logic/
    ├── compose/                 # Compose code generator
    ├── swiftUI/                 # SwiftUI code generator
    └── css/                     # CSS regenerator

testing/Android/                 # SDUI Runtime Renderer
├── app/src/main/
│   ├── assets/tmpOutput.json    # IR loaded at runtime
│   └── java/.../
│       ├── ir/                  # IR models (duplicated)
│       ├── sdui/                # Runtime interpreters
│       │   ├── PropertyApplier.kt
│       │   ├── ComponentRenderer.kt
│       │   ├── BackgroundApplier.kt
│       │   ├── BorderApplier.kt
│       │   ├── FilterApplier.kt
│       │   ├── ClipPathApplier.kt
│       │   ├── GridApplier.kt
│       │   ├── TextStyleApplier.kt
│       │   └── OutlineApplier.kt
│       ├── screenshot/          # Auto screenshot capture
│       │   ├── ScreenshotManager.kt
│       │   └── ScreenshotCaptureScreen.kt
│       └── ui/
│           └── ComponentListScreen.kt
└── PROPERTY_TRACKING.md         # Implementation status

testing/screenshots/             # Pulled screenshots from device
```

## Testing Workflow

### Option 1: Full Automated Test Script

```bash
./test-android.sh [input.json]
```

**What it does:**
1. Runs Style Converter → `out/tmpOutput.json`
2. Copies IR to Android assets
3. Builds & installs app (uses Java 21)
4. Launches app, waits for render
5. Takes screenshots while scrolling (max 50, resized to <2000px)
6. Captures logcat

**Output:** `testing/screenshots/screenshot_*.png` + `logcat.txt`

### Option 2: Auto Screenshot Capture (Per-Component)

The Android app automatically captures individual screenshots of each component on launch.

**How it works:**
1. On app startup, clears any existing screenshots
2. Loads components from `tmpOutput.json`
3. Renders each component one by one
4. Captures a screenshot of each component using Compose's GraphicsLayer
5. Saves to device storage with indexed filenames
6. Shows completion summary with adb pull command

**Screenshot location (Android 11+):**
```
/sdcard/Android/data/com.styleconverter.test/files/test_screenshots/
```

**Pull screenshots:**
```bash
adb pull /sdcard/Android/data/com.styleconverter.test/files/test_screenshots/ ./screenshots/
```

**Screenshot naming:** `{index}_{ComponentName}.png` (e.g., `000_SVG_Fill_Basic.png`)

**Each screenshot includes:**
- Component name and ID
- Property count badge
- Rendered component preview
- List of applied properties

### Manual Testing

```bash
# 1. Convert CSS to IR
./gradlew run --args="convert --from css --to compose -i examples/your-test.json -o out"

# 2. Copy to Android assets
cp out/tmpOutput.json testing/Android/app/src/main/assets/

# 3. Build and install
cd testing/Android
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew installDebug

# 4. Launch app (screenshots auto-capture on startup)
adb shell am start -n com.styleconverter.test/.MainActivity

# 5. Wait for capture to complete, then pull screenshots
adb pull /sdcard/Android/data/com.styleconverter.test/files/test_screenshots/ ./screenshots/
```

## IR Value Normalization

All CSS values normalize to universal formats:

| Type | CSS | Normalized | Platform Use |
|------|-----|------------|--------------|
| Colors | oklch, hsl, hex, rgb | sRGB (0-1 floats) | `Color(r,g,b,a)` |
| Lengths | px, pt, em, rem, % | pixels (absolute only) | `Dp` |
| Angles | deg, rad, turn, grad | degrees | `rotate()` |
| Time | s, ms | milliseconds | animation duration |
| Font Weight | normal, bold, 100-900 | numeric 100-900 | `FontWeight` |
| Opacity | 0-1, 0%-100% | 0-1 float | `alpha()` |

**`null` means runtime-dependent:** `var()`, `calc()`, `em`, `%`, `vw` cannot be pre-computed.

---

## Android SDUI Implementation Status

### Fully Working (Visual Rendering)

**Layout & Sizing** (12 properties)
- `Width`, `Height`, `MinWidth`, `MaxWidth`, `MinHeight`, `MaxHeight`
- `BlockSize`, `InlineSize`, `MinBlockSize`, `MaxBlockSize`, `MinInlineSize`, `MaxInlineSize`
- `AspectRatio`

**Flexbox Layout** (8 properties)
- `Display` (flex, grid, block, none)
- `FlexDirection`, `FlexWrap`, `FlexGrow`
- `JustifyContent`, `AlignItems`, `AlignContent`
- `Gap`, `RowGap`

**Spacing** (16 properties)
- `PaddingTop`, `PaddingRight`, `PaddingBottom`, `PaddingLeft`
- `PaddingBlockStart`, `PaddingBlockEnd`, `PaddingInlineStart`, `PaddingInlineEnd`
- `MarginTop`, `MarginRight`, `MarginBottom`, `MarginLeft`
- `MarginBlockStart`, `MarginBlockEnd`, `MarginInlineStart`, `MarginInlineEnd`

**Colors & Background** (4 properties)
- `BackgroundColor`
- `BackgroundImage` (linear/radial/conic gradients)
- `Opacity`

**Borders** (24 properties)
- `BorderTopWidth`, `BorderRightWidth`, `BorderBottomWidth`, `BorderLeftWidth`
- `BorderTopColor`, `BorderRightColor`, `BorderBottomColor`, `BorderLeftColor`
- `BorderTopStyle`, `BorderRightStyle`, `BorderBottomStyle`, `BorderLeftStyle`
- `BorderTopLeftRadius`, `BorderTopRightRadius`, `BorderBottomRightRadius`, `BorderBottomLeftRadius`
- `BorderStartStartRadius`, `BorderStartEndRadius`, `BorderEndStartRadius`, `BorderEndEndRadius`
- `BoxShadow` (multiple, inset, spread, blur)

**Effects** (6 properties)
- `Filter` (blur, brightness, contrast, grayscale, sepia, hue-rotate, invert, saturate)
- `BackdropFilter`
- `ClipPath` (inset, circle, ellipse, polygon)
- `MixBlendMode`, `Isolation`

**Transforms** (8 properties)
- `Transform` (translate, rotate, scale, skew)
- `TransformOrigin`
- `Rotate`, `Scale`, `Translate`, `Zoom`
- `ZIndex`

**Typography** (11 properties)
- `Color` (text)
- `FontSize`, `FontWeight`, `FontStyle`, `FontFamily`
- `LineHeight`, `LetterSpacing`
- `TextAlign`, `VerticalAlign`
- `TextDecoration` (line, style, color)

**Visibility** (4 properties)
- `Visibility`, `Overflow`, `OverflowX`, `OverflowY`

---

### Config Extraction Only (Data Available, Needs Custom Rendering)

These properties have `extract*Config()` functions that parse the data, but require additional implementation:

**Requires Custom Canvas/Shader:**
- Border Images: `BorderImageSource`, `BorderImageSlice`, `BorderImageWidth`, `BorderImageOutset`, `BorderImageRepeat`
- Masks: `MaskImage`, `MaskSize`, `MaskRepeat`, `MaskPosition`, `MaskMode`, `MaskComposite`, `MaskOrigin`, `MaskClip`, `MaskType`
- SVG: `Fill`, `Stroke`, `StrokeWidth`, `StrokeDasharray`, etc.

**Requires Compose Animation APIs:**
- Animations: `AnimationName`, `AnimationDuration`, `AnimationTimingFunction`, `AnimationDelay`, `AnimationIterationCount`, `AnimationDirection`, `AnimationFillMode`, `AnimationPlayState`
- Transitions: `TransitionProperty`, `TransitionDuration`, `TransitionTimingFunction`, `TransitionDelay`
- Scroll Timelines: `ScrollTimeline`, `ViewTimeline`, `AnimationTimeline`

**Handled at Container Level (not Modifier):**
- Flex items: `FlexBasis`, `FlexShrink`, `AlignSelf`, `Order`
- Grid: `GridTemplateColumns`, `GridTemplateRows`, `GridArea`, `GridColumnStart/End`, `GridRowStart/End`

**Text Styling (via TextStyleApplier):**
- `TextOverflow`, `LineClamp`, `MaxLines`, `WhiteSpace`, `WordBreak`
- `TextShadow`, `TextJustify`, `TextAlignLast`, `Hyphens`

---

### Config Extraction Implemented (Batch 12)

These properties now have full config extraction via dedicated functions:

**Multi-Column Layout** - `extractMultiColumnConfig()`
- `ColumnCount`, `ColumnWidth`, `ColumnGap`
- `ColumnRuleWidth`, `ColumnRuleStyle`, `ColumnRuleColor`
- `ColumnSpan`, `ColumnFill`

**Outline** - `extractOutlineConfig()` in OutlineApplier.kt
- `OutlineWidth`, `OutlineColor`, `OutlineStyle`, `OutlineOffset`

**Font Variants** - `extractFontVariantConfig()`
- `FontVariantNumeric`, `FontVariantCaps`, `FontVariantLigatures`
- `FontFeatureSettings`, `FontKerning`, `FontOpticalSizing`

**Text Decoration** - `extractTextDecorationConfig()`
- `TextDecorationThickness`, `TextUnderlineOffset`, `TextUnderlinePosition`

**Counters** - `extractCounterConfig()`, `extractQuotesConfig()`
- `CounterReset`, `CounterIncrement`, `CounterSet`, `Quotes`

**Form Styling** - `extractFormStylingConfig()` / `extractAccentConfig()`
- `AccentColor`, `CaretColor`, `ColorScheme`

---

### Not Yet Implemented

**Motion Paths** (5 properties) - CSS motion path animations
- `OffsetPath`, `OffsetDistance`, `OffsetRotate`, `OffsetAnchor`, `OffsetPosition`

**CSS Regions** (11 properties) - Paged media / print layout (N/A on mobile)
- `FlowInto`, `FlowFrom`, `RegionFragment`, `Continue`
- `BookmarkLevel`, `BookmarkLabel`, `BookmarkState`
- `StringSet`, `Running`, `Leader`, `FootnoteDisplay`, `FootnotePolicy`

---

### Not Applicable to Mobile

```
Cursor                           # No mouse cursor on mobile
Resize                           # No drag handles
PageBreak*                       # No printing
```

---

## Key Files Reference

| File | Purpose |
|------|---------|
| `PropertyApplier.kt` | Main IR → Modifier converter (1100 lines) |
| `ComponentRenderer.kt` | Layout detection & rendering (640 lines) |
| `ValueExtractors.kt` | Extract Dp, Color, Float from IR JSON |
| `BackgroundApplier.kt` | Gradient rendering |
| `BorderApplier.kt` | Per-side border drawing |
| `FilterApplier.kt` | CSS filter → ColorMatrix |
| `TextStyleApplier.kt` | Typography properties |
| `GridApplier.kt` | CSS Grid → LazyVerticalGrid |

## Adding New Properties

1. **IR Model**: Add to `irmodels/properties/` (if new property)
2. **Parser**: Add to `parsing/css/properties/longhands/`
3. **Registry**: Register in `PropertyParserRegistry.kt`
4. **Android**: Add case in `PropertyApplier.applyPropertyInternal()`
5. **Test**: Add example to `visual-test.json`

## Tech Stack

- Kotlin 2.1.0, Java 21+, Gradle 8.14
- kotlinx.serialization-json 1.6.3
- Android: Compose BOM 2024.12, Material3, API 24+
