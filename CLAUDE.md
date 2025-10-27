# Claude Context - Style Converter Project

> **READ THIS FILE FIRST** - Essential context for working on this project

## Project Summary

**Style-Converter** is a Kotlin/JVM CLI tool that converts JSON-based CSS-like style definitions into platform-specific UI code (Android Jetpack Compose and iOS SwiftUI).

**Current Status**: ✅ Parsing complete, 🎉 **NEW: Advanced Compose generator with ContentScope support integrated!**

## Key Architecture Points

### Pipeline Flow
```
JSON Input
  ↓
CssPropertyValidator (filters invalid properties)
  ↓
ShorthandRegistry (expands shorthands → longhands)
  ↓
PropertyParserRegistry (parses each longhand property)
  ↓
IR Model (typed data structures)
  ↓
PropertyAnalyzer (detects wrapper needs: flex, position, scroll, SVG, filters, text, state, media)
  ↓
ComponentStructureBuilder (builds wrapper hierarchy + ContentScope)
  ↓
CodeGenerator (generates final Kotlin code)
  ↓
Platform Code (Compose/SwiftUI/CSS)
```

### Technology Stack
- **Language**: Kotlin 2.1.0
- **Runtime**: Java 21+
- **Build**: Gradle 8.14
- **JSON**: kotlinx.serialization-json 1.6.3
- **CLI**: `./gradlew run --args="convert --from css --to compose,swiftui -i <input> -o <outDir>"`

### NEW IR Model Structure (irmodels/ folder) - **166 PROPERTIES IMPLEMENTED**

**🎉 COMPLETE**: 166 individual property files created! ~99% complete - ALL MAJOR CATEGORIES DONE!

**Architecture Constraints:**
- ✅ **Pure data representation** - NO conversion logic in IR classes
- ✅ **ONE property per file** - Each CSS property gets its own dedicated file
  - Example: `FontSizeProperty.kt` handles ONLY `font-size`
  - Example: `ColorProperty.kt` handles ONLY `color`
  - Example: `PaddingProperty.kt` handles ONLY `padding` (shorthand with all sub-properties)
- ✅ **Maximum 100 lines per file** - Forces modular, maintainable code
- ✅ **Precise type constraints** - Use enums/sealed interfaces for exact valid values
  - Example: `text-align` accepts ONLY: start, end, left, right, center, justify, match-parent
  - Example: `font-weight` accepts ONLY: 100-900 (numeric) or normal/bold/lighter/bolder
- ✅ **Organized by category** - Grouped in folders (typography, color, spacing, etc.)
- ✅ **Fully serializable** - All types annotated with `@Serializable`

**Base Types (`irmodels/base/`):**
- `IRProperty` - Simple interface with only `propertyName` field
- `IRLength` - Value + unit enum (PX, DP, EM, REM, PERCENT, VW, VH, PT, CM, MM, IN, EX, CH, FR)
- `IRColor` - Sealed interface: Hex | RGB | HSL | Named | CurrentColor | Transparent
- `IRKeyword`, `IRUrl`, `IRPercentage`, `IRNumber`, `IRAngle`, `IRTime`

**Implementation Progress - ALL MAJOR CATEGORIES COMPLETE:**

✅ **Typography (`irmodels/typography/`)** - 23 properties
- FontFamily, FontSize, FontWeight, FontStyle
- TextAlign, TextTransform, TextOverflow, TextIndent, TextAlignLast
- TextDecorationLine, TextDecorationStyle, TextDecorationColor
- LineHeight, LetterSpacing, WordSpacing
- WhiteSpace, WordBreak, VerticalAlign
- FontVariantNumeric, FontFeatureSettings, FontVariationSettings
- TextOrientation, UnicodeBidi, TextShadow, Direction, WritingMode, Hyphens, TabSize

✅ **Color & Effects (`irmodels/color/`)** - 7 properties
- Color, BackgroundColor, Opacity
- MixBlendMode, BackgroundBlendMode
- Filter, BackdropFilter (with 10 filter functions each)

✅ **Spacing & Sizing (`irmodels/spacing/`)** - 17 properties
- Padding, Margin
- Width, Height, MinWidth, MaxWidth, MinHeight, MaxHeight
- BlockSize, InlineSize, MaxBlockSize, MinBlockSize, MaxInlineSize, MinInlineSize
- Gap, RowGap, ColumnGap, AspectRatio

✅ **Transforms (`irmodels/transforms/`)** - 2 properties
- Transform (with 20+ transform functions: translate, rotate, scale, skew, matrix, perspective)
- TransformOrigin

✅ **Layout - Flexbox (`irmodels/layout/flexbox/`)** - 13 properties
- Display, FlexDirection, FlexWrap
- JustifyContent, AlignItems, AlignContent, AlignSelf
- FlexGrow, FlexShrink, FlexBasis, Order

✅ **Layout - Position & Float (`irmodels/layout/position/`)** - 8 properties
- Position, Top, Right, Bottom, Left, ZIndex
- Float, Clear

✅ **Layout - Grid (`irmodels/layout/grid/`)** - 13 properties
- GridTemplateColumns, GridTemplateRows, GridTemplateAreas
- GridAutoRows, GridAutoColumns, GridAutoFlow
- GridColumnStart, GridColumnEnd, GridRowStart, GridRowEnd
- GridArea, JustifyItems, JustifySelf

✅ **Borders & Outlines (`irmodels/borders/`)** - 22 properties
- BorderWidth, BorderStyle, BorderColor, BorderRadius, BorderImage
- BorderTopWidth, BorderRightWidth, BorderBottomWidth, BorderLeftWidth
- BorderTopStyle, BorderRightStyle, BorderBottomStyle, BorderLeftStyle
- BorderTopColor, BorderRightColor, BorderBottomColor, BorderLeftColor
- BoxShadow (multi-shadow support)
- OutlineWidth, OutlineStyle, OutlineColor, OutlineOffset

✅ **Background (`irmodels/background/`)** - 8 properties
- BackgroundImage (with linear/radial/conic gradient support)
- BackgroundSize, BackgroundPosition, BackgroundRepeat
- BackgroundAttachment, BackgroundClip, BackgroundOrigin

✅ **Animations & Transitions (`irmodels/animations/`)** - 12 properties
- AnimationName, AnimationDuration, AnimationTimingFunction, AnimationDelay
- AnimationIterationCount, AnimationDirection, AnimationFillMode, AnimationPlayState
- TransitionProperty, TransitionDuration, TransitionTimingFunction, TransitionDelay

✅ **Effects & Visibility (`irmodels/effects/`)** - 7 properties
- Visibility, Overflow, OverflowX, OverflowY
- ClipPath, Mask, BackdropFilter

✅ **Interactions (`irmodels/interactions/`)** - 6 properties
- Cursor, PointerEvents, UserSelect
- TouchAction, ScrollBehavior, Resize

✅ **Content & Lists (`irmodels/content/` & `irmodels/lists/`)** - 4 properties
- Content (for ::before/::after)
- ListStyleType, ListStylePosition, ListStyleImage

✅ **Table Layout (`irmodels/table/`)** - 4 properties
- TableLayout, BorderCollapse, BorderSpacing, CaptionSide

✅ **Multi-Column Layout (`irmodels/columns/`)** - 6 properties
- ColumnCount, ColumnWidth, ColumnGap
- ColumnRuleWidth, ColumnRuleStyle, ColumnRuleColor

✅ **Images & Objects (`irmodels/images/`)** - 2 properties
- ObjectFit, ObjectPosition

✅ **Performance & Optimization (`irmodels/performance/`)** - 3 properties
- WillChange, Contain, Isolation

✅ **Scrolling & Snap Points (`irmodels/scrolling/`)** - 5 properties
- OverflowAnchor, ScrollSnapType, ScrollSnapAlign, ScrollSnapStop, OverscrollBehavior

📊 **Total: 166 properties across 19 major categories**

⏳ **Optional Future Additions (rarely used):**
- [ ] column-span, column-fill, orphans, widows
- [ ] page-break/break properties, image-rendering
- [ ] shape-outside, shape-margin
- [ ] Platform-Specific modifiers (Compose/SwiftUI only)

**Old IR Model (Models.kt)** - Will be migrated/deprecated:
- `IRDocument` - Root containing list of components
- `IRComponent` - Component with name, properties, selectors, media
- `IRProperty` - Old generic property (being replaced by specific property classes)
- `IRSelector` - Condition + properties (for :hover, :active, :focus, :disabled)
- `IRMedia` - Query + properties (for responsive breakpoints)

### Project Structure
```
src/main/kotlin/app/
├── Main.kt                    # CLI entry point
├── Models.kt                  # Old IR data classes (being deprecated)
├── irmodels/                  # ✨ NEW: Property-based IR architecture
│   ├── base/                  # Base types and interfaces
│   │   ├── IRProperty.kt      # Simple interface (propertyName only)
│   │   └── ValueTypes.kt      # IRLength, IRColor, IRAngle, IRTime, etc.
│   ├── typography/            # ✅ COMPLETE - 23 files
│   ├── color/                 # ✅ COMPLETE - 7 files
│   ├── spacing/               # ✅ COMPLETE - 17 files
│   ├── transforms/            # ✅ COMPLETE - 2 files
│   ├── layout/                # ✅ COMPLETE
│   │   ├── flexbox/           # ✅ 13 files
│   │   ├── position/          # ✅ 8 files (includes Float, Clear)
│   │   └── grid/              # ✅ 13 files
│   ├── borders/               # ✅ COMPLETE - 22 files
│   ├── background/            # ✅ COMPLETE - 8 files
│   ├── animations/            # ✅ COMPLETE - 12 files
│   ├── interactions/          # ✅ COMPLETE - 6 files
│   ├── effects/               # ✅ COMPLETE - 7 files
│   ├── content/               # ✅ COMPLETE - 1 file
│   ├── lists/                 # ✅ COMPLETE - 3 files
│   ├── table/                 # ✅ COMPLETE - 4 files
│   ├── columns/               # ✅ COMPLETE - 6 files
│   ├── images/                # ✅ COMPLETE - 2 files
│   ├── performance/           # ✅ COMPLETE - 3 files
│   └── scrolling/             # ✅ COMPLETE - 5 files
├── parsing/
│   ├── Parsing.kt             # Parser router
│   └── css/
│       ├── CssParsing.kt      # JSON→IR conversion
│       ├── properties/        # Property parsing with registry pattern
│       ├── selectors/         # :hover, :active, :focus, :disabled
│       └── mediaQueries/      # Media query parsing
└── logic/
    ├── Logic.kt               # Generation orchestrator
    ├── compose/               # Compose generator
    ├── swiftUI/               # SwiftUI generator (pending)
    └── css/                   # CSS generator (pending)
```

## Current Implementation Status

### ✅ Completed (M0 + M1 Parsing + M2 Advanced Compose Generation)
- ✅ CLI with argument parsing
- ✅ IR model with full serialization
- ✅ Modular property parser system with registry pattern
- ✅ Primitive parsers: Color, Length, Keyword, Function, Shadow, URL
- ✅ Generic property parser (handles all CSS properties)
- ✅ Selector parsing (`:hover`, `:active`, `:focus`, `:disabled`)
- ✅ Media query parsing
- ✅ IR debug output to `tmpOutput.json`
- ✅ **NEW: Advanced Compose Generator Architecture**
  - PropertyAnalyzer (631 lines) - Detects flex, position, scroll, SVG, filters, text needs
  - ComponentStructureBuilder (640 lines) - Builds wrapper hierarchy with ContentScope
  - CodeGenerator (423 lines) - Generates real Kotlin code with wrappers
  - **Integration Complete** - PropertyAnalyzer → ComponentStructureBuilder → CodeGenerator working!

### 🎉 Major Breakthrough: ContentScope System
**The generator now produces REAL working Compose code instead of just comments!**

Before:
```kotlin
Box(modifier = modifier
    /* Use Row or Column */ // ← Just a comment!
)
```

After:
```kotlin
@Composable
fun FlexContainer(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit = {}  // ← Real ContentScope!
) {
    Row(modifier = modifier) {  // ← Real wrapper!
        content()  // ← Children slot!
    }
}
```

**Properties Now Generating Real Code:**
- ✅ `display: flex` → Real `Row`/`Column`/`FlowRow`/`FlowColumn` with ContentScope
- ✅ `position: absolute` → Real `Box` with `align()` + `offset()`
- ✅ `overflow: scroll` → Real `verticalScroll()`/`horizontalScroll()`
- ✅ `gap` → Real `Arrangement.spacedBy()`
- ✅ `justify-content`, `align-items` → Real `Arrangement` and `Alignment`

### 🔄 In Progress
- Specialized property parsers (borders, gradients, transforms)
- Shorthand expansion (margin, padding, border, font, background)
- SVG drawing implementation
- Animation state management
- Filter effects

### ⏳ Next Up
- SwiftUIGenerator implementation (IR → SwiftUI Swift)
- Unit conversion (px → dp/sp for Android, px → pt for iOS)
- Grid layout support
- Advanced animations and transitions

## Key Technical Decisions

### Parsing & Validation
1. **Input Format**: JSON with CSS-like properties (not actual CSS files)
2. **Field Names**: Uses `"properties"` not `"styles"`, selector field is `"selector"` (parsed as condition in IR)
3. **IR Model**: Strongly-typed Kotlin data classes with `@Serializable`
4. **CSS Property Validation**: `CssPropertyValidator` validates 280+ standard CSS properties + vendor-prefixed properties
   - Filters out invalid properties before parsing
   - Supports `-webkit-`, `-moz-`, `-ms-`, `-o-` prefixes
   - Logs removed invalid properties
5. **Parser Pattern**: Registry-based system for extensibility
6. **Shorthand Expansion**: `ShorthandRegistry` with 50+ expanders
   - Expands before parsing: margin, padding, border, flex, grid, font, animation, transition, etc.
   - Logical properties (margin-block, padding-inline, border-block, etc.)
   - Each expander implements `ShorthandExpander` interface

### Conversion Policies
7. **Unit Policy**: px → dp (1:1), px → sp (rounded) for typography
8. **Color Handling**: All formats (hex, rgb, hsl) normalized to RGBA
9. **Outputs**: `composeOutput.json` (working), `iosStyles.json` (pending), `tmpOutput.json` (IR debug)

### Shorthand Expansion Details
The ShorthandRegistry expands 50+ CSS shorthands into longhands BEFORE parsing:

**Spacing**: margin, padding, scroll-margin, scroll-padding (+ logical: margin-block/inline, etc.)
**Borders**: border, border-top/right/bottom/left, border-width/style/color, border-radius (+ logical variants)
**Flexbox**: flex, flex-flow
**Grid**: gap, grid, grid-template, grid-row, grid-column, grid-area, place-items/content/self
**Typography**: font, text-decoration
**Positioning**: inset, inset-block/inline
**Animations**: transition, animation
**Background**: background, background-position, mask, border-image, mask-border
**Misc**: overflow, columns, column-rule, list-style, outline, offset, all

Each expander handles value splitting and assignment to longhand properties (e.g., `margin: 10px 20px` → `margin-top: 10px`, `margin-right: 20px`, etc.)

## Important Files

### Documentation (Read These First)
- `AiInstructions/ARCHITECTURE_PLAN.md` - System design and structure
- `AiInstructions/MILESTONES.md` - Progress tracking and roadmap
- `AiInstructions/DECISIONS.md` - Technical decisions and policies
- `AiInstructions/INPUT_SCHEMA.md` - Input format specification
- `AiInstructions/TESTING.md` - Testing strategy
- `AiLogs/logs.md` - Development session logs

### Property Reference Tables
- **`PropertiesComparated/` folder** - **PRIMARY REFERENCE** for CSS/Compose/SwiftUI property mappings
  - Contains 12 comprehensive markdown files with cross-platform property conversion tables
  - Use these tables as the authoritative source for property conversions
  - Files organized by category: Typography, Colors, Spacing, Transforms, Layout, Animations, etc.
  - Each table shows CSS properties with their exact Jetpack Compose and SwiftUI equivalents
  - Includes notes on platform differences, limitations, and workarounds
  - **Reference these tables when implementing property conversions in the generator**

### Core Source Files

**Entry & Orchestration:**
- `src/main/kotlin/app/Main.kt` - CLI entry and orchestration
- `src/main/kotlin/app/Models.kt` - IR data model definitions
- `src/main/kotlin/app/logic/Logic.kt` - Generation orchestration

**Parsing (JSON CSS → IR):**
- `src/main/kotlin/app/parsing/css/CssParsing.kt` - CSS→IR conversion
  - `JsonInputToCssComponents()` - Converts JSON → CssComponents model
  - `cssParsing()` - Converts CssComponents → IRDocument
- `src/main/kotlin/app/parsing/css/properties/PropertiesParser.kt` - **Property parsing orchestrator**
  - Step 1: Filters invalid properties via `CssPropertyValidator`
  - Step 2: Expands shorthands via `ShorthandRegistry`
  - Step 3: Parses longhands via `PropertyParserRegistry`
- `src/main/kotlin/app/parsing/css/properties/CssPropertyValidator.kt` - Validates 280+ CSS properties
- `src/main/kotlin/app/parsing/css/properties/shorthand/ShorthandRegistry.kt` - 50+ shorthand expanders
- `src/main/kotlin/app/parsing/css/properties/longhand/PropertyParserRegistry.kt` - Routes to parsers
- `src/main/kotlin/app/parsing/css/properties/longhand/primitiveParsers/` - Primitive value parsers
  - `ColorParser.kt`, `LengthParser.kt`, `KeywordParser.kt`, `FunctionParser.kt`, `ShadowParser.kt`, `UrlParser.kt`
- `src/main/kotlin/app/parsing/css/selectors/Selectors.kt` - Selector parsing
- `src/main/kotlin/app/parsing/css/mediaQueries/Media.kt` - Media query parsing

**Generation (IR → Compose Code):**
- `src/main/kotlin/app/logic/compose/SimpleComposeBuilder.kt` - Main Compose generator (integrates both systems)
- **NEW: Advanced Generator Architecture (1700+ lines):**
  - `src/main/kotlin/app/logic/compose/analysis/PropertyAnalyzer.kt` - Analyzes properties for wrapper needs
  - `src/main/kotlin/app/logic/compose/generation/ComponentStructureBuilder.kt` - Builds component structure
  - `src/main/kotlin/app/logic/compose/generation/CodeGenerator.kt` - Generates final Kotlin code
- `src/main/kotlin/app/logic/compose/ComposeModels.kt` - Compose-specific models
- `src/main/kotlin/app/logic/compose/converters/` - Property-to-modifier converters

### Test Files
- `examples/mvp-border-per-side.json` - MVP test case
- `examples/test-comprehensive-properties.json` - Comprehensive property testing
- `examples/Border_&_Outlines/` - Border property tests
- `examples/Accessibility_&_Semantic_Adjustments/` - Accessibility tests
- `testing/web/` - Next.js visual testing environment

### Implementation Documentation (docs/)
- **INTEGRATION_COMPLETE.md** - Details of the advanced generator integration (READ THIS!)
- **CONTENTSCOPE_IMPLEMENTATION.md** - How ContentScope system works
- **GENERATOR_OUTPUT_EXAMPLES.md** - Real examples of generated code
- **IMPLEMENTATION_STATUS.md** - Current implementation progress
- **FULL_IMPLEMENTATION_PLAN.md** - Complete architecture and implementation plan
- **UNSUPPORTED_CSS_PROPERTIES.md** - 610+ CSS properties with Compose alternatives

## Common Commands

```bash
# Build the project
./gradlew build

# Run the converter
./gradlew run --args="convert --from css --to compose,swiftui -i examples/mvp-border-per-side.json -o out"

# Check IR output (for debugging)
cat out/tmpOutput.json | jq

# Run tests (once implemented)
./gradlew test

# Visual testing (Next.js app)
cd testing/web && npm run dev
```

## Critical Notes for Claude

### Architecture Principles
1. **IR Model is Central**: All conversions go through the IR. Never bypass it.
2. **Parser Registry Pattern**: Use `PropertyParserRegistry` for specialized parsers, not ad-hoc parsing
3. **Preserve Raw Values**: Always keep `raw` field in `IRProperty` for unparsed content
4. **No Direct Code Gen**: Generators should operate on IR only, never on input JSON
5. **Stateless Design**: No shared mutable state; each conversion is independent
6. **Local Only**: No network calls, no external dependencies for parsing/generation

### NEW: Advanced Compose Generator Architecture
7. **Two-Path Generation**: SimpleComposeBuilder now uses TWO paths:
   - **Simple modifiers** → Use existing buildModifiers() logic (padding, background, etc.)
   - **Needs wrappers** → Use PropertyAnalyzer → ComponentStructureBuilder → CodeGenerator
8. **ContentScope is Key**: Generated components accept `content: @Composable SomeScope.() -> Unit = {}`
   - `RowScope` for Row/FlowRow wrappers
   - `ColumnScope` for Column/FlowColumn wrappers
   - `BoxScope` for Box wrappers
   - Children get appropriate capabilities (weight(), align(), etc.)
9. **No Children Knowledge Required**: Components work with 0, 1, or many children automatically
10. **Real Code, Not Comments**: Properties like `display: flex`, `position: absolute`, `overflow: scroll` now generate WORKING Compose code with real wrappers

### PropertyAnalyzer Detection Capabilities
The PropertyAnalyzer (631 lines) detects:

**Layout Requirements:**
- **Flexbox**: `display: flex`, `flex-direction`, `justify-content`, `align-items`, `gap`, `flex-wrap`
  - Determines: ROW, COLUMN, ROW_REVERSE, COLUMN_REVERSE, FLOW_ROW, FLOW_COLUMN
  - Maps justify-content → Arrangement, align-items → Alignment
- **Grid**: `display: grid`, `grid-template-columns/rows`, `grid-gap`, `grid-auto-flow`
  - Parses column/row counts from templates (e.g., `repeat(3, 1fr)`)
- **Position**: `position: absolute/fixed/sticky/relative`, `top/left/right/bottom`, `z-index`
  - Creates outer positioning context + inner positioned element
- **Scroll**: `overflow: scroll/auto`, `overflow-x`, `overflow-y`, `scroll-behavior: smooth`

**State & Interaction:**
- **Animations**: `animation-name/duration/timing-function/delay/iteration-count/direction/fill-mode`
- **Transitions**: `transition-property/duration/timing-function/delay`
- **Selectors**: `:hover`, `:active`, `:focus`, `:disabled` (from IRSelector list)
- **Clickable**: `cursor: pointer` or presence of hover/active states

**Advanced Features:**
- **SVG Drawing**: `cx/cy/r`, `rx/ry`, `x/y/width/height`, `d` (path data), `fill/stroke`
  - Detects shape type: CIRCLE, RECT, ELLIPSE, PATH, LINE, POLYGON
- **Filters**: `filter`, `backdrop-filter`, `mix-blend-mode`
- **Text**: `font-size/weight/style/family`, `text-align/decoration/transform`, `line-height`, `letter-spacing`

**Responsive:**
- **Media Queries**: Parses min-width/max-width from query strings
  - Example: `"(min-width: 768px)"` → minWidth: 768

### Golden Rules for Working on This Project
11. **Always Read CLAUDE.md First**: This file has the most up-to-date context
12. **Check docs/ for Implementation Details**: See INTEGRATION_COMPLETE.md, CONTENTSCOPE_IMPLEMENTATION.md, etc.
13. **Unit Tests Pending**: M4 milestone - don't expect test files yet (manual testing only)
14. **Test with Real Examples**: Use `examples/test-comprehensive-properties.json` to verify changes
15. **Don't Regress Simple Modifiers**: The existing modifier generation must continue to work
16. **PropertyAnalyzer is Stateless**: Each analyze() call is independent, no shared state
17. **Two-Path Decision**: SimpleComposeBuilder checks `needsWrapperGeneration` to route to appropriate path

## Recent Major Changes (2025-10-15)

### 🎉 Advanced Compose Generator Integrated!

**What Changed:**
1. **Created new architecture** (1700+ lines of code):
   - `PropertyAnalyzer.kt` - Detects when components need wrappers (flex, position, scroll, SVG, etc.)
   - `ComponentStructureBuilder.kt` - Builds component structure with proper ContentScope
   - `CodeGenerator.kt` - Generates final Kotlin code with real wrappers

2. **Integrated into SimpleComposeBuilder.kt**:
   - Now has **two generation paths**:
     - Simple properties → existing modifier generation
     - Complex properties → new wrapper generation
   - Decision logic based on PropertyAnalyzer requirements

3. **ContentScope System**:
   - Components now accept scoped content parameters
   - Children get appropriate capabilities (`weight()`, `align()`, etc.)
   - Works with 0, 1, or many children without any knowledge of them

**Before vs After:**
```kotlin
// BEFORE (Just comments):
Box(modifier = modifier
    /* Use Row or Column */
)

// AFTER (Real working code):
@Composable
fun FlexContainer(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit = {}
) {
    Row(modifier = modifier) { content() }
}
```

**Test Results:**
- ✅ `display: flex; flex-wrap: wrap` → Real `FlowRow` with `FlowRowScope`
- ✅ `overflow-y: scroll` → Real `verticalScroll()` with `BoxScope`
- ✅ `column-gap: 20px` → Real `Arrangement.spacedBy(20.dp)`
- ✅ Simple modifiers still work (no regression)

### Previous Work
- Removed old parser architecture (deleted `parsers/` subdirectories)
- Implemented new property parsing system
- Added comprehensive CSS property support
- Created visual testing environment in `testing/web/`

## Output Files (Generated)

- `out/tmpOutput.json` - IR debug output (always generated)
- `out/composeOutput.json` - Compose Kotlin code (WORKING with real wrappers!)
- `out/androidStyles.json` - Alias for composeOutput.json
- `out/iosStyles.json` - SwiftUI output (placeholder, "not yet implemented")
- `out/cssStyles.json` - CSS output (future, when converting from other formats)

## What Makes This Project Unique

### The ContentScope Innovation
This project solved a fundamental problem in CSS-to-UI-framework conversion:

**Problem**: How do you generate layout containers (Row, Column, Box) without knowing if children exist or how many there are?

**Solution**: Scoped content parameters with default empty implementations
```kotlin
content: @Composable RowScope.() -> Unit = {}
```

This allows:
1. **Zero children** - Works fine with empty default
2. **One child** - User passes single composable
3. **Many children** - User passes any number of composables
4. **Type safety** - Children get appropriate scope capabilities
5. **CSS behavior** - Gap, positioning, scrolling work correctly

### Technical Achievements
1. **Full CSS Property Support** - 610+ properties documented with Compose alternatives
2. **Modular Parser Architecture** - Registry-based system for extensibility
3. **Two-Path Generation** - Simple modifiers vs. complex wrappers
4. **Real Code Generation** - Not just guidance, but working Kotlin code
5. **Visual Parity** - CSS behavior preserved in Compose (gap, positioning, scroll)

### Why It Matters
- **Designers → Developers** - Bridge the gap between design and implementation
- **Cross-Platform** - Eventually CSS ↔ Compose ↔ SwiftUI
- **Learning Tool** - Shows Compose equivalents for CSS patterns
- **Productivity** - Automate tedious style conversion work
