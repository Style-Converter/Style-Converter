## Milestones and Scope

### M0 — Project Bootstrap (this step)
- Objectives and architecture docs
- Logging and contribution scaffolding

### M1 — MVP: JSON Styles → Android/SwiftUI (Core Styles)
- Supported style properties (initial):
  - Colors: `color`, `background-color`, `opacity`
  - Typography: `font-size`, `font-weight`, `font-style`, `line-height`, `letter-spacing`
  - Spacing: `margin`, `padding`
  - Borders: per-side width/color, per-corner radius; `border` and related shorthands expanded
  - Size: `width`, `height`, `min-*`, `max-*`
  - Layout: `display: flex`, `flex-direction`, `flex-wrap`, `justify-content`, `align-items`, `gap`
  - Shadows: `box-shadow` (single)
- Deliverables:
  - CLI command `style-converter convert --from json --to compose,swiftui`
  - IR and mapping tables for above
  - Golden tests for 20+ canonical cases

### M2 — Property Expansion and Edge Cases
- Multiple shadows, complex borders, gradients (best-effort), text decoration, overflow
- Improved unit conversions (px↔dp/sp/pt)
- Diagnostics for unsupported features

### M3 — Input from Android Compose → JSON/SwiftUI
- Parse a subset of `Modifier` chains and typography
- Round-trip tests (JSON → IR → Compose → IR → JSON)

### M4 — Input from SwiftUI → JSON/Compose
- Parse a subset of SwiftUI modifiers
- Round-trip tests (JSON → IR → SwiftUI → IR → JSON)

### M5 — Layout Fidelity and Visual Testing
- Rendering harness to screenshot small components per platform
- Perceptual diff thresholds; regression suite

### M6 — Plugin System and Community Rules
- External rule packs via JSON/YAML with versioning
- Validation of contributed mappings

### M7 — IDE Integrations
- VS Code extension for inline previews
- Quick-fix suggestions


