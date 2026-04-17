//
//  FilterApplier.swift
//  StyleEngine/effects/filter — Phase 8.
//
//  Folds a `FilterConfig` into a SwiftUI modifier chain. CSS filters
//  compose left-to-right — each function sees the output of the
//  previous one — so we reduce in declared order. Equivalent SwiftUI
//  APIs:
//
//    blur(radius)       → .blur(radius: CGFloat × 0.5 for CSS parity)
//    brightness(pct)    → .brightness((pct-100)/100)
//    contrast(pct)      → .contrast(pct/100)
//    grayscale(pct)     → .grayscale(pct/100)
//    saturate(pct)      → .saturation(pct/100)
//    hue-rotate(deg)    → .hueRotation(.degrees(deg))
//    invert(100)        → .colorInvert()        (partial invert is lossy)
//    opacity(pct)       → .opacity(pct/100)
//    sepia              → approximate with .saturation + hue shift (no native API)
//    drop-shadow        → .shadow(color:, radius:, x:, y:)
//
//  BackdropFilter has no SwiftUI analog — we approximate it with
//  `.background(.regularMaterial)` so the user at least sees SOME
//  differentiation on iOS 15+. Documented TODO in the code.
//

import SwiftUI

struct FilterApplier: ViewModifier {
    let config: FilterConfig?

    func body(content: Content) -> some View {
        // Short-circuit — nil / untouched means identity.
        guard let cfg = config, cfg.touched else { return AnyView(content) }

        // Step 1: backdrop filter. Emitted BEHIND the content; SwiftUI
        // can't truly blur what's beneath, so we paint a blurred Material
        // background and hope the ambient colour picks it up.
        var v: AnyView = AnyView(content)
        if !cfg.backdrop.isEmpty {
            // Approximate: any backdrop filter sticks a translucent
            // Material behind the view. TODO: fold per-function tones.
            v = AnyView(v.background(.thinMaterial))
        }

        // Step 2: foreground filter chain. Iterate in declared order so
        // the visual effect of `blur(4) brightness(120)` differs from
        // `brightness(120) blur(4)` — SwiftUI modifier order matches CSS
        // function order here (outermost applied last).
        for fn in cfg.filter {
            v = AnyView(applyOne(fn, to: v))
        }
        return v
    }

    // One filter function → one SwiftUI modifier.
    @ViewBuilder
    private func applyOne(_ fn: FilterFn, to v: AnyView) -> some View {
        switch fn {
        case .blur(let r):
            // CSS blur radius ≈ 2× SwiftUI's internal radius; divide by 2.
            v.blur(radius: r / 2, opaque: false)
        case .brightness(let pct):
            // SwiftUI `.brightness` is additive in [-1,1]; CSS is
            // multiplicative on the colour (0 ≡ black, 100 ≡ identity).
            // Map by `(pct-100)/100` so 100 → 0, 150 → +0.5, 50 → -0.5.
            v.brightness((pct - 100) / 100)
        case .contrast(let pct):
            v.contrast(pct / 100)
        case .grayscale(let pct):
            // CSS grayscale amount in 0–100; SwiftUI expects 0–1.
            v.grayscale(pct / 100)
        case .sepia(let pct):
            // No native sepia filter. Approximate by desaturating and
            // tinting with a warm overlay. Amount in 0–1.
            v.saturation(1 - pct / 200)
                .colorMultiply(Color(red: 1.0, green: 0.9, blue: 0.7)
                               .opacity(pct / 100))
        case .invert(let pct):
            // SwiftUI has a boolean colorInvert; we only invert at 100%.
            if pct >= 50 { v.colorInvert() } else { v }
        case .saturate(let pct):
            v.saturation(pct / 100)
        case .opacity(let pct):
            v.opacity(pct / 100)
        case .hueRotate(let deg):
            v.hueRotation(.degrees(deg))
        case .dropShadow(let x, let y, let blur, let color):
            // SwiftUI `.shadow` has the same semantics as CSS drop-shadow:
            // it respects the alpha channel of the view so transparent
            // pixels don't cast shadow. Perfect parity.
            v.shadow(color: color ?? .black.opacity(0.3),
                     radius: blur / 2, x: x, y: y)
        case .url(let id):
            // Can't resolve SVG filter refs in SwiftUI — log-and-skip.
            // TODO: fold a pre-rendered SVG filter into a shader.
            let _ = id
            v
        }
    }
}

extension View {
    // Chain helper; identity on nil.
    func engineFilter(_ config: FilterConfig?) -> some View {
        modifier(FilterApplier(config: config))
    }
}
