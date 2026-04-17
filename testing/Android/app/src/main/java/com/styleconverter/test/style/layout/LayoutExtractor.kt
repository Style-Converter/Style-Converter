package com.styleconverter.test.style.layout

import com.styleconverter.test.style.PropertyRegistry
import kotlinx.serialization.json.JsonElement

/**
 * Phase 7 step 1 scaffold extractor for the CSS layout family.
 *
 * Responsibilities:
 *   1. Claim every layout IR property name against [PropertyRegistry] so the
 *      legacy [com.styleconverter.test.style.StyleApplier] dispatch switch
 *      defers to the style-engine path instead of double-applying.
 *   2. Expose a single [extractLayoutConfig] entrypoint that returns a fully
 *      resolved [LayoutConfig]. In step 1 every field resolves to null — the
 *      real extraction logic arrives in Phase 7 steps 2–5.
 *
 * Precedent: [com.styleconverter.test.style.typography.TypographyExtractor] —
 * same init-block registration + single `extract*Config` entrypoint shape.
 *
 * Owner strings match the IR sub-folder name for introspection-friendly
 * coverage audits via [PropertyRegistry.allRegistered]. The four sub-folders
 * are: layout/flexbox, layout/grid, layout/position, layout/advanced; plus
 * the four layout root properties (Clear / Float / Overlay / ReadingFlow)
 * registered under "layout".
 */
object LayoutExtractor {

    init {
        // ----- layout/flexbox -----
        // See src/main/kotlin/app/irmodels/properties/layout/flexbox/*.kt for
        // the source-of-truth property files. Each name below maps 1:1 to an
        // IRProperty.type emitted by the CSS parser.
        PropertyRegistry.migrated(
            "Display",
            "FlexDirection", "FlexWrap",
            "FlexGrow", "FlexShrink", "FlexBasis",
            "JustifyContent", "AlignItems", "AlignContent",
            "AlignSelf", "Order",
            // Legacy box-orient lives under flexbox in the IR tree because it
            // predates the flex spec but shares axis semantics.
            "BoxOrient",
            owner = "layout/flexbox"
        )

        // ----- layout/grid -----
        // Mirrors src/main/kotlin/app/irmodels/properties/layout/grid/*.kt.
        // GridTemplate (shorthand) + GridAutoTrack (CSS Grid 3 proposal) are
        // parse-only at this stage but the tripwire test requires them listed.
        PropertyRegistry.migrated(
            "GridTemplateColumns", "GridTemplateRows", "GridTemplateAreas",
            "GridTemplate",
            "GridAutoColumns", "GridAutoRows", "GridAutoFlow",
            "GridAutoTrack",
            "GridArea",
            "GridColumnStart", "GridColumnEnd",
            "GridRowStart", "GridRowEnd",
            "JustifyItems", "JustifySelf",
            "AlignTracks", "JustifyTracks",
            "MasonryAutoFlow",
            owner = "layout/grid"
        )

        // ----- layout/position -----
        // Mirrors src/main/kotlin/app/irmodels/properties/layout/position/.
        // Logical insets (InsetBlock*, InsetInline*) register alongside
        // physical top/right/bottom/left; step 4 will fold them into a
        // single InsetRect via LayoutDirection.
        PropertyRegistry.migrated(
            "Position",
            "Top", "Right", "Bottom", "Left",
            "InsetBlockStart", "InsetBlockEnd",
            "InsetInlineStart", "InsetInlineEnd",
            "ZIndex",
            owner = "layout/position"
        )

        // ----- layout/advanced -----
        // CSS Anchor Positioning (2024) + CSS Motion Path + Position Try.
        // All parse-only on Compose today — registering so the legacy switch
        // doesn't silently claim them while the style-engine catches up.
        PropertyRegistry.migrated(
            "AnchorName", "AnchorScope",
            "InsetArea",
            "OffsetPath", "OffsetDistance", "OffsetAnchor", "OffsetPosition",
            "OffsetRotate", "Offset",
            "PositionAnchor", "PositionArea",
            "PositionFallback", "PositionTry",
            "PositionTryFallbacks", "PositionTryOptions",
            "PositionTryOrder",
            "PositionVisibility",
            owner = "layout/advanced"
        )

        // ----- layout root -----
        // Four top-level IR files directly under layout/: Clear / Float /
        // Overlay / ReadingFlow. FloatExtractor already handles Clear+Float
        // at runtime but did not previously register with PropertyRegistry —
        // we claim them here so the tripwire test stays green.
        PropertyRegistry.migrated(
            "Clear", "Float",
            "Overlay",
            "ReadingFlow",
            owner = "layout"
        )
    }

    /**
     * Extract a fully-resolved [LayoutConfig] from the IR property stream.
     *
     * Step 1 stub: returns [LayoutConfig.Empty] unconditionally. Subsequent
     * Phase 7 steps replace the stub with real per-property extraction. The
     * method signature is locked in now so the facade and ComponentRenderer
     * hook don't churn between steps.
     *
     * @param properties (propertyType, data) pairs from IRComponent.properties.
     * @return LayoutConfig with all fields null in step 1.
     */
    fun extractLayoutConfig(properties: List<Pair<String, JsonElement?>>): LayoutConfig {
        // TODO(phase7/step2-5): populate each category from `properties`.
        // Intentionally ignoring the input in step 1 — every field null means
        // the legacy rendering path runs unchanged.
        return LayoutConfig.Empty
    }
}
