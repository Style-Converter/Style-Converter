## Architecture Plan (Implemented in Kotlin)

### System Overview
- **CLI + Library** written in Kotlin/JVM
- **Core pipeline**: Parse JSON → IR Model → Generate (Compose/SwiftUI/CSS)
- Extensibility via parser registry pattern for property-specific handling
- Currently single-threaded, stateless conversions

### Project Structure
```
src/main/kotlin/app/
├── Main.kt                          # CLI entry point
├── Models.kt                        # IR data classes
├── parsing/
│   ├── Parsing.kt                   # Parser routing (css/compose/swiftui)
│   ├── css/
│   │   ├── CssJsonModel.kt          # CSS input model
│   │   ├── CssParsing.kt            # JSON→IR conversion
│   │   ├── properties/
│   │   │   ├── PropertiesParser.kt  # Property parsing orchestrator
│   │   │   └── parsers/
│   │   │       ├── PropertyParserRegistry.kt    # Routes properties to parsers
│   │   │       ├── PropertyParserInterface.kt   # Parser contract
│   │   │       ├── GenericPropertyParser.kt     # Fallback parser
│   │   │       └── primitiveParsers/
│   │   │           ├── ColorParser.kt
│   │   │           ├── LengthParser.kt
│   │   │           ├── KeywordParser.kt
│   │   │           ├── FunctionParser.kt
│   │   │           ├── ShadowParser.kt
│   │   │           ├── UrlParser.kt
│   │   │           └── PrimitiveParser.kt       # Base utilities
│   │   ├── selectors/               # Selector parsing (:hover, :active, etc)
│   │   └── mediaQueries/            # Media query parsing
│   ├── compose/                     # Future: Compose→IR parser
│   └── swiftUI/                     # Future: SwiftUI→IR parser
└── logic/
    ├── Logic.kt                     # Generation orchestrator
    ├── compose/
    │   └── ComposeGenerator.kt      # IR→Compose (placeholder)
    ├── swiftUI/
    │   └── SwiftUIGenerator.kt      # IR→SwiftUI (placeholder)
    └── css/
        └── CssGenerator.kt          # IR→CSS (placeholder)
```

### Components (Current Implementation)

#### 1. Parser Layer
- **JSON CSS Parser** (`app.parsing.css`)
  - `JsonInputToCssComponents`: Maps JSON input to intermediate CSS model
  - `cssParsing`: Converts CSS model to IR
  - Property parsing via `PropertiesParser` + registry system
  - Selector parsing: `:hover`, `:active`, `:focus`, `:disabled`
  - Media query parsing: `(min-width: ...)`, etc.

#### 2. IR Model (`app.Models.kt`)
- **Core types** (all `@Serializable` for JSON output):
  - `IRDocument(components: List<IRComponent>)`
  - `IRComponent(name, properties, selectors, media)`
  - `IRProperty(propertyName, lengths, colors, urls, keywords, shadows, raw)`
  - `IRSelector(condition, properties)`
  - `IRMedia(query, properties)`
- **Value types**:
  - `IRLength(value, unit, function)` - numeric values with units
  - `IRColor(raw, function)` - color values
  - `IRKeyword(value)` - CSS keywords
  - `IRFunction(name, args)` - CSS functions (calc, var, etc)
  - `IRShadow(xOffset, yOffset, blur, spread, color, inset)` - box shadows
  - `IRUrl(url, function)` - URL references
  - `IRFunctionArg` - typed function arguments

#### 3. Property Parser System
- **Registry Pattern**: `PropertyParserRegistry.find(propertyName)`
  - Currently returns `GenericPropertyParser` for all properties
  - Designed for future specialized parsers (borders, shadows, etc.)
- **Primitive Parsers**: Reusable parsing logic
  - `ColorParser`: hex, rgb/rgba, hsl/hsla, named colors
  - `LengthParser`: px, em, rem, %, vh/vw, etc.
  - `KeywordParser`: CSS keywords (auto, inherit, none, etc.)
  - `FunctionParser`: calc(), var(), min(), max(), etc.
  - `ShadowParser`: box-shadow values
  - `UrlParser`: url() function
- **Generic Parser**: Detects and parses mixed property values
  - Uses primitive parsers to identify types
  - Stores all parsed values in appropriate `IRProperty` fields

#### 4. Generators (Placeholders)
- **ComposeGenerator**: `generateCompose(ir) → JsonObject`
- **SwiftUIGenerator**: `generateSwiftUI(ir) → JsonObject`
- **CssGenerator**: `generateCss(ir) → JsonObject`
- Currently print "not yet implemented" and exit

#### 5. CLI (`Main.kt`)
- Command: `convert --from <format> --to <targets> -i <input> -o <outDir>`
- Argument parsing: handles `--flag value` and `-f value` formats
- Workflow:
  1. Read input JSON file
  2. Route to appropriate parser based on `--from` flag
  3. Generate IR and save to `tmpOutput.json` (debug)
  4. Call generators for each target in `--to`
  5. Write output files to `<outDir>/`

### Data Flow (Actual)
```
Input JSON file
    ↓
Main.parseArgs() → validate args
    ↓
File.readText() → JsonElement
    ↓
parsing(root, "css") → cssParsing()
    ↓
JsonInputToCssComponents() → CssComponents
    ↓
PropertiesParser.parse() → List<IRProperty>
    ↓
IRDocument (serialized to tmpOutput.json)
    ↓
logic(ir, targets) → generateCompose/SwiftUI/CSS
    ↓
Output JSON files (androidStyles.json, iosStyles.json, etc.)
```

### Technology Stack (Finalized)
- **Language**: Kotlin 2.1.0
- **Build tool**: Gradle 8.14 with Kotlin DSL
- **JVM**: Java 21+
- **JSON**: kotlinx.serialization-json 1.6.3
- **Testing**: To be implemented (Gradle test framework)
- **Formatting**: Kotlin stdlib formatting

### Future Enhancements
- Specialized property parsers in registry (borders, gradients, transforms, etc.)
- Shorthand expansion (margin, padding, border, font, etc.)
- Generator implementations (Compose, SwiftUI, CSS)
- Unit normalization and conversion
- Validation and diagnostics system
- Compose/SwiftUI input parsers
- Test suite with golden outputs
- Mapping tables (YAML/JSON) for community contributions

### Performance Considerations
- Stateless conversions (no shared mutable state)
- Future: Parser caching/memoization
- Future: Parallel processing for multiple components

### Security/Privacy
- Local file processing only
- No network calls
- No code execution from input


