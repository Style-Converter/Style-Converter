//
//  ClipConfig.swift
//  StyleEngine/effects/clip — Phase 8.
//
//  CSS `clip-path` plus the legacy `clip` rectangle. Both map onto
//  SwiftUI `.clipShape(_:)` at render time, so we normalise every
//  variant to a concrete `Shape` description that `ClipApplier` can
//  feed through `.clipShape`. `ClipRule` is a nonzero / evenodd
//  enum that only matters for polygon winding on iOS 16+.
//

import SwiftUI

// A single clip-path shape, canonicalised so the applier can
// build a SwiftUI Shape without re-parsing IR.
enum ClipShape: Equatable {
    // `clip-path: none` — no clipping applied.
    case none
    // `inset(top right bottom left)` with optional uniform corner radius.
    case inset(top: CGFloat, right: CGFloat, bottom: CGFloat,
               left: CGFloat, cornerRadius: CGFloat)
    // `circle(radius at cx cy)` — radius in points; center in unit-space
    // (0…1). Defaults to 50% 50% center when `at` was omitted.
    case circle(radius: CGFloat, isRadiusPercent: Bool, cx: CGFloat, cy: CGFloat)
    // `ellipse(rx ry at cx cy)` — same centre convention as circle.
    case ellipse(rx: CGFloat, ry: CGFloat, isRxPercent: Bool, isRyPercent: Bool,
                 cx: CGFloat, cy: CGFloat)
    // `polygon((x,y), (x,y), …)` — coordinates in percent units per CSS.
    case polygon(points: [CGPoint])
    // `path('M 10 10 L 20 20 Z')` — raw SVG path data.
    case path(data: String)
    // `rect(top right bottom left)` / `xywh(x y w h)` — reuse inset with
    // explicit top-left coords.
    case rect(top: CGFloat?, right: CGFloat?, bottom: CGFloat?, left: CGFloat?,
             cornerRadius: CGFloat)
    case xywh(x: CGFloat, y: CGFloat, w: CGFloat, h: CGFloat, cornerRadius: CGFloat)
    // `clip-path: url(#id)` — references an SVG clipPath. No SwiftUI
    // equivalent — we carry the id so the applier can log / skip.
    case url(id: String)
    // `clip-path: border-box` etc. — just a geometry-box keyword with no
    // shape. We treat these as identity for now (no clipping), matching
    // CSS behaviour when no shape is specified.
    case geometryBoxOnly(box: String)
}

// Winding rule for polygon clipping. SwiftUI's Path ignores this by
// default (always nonzero) so we document the limitation and only use
// evenodd for the path-shape case where we control the Path directly.
enum ClipRule: Equatable {
    case nonzero
    case evenodd
}

// Legacy CSS 2.1 `clip: rect(t, r, b, l)`. Only effective for
// `position: absolute` elements per spec — we apply universally since
// our gallery items aren't positioned.
enum LegacyClip: Equatable {
    case auto
    case rect(top: CGFloat, right: CGFloat, bottom: CGFloat, left: CGFloat)
}

struct ClipConfig: Equatable {
    // Primary clip-path value. `nil` ≡ not set; `.some(.none)` ≡
    // explicit `clip-path: none`.
    var shape: ClipShape? = nil
    // Winding rule (only consulted for .polygon/.path shapes).
    var rule: ClipRule = .nonzero
    // Legacy clip rectangle, applied via `.clipShape(Rectangle())`
    // after inset framing. Mutually exclusive with `shape` in practice.
    var legacy: LegacyClip? = nil
    // Touched flag — true the moment any extractor wrote here.
    var touched: Bool = false
}
