# borders/sides/ — web style-engine scaffold

Mirrors `src/main/kotlin/app/irmodels/properties/borders/sides/`. Each IR
property in that directory gets a `{Property}Config.ts`,
`{Property}Extractor.ts`, and `{Property}Applier.ts` here.

See `CLAUDE.md` → *Per-property contract*.

## Expected properties

- BorderBlockEndColor
- BorderBlockEndStyle
- BorderBlockEndWidth
- BorderBlockStartColor
- BorderBlockStartStyle
- BorderBlockStartWidth
- BorderBottomColor
- BorderBottomStyle
- BorderBottomWidth
- BorderBoundary
- BorderInlineEndColor
- BorderInlineEndStyle
- BorderInlineEndWidth
- BorderInlineStartColor
- BorderInlineStartStyle
- …

## Status

Empty — properties migrate in one-at-a-time from `../StyleBuilder.ts` as
they're implemented per the `testing/ROLLOUT.md` phase plan.
