## Decisions (Current)

### Targets
- iOS target: SwiftUI
- Android target: Jetpack Compose with Material 3

### Input
- Source format: JSON-based style spec with CSS-like properties
- Supports components with properties, selectors (`:hover`, `:active`, `:focus`, `:disabled`), and media queries
- Input schema key: `"components"` → `"properties"`, `"selectors"`, `"media"`
- Actual field names match test files: uses `"properties"` not `"styles"`, `"selector"` field is `"when"`

### Output
- File naming conventions:
  - Web: `cssStyles.json` (when generating CSS from other formats)
  - iOS: `iosStyles.json`
  - Android: `androidStyles.json`
  - Intermediate: `tmpOutput.json` (IR dump for debugging)
- Diagnostics: to be implemented (planned as inline comments or separate report)

### Implementation
- **Language/stack: Kotlin/JVM with Gradle**
- JVM toolchain: Java 21+ (upgraded from original Java 17 requirement)
- Kotlin version: 2.1.0 (upgraded for Java 21+ support)
- Gradle version: 8.14
- Dependencies: kotlinx.serialization-json for JSON handling
- Main entry: `app.MainKt` CLI with command: `style-converter convert --from <format> --to <targets> -i <input> -o <outDir>`

### Architecture (implemented)
- **Pipeline**: Parse JSON → IR Model → Generate platform code
- **Parsing**: Modular property parser system with registry pattern
  - `PropertiesParser` orchestrates parsing
  - `PropertyParserRegistry` routes properties to specialized parsers
  - `GenericPropertyParser` handles all properties currently (specialized parsers planned)
  - Primitive parsers: ColorParser, LengthParser, KeywordParser, FunctionParser, ShadowParser, UrlParser
- **IR Model**: Strongly-typed with `@Serializable` annotations
  - `IRDocument` → `IRComponent` → `IRProperty`, `IRSelector`, `IRMedia`
  - Property types: `IRLength`, `IRColor`, `IRKeyword`, `IRFunction`, `IRShadow`, `IRUrl`
- **Generation**: Placeholder implementations (to be completed)
  - ComposeGenerator, SwiftUIGenerator, CssGenerator

### Policies (current)
- Units:
  - Layout: px → dp (1:1)
  - Typography: px → sp (round to nearest integer)
  - Line-height: unitless values multiply font size; px values converted proportionally
- Fonts:
  - MVP: system fonts only; extensible to custom families later
- Colors:
  - Support all common web formats (hex, rgb/rgba, hsl/hsla) assuming sRGB; normalize to RGBA internally
- Shadows:
  - Prefer Compose Material 3 elevation where parity holds; fallback to custom shadow when elevation cannot express intent
- Borders:
  - MVP: per-side widths and colors; per-corner radii supported
  - Parsed from shorthand and longhand forms
- Variables (CSS custom properties):
  - MVP: resolved values only (no `var(--x)`)
  - Future: optional top-level `globals` for tokens and a reference mechanism


