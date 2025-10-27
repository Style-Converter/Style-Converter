## Milestones and Scope

### M0 — Project Bootstrap ✅ COMPLETED
- ✅ Objectives and architecture docs
- ✅ Logging and contribution scaffolding
- ✅ Kotlin/Gradle project setup
- ✅ Java 21 + Kotlin 2.1.0 + Gradle 8.14
- ✅ Basic CLI structure with argument parsing
- ✅ IR model with serialization support

### M1 — MVP: JSON CSS → IR Parsing 🔄 IN PROGRESS
**Current Status**: Parsing infrastructure complete, generators pending

**Completed**:
- ✅ CLI command `style-converter convert --from css --to compose,swiftui -i <input> -o <outDir>`
- ✅ IR model design (`IRDocument`, `IRComponent`, `IRProperty`, etc.)
- ✅ Modular property parser system with registry pattern
- ✅ Primitive parsers (Color, Length, Keyword, Function, Shadow, URL)
- ✅ Generic property parser (detects and parses mixed values)
- ✅ Selector parsing (`:hover`, `:active`, `:focus`, `:disabled`)
- ✅ Media query parsing
- ✅ IR serialization to JSON (debug output: `tmpOutput.json`)
- ✅ Example files for testing (`examples/mvp-border-per-side.json`, etc.)

**In Progress**:
- 🔄 Specialized property parsers (borders, gradients, shorthands)
- 🔄 Shorthand expansion (margin, padding, border, font, background)

**Pending**:
- ⏳ ComposeGenerator implementation
- ⏳ SwiftUIGenerator implementation
- ⏳ Unit conversion and normalization
- ⏳ Gradle test suite with golden outputs (20+ cases)
- ⏳ Diagnostics and validation system

**Supported Properties** (parseable to IR):
- All CSS properties via `GenericPropertyParser`
- Detects: colors, lengths, keywords, functions, shadows, URLs
- Preserves: raw values for unparsed content

**Target Properties** (for M1 completion):
- Colors: `color`, `background-color`, `opacity`
- Typography: `font-size`, `font-weight`, `font-style`, `line-height`, `letter-spacing`
- Spacing: `margin`, `padding` (with shorthand expansion)
- Borders: per-side width/color/style, per-corner radius (with shorthand expansion)
- Size: `width`, `height`, `min-width`, `min-height`, `max-width`, `max-height`
- Layout: `display`, `flex-direction`, `flex-wrap`, `justify-content`, `align-items`, `gap`
- Effects: `box-shadow`, `text-shadow`, `opacity`

### M2 — Code Generation Implementation ⏳ PLANNED
- Implement `generateCompose()` to output Jetpack Compose Kotlin code
- Implement `generateSwiftUI()` to output SwiftUI Swift code
- Unit conversion: px → dp (Android), px → pt (iOS)
- Typography mapping: font-size, font-weight, line-height
- Color conversion: hex/rgb → Color types
- Border and shadow rendering
- Layout modifiers (flexbox → Compose/SwiftUI equivalents)
- Output formatting and code style

### M3 — Property Expansion and Edge Cases ⏳ PLANNED
- Multiple shadows
- Complex borders (per-side colors/widths/styles)
- Gradients (linear-gradient, radial-gradient)
- Text decoration
- Overflow and clipping
- Transform properties
- Improved unit conversions (rem, em, vh/vw)
- Diagnostics for unsupported/lossy conversions

### M4 — Testing and Validation ⏳ PLANNED
- Gradle test framework setup
- Golden test suite (20+ cases)
- Snapshot testing for IR output
- Code generation comparison tests
- Property parser unit tests
- Selector and media query tests
- Error handling and edge case tests

### M5 — Input from Android Compose → IR ⏳ FUTURE
- Parse Kotlin Compose code to IR
- Modifier chain parsing
- Typography and text style extraction
- Round-trip tests (JSON → IR → Compose → IR)

### M6 — Input from SwiftUI → IR ⏳ FUTURE
- Parse SwiftUI code to IR
- Modifier parsing
- View style extraction
- Round-trip tests (JSON → IR → SwiftUI → IR)

### M7 — Layout Fidelity and Visual Testing ⏳ FUTURE
- Screenshot comparison harness
- Perceptual diff validation
- Cross-platform parity checks
- Regression test suite

### M8 — Plugin System and Community Rules ⏳ FUTURE
- External mapping rules via YAML/JSON
- Parser plugin registration
- Generator plugin system
- Community contribution workflow
- Versioned rule packs

### M9 — IDE Integrations ⏳ FUTURE
- VS Code extension
- IntelliJ IDEA plugin
- Inline style previews
- Quick-fix suggestions
- Real-time conversion


