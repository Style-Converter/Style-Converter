//
//  BlendModeApplier.swift
//  StyleEngine/effects/blend — Phase 4.
//
//  Applies a BlendModeConfig via SwiftUI's `.blendMode(_:)`. The applier
//  prefers `mix` (per the CSS cascade intent — mix-blend-mode acts on
//  the whole element). When `mix` is nil but `background` has entries,
//  we use the first background blend mode on the whole view, since
//  SwiftUI doesn't expose per-layer blending. Documented limitation.
//

import SwiftUI

struct BlendModeApplier: ViewModifier {
    let config: BlendModeConfig?

    func body(content: Content) -> some View {
        guard let cfg = config, cfg.hasAny else { return AnyView(content) }

        // Priority: `mix` wins because it's the correct CSS semantic for
        // blending the whole element against its siblings/parents. We
        // fall back to `background[0]` only as an approximation — true
        // per-layer blending isn't exposed by SwiftUI.
        let mode = cfg.mix ?? cfg.background.first
        guard let m = mode else { return AnyView(content) }

        // `.blendMode(_:)` is exposed on all SwiftUI views since iOS 13.
        return AnyView(content.blendMode(m))
    }
}

extension View {
    // Chain helper. Use once in applyStyle.
    func engineBlendMode(_ config: BlendModeConfig?) -> some View {
        modifier(BlendModeApplier(config: config))
    }
}
