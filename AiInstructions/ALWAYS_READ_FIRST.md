## Always Read First

**Project**: Style-Converter

**Purpose**: Convert styles between CSS, Android Jetpack Compose, and iOS Swift (SwiftUI) with high visual parity and explainable mappings.

### Golden Rules
- Convert to a common IR first, then generate target code
- Prefer visual fidelity over literal one-to-one API mapping
- Be deterministic and explainable; every output ties back to a rule
- Warn loudly on unsupported or lossy conversions

### MVP Scope (CSS → Compose, SwiftUI)
- Core properties: color, typography, spacing, borders, size, flexbox subset, single shadow

### Start Here
- `AiInstructions/PROJECT_OBJECTIVE.md`
- `AiInstructions/ARCHITECTURE_PLAN.md`
- `AiInstructions/MILESTONES.md`
- `AiInstructions/OPEN_QUESTIONS.md`

### Output Expectations
- Generate idiomatic Kotlin/Swift code blocks ready to paste
- Provide diagnostics alongside code when needed


