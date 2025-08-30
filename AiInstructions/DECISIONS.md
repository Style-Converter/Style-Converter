## Decisions (Current)

### Targets
- iOS target: SwiftUI
- Android target: Jetpack Compose with Material 3

### Input
- Source format: JSON-based style spec (not raw CSS). It may include selectors, media queries, and shorthands. A formal schema is proposed in `AiInstructions/INPUT_SCHEMA.md`.

### Output
- File naming conventions:
  - Web: `webStyles.json`
  - iOS: `iosStyles.json`
  - Android: `androidStyles.json`
- Diagnostics: inline comments embedded in generated outputs when applicable

### Implementation
- Language/stack: TypeScript/Node.js for CLI and library

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
- Variables (CSS custom properties):
  - MVP: resolved values only (no `var(--x)`)
  - Future: optional top-level `globals` for tokens and a reference mechanism; not required for MVP


