# Style-engine rollout

Long-running plan to bring iOS, Android, and Web SDUI renderers to
exhaustive IR property coverage. Referenced from `CLAUDE.md`.

Ground rules live in `CLAUDE.md` under *Style-engine architecture*,
*Per-property contract*, and *Done definition for a property*. Read those
first. This file is the **execution plan** that sits on top.

## Canonical category set (mirrors irmodels)

```
animations      appearance       background       borders/
  ├─ sides          (width, color, style, per-side + logical)
  ├─ radius         (per-corner + logical)
  └─ image          (source, slice, width, outset, repeat)
color            columns          container        content
counters         effects/
  ├─ clip           (clip-path)
  ├─ mask
  ├─ shadow         (box-shadow, text-shadow)
  ├─ filter         (filter + backdrop-filter)
  ├─ shapes         (shape-outside, shape-margin…)
  └─ blend          (mix-blend-mode, background-blend-mode, isolation)
experimental     global           images           interactions
layout/
  ├─ advanced
  ├─ flexbox
  ├─ grid
  └─ position
lists            math             navigation       paging
performance      print            regions          rendering
rhythm           scrolling        shapes           sizing
spacing          speech           svg              table
transforms       typography
```

Every platform's style engine has this tree. Empty categories keep a
`README.md` explaining what goes in them.

## Phase matrix

Phases are **sequential in their dependencies**: Phase 1 requires Phase 0,
Phases 2–10 require Phase 1. Within Phase 0 and within each category
phase, work is parallel.

| # | Phase | Files | Parallelism | SSIM gate |
|---|---|---|---|---|
| 0 | Architectural scaffold | all three trees + registries + ROLLOUT.md | 3 agents (one per platform) | ≥ baseline on existing 109 components |
| 1 | Primitive extractors (length, color, angle, time, number, keyword) | ~6 shared files × 3 platforms | 1 agent per platform × 3 | primitives-test fixture ≥ 0.95 cross-platform |
| 2 | spacing (26) | padding, margin, gap, scroll-padding/margin | 1 agent × 3 platforms | ≥ 0.95 on every variant |
| 3 | sizing (7) | width, height, min/max, aspect-ratio, block/inline-size | 1 agent × 3 | ≥ 0.95 |
| 4 | colors + background (37) | bg color, gradients, image, position, size, repeat | 2 agents (colors, images) × 3 | ≥ 0.95 |
| 5 | borders (47) | widths, colors, styles, radius, image | 3 agents (sides, radius, image) × 3 | ≥ 0.95 |
| 6 | typography (110) | fonts, text align, decoration, variants, emphasis, writing-mode | 4 agents × 3 | ≥ 0.95 |
| 7 | layout (51) | flexbox, grid, position, advanced | 3 agents × 3 | ≥ 0.95 |
| 8 | effects + transforms (40) | shadow, filter, clip-path, mask, transform, 3D, motion | 3 agents × 3 | ≥ 0.95 |
| 9 | animations + transitions (26) | all timing + state properties | 1 agent × 3 | end-frame ≥ 0.95 |
| 10 | long tail (~120) | scrolling, svg, tables, lists, columns, counters, container | parallel batches | ≥ 0.95 where testable |
| 11 | baseline + docs + coverage matrix | final sweep | serial | all above hold |

## Fan-out protocol per phase

For each category phase:

1. **Fixture agent** writes `examples/properties/{category}/{property}.json` with
   one component per CSS value variant the parser supports. See
   `src/main/kotlin/app/parsing/css/properties/longhands/{category}/` for
   the exhaustive list — NEVER guess, read the parser.
2. **Implementation agents** (one per property family, per platform) write
   the `Config/Extractor/Applier` triplet in the canonical subfolder.
   Rules: short file, fully commented, registered with `PropertyRegistry`.
3. **Verification**: run `./test-all.sh examples/properties/{category}/{property}.json`
   and review `testing/report/index.html`.
4. **Iterate** until every variant in the fixture hits SSIM ≥ 0.95 across
   all three pairs.
5. **Baseline + commit**: `UPDATE_BASELINE=1 ./test-all.sh …` for that
   fixture, commit captures + baseline + code in one PR.

## Resume points

If context runs out mid-phase, pick up here:

- **Did Phase 0 complete?** Check: all three platforms have the canonical
  folder tree under `style/`, `StyleEngine/`, `style/engine/` respectively,
  each with a `PropertyRegistry`. The pre-refactor Android + current
  Android MUST produce byte-identical screenshots (test via the
  baseline committed at start of Phase 0).
- **Phase 1**: each platform's extractor file for `length` handles every
  unit in `ValueTypes.kt`'s `LengthUnit` enum, verified via
  `examples/primitives-test.json`.
- **Phase N category**: read `testing/baseline/` and the comparison
  report — categories that show ≥ 0.95 SSIM across pairs are done.

## Non-goals (don't do this)

- Don't re-implement CSS parsing inside the style engines. The engines
  consume already-parsed IR; if a value isn't in the IR, it means the
  PARSER needs work, not the engine.
- Don't split shared primitives across categories. Colors,
  lengths, angles, etc. live in ONE shared extractor per platform and
  every category imports from it.
- Don't merge Config/Extractor/Applier into a single file "for
  brevity" — the separation is a coverage-audit tool. `git ls-files
  **/*Config.*` should produce the property inventory.
