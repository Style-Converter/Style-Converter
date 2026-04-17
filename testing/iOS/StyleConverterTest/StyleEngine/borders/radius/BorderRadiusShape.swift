//
//  BorderRadiusShape.swift
//  StyleEngine/borders/radius — Phase 5.
//
//  Custom Shape that honours four independent elliptical corners.
//  SwiftUI's `RoundedRectangle(cornerRadius:)` is uniform-only and
//  `UnevenRoundedRectangle` (iOS 16+) supports per-corner square radii
//  but not elliptical. We draw the path by hand so CSS's "20px 10px"
//  corners render faithfully.
//
//  Reference:
//    CSS Backgrounds 3 §5.4  — border-radius path construction.
//    Apple docs — Path.addArc(tangent1End:tangent2End:radius:)
//    which approximates elliptical arcs via a scaled transform.
//

// SwiftUI for the Shape protocol.
import SwiftUI

// Trim-safe, animatable-ignoring Shape. We don't support interpolation
// across mismatched corner counts (no `animatableData`) — radius is
// mostly static in CSS.
struct BorderRadiusShape: InsettableShape {
    // Corner bundle produced by BorderRadiusExtractor.
    var radius: BorderRadiusConfig
    // Inset distance — populated by `.strokeBorder(…)` so the stroke sits
    // inside the drawn rectangle instead of straddling its edge. Defaults
    // to zero for the plain `.fill` / `.clipShape` paths.
    var inset: CGFloat = 0

    // InsettableShape: return a copy with the inset increased. The rect
    // shrinks inside `path(in:)` by `inset` on every side, and each
    // corner radius decreases by the same amount (clamped to zero) so
    // the stroked path stays parallel to the outline.
    func inset(by amount: CGFloat) -> BorderRadiusShape {
        var copy = self
        copy.inset += amount
        return copy
    }

    // Walk the four corners clockwise from the top-left. Each leg draws:
    //   1. A straight line to the next corner's tangent point.
    //   2. An elliptical arc into the corner using an axis-scaled
    //      GraphicsContext so we can reuse `addArc(center:radius:...)`.
    func path(in rect: CGRect) -> Path {
        // Apply the stroke-inset so `.strokeBorder(lineWidth: w)` draws
        // fully inside the element instead of straddling the perimeter.
        let rect = rect.insetBy(dx: inset, dy: inset)
        var p = Path()
        // Clamp each radius to half the short side so corners never
        // overlap (CSS Backgrounds 3 §5.4 normalisation step). The inset
        // also subtracts from each radius to keep the curve parallel.
        let halfW = rect.width / 2
        let halfH = rect.height / 2
        let shrink: (CGFloat) -> CGFloat = { max(0, $0 - self.inset) }
        let tl = clamp(BorderRadiusCorner(x: shrink(radius.topLeft.x),
                                          y: shrink(radius.topLeft.y)),
                       halfW: halfW, halfH: halfH)
        let tr = clamp(BorderRadiusCorner(x: shrink(radius.topRight.x),
                                          y: shrink(radius.topRight.y)),
                       halfW: halfW, halfH: halfH)
        let br = clamp(BorderRadiusCorner(x: shrink(radius.bottomRight.x),
                                          y: shrink(radius.bottomRight.y)),
                       halfW: halfW, halfH: halfH)
        let bl = clamp(BorderRadiusCorner(x: shrink(radius.bottomLeft.x),
                                          y: shrink(radius.bottomLeft.y)),
                       halfW: halfW, halfH: halfH)

        // Start at the top edge, just right of the top-left curve.
        p.move(to: CGPoint(x: rect.minX + tl.x, y: rect.minY))
        // Top edge → start of top-right curve.
        p.addLine(to: CGPoint(x: rect.maxX - tr.x, y: rect.minY))
        // Top-right elliptical quarter.
        addEllipticalCorner(&p,
                            from: CGPoint(x: rect.maxX - tr.x, y: rect.minY),
                            to:   CGPoint(x: rect.maxX,        y: rect.minY + tr.y),
                            center: CGPoint(x: rect.maxX - tr.x, y: rect.minY + tr.y),
                            radiusX: tr.x, radiusY: tr.y,
                            startAngle: .degrees(270), endAngle: .degrees(360))
        // Right edge → start of bottom-right curve.
        p.addLine(to: CGPoint(x: rect.maxX, y: rect.maxY - br.y))
        addEllipticalCorner(&p,
                            from: CGPoint(x: rect.maxX,        y: rect.maxY - br.y),
                            to:   CGPoint(x: rect.maxX - br.x, y: rect.maxY),
                            center: CGPoint(x: rect.maxX - br.x, y: rect.maxY - br.y),
                            radiusX: br.x, radiusY: br.y,
                            startAngle: .degrees(0), endAngle: .degrees(90))
        // Bottom edge → start of bottom-left curve.
        p.addLine(to: CGPoint(x: rect.minX + bl.x, y: rect.maxY))
        addEllipticalCorner(&p,
                            from: CGPoint(x: rect.minX + bl.x, y: rect.maxY),
                            to:   CGPoint(x: rect.minX,        y: rect.maxY - bl.y),
                            center: CGPoint(x: rect.minX + bl.x, y: rect.maxY - bl.y),
                            radiusX: bl.x, radiusY: bl.y,
                            startAngle: .degrees(90), endAngle: .degrees(180))
        // Left edge → back to top-left tangent.
        p.addLine(to: CGPoint(x: rect.minX, y: rect.minY + tl.y))
        addEllipticalCorner(&p,
                            from: CGPoint(x: rect.minX,        y: rect.minY + tl.y),
                            to:   CGPoint(x: rect.minX + tl.x, y: rect.minY),
                            center: CGPoint(x: rect.minX + tl.x, y: rect.minY + tl.y),
                            radiusX: tl.x, radiusY: tl.y,
                            startAngle: .degrees(180), endAngle: .degrees(270))
        p.closeSubpath()
        return p
    }

    // Clamp corner to half-side per spec so overlapping radii degrade
    // gracefully instead of producing self-intersecting paths.
    private func clamp(_ c: BorderRadiusCorner,
                       halfW: CGFloat, halfH: CGFloat) -> BorderRadiusCorner {
        BorderRadiusCorner(x: min(max(c.x, 0), halfW),
                           y: min(max(c.y, 0), halfH))
    }

    // Draw an elliptical corner by translating + scaling so the arc is
    // a plain circle, then adding a quarter-arc. Avoids wrestling with
    // `Path.addArc(tangent1End:…)` which only does circular tangent arcs.
    // When both radii are zero, drop to a line corner so the path stays
    // sharp.
    private func addEllipticalCorner(_ p: inout Path,
                                     from a: CGPoint, to b: CGPoint,
                                     center: CGPoint,
                                     radiusX: CGFloat, radiusY: CGFloat,
                                     startAngle: Angle, endAngle: Angle) {
        if radiusX <= 0 || radiusY <= 0 {
            // Sharp corner — step the path to the next tangent via a
            // straight segment through the corner. `a` and `b` collapse
            // to the same point when radii are zero (per clamp) so this
            // is effectively a no-op move.
            p.addLine(to: b)
            return
        }
        // Pre-built circular arc path, then transform. Using a scaled
        // affine gives a true elliptical arc without sampling a polyline.
        var arc = Path()
        arc.addArc(center: .zero, radius: 1,
                   startAngle: startAngle, endAngle: endAngle,
                   clockwise: false)
        let transform = CGAffineTransform(translationX: center.x, y: center.y)
            .scaledBy(x: radiusX, y: radiusY)
        p.addPath(arc, transform: transform)
    }
}
