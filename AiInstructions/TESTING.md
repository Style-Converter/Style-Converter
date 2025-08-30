## Testing Strategy

### Snapshot/Golden Tests (what they are)
- We store expected outputs ("goldens") for representative inputs in version control.
- Each test converts a known input and compares the result to the stored golden.
- If code generation changes unintentionally, the diffs fail the test and highlight exactly what changed.
- When intended changes occur, we update the goldens alongside the code change.

### Why this helps
- Ensures deterministic, stable output for the same input
- Prevents regressions in mappings and formatting

### MVP Plan
- 20+ golden cases covering: colors, typography, spacing, borders, size, flex (including wrap), single shadow, selectors, and basic media queries
- Include diagnostics in-line comments within the generated code where applicable

### Visual Tests (later)
- Render small components on each platform and compare screenshots (perceptual diff)
- Used to validate visual parity beyond textual code equivalence


