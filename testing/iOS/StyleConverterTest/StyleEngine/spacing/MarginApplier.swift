//
//  MarginApplier.swift
//  StyleEngine/spacing — Phase 2.
//
//  ViewModifier that applies a MarginConfig. Handles:
//    1. Absolute / relative positive margins → `.padding` on the outside
//       of the element (SwiftUI has no native "margin"; outer padding
//       reproduces the CSS collapsing-free subset).
//    2. Negative margins → `.offset` with signed values.
//    3. `auto` margins → `.frame(maxWidth: .infinity, alignment: …)` to
//       push or center the element within its parent (matching CSS
//       `margin: 0 auto` block-centering).
//    4. Percent margins → GeometryReader for parent-width, same as padding.
//

// SwiftUI for the modifier surface + GeometryReader.
import SwiftUI

// Modifier attached by the StyleBuilder view extension below. Deliberately
// separate from PaddingApplier because the `auto` and negative cases need
// extra SwiftUI machinery that would bloat PaddingApplier past 200 lines.
struct MarginApplier: ViewModifier {
    let config: MarginConfig?
    let context: SpacingContext

    func body(content: Content) -> some View {
        // Short-circuit: nothing to do.
        guard let cfg = config, cfg.hasAny else { return AnyView(content) }

        // Resolve each side. `isPadding:false` preserves negatives + auto.
        let t = SpacingResolver.resolve(cfg.top, ctx: context, isPadding: false)
        let r = SpacingResolver.resolve(cfg.right, ctx: context, isPadding: false)
        let b = SpacingResolver.resolve(cfg.bottom, ctx: context, isPadding: false)
        let l = SpacingResolver.resolve(cfg.left, ctx: context, isPadding: false)

        // Does any percent side exist? Wrap in GeometryReader if so.
        let hasPercent = isPercent(t) || isPercent(r) || isPercent(b) || isPercent(l)
        let hAuto = cfg.horizontalAutoAlignment
        let vAuto = cfg.verticalAutoAlignment

        if hasPercent {
            return AnyView(
                GeometryReader { geo in
                    let pw = geo.size.width > 0 ? geo.size.width : 390
                    applyResolved(to: content, t: t, r: r, b: b, l: l,
                                  parentWidth: pw, hAuto: hAuto, vAuto: vAuto)
                }
            )
        }

        return AnyView(
            applyResolved(to: content, t: t, r: r, b: b, l: l,
                          parentWidth: CGFloat(context.viewportWidth),
                          hAuto: hAuto, vAuto: vAuto)
        )
    }

    // Compose the final view: outer padding for positive space, offset
    // for negatives, frame for auto. Intentionally in one function so we
    // control the modifier chain order precisely.
    @ViewBuilder
    private func applyResolved(to content: Content,
                               t: ResolvedLength, r: ResolvedLength,
                               b: ResolvedLength, l: ResolvedLength,
                               parentWidth: CGFloat,
                               hAuto: HorizontalAutoAlignment,
                               vAuto: VerticalAutoAlignment) -> some View {
        // Separate signed pixels for each side — non-auto, non-percent
        // sides produce a single Double. Auto sides collapse to 0 since
        // their "spacing" is expressed via the frame alignment instead.
        let tpx = signedPx(t, parentWidth: parentWidth)
        let rpx = signedPx(r, parentWidth: parentWidth)
        let bpx = signedPx(b, parentWidth: parentWidth)
        let lpx = signedPx(l, parentWidth: parentWidth)

        // Split each axis into a non-negative "outer space" (handled by
        // `.padding`) and a possibly-negative "pull" (handled by `.offset`).
        // CSS `margin: -10px` means pull the element 10px toward origin,
        // which maps exactly to SwiftUI's `.offset`.
        let outerTop    = max(0, tpx)
        let outerBottom = max(0, bpx)
        let outerLeft   = max(0, lpx)
        let outerRight  = max(0, rpx)
        let pullY = min(0, tpx) - min(0, bpx)  // Negative top pulls up;
                                               // negative bottom pulls down (subtract).
        let pullX = min(0, lpx) - min(0, rpx)  // Negative left pulls left;
                                               // negative right pulls right (subtract).

        content
            // Outer padding reproduces positive margin via outer space.
            .padding(EdgeInsets(top: outerTop, leading: outerLeft,
                                 bottom: outerBottom, trailing: outerRight))
            // Negative margin offsets.
            .offset(x: pullX, y: pullY)
            // Horizontal auto → frame-based alignment inside parent.
            .modifier(HAutoFrameModifier(mode: hAuto))
            // Vertical auto — rare, but fixtures do emit it.
            .modifier(VAutoFrameModifier(mode: vAuto))
    }

    // Scalarise a ResolvedLength (signed, auto→0, skip→0).
    private func signedPx(_ r: ResolvedLength, parentWidth: CGFloat) -> CGFloat {
        switch r {
        case .px(let n):      return n
        case .percent(let p): return p * parentWidth
        case .auto, .skip:    return 0
        }
    }

    // True iff the side is percent.
    private func isPercent(_ r: ResolvedLength) -> Bool {
        if case .percent = r { return true }
        return false
    }
}

// Horizontal-auto frame handler. Wraps the element in a full-width frame
// and aligns it according to which side carries `auto`. Matches the CSS
// `margin: 0 auto` block-centering idiom.
private struct HAutoFrameModifier: ViewModifier {
    let mode: HorizontalAutoAlignment
    func body(content: Content) -> some View {
        switch mode {
        case .none:      content
        case .center:     content.frame(maxWidth: .infinity, alignment: .center)
        case .pushLeft:   content.frame(maxWidth: .infinity, alignment: .leading)
        case .pushRight:  content.frame(maxWidth: .infinity, alignment: .trailing)
        }
    }
}

// Vertical counterpart. `.infinity` on maxHeight only makes sense inside
// a bounded parent; our phone frame is 844pt, so the ZStack parent gives
// it meaningful bounds.
private struct VAutoFrameModifier: ViewModifier {
    let mode: VerticalAutoAlignment
    func body(content: Content) -> some View {
        switch mode {
        case .none:        content
        case .center:      content.frame(maxHeight: .infinity, alignment: .center)
        case .pushTop:     content.frame(maxHeight: .infinity, alignment: .top)
        case .pushBottom:  content.frame(maxHeight: .infinity, alignment: .bottom)
        }
    }
}

// View helper — parallels engineSpacingPadding.
extension View {
    func engineSpacingMargin(_ config: MarginConfig?,
                             context: SpacingContext) -> some View {
        modifier(MarginApplier(config: config, context: context))
    }
}
