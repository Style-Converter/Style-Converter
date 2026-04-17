package com.styleconverter.test.style.layout

import androidx.compose.ui.Modifier

// Phase 7 step 1 scaffold applier.
//
// Split mirror of LayoutExtractor: the extractor produces a LayoutConfig, the
// applier consumes it to produce either:
//   (a) a Compose Modifier contribution for CHILD-level layout properties
//       (zIndex, alignSelf, order) — applied on the component itself; or
//   (b) a ContainerDecision that the ComponentRenderer consumes to pick
//       the correct Compose container primitive (Row / Column / Box /
//       LazyVerticalGrid / custom position-layer).
//
// Step 1 leaves both hooks as identity: Modifier is returned unchanged, and
// ContainerDecision.default signals "let the legacy renderer decide." This
// keeps the Phase 7 rollout zero-behavioral-change until step 2 starts
// populating FlexContainer decisions.

/**
 * High-level classification of the Compose container primitive the renderer
 * should use for this component. The enum is intentionally coarse — the
 * finer details (arrangement, alignment, scroll behaviour, etc.) live on
 * the enclosing [ContainerDecision] struct so they can be expanded per
 * category without churning this enum.
 */
enum class ContainerKind {
    /** Step 1 sentinel: defer to the legacy [com.styleconverter.test.style.core.renderer.ComponentRenderer] container logic. */
    Legacy,

    /** CSS `display: flex` or `inline-flex` — render via Row/Column + Arrangement. */
    Flex,

    /** CSS `display: grid` or `inline-grid` — render via LazyVerticalGrid / GridRenderer. */
    Grid,

    /** CSS `display: block | inline-block | inline | flow-root`. */
    Block,

    /** CSS `display: none` — suppress render. */
    None
}

/**
 * Decision payload returned by [LayoutApplier.containerDecision]. Encapsulates
 * everything the ComponentRenderer needs to pick + configure its container
 * primitive.
 *
 * Step 1: every field is null and [kind] is [ContainerKind.Legacy]. The
 * ComponentRenderer consumer falls through to the existing logic.
 *
 * Later steps populate:
 *   - flexbox step: kind = Flex + arrangement + alignment
 *   - grid step: kind = Grid + gridTemplate
 *   - position step: a wrapping layer is applied around whatever container
 *     the flex/grid step picks (composition, not replacement).
 */
data class ContainerDecision(
    val kind: ContainerKind = ContainerKind.Legacy,
    // Placeholder slots — filled in by later phase steps. Typed as Any? so
    // step 1 doesn't have to import the Compose Arrangement / Alignment
    // types prematurely; step 2 will tighten these.
    val arrangement: Any? = null,
    val alignment: Any? = null
) {
    companion object {
        /**
         * Step 1 default — instructs the renderer to keep using the legacy
         * path. All follow-up steps compare against this instance to detect
         * "no engine override" cheaply.
         */
        val default: ContainerDecision = ContainerDecision()
    }
}

/**
 * Applier for layout configs. Owns both sides of the Phase 7 contract:
 *   - [childModifier]     — contributions to the component's own Modifier
 *     (zIndex, alignSelf, order, position-relative offsets).
 *   - [containerDecision] — how the component should render ITS CHILDREN
 *     (flex row/column, grid, legacy fallback).
 *
 * Step 1: both are identity / default.
 */
object LayoutApplier {

    /**
     * Build the Compose [Modifier] contribution for child-level layout
     * properties (zIndex, alignSelf, order, position inset when
     * position=relative/sticky, etc.).
     *
     * Step 1: identity. The legacy [com.styleconverter.test.style.StyleApplier]
     * still produces the real modifier chain because every layout property
     * stays on its legacy path until step 6 reconciliation.
     */
    fun childModifier(config: LayoutConfig): Modifier {
        // TODO(phase7/step2+): wire zIndex, alignSelf, order, position offsets.
        // Returning the empty Modifier means ComponentRenderer's `.then(...)`
        // is a no-op — exactly what we want for zero behavioral change.
        return Modifier
    }

    /**
     * Decide which Compose container to render children with.
     *
     * Step 1: always returns [ContainerDecision.default] so the
     * ComponentRenderer falls through to its existing dispatch.
     */
    fun containerDecision(config: LayoutConfig): ContainerDecision {
        // TODO(phase7/step2-5): switch on config.display, flex*, grid*, position.
        return ContainerDecision.default
    }
}
