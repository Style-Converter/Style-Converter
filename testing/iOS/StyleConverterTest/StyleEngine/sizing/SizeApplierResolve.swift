//
//  SizeApplierResolve.swift
//  StyleEngine/sizing — Phase 3.
//
//  LengthValue → CGFloat resolver for sizing axes. Separated from the
//  SwiftUI modifier code so both (a) `SizeApplier.swift` stays under
//  200 lines and (b) this logic is testable via SizingSelfTest with no
//  SwiftUI dependency.
//
//  Three entry points:
//    * `exact(_:)`       — for Width/Height. Returns nil when the
//                           property is absent, auto, intrinsic, or
//                           otherwise should not pin the axis.
//    * `constraint(_:)`  — for Min/Max *. Returns nil when the
//                           property is absent OR `.none` (no clamp).
//    * `wantsIntrinsic`  — predicate for min-content / max-content /
//                           bare fit-content, signalling that the
//                           applier should call `.fixedSize` on this axis.
//

// CoreGraphics for CGFloat; SpacingResolver supplies the shared resolver.
import CoreGraphics

enum SizeApplierResolve {

    // Exact width/height. `nil` output means "do not attach `.frame(width:)`".
    // We go through SpacingResolver so em/rem/vw/calc all resolve via the
    // single canonical path used by Phase 2 spacing.
    // `allowPercent` defaults to true (width axis). Height-axis callers
    // pass `false` so `height: 50%` degrades to nil (CSS "auto when
    // parent has no definite height" fallback) instead of pinning to
    // 50% of the 844-high viewport.
    static func exact(_ v: LengthValue?,
                      ctx: SpacingContext,
                      parent: CGFloat,
                      allowPercent: Bool = true) -> CGFloat? {
        // Absent → nil → no `.frame()` attached on that axis.
        guard let v = v else { return nil }

        switch v {
        // `.auto` on width/height means "let layout decide" — leave the
        // axis unsized.
        case .auto: return nil
        // Intrinsic (min-content / max-content / fit-content) → the
        // SizeApplier handles these through `.fixedSize`; we return nil
        // so no fixed width is attached.
        case .intrinsic(let kind):
            // fit-content with a bound is a clamp, handled by the applier
            // via `maxWidth`. Caller queries `fitContentBound` directly.
            if case .fitContent(let bound) = kind, let b = bound {
                return resolveConcrete(b, ctx: ctx, parent: parent,
                                       allowPercent: allowPercent)
            }
            return nil
        // `.none` cannot appear on Width/Height in valid CSS; defensive.
        case .none:     return nil
        // `.unknown` — the converter emitted a shape we don't recognise.
        case .unknown:  return nil
        // Percent when disallowed (height axis) → skip.
        case .relative(_, .percent, _) where !allowPercent: return nil
        default:        return resolveConcrete(v, ctx: ctx, parent: parent,
                                               allowPercent: allowPercent)
        }
    }

    // Min/Max clamp. Nil = no clamp. `.none` → nil (explicit "no clamp").
    static func constraint(_ v: LengthValue?,
                           ctx: SpacingContext,
                           parent: CGFloat,
                           allowPercent: Bool = true) -> CGFloat? {
        guard let v = v else { return nil }
        switch v {
        // `max-*: none` and friends → no clamp.
        case .none, .auto, .unknown: return nil
        // Intrinsic constraint (rare) → we can't express "min-content
        // constraint" in SwiftUI's frame API, so skip.
        case .intrinsic:             return nil
        // Percent on height axis is undefined here — skip to preserve
        // pre-Phase-3 byte-stable rendering.
        case .relative(_, .percent, _) where !allowPercent: return nil
        default: return resolveConcrete(v, ctx: ctx, parent: parent,
                                        allowPercent: allowPercent)
        }
    }

    // True when the axis wants SwiftUI `.fixedSize` (intrinsic sizing).
    // Min-content / max-content / bare fit-content all collapse to this
    // because SwiftUI doesn't distinguish them.
    static func wantsIntrinsic(_ v: LengthValue?) -> Bool {
        guard let v = v else { return false }
        if case .intrinsic(let kind) = v {
            switch kind {
            case .minContent, .maxContent: return true
            // Bounded fit-content is expressed via `.frame(maxWidth:)` —
            // not `.fixedSize` — so wantsIntrinsic returns false.
            case .fitContent(let b):       return b == nil
            }
        }
        return false
    }

    // ─── Internals ──────────────────────────────────────────────────────

    // Funnel every resolvable case through SpacingResolver. Percent
    // values come back as `.percent(fraction)` which we multiply by the
    // parent length. `.skip` collapses to nil so the caller omits the
    // modifier rather than pinning the axis to zero.
    private static func resolveConcrete(_ v: LengthValue,
                                        ctx: SpacingContext,
                                        parent: CGFloat,
                                        allowPercent: Bool = true) -> CGFloat? {
        // Guard: percent disallowed → skip. Keeps height-axis calls
        // consistent with the top-level `allowPercent=false` path.
        if !allowPercent, case .relative(_, .percent, _) = v { return nil }
        // `isPadding:false` keeps signs and forwards auto as `.auto`,
        // but we've already filtered auto above so the flag just
        // preserves negatives — which is what we want on sizing too.
        let r = SpacingResolver.resolve(v, ctx: ctx, isPadding: false)
        switch r {
        case .px(let n):      return n
        // Percent of parent axis length (width axis uses parentW, height
        // axis uses parentH — the caller picks which parent to pass in).
        case .percent(let f): return f * parent
        case .auto, .skip:    return nil
        }
    }
}
