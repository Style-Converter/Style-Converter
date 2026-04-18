//
//  GradientApplier.swift
//  StyleEngine/background — Phase 4.
//
//  Shared helpers that turn parsed BackgroundImageLayer values into
//  SwiftUI gradient Views. Lives separately from BackgroundImageApplier
//  because the layer-stacking logic alone is close to 200 lines; this
//  file owns the "one layer → one View" conversion.
//

import SwiftUI

enum GradientApplier {

    // Render one layer to an opaque `some View`. Returns an erased view
    // so BackgroundImageApplier can iterate and stack freely.
    static func render(_ layer: BackgroundImageLayer) -> AnyView {
        switch layer {
        case .none:
            // CSS `none` layer — paint Clear so stacking order is preserved.
            return AnyView(Color.clear)
        case .url:
            // URL/data-URI rendering is a non-goal for Phase 4; paint
            // Clear so the stack index math still works. Documented
            // limitation in the Phase 4 report.
            return AnyView(Color.clear)
        case .linear(let angle, let stops):
            return AnyView(linear(angle: angle, stops: stops))
        case .radial(let shape, let stops):
            return AnyView(radial(shape: shape, stops: stops))
        case .conic(let from, let stops):
            return AnyView(conic(fromDeg: from, stops: stops))
        case .repeating(_, let angle, let stops):
            // Stub: SwiftUI has no repeating-gradient primitive. We
            // fall back to a single-pass gradient (same stops) so the
            // layer still contributes colour and the rest of the stack
            // composites cleanly. Documented limitation.
            return AnyView(linear(angle: angle, stops: stops))
        }
    }

    // ── Linear ─────────────────────────────────────────────────────────

    // Convert a CSS `background` list of stops to SwiftUI gradient stops.
    // CSS 0% lives at the start, 100% at the end; nil positions let
    // SwiftUI interpolate automatically so we just omit them.
    private static func toGradient(_ stops: [BackgroundImageStop]) -> Gradient {
        // Resolve every colour to a SwiftUI Color; fall back to clear
        // when dynamic / unknown so downstream interpolation still works.
        let resolved: [Gradient.Stop] = stops.map { s in
            let color = s.color.toSwiftUIColor() ?? .clear
            // Position nil → spread evenly: SwiftUI doesn't accept nil,
            // so we use Gradient.Stop only when we have a position, and
            // fall back to a plain `.init(colors:)` path when no stops
            // had positions. But to keep a single Gradient type, we
            // emit .init(colors:) when every position is nil.
            return Gradient.Stop(color: color, location: CGFloat(s.position ?? -1))
        }
        // If every stop had nil (we coded as -1), use colors-only init.
        if resolved.allSatisfy({ $0.location < 0 }) {
            return Gradient(colors: resolved.map { $0.color })
        }
        // Otherwise we need real 0..1 locations. For nil entries we fall
        // back to even distribution.
        let total = max(1, stops.count - 1)
        let withDefaults = stops.enumerated().map { (i, s) -> Gradient.Stop in
            let c = s.color.toSwiftUIColor() ?? .clear
            let loc = s.position ?? (Double(i) / Double(total))
            return Gradient.Stop(color: c, location: CGFloat(loc))
        }
        return Gradient(stops: withDefaults)
    }

    // CSS angle (0deg = up) → SwiftUI (startPoint, endPoint) on a unit
    // square. 0deg means gradient goes bottom→top in CSS; map to that.
    private static func endpoints(forAngleDeg angle: Double) -> (UnitPoint, UnitPoint) {
        // Normalise angle to [0, 360).
        let a = ((angle.truncatingRemainder(dividingBy: 360)) + 360)
                .truncatingRemainder(dividingBy: 360)
        // Convert CSS convention (clockwise from north) to radians used
        // by our vector math. North = -PI/2 in standard math.
        let theta = (a - 90) * .pi / 180
        // Unit-circle end point from centre (0.5, 0.5).
        let dx = cos(theta) * 0.5
        let dy = sin(theta) * 0.5
        let start = UnitPoint(x: 0.5 - dx, y: 0.5 - dy)
        let end   = UnitPoint(x: 0.5 + dx, y: 0.5 + dy)
        return (start, end)
    }

    // LinearGradient constructor. `angle` default per CSS spec = 180deg
    // (i.e. to bottom, which means top→bottom direction).
    private static func linear(angle: Double?, stops: [BackgroundImageStop]) -> some View {
        let (s, e) = endpoints(forAngleDeg: angle ?? 180)
        return LinearGradient(gradient: toGradient(stops),
                              startPoint: s, endPoint: e)
    }

    // ── Radial ─────────────────────────────────────────────────────────

    // Radial fills from the centre out. Shape hints (`circle`/`ellipse`)
    // change the aspect ratio handling; SwiftUI's RadialGradient is
    // inherently elliptical and stretches with the view, so circle shape
    // is applied at the container level (clipShape aspectRatio) when
    // needed — we accept the ellipse default here.
    private static func radial(shape _: String?, stops: [BackgroundImageStop]) -> some View {
        // Radius values are deliberately hard-coded to sensible defaults
        // (0 → diagonal). A future phase can parse CSS radial size /
        // closest-side etc. to vary these.
        RadialGradient(gradient: toGradient(stops),
                       center: .center,
                       startRadius: 0,
                       endRadius: 200)
    }

    // ── Conic ──────────────────────────────────────────────────────────

    // AngularGradient is SwiftUI's conic equivalent. CSS's `from <angle>`
    // sets the starting position; we pass it through as the `angle`
    // parameter (which controls where colour 0% lives).
    private static func conic(fromDeg: Double?, stops: [BackgroundImageStop]) -> some View {
        AngularGradient(gradient: toGradient(stops),
                        center: .center,
                        angle: .degrees(fromDeg ?? 0))
    }
}
