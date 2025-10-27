# Style-Converter

Convert JSON-based style specifications into Android Jetpack Compose (Material 3) and iOS SwiftUI code.

**Status**: 🔄 MVP in progress - Parsing complete, generators pending

## Overview

Style-Converter is a Kotlin/JVM CLI tool that transforms CSS-like style definitions into platform-specific UI code. It supports:
- **Input**: JSON with CSS properties, selectors (`:hover`, `:active`), and media queries
- **Output**: Jetpack Compose (Kotlin), SwiftUI (Swift), and CSS (planned)
- **Architecture**: Parse → Intermediate Representation (IR) → Generate

See `AiInstructions/` for detailed architecture, decisions, and milestones.

## Requirements

- **Java**: 21 or higher
- **Kotlin**: 2.1.0 (managed by Gradle)
- **Gradle**: 8.14+ (wrapper included)

## Quick Start

### 1. Run the converter
```bash
./gradlew run --args="convert --from css --to compose,swiftui -i examples/mvp-border-per-side.json -o out"
```

### 2. Check outputs
- **IR debug output**: `out/tmpOutput.json` (intermediate representation)
- **Android output**: `out/androidStyles.json` (once generator implemented)
- **iOS output**: `out/iosStyles.json` (once generator implemented)

### 3. Explore examples
```bash
ls examples/
# mvp-border-per-side.json - Per-side borders, selectors, media queries
# Accessibility_&_Semantic_Adjustments/
# Backgrounds_&_Masks/
# Border_&_Outlines/
# PrimitiveParsers/ - Color and length parsing tests
```

## Current Capabilities

### ✅ Implemented
- **Parsing Pipeline**: JSON → CssComponents → IR
- **Property Parser System**: Modular registry-based architecture
- **Primitive Parsers**: Colors (hex, rgb, hsl, named), lengths (px, em, rem, %), keywords, functions (calc, var, etc.), shadows, URLs
- **Selectors**: `:hover`, `:active`, `:focus`, `:disabled`
- **Media Queries**: `(min-width: ...)`, `(max-width: ...)`, etc.
- **IR Serialization**: Pretty JSON output for debugging

### 🔄 In Progress
- Specialized property parsers (borders, gradients, transforms)
- Shorthand expansion (margin, padding, border, font, background)

### ⏳ Planned
- **Compose Generator**: IR → Jetpack Compose Kotlin code (M2)
- **SwiftUI Generator**: IR → SwiftUI Swift code (M2)
- **Unit Conversion**: px → dp (Android), px → pt (iOS)
- **Test Framework**: Gradle test suite with golden outputs (M4)
- **Diagnostics**: Warnings for unsupported/lossy conversions

## CLI Usage

```bash
./gradlew run --args="convert --from <format> --to <targets> -i <input> -o <outDir>"
```

**Arguments:**
- `--from <format>`: Input format (`css`, `compose`, `swiftui`)
  - Currently only `css` is implemented
- `--to <targets>`: Comma/space-separated output targets (`compose`, `swiftui`, `css`)
  - Multiple targets: `--to compose,swiftui`
- `-i <input>` or `--input <path>`: Input JSON file path
- `-o <outDir>` or `--outDir <path>`: Output directory (default: `out`)

**Examples:**
```bash
# Convert CSS to Compose and SwiftUI
./gradlew run --args="convert --from css --to compose,swiftui -i examples/mvp-border-per-side.json -o out"

# Convert to specific target
./gradlew run --args="convert --from css --to compose -i examples/Border_&_Outlines/standard.json -o build/output"
```

## Input Format

JSON schema with components, properties, selectors, and media queries:

```json
{
  "components": {
    "PrimaryButton": {
      "properties": {
        "background-color": "#6200EE",
        "padding": "10px 16px",
        "border-radius": "8px",
        "color": "#FFFFFF"
      },
      "selectors": [
        { "when": ":hover", "properties": { "opacity": 0.9 } }
      ],
      "media": [
        { "query": "(min-width: 768px)", "properties": { "padding": "14px 20px" } }
      ]
    }
  }
}
```

See `AiInstructions/INPUT_SCHEMA.md` for complete specification.

## Supported CSS Properties

The parser detects and categorizes these value types:
- **Colors**: hex (#RGB, #RRGGBB, #RRGGBBAA), rgb/rgba, hsl/hsla, named colors
- **Lengths**: px, em, rem, %, vh, vw, pt, cm, mm, in
- **Keywords**: flex, block, inline, auto, bold, italic, solid, dashed, etc.
- **Functions**: calc(), var(), min(), max(), clamp(), rgb(), hsl(), etc.
- **Shadows**: box-shadow and text-shadow (single and multiple)
- **URLs**: url() function for images and fonts

All CSS properties are accepted; unknown values are preserved in the `raw` field.

## Project Structure

```
src/main/kotlin/app/
├── Main.kt                    # CLI entry point
├── Models.kt                  # IR data classes
├── parsing/
│   ├── Parsing.kt             # Parser router
│   ├── css/
│   │   ├── CssParsing.kt      # JSON→IR conversion
│   │   ├── properties/        # Property parsing system
│   │   ├── selectors/         # Selector parsing
│   │   └── mediaQueries/      # Media query parsing
│   ├── compose/               # Compose→IR (future)
│   └── swiftUI/               # SwiftUI→IR (future)
└── logic/
    ├── Logic.kt               # Generation orchestrator
    ├── compose/               # Compose generator (pending)
    ├── swiftUI/               # SwiftUI generator (pending)
    └── css/                   # CSS generator (pending)
```

## Development

### Building
```bash
./gradlew build
```

### Running tests (once implemented)
```bash
./gradlew test
```

### Generating IR output for debugging
The converter automatically outputs the IR to `<outDir>/tmpOutput.json` for inspection.

## Documentation

- **`AiInstructions/ALWAYS_READ_FIRST.md`** - Quick overview and golden rules
- **`AiInstructions/PROJECT_OBJECTIVE.md`** - Goals, scope, and acceptance criteria
- **`AiInstructions/ARCHITECTURE_PLAN.md`** - System design and implementation details
- **`AiInstructions/MILESTONES.md`** - Progress tracking and roadmap
- **`AiInstructions/INPUT_SCHEMA.md`** - Complete input format specification
- **`AiInstructions/DECISIONS.md`** - Technical decisions and policies
- **`AiInstructions/OPEN_QUESTIONS.md`** - Resolved and open design questions
- **`AiInstructions/TESTING.md`** - Testing strategy and test plans
- **`AiLogs/logs.md`** - Development session logs

## Contributing

See `AiInstructions/CONTRIBUTING_NOTES.md` for principles and workflow.

## License

See `LICENSE` file for details.
