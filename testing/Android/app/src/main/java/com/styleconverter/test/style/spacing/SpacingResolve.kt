package com.styleconverter.test.style.spacing

// Shared helper that collapses a LengthValue into Dp for the padding/margin
// appliers. The logic lives here so both Appliers share one source of truth
// for em/rem/vw/vh/% resolution. Anything context-dependent (percentages
// measured against parent width, em against a font size that may come from a
// Composable) takes the required context as a parameter — this file has no
// Compose imports so it's cheap to unit-test.

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.styleconverter.test.style.core.types.LengthUnit
import com.styleconverter.test.style.core.types.LengthValue

/**
 * Context needed to resolve a LengthValue to absolute px. Passed by the
 * Applier which knows the Compose-side environment (font size, viewport,
 * parent width).
 *
 * Percentage resolution: CSS spec says padding-% and margin-% on ALL four
 * sides resolve against the parent's CONTENT WIDTH (yes, even top/bottom).
 * The Applier is responsible for providing [parentWidthPx] from the nearest
 * ancestor box (we use the incoming BoxWithConstraints max width as a
 * pragmatic proxy).
 */
data class SpacingContext(
    // Current font size in px for em resolution (1em = fontSizePx).
    val fontSizePx: Float = 16f,
    // Root font size in px for rem resolution. Defaults to the CSS default.
    val rootFontSizePx: Float = 16f,
    // Viewport width/height in px for vw/vh/vmin/vmax.
    val viewportWidthPx: Float = 390f,
    val viewportHeightPx: Float = 844f,
    // Parent content-box width in px for % resolution. Optional — when null
    // we fall back to viewport width so rendering at least looks plausible.
    val parentWidthPx: Float? = null,
)

/**
 * Reduce [value] to a concrete Dp. Returns 0.dp when the length is Unknown
 * (treat missing as zero — that matches the legacy SpacingApplier behaviour
 * for px-only inputs and the CSS initial value of padding/margin).
 *
 * Calc() is not yet evaluated here (it needs the full CalcExpressionEvaluator
 * pipeline which has its own IR types); we return 0.dp as a safe default and
 * log via a debug hook left for future Phase 3 work. Callers that care about
 * calc can inspect the LengthValue directly before calling resolve().
 */
fun resolveToDp(value: LengthValue?, ctx: SpacingContext): Dp {
    // Missing side → zero. This matches how the old SpacingApplier defaulted
    // unspecified sides, so px-only regressions stay byte-identical.
    if (value == null) return 0.dp
    return when (value) {
        is LengthValue.Exact -> value.px.toFloat().dp
        is LengthValue.Relative -> resolveRelative(value, ctx).dp
        is LengthValue.Calc -> 0.dp  // TODO Phase 3: wire CalcExpressionEvaluator.
        is LengthValue.Auto -> 0.dp  // Auto on padding isn't meaningful; margin handled separately.
        is LengthValue.Intrinsic -> 0.dp  // min-content/max-content invalid here.
        is LengthValue.Fraction -> 0.dp  // fr invalid outside grid tracks.
        is LengthValue.Unknown -> 0.dp
    }
}

/** Convert a Relative LengthValue into a Float px count. */
private fun resolveRelative(r: LengthValue.Relative, ctx: SpacingContext): Float {
    val v = r.value.toFloat()
    return when (r.unit) {
        LengthUnit.PERCENT -> (ctx.parentWidthPx ?: ctx.viewportWidthPx) * v / 100f
        LengthUnit.EM -> v * ctx.fontSizePx
        LengthUnit.REM -> v * ctx.rootFontSizePx
        // Viewport-relative units. Compose fronts a small/large/dynamic
        // distinction that we don't meaningfully support yet; treat the three
        // groups as identical to the classic viewport.
        LengthUnit.VW, LengthUnit.SVW, LengthUnit.LVW, LengthUnit.DVW -> ctx.viewportWidthPx * v / 100f
        LengthUnit.VH, LengthUnit.SVH, LengthUnit.LVH, LengthUnit.DVH -> ctx.viewportHeightPx * v / 100f
        LengthUnit.VMIN, LengthUnit.SVMIN, LengthUnit.LVMIN, LengthUnit.DVMIN ->
            minOf(ctx.viewportWidthPx, ctx.viewportHeightPx) * v / 100f
        LengthUnit.VMAX, LengthUnit.SVMAX, LengthUnit.LVMAX, LengthUnit.DVMAX ->
            maxOf(ctx.viewportWidthPx, ctx.viewportHeightPx) * v / 100f
        // Fallbacks: keep a sane value rather than crashing. Most of these
        // units (ex/ch/cap/ic/lh/rlh/vi/vb/cq*/fr) aren't used by spacing in
        // practice. If the pxFallback is present we take it.
        else -> r.pxFallback?.toFloat() ?: 0f
    }
}
