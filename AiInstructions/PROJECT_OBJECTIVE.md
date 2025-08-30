## Style-Converter — Project Objective

**One-liner**: Convert style definitions between platforms (CSS ⇄ Android Jetpack Compose ⇄ iOS Swift) with pixel-accurate visual parity and clear, explainable mappings.

### Problem
Designs and styles exist across multiple ecosystems. Manually translating styles between web (CSS), Android (Jetpack Compose), and iOS (Swift) is time-consuming and error-prone.

### Goal
Given style text as input, output one or more text files containing equivalent styles/components for target platforms, preserving visual appearance as closely as possible.

### Primary Use Case (initial)
- Input: JSON style specification containing CSS-like properties, with optional selectors and media queries (subset)
- Output: Two lists of code snippets
  - Android: Kotlin Jetpack Compose composables using idiomatic `Modifier`/`TextStyle`/layout primitives
  - iOS: Swift (SwiftUI, unless otherwise specified) components with matching modifiers

### Secondary Use Cases (next)
- Input: Android Compose modifiers/composables
- Output: CSS and Swift
- Input: iOS Swift components (SwiftUI)
- Output: CSS and Compose

### Acceptance Criteria
- Correctness: Visual parity must be within an acceptably small delta for supported properties
- Determinism: Same input yields identical output
- Explainability: Each mapping is traceable to a rule (documented mapping table)
- Extensibility: New properties/platforms can be added without refactoring core architecture
- Validation: Automated snapshot tests compare rendered output (golden tests per platform where feasible)

### Constraints and Assumptions
- Initial input is JSON per `AiInstructions/INPUT_SCHEMA.md`; supports shorthands, and a subset of selectors and media queries
- Initial iOS target is SwiftUI
- Prefer idiomatic platform code while preserving look over strictly literal conversions
- When a property is unsupported on a target platform, output a warning with best-effort fallback

### Non-Goals (for MVP)
- Full CSS parsing (selectors, media queries, animations, complex specificity)
- Layout engines beyond a defined Flexbox subset in MVP
- Image asset generation or font file management

### High-Level Approach
1. Parse input into a common, strongly-typed Intermediate Representation (IR)
2. Validate and normalize IR (resolve units, color spaces, defaults)
3. Generate platform-specific code from IR via rule-based mappers
4. Provide diagnostics (unsupported properties, lossy conversions) and references

### Deliverables
- Specification of supported properties and mappings
- CLI and library APIs for conversion
- Test suite with golden cases and cross-platform parity checks
- Documentation for mappings, limitations, and examples

### Glossary
- IR: Intermediate Representation — platform-agnostic style model
- Visual parity: Rendered UI appears the same to the human eye within tolerance
- Mapping rule: A documented, test-backed transform from IR to target code


