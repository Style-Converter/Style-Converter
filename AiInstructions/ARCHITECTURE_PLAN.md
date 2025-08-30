## Architecture Plan (Incremental, Test-Driven)

### System Overview
- CLI + Library
- Core pipeline: Parse → Normalize → IR → Generate (Android/iOS) → Validate → Report
- Extensibility via plugin-like mapping modules per platform

### Components
- Parser Layer
  - JSON Style Spec Parser (MVP)
  - CSS Declaration Parser (future/optional)
  - Compose Parser (next)
  - SwiftUI Parser (next)
- Normalization
  - Unit resolution (px, dp/sp/pt/rem), color normalization (RGBA/HEX/HSLA), font normalization
  - Expand shorthands (e.g., `margin`, `padding`, `border`) into per-side longhands and per-corner radii
  - (Future) Token resolution step using optional `globals`; MVP expects resolved literals
- IR Model
  - StyleIR: color, typography, spacing, border, shadows, layout
  - LayoutIR: flexbox subset (direction, wrap, justify, align, gap)
  - EffectsIR: shadows, opacity, transforms (subset)
- Generators
  - AndroidGenerator (Jetpack Compose)
  - IOSGenerator (SwiftUI)
- Validation & Diagnostics
  - Capability matrix (supported/partial/unsupported)
  - Warnings and suggestions

### Data Flow
1. Input text → `Parser` → Raw AST
2. AST → `Normalizer` → IR
3. IR → `Generators` → target code text blocks
4. IR + Target code → `Validator` → diagnostics

### Technology Choices (tentative)
- Language: TypeScript or Kotlin; propose TypeScript for portability and fast tooling
- CLI: Node.js + `commander` or `yargs`
- Parsing: JSON schema validation (e.g., `ajv`) for input spec; `postcss` optional for raw CSS in the future; custom lightweight parsers for Compose/SwiftUI later
- Testing: Jest + snapshot tests; golden outputs per case
- Formatting: Prettier for generated code; platform-aware style guides

### Versioned Mapping Tables
- YAML/JSON mapping definitions to decouple rules from code
- Enable community contributions via data files + tests

### Performance Considerations
- Stateless conversions; memoized normalization
- Batch processing for multiple inputs

### Security/Privacy
- Local file processing by default; no network calls for core conversions


