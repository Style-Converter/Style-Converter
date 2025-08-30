## Open Questions (Please Confirm)

### Platforms / Targets
1. iOS target is SwiftUI, not UIKit — correct? Minimum iOS version?
2. Android: Jetpack Compose version and Material version (M2 vs M3)? Min SDK?

### Input Format
3. Input format: JSON spec with components, optional selectors/media; shorthands allowed — confirm the draft in `INPUT_SCHEMA.md` works for you.
4. Variables support: By variables we mean CSS custom properties (e.g., `--primary`, used via `var(--primary)`). Should MVP require supporting them, or will the input JSON provide resolved values only?
5. Compose/Swift input later: restricted to a subset (e.g., `Modifier` chains, basic views)?

### Units, Typography, Colors
6. Mapping px→dp/sp rules: assume 1px ≈ 1dp for layout and px→sp for font-size? Any scaling factors?
7. Font families: restrict to system fonts initially or support custom families? Preferred fallbacks?
8. Color formats supported: hex, rgba, hsla? Assume sRGB?

### Layout & Effects
9. Flexbox subset only (direction, justify, align, gap) for MVP? Handling of wrap?
10. Shadows: single `box-shadow` only for MVP? Elevation vs custom shadow rendering on Compose?
11. Borders granularity: Do you need different border widths/colors/radii for each side (top/right/bottom/left) in MVP, or is a single uniform border sufficient at first?

### Output Shape & Files
12. Output as: two files (Compose and SwiftUI) per input block? Naming convention?
13. Should we include comments with diagnostics in generated code, or write a separate report file?

### Tooling & Implementation
14. Preferred implementation language for the converter: TypeScript/Node (proposed) or Kotlin/Swift?
15. CLI interface expectations and API shape? Example command usage?
16. Testing: okay to use snapshot/golden tests; any requirement for visual (screenshot) parity later?

### Roadmap Priorities
17. After MVP, prioritize CSS→(others) breadth vs reverse (Compose/Swift→CSS) first?
18. Any specific components/properties that are must-have in MVP?


