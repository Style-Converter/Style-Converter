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


