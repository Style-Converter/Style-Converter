package com.styleconverter.test.style.layout

// Phase 7 layout style-engine aggregate config.
//
// This is the SCAFFOLD introduced in step 1 of the Phase 7 rollout. Every field
// is nullable and defaults to null. Later phase steps populate the individual
// categories (flexbox -> step 2, grid -> step 3, position -> step 4,
// advanced/root -> step 5, legacy reconciliation -> step 6).
//
// The shape mirrors the IR property tree under
//   src/main/kotlin/app/irmodels/properties/layout/
// so that any field here traces one-to-one back to a CSS longhand.
//
// Rationale for one aggregate Config (rather than per-category configs):
//   - flexbox / grid / position properties interact at the *container* level
//     (Display=Grid implies GridTemplate* matter; Position=Absolute implies
//     Top/Right/Bottom/Left become physical pixels). A single rollup lets
//     LayoutApplier + ComponentRenderer branch once rather than threading
//     seven config objects through the renderer.
//   - Keeps parity with TypographyConfig precedent (see typography/TypographyConfig.kt).

/**
 * Aggregate configuration for CSS layout properties.
 *
 * All fields are nullable — a null value means "not specified in CSS, inherit
 * / initial / platform default." The Applier and ComponentRenderer consumer
 * are responsible for deciding the default when null.
 *
 * Fields group by IR sub-folder:
 *   - flexbox/  -> display + flex* + align/justify*
 *   - grid/     -> gridTemplate* + gridAuto* + grid placements + justify/align Items/Self
 *   - position/ -> position + inset + zIndex
 *   - advanced/ -> (not yet modelled in this scaffold; future step)
 *   - root      -> clear + float
 */
data class LayoutConfig(
    // ===== flexbox/ =====
    // Maps to DisplayProperty.kt. `null` = legacy block/inline defaults.
    val display: DisplayKind? = null,
    // FlexDirection + FlexWrap govern the main/cross axis. Nullable so
    // containers with no flex property at all skip the flex codepath entirely.
    val flexDirection: FlexDirection? = null,
    val flexWrap: FlexWrap? = null,
    // Alignment keywords — one enum reused across all 5 align/justify props
    // because CSS shares the <content-alignment> / <self-alignment> grammars.
    val justifyContent: AlignmentKeyword? = null,
    val alignItems: AlignmentKeyword? = null,
    val alignContent: AlignmentKeyword? = null,
    // Child-level alignment override.
    val alignSelf: AlignmentKeyword? = null,
    // FlexGrow/Shrink are numeric; Order is signed integer.
    val flexGrow: Float? = null,
    val flexShrink: Float? = null,
    val flexBasis: FlexBasisValue? = null,
    val order: Int? = null,

    // ===== grid/ =====
    // Template tracks — see irmodels/layout/grid/TrackSize.kt for the shape.
    val gridTemplateColumns: GridTrackList? = null,
    val gridTemplateRows: GridTrackList? = null,
    // 2-D name map for grid-template-areas.
    val gridTemplateAreas: List<List<String>>? = null,
    // Auto tracks for implicitly created rows/columns.
    val gridAutoColumns: GridTrackList? = null,
    val gridAutoRows: GridTrackList? = null,
    val gridAutoFlow: GridAutoFlow? = null,
    // Item placements.
    val gridArea: GridPlacement? = null,
    val gridColumn: GridLinePair? = null,
    val gridRow: GridLinePair? = null,
    // Item + content alignment keywords (shared enum with flexbox).
    val justifyItems: AlignmentKeyword? = null,
    val justifySelf: AlignmentKeyword? = null,

    // ===== position/ =====
    val position: PositionKind? = null,
    // Resolved top/right/bottom/left after mapping logical inset-*-* to physical
    // via LayoutDirection. See irmodels/layout/position/PositionValueTypes.kt.
    val inset: InsetRect? = null,
    // null == `auto` (CSS default). Int so negative values are legal.
    val zIndex: Int? = null,

    // ===== root (Clear + Float) =====
    // Reuse the existing FloatConfig enums so step 2 doesn't have to rewrite
    // callers that already consume ClearValue / FloatValue.
    val clear: ClearValue? = null,
    val float: FloatValue? = null
) {
    companion object {
        /** Identity config — every field null. Used as the scaffold default. */
        val Empty = LayoutConfig()
    }
}

// --- Placeholder enums / sealed classes ------------------------------------
// These are empty shells for step 1. Later steps flesh them out.

/**
 * CSS `display` values. Mirrors irmodels/layout/flexbox/DisplayProperty.kt.
 * Step 1: enum only — no dispatch logic reads it yet.
 */
enum class DisplayKind {
    Flex, InlineFlex,
    Grid, InlineGrid,
    Block, InlineBlock, Inline,
    None, Contents, FlowRoot
}

/**
 * Main-axis direction for flex containers. Matches
 * irmodels/layout/flexbox/FlexDirectionProperty.kt value set.
 */
enum class FlexDirection {
    Row, RowReverse, Column, ColumnReverse
}

/**
 * Flex-wrap rule. Matches irmodels/layout/flexbox/FlexWrapProperty.kt.
 */
enum class FlexWrap {
    NoWrap, Wrap, WrapReverse
}

/**
 * Unified alignment keyword for both <content-alignment> and <self-alignment>
 * CSS productions. Step 1 stub — not every value is legal in every slot
 * (e.g. stretch isn't legal for justify-content), but the scaffold keeps one
 * enum until step 2 decides whether to split.
 */
enum class AlignmentKeyword {
    Start, End, Center, Stretch,
    FlexStart, FlexEnd,
    SpaceBetween, SpaceAround, SpaceEvenly,
    Baseline, Normal, Auto
}

/**
 * `flex-basis` value. Sealed because it can be a length, a keyword
 * (`auto`, `content`), or a percentage. Step 1 carries just the default
 * case so nothing compiles against specific variants yet.
 */
sealed class FlexBasisValue {
    /** Placeholder — later steps add Length / Percentage / Auto / Content variants. */
    object Default : FlexBasisValue()
}

/**
 * Track list for grid-template-{columns,rows} and grid-auto-{columns,rows}.
 * Sealed to accommodate fixed lengths, `repeat()`, `minmax()`, named lines.
 */
sealed class GridTrackList {
    /** Placeholder default for step 1. */
    object Default : GridTrackList()
}

/**
 * grid-auto-flow keywords: row / column / row dense / column dense.
 */
enum class GridAutoFlow {
    Row, Column, RowDense, ColumnDense
}

/**
 * Item placement for grid-area (four-line shorthand). Step 1 default shell.
 */
sealed class GridPlacement {
    object Default : GridPlacement()
}

/**
 * grid-column / grid-row pair (start + end line). Step 1 default shell.
 */
sealed class GridLinePair {
    object Default : GridLinePair()
}

/**
 * CSS `position` values.
 */
enum class PositionKind {
    Static, Relative, Absolute, Fixed, Sticky
}

/**
 * Rectangular inset (top/right/bottom/left) after logical-to-physical mapping.
 * Step 1 keeps a data class with nullable Dp placeholders so nothing compiles
 * against specific fields yet; later steps will populate via LayoutDirection.
 */
data class InsetRect(
    // Null here = CSS `auto`. Float so it can carry raw Dp values without
    // importing androidx.compose.ui.unit.Dp into the scaffold (that comes in
    // step 4 when PositionApplier is rewired).
    val top: Float? = null,
    val right: Float? = null,
    val bottom: Float? = null,
    val left: Float? = null
) {
    companion object {
        /** All-auto inset. */
        val Auto = InsetRect()
    }
}
