Session start: 2025-08-17

- Created `AiInstructions/PROJECT_OBJECTIVE.md` describing goals, scope, and approach
- Created `AiInstructions/ARCHITECTURE_PLAN.md` outlining pipeline and components
- Created `AiInstructions/MILESTONES.md` with MVP-first roadmap
- Created `AiInstructions/CONTRIBUTING_NOTES.md` for process and principles

Questions to confirm (blocking detailed design):
- iOS target: SwiftUI details (version) and Android Compose/Material versions
- Exact CSS input format and allowed shorthands/variables
- Unit conversion policy (px↔dp/sp) and font family strategy
- Flexbox subset for MVP, shadow and border complexity scope
- Output file naming and diagnostics delivery (inline vs separate)
- Preferred implementation language (TS/Node vs Kotlin/Swift)
- CLI expectations and test strategy (goldens, screenshots later)

Decisions recorded from user (2025-08-17):
- iOS target: SwiftUI
- Android: Jetpack Compose with Material 3
- Input will be JSON with components; will include selectors/media (schema TBD); shorthands allowed
- Fonts: system fonts for MVP; broader font support later
- Colors: support all formats shared across CSS/Compose/Swift
- Layout: include flex-wrap; prioritize adding grid soon
- Shadows: prefer elevation, fallback to custom if needed
- Output naming: `webStyles.json`, `iosStyles.json`, `androidStyles.json`
- Diagnostics: inline comments
- Language: TypeScript/Node for CLI + library

Added/updated docs:
- `AiInstructions/DECISIONS.md`
- `AiInstructions/INPUT_SCHEMA.md` (draft schema for JSON input)
- Updated: `PROJECT_OBJECTIVE.md`, `ARCHITECTURE_PLAN.md`, `MILESTONES.md`, `OPEN_QUESTIONS.md`
- `TESTING.md` describing snapshot/golden tests

Further confirmations (from user):
- Units policy confirmed (px→dp; px→sp; line-height rules)
- Variables: MVP will use resolved values only; tokens/globals to be added later
- Borders: per-side control required in MVP (width/color) and per-corner radii

Docs updated accordingly (`DECISIONS.md`, `INPUT_SCHEMA.md`, `ARCHITECTURE_PLAN.md`, `MILESTONES.md`).

Scaffold added:
- Node/TypeScript project with CLI `style-converter`
- Schema validation via AJV, IR types, parsing/normalization modules
- Generators placeholders for Compose/SwiftUI
- First golden test for per-side borders and scaffolded outputs

Next work items:
- Implement margin/padding shorthands and layout (flex + wrap)
- Generate minimal Compose/SwiftUI code for colors, spacing, borders

---

2025-08-17 (later the same day) — Kotlin migration

- Per user request, removed all TypeScript/Node sources and tests from the repo, preserving only `AiInstructions/`, `AiLogs/`, `LICENSE`, and `README.md`.
- Scaffolded a fresh Kotlin Gradle project at repo root:
  - `settings.gradle.kts`, `build.gradle.kts`
  - `src/main/kotlin/app/Main.kt` implementing MVP pipeline (parse JSON → IR → emit `androidStyles.json` and `iosStyles.json`).
- Installed JDK 17 (Temurin) via winget; installed local Gradle 8.9 distribution in repo and updated PATH for the session.
- Verified `gradle build` succeeds.
- Added `examples/` with `mvp-border-per-side.json` and usage `examples/README.md`.
- Sanity-run pending: execute CLI against example to produce outputs in `out/`.

---

2025-08-30 — Kotlin CLI progress: CSS parser scaffolding, IR printing

- Implemented CSS parsing pipeline for JSON spec (per `AiInstructions/INPUT_SCHEMA.md`) in Kotlin:
  - Added IR model annotations with `@Serializable` for pretty JSON output
  - Created per-style modules under `src/main/kotlin/app/parsing/css/styles/`:
    - `background/Background.kt`: background-color → `BaseIR.backgroundColor` (hex parsing, supports #RRGGBB and #AARRGGBB)
    - `opacity/Opacity.kt`: opacity → `BaseIR.opacity`
    - `border/Border.kt`: per-side border longhands and corner radii → `BorderIR` and `BorderRadius` (parses "<width> <style> <color>")
    - `spacing/Spacing.kt`: margin-top → `SpacingIR.marginTopPx`
    - `size/Size.kt`: width → `SizeIR.widthPx`
    - `layout/Layout.kt`: display → `LayoutIR.display`
  - Added styles router `styles/BaseStyles.kt` that dispatches CSS props to handlers and merges into `BaseIR`.
  - Implemented selectors and media parsing:
    - `selectors/Selectors.kt`: parses `when` + `styles` → `SelectorIR(condition, stylesIR)`
    - `mediaQueries/Media.kt`: parses `query` + `styles` → `MediaIR(query, stylesIR)`
  - Wired `cssParsing` to build `DocumentIR(components=...)` from `components` object.

- Main pipeline improvements:
  - `Main.kt` now prints the parsed IR as pretty JSON using kotlinx.serialization before generation.
  - Example run (`examples/mvp-border-per-side.json`) successfully prints human-readable IR and then exits at generator stub with a friendly message.

- Model fixes:
  - `SelectorIR` now uses `condition: String` (was invalid `when` identifier).
  - `ComponentIR` uses `name: String`.

- Generators are still placeholders (Compose/SwiftUI/CSS): they print a message and exit gracefully.

- Notes vs instructions alignment:
  - Original docs referenced a TypeScript/Node stack; current implementation is Kotlin CLI. Architecture concepts still apply (Parse → IR → Generate). Update docs later to reflect Kotlin path or maintain both tracks explicitly.

- Next steps suggested:
  - Expand style support: padding/margin shorthands, typography fields, height/min/max sizes, simple flex props.
  - Implement minimal Compose/SwiftUI generators to emit basic code snippets from IR.
  - Add golden tests for the Kotlin pipeline (Gradle test task) matching `AiInstructions/TESTING.md` intent.

---

2025-10-13 — Architecture refactoring: Property parser system + Java/Kotlin/Gradle upgrades

**Major Refactoring:**
- Replaced per-style modules with unified property parser system:
  - Removed old `styles/` directory structure
  - Created `properties/parsers/` with registry pattern
  - Implemented `PropertyParserRegistry` for routing properties to specialized parsers
  - Created `PropertyParserInterface` contract for all parsers
  - Implemented `GenericPropertyParser` as universal fallback that detects value types
  - Built primitive parsers as reusable components:
    - `ColorParser.kt`: Hex, RGB/RGBA, HSL/HSLA, named colors
    - `LengthParser.kt`: px, em, rem, %, vh/vw, etc.
    - `KeywordParser.kt`: CSS keywords (flex, bold, auto, etc.)
    - `FunctionParser.kt`: calc(), var(), transform functions, etc.
    - `ShadowParser.kt`: box-shadow and text-shadow parsing
    - `UrlParser.kt`: url() function parsing
    - `PrimitiveParser.kt`: Base utilities for primitive detection

**IR Model Evolution:**
- Simplified to single `IRProperty` class instead of separate style classes
- Properties now contain typed lists: lengths, colors, keywords, functions, shadows, urls
- Added `raw` field to preserve original value for fallback
- Created `IRFunction` and `IRFunctionArg` for function value representation
- All IR models marked `@Serializable` for JSON debug output

**Parser Architecture:**
- `PropertiesParser` orchestrates parsing of all properties
- Looks up parser via `PropertyParserRegistry.find(propertyName)`
- Currently uses `GenericPropertyParser` for all properties (specialized parsers planned)
- Generic parser detects and classifies value types automatically
- Returns `IRProperty` with all detected values categorized

**Build System Updates:**
- Upgraded from Java 17 to Java 21 (required for system compatibility)
- Upgraded Kotlin from 1.9.24 to 2.1.0 (Java 21 support)
- Upgraded Gradle from 8.13 to 8.14
- Removed `jvmToolchain` constraint to allow Java 21 usage
- Fixed gradlew permissions for execution

**Testing:**
- Successful manual test run with `examples/mvp-border-per-side.json`
- IR output generates correctly to `out/tmpOutput.json`
- Pipeline confirmed working: JSON → CssComponents → IR → JSON output
- Generators still placeholder (print message and exit)

**Documentation Updates:**
- Rewrote all `AiInstructions/*.md` files to reflect actual Kotlin implementation
- `DECISIONS.md`: Added Kotlin stack details, architecture overview, implementation status
- `ARCHITECTURE_PLAN.md`: Complete rewrite with actual project structure, data flow, tech stack
- `MILESTONES.md`: Updated with progress indicators (✅ completed, 🔄 in progress, ⏳ planned)
- `INPUT_SCHEMA.md`: Documented actual JSON structure, parsing process, value type support
- `OPEN_QUESTIONS.md`: Categorized resolved vs open questions, added new questions for generators
- `TESTING.md`: Expanded with current manual testing and detailed M4 test plan
- Updated `AiLogs/logs.md` with this session's progress

**Current State:**
- ✅ Full parsing pipeline functional (JSON CSS → IR)
- ✅ Modular, extensible parser architecture
- ✅ All primitive value types supported (colors, lengths, keywords, functions, shadows, URLs)
- ✅ Selectors and media queries parsing
- ✅ IR serialization for debugging
- ⏳ Generators pending (Compose, SwiftUI, CSS)
- ⏳ Specialized property parsers pending (borders, gradients, shorthands)
- ⏳ Test framework setup pending

**Next Priorities:**
1. Implement Compose generator (M2)
2. Implement SwiftUI generator (M2)
3. Add shorthand expansion (margin, padding, border, etc.)
4. Set up Gradle test framework with golden tests (M4)
5. Implement specialized property parsers for complex properties

**Known Issues:**
- PropertyParserRegistry has commented-out specialized parser registration (needs implementation)
- Generators call exitProcess(0) instead of returning actual output
- No unit conversion yet (px → dp/sp/pt)
- No shorthand expansion (margin, padding, border stored as-is)


