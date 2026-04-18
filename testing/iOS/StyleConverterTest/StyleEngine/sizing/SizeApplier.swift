//
//  SizeApplier.swift
//  StyleEngine/sizing — Phase 3.
//
//  ViewModifier that turns a SizeConfig into a chain of SwiftUI
//  `.frame(...)` calls. Percentage lengths need the parent width, so
//  whenever any axis is percent we wrap the content in a GeometryReader.
//  All other lengths are pre-resolved through SpacingResolver so this
//  file stays focused on the SwiftUI wiring.
//
//  Approximations vs CSS:
//    * min-content / max-content → `.fixedSize(...)` on the relevant
//      axis. SwiftUI doesn't differentiate the two; both collapse to
//      the intrinsic size.
//    * fit-content(bound) → clamp with `.frame(maxW/maxH: bound)`.
//      Unbounded fit-content (no bound arrived) falls back to
//      `.fixedSize` — same heuristic as min/max-content.
//    * `auto` → no `.frame` on that axis (SwiftUI's default).
//    * `.none` on max-* → no `maxWidth/Height` attached (no clamp).
//

// SwiftUI for ViewModifier + GeometryReader + frame APIs.
import SwiftUI

// Public modifier. Attached by StyleBuilder.applyStyle via the view
// extension below. Fast-paths the no-sizing case with a single guard.
struct SizeApplier: ViewModifier {
    // The resolved sizing. When nil or empty we return `content` unchanged.
    let config: SizeConfig
    // Threaded spacing context — we reuse it so em/rem/vw all resolve
    // through the same code path as Phase 2 spacing.
    let context: SpacingContext

    func body(content: Content) -> some View {
        // Fast path: nothing to apply.
        guard config.hasAny else { return AnyView(content) }

        // Percent axes resolve against the threaded viewport size. We
        // intentionally do NOT use GeometryReader here because GR has
        // an expand-to-fill side-effect that inflates component card
        // heights in the screenshot harness — breaking byte-stable
        // rendering on the existing baselines. The 390×844 canvas is
        // the effective parent for top-level components; nested
        // components would need a richer context, deferred to a later
        // phase (there are no nested-percent cases in visual-test.json).
        return AnyView(
            SizeApplierMath.apply(content,
                                  config: config,
                                  context: context,
                                  parentW: CGFloat(context.viewportWidth),
                                  parentH: CGFloat(context.viewportHeight))
        )
    }
}

// Helpers live in an enum to keep `SizeApplier.body` concise and to let
// the math be unit-tested without constructing a SwiftUI view tree.
enum SizeApplierMath {

    // Apply every sizing axis. Returns an AnyView so the caller can
    // embed us in either the GeometryReader branch or the fast path.
    static func apply<Content: View>(_ content: Content,
                                     config c: SizeConfig,
                                     context ctx: SpacingContext,
                                     parentW: CGFloat,
                                     parentH: CGFloat) -> AnyView {
        // Resolve each axis to a concrete CGFloat (or nil for
        // unresolvable / auto / none). We split width and height lanes
        // because SwiftUI's `.frame` builder wants both as paired args.
        let w  = SizeApplierResolve.exact(c.width,  ctx: ctx, parent: parentW)
        // Height axis: CSS says `height: %` is `auto` when parent has no
        // definite height. Our parent is a ScrollView with unbounded
        // height, so we treat percent-heights as "skip" — matches the
        // pre-Phase-3 rendering and the CSS fallback. Same for min/max
        // height percent constraints.
        let h  = SizeApplierResolve.exact(c.height,
                                          ctx: ctx, parent: parentH,
                                          allowPercent: false)
        let minW = SizeApplierResolve.constraint(c.minWidth,  ctx: ctx, parent: parentW)
        let maxW = SizeApplierResolve.constraint(c.maxWidth,  ctx: ctx, parent: parentW)
        let minH = SizeApplierResolve.constraint(c.minHeight, ctx: ctx, parent: parentH,
                                                 allowPercent: false)
        let maxH = SizeApplierResolve.constraint(c.maxHeight, ctx: ctx, parent: parentH,
                                                 allowPercent: false)

        // Start unmodified and layer modifiers in CSS order: first the
        // min/max clamps (only attach if present), then exact width/height,
        // then aspect-ratio. `.frame(alignment:.topLeading)` matches the
        // CSS block-model origin — important for visual parity.
        var out = AnyView(content)

        // Min/max clamp. Skip attaching if everything is nil so we don't
        // produce `.frame()` with all-nil parameters.
        if minW != nil || maxW != nil || minH != nil || maxH != nil {
            out = AnyView(out.frame(minWidth: minW, maxWidth: maxW,
                                    minHeight: minH, maxHeight: maxH,
                                    alignment: .topLeading))
        }

        // Exact width/height. Only attach when at least one is resolved.
        if w != nil || h != nil {
            out = AnyView(out.frame(width: w, height: h, alignment: .topLeading))
        }

        // Intrinsic sizing axes (min/max/fit-content with no bound).
        // Attach `.fixedSize` on exactly the axes that want intrinsic
        // behaviour — SwiftUI's equivalent of CSS min/max-content.
        let fixH = SizeApplierResolve.wantsIntrinsic(c.width)
        let fixV = SizeApplierResolve.wantsIntrinsic(c.height)
        if fixH || fixV {
            out = AnyView(out.fixedSize(horizontal: fixH, vertical: fixV))
        }

        // AspectRatio last — aspectRatio reinterprets any remaining
        // degree of freedom in the frame. When `auto`, we skip so
        // SwiftUI uses the natural ratio.
        if let ar = c.aspectRatio, !ar.isAuto, ar.ratio > 0 {
            out = AnyView(out.aspectRatio(ar.ratio, contentMode: .fit))
        }
        return out
    }
}

// Thin View extension so StyleBuilder can chain `.engineSizing(cfg, ctx)`
// without exposing the ViewModifier type at call-sites.
extension View {
    func engineSizing(_ config: SizeConfig,
                      context: SpacingContext) -> some View {
        modifier(SizeApplier(config: config, context: context))
    }
}
