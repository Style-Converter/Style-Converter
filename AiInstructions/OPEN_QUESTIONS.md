## Open Questions

### Status Legend
- ✅ **RESOLVED** - Confirmed and documented
- 🔄 **PARTIALLY RESOLVED** - Decided but implementation incomplete
- ⏳ **DEFERRED** - Postponed to future milestone
- ❓ **OPEN** - Awaiting decision

---

## Resolved Questions

### Platforms / Targets
**1. ✅ iOS target is SwiftUI, not UIKit — correct? Minimum iOS version?**
- **Answer**: SwiftUI confirmed. Minimum iOS version TBD (to be specified during generator implementation).
- **Status**: Target confirmed, version spec deferred to M2.

**2. ✅ Android: Jetpack Compose version and Material version (M2 vs M3)? Min SDK?**
- **Answer**: Jetpack Compose with Material 3. Minimum SDK TBD (to be specified during generator implementation).
- **Status**: Material 3 confirmed, SDK spec deferred to M2.

### Input Format
**3. ✅ Input format: JSON spec with components, optional selectors/media; shorthands allowed**
- **Answer**: Confirmed. Uses `"properties"` field (not `"styles"`). Schema documented in `INPUT_SCHEMA.md`.
- **Status**: Implemented in `CssParsing.kt`.

**4. ✅ Variables support: CSS custom properties (e.g., `--primary`, used via `var(--primary)`)**
- **Answer**: MVP uses resolved values only. `var()` functions parsed but not resolved.
- **Status**: Future feature - token resolution system planned for later milestone.

**5. ⏳ Compose/Swift input later: restricted to a subset?**
- **Answer**: Yes, subset approach confirmed. Details TBD when implementing reverse parsers.
- **Status**: Deferred to M5 (Compose→IR) and M6 (SwiftUI→IR).

### Units, Typography, Colors
**6. ✅ Mapping px→dp/sp rules**
- **Answer**: 1px ≈ 1dp for layout, px→sp for font-size. No scaling factors in MVP.
- **Status**: Policy documented, implementation pending in generators.

**7. ✅ Font families: restrict to system fonts initially?**
- **Answer**: System fonts only for MVP. Custom families extensible later.
- **Status**: Policy documented, implementation pending.

**8. ✅ Color formats supported: hex, rgba, hsla? Assume sRGB?**
- **Answer**: All common formats supported (hex, rgb/rgba, hsl/hsla, named). Assume sRGB.
- **Status**: Implemented in `ColorParser.kt`.

### Layout & Effects
**9. ✅ Flexbox subset: direction, justify, align, gap, wrap?**
- **Answer**: Yes, flexbox subset including wrap.
- **Status**: Parsing supported, generator implementation pending.

**10. ✅ Shadows: single `box-shadow` only for MVP? Elevation vs custom?**
- **Answer**: Single shadow for MVP. Prefer elevation mapping where possible, fallback to custom.
- **Status**: Parsing supported via `ShadowParser.kt`, generator implementation pending.

**11. ✅ Borders granularity: per-side widths/colors/radii?**
- **Answer**: Per-side control required (top/right/bottom/left for width/color/style, per-corner radii).
- **Status**: Parsing supported, tested with `mvp-border-per-side.json`.

### Output Shape & Files
**12. ✅ Output files: naming convention?**
- **Answer**: `androidStyles.json`, `iosStyles.json`, `cssStyles.json`. Debug IR: `tmpOutput.json`.
- **Status**: Implemented in `Main.kt` and `Logic.kt`.

**13. 🔄 Diagnostics: inline comments or separate report?**
- **Answer**: Inline comments preferred.
- **Status**: Design confirmed, implementation pending.

### Tooling & Implementation
**14. ✅ Implementation language: TypeScript/Node or Kotlin/Swift?**
- **Answer**: **Kotlin/JVM with Gradle** (changed from original TypeScript plan).
- **Status**: Fully implemented with Kotlin 2.1.0, Java 21, Gradle 8.14.

**15. ✅ CLI interface and API shape?**
- **Answer**: `style-converter convert --from <format> --to <targets> -i <input> -o <outDir>`
- **Status**: Implemented in `Main.kt`.

**16. ✅ Testing: snapshot/golden tests? Visual parity?**
- **Answer**: Snapshot/golden tests for MVP. Visual testing deferred to M7.
- **Status**: Test framework setup pending (M4).

### Roadmap Priorities
**17. ⏳ After MVP: CSS→others breadth vs reverse (Compose/Swift→CSS)?**
- **Answer**: Complete CSS→Compose/SwiftUI first, then reverse parsers.
- **Status**: Reflected in milestone ordering (M1-M3 before M5-M6).

**18. 🔄 Specific must-have properties in MVP?**
- **Answer**: Colors, typography, spacing, borders, sizes, basic flexbox, single shadow.
- **Status**: Listed in `MILESTONES.md` M1 section.

---

## New Open Questions (Current Implementation)

### Code Generation
**19. ❓ Compose output format: Single file with all components or separate files?**
- Should `generateCompose()` output one Kotlin file with all components, or multiple files?
- Considerations: Import management, file organization, IDE navigation

**20. ❓ SwiftUI output format: Struct per component or ViewModifier approach?**
- Should each component be a View struct, ViewModifier, or custom modifier function?
- Example: `Button().primaryButtonStyle()` vs `PrimaryButtonView()`

**21. ❓ Property mapping strategy: Direct mapping or semantic interpretation?**
- For properties with no direct equivalent, use closest match or skip with warning?
- Example: `text-decoration: underline` → Compose/SwiftUI underline modifier?

**22. ❓ Shorthand expansion: During parsing or generation?**
- Should `padding: "10px 20px"` expand to individual sides in IR or during code gen?
- Current: Stored as-is in IR with raw value

**23. ❓ Unit conversion: Hardcoded rules or configurable?**
- Should px→dp/sp conversion ratios be hardcoded or user-configurable?
- Consider: Different design systems may have different scaling

### Testing and Validation
**24. ❓ Test framework: JUnit 5 or Kotlin Test?**
- Which testing framework should be used for Gradle test suite?

**25. ❓ Golden test format: JSON IR comparison or generated code comparison?**
- Should golden tests compare IR output or final generated code?
- Or both?

**26. ❓ Error handling: Exceptions or Result types?**
- How should parsing/generation errors be handled?
- Throw exceptions, return Result<T, Error>, or accumulate errors?

### Future Features
**27. ❓ Plugin system: JVM ServiceLoader or custom registry?**
- How should parser/generator plugins be loaded and registered?

**28. ❓ Design token support: Separate file or embedded?**
- Should design tokens (colors, sizes, etc.) be in a separate JSON or part of input?

**29. ❓ Responsive breakpoints: How to map to Compose/SwiftUI?**
- Media queries → Configuration-based conditionals, or different component variants?

**30. ❓ Theme system: Generate Material Theme / SwiftUI theme files?**
- Should generator output theme definition files for colors, typography, etc.?


