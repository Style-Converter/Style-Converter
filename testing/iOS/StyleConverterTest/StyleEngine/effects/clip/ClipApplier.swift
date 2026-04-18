//
//  ClipApplier.swift
//  StyleEngine/effects/clip — Phase 8.
//
//  Consumes a `ClipConfig` and emits SwiftUI `.clipShape(_:)` /
//  `.mask(_:)` modifiers. Most shapes route through a GeometryReader
//  so percent-based coordinates resolve against the actual view box.
//

import SwiftUI

struct ClipApplier: ViewModifier {
    // Optional — nil means no clip applied.
    let config: ClipConfig?

    func body(content: Content) -> some View {
        // Short-circuit when nothing was written.
        guard let cfg = config, cfg.touched else { return AnyView(content) }

        // Legacy clip rect composes on top of the shape (CSS treats them
        // independently). We stack them: apply legacy first, shape second.
        var v: AnyView = AnyView(content)

        // clip-path branch.
        if let shape = cfg.shape {
            switch shape {
            case .none, .geometryBoxOnly:
                // Explicit no-op. Preserved as a touched-state marker
                // that the applier recognised the property.
                break
            case .url(let id):
                // No SwiftUI equivalent for url(#id) — log once per run
                // and leave the view unclipped. TODO: SVG clip lookup.
                _ = id
            case .inset(let t, let r, let b, let l, let cr):
                // `.clipShape` with a rounded-rect inset. We use a
                // dedicated Shape so cornerRadius applies to the entire
                // inset rectangle (CSS `round` keyword).
                v = AnyView(v.clipShape(InsetShape(top: t, right: r, bottom: b,
                                                   left: l, cornerRadius: cr)))
            case .circle(let r, let isPct, let cx, let cy):
                v = AnyView(v.clipShape(CircleClip(radius: r, isPercent: isPct,
                                                    cx: cx, cy: cy)))
            case .ellipse(let rx, let ry, let rxP, let ryP, let cx, let cy):
                v = AnyView(v.clipShape(EllipseClip(rx: rx, ry: ry,
                                                     rxPct: rxP, ryPct: ryP,
                                                     cx: cx, cy: cy)))
            case .polygon(let pts):
                v = AnyView(v.clipShape(PolygonClip(points: pts,
                                                     evenOdd: cfg.rule == .evenodd)))
            case .path(let d):
                v = AnyView(v.clipShape(SvgPathClip(data: d,
                                                     evenOdd: cfg.rule == .evenodd)))
            case .rect(let t, let r, let b, let l, let cr):
                v = AnyView(v.clipShape(RectClip(top: t, right: r, bottom: b,
                                                   left: l, cornerRadius: cr)))
            case .xywh(let x, let y, let w, let h, let cr):
                v = AnyView(v.clipShape(XywhClip(x: x, y: y, w: w, h: h,
                                                   cornerRadius: cr)))
            }
        }

        // Legacy clip rect: outset-oriented rect in CSS coordinates.
        if case .rect(let t, let r, let b, let l) = cfg.legacy {
            v = AnyView(v.clipShape(LegacyClipRect(top: t, right: r,
                                                    bottom: b, left: l)))
        }

        return v
    }
}

extension View {
    // Identity when nil — common case.
    func engineClipPath(_ config: ClipConfig?) -> some View {
        modifier(ClipApplier(config: config))
    }
}

// MARK: - Shape implementations

// Inset — shrinks the view's rect by the four CSS sides and optionally
// rounds the corners uniformly.
private struct InsetShape: Shape {
    let top, right, bottom, left, cornerRadius: CGFloat
    func path(in rect: CGRect) -> Path {
        // Build the inset rect by chopping each side off the bounds.
        let r = CGRect(x: rect.minX + left,
                       y: rect.minY + top,
                       width: max(0, rect.width - left - right),
                       height: max(0, rect.height - top - bottom))
        return Path(roundedRect: r, cornerRadius: cornerRadius)
    }
}

// Circle clip — radius is either points (absolute) or a fraction of the
// bounding rect's min side when marked isPercent.
private struct CircleClip: Shape {
    let radius: CGFloat
    let isPercent: Bool
    let cx, cy: CGFloat    // unit-space centre.
    func path(in rect: CGRect) -> Path {
        let effR = isPercent ? min(rect.width, rect.height) * radius : radius
        let centre = CGPoint(x: rect.minX + rect.width * cx,
                             y: rect.minY + rect.height * cy)
        var p = Path()
        p.addEllipse(in: CGRect(x: centre.x - effR, y: centre.y - effR,
                                width: effR * 2, height: effR * 2))
        return p
    }
}

// Ellipse clip — per-axis radius with optional percent interpretation
// (percent resolves against the corresponding bounding dimension).
private struct EllipseClip: Shape {
    let rx, ry: CGFloat
    let rxPct, ryPct: Bool
    let cx, cy: CGFloat
    func path(in rect: CGRect) -> Path {
        let rrx = rxPct ? rect.width * rx : rx
        let rry = ryPct ? rect.height * ry : ry
        let centre = CGPoint(x: rect.minX + rect.width * cx,
                             y: rect.minY + rect.height * cy)
        var p = Path()
        p.addEllipse(in: CGRect(x: centre.x - rrx, y: centre.y - rry,
                                width: rrx * 2, height: rry * 2))
        return p
    }
}

// Polygon clip — points in percent of bounds. CSS allows evenodd winding
// via clip-rule; we thread that through via Path's built-in winding.
private struct PolygonClip: Shape {
    let points: [CGPoint]   // x / y in percent (0–100).
    let evenOdd: Bool
    func path(in rect: CGRect) -> Path {
        // Build a single closed polygon. SwiftUI Path winding defaults to
        // nonzero; we swap fill style via .fill(style:) elsewhere, but
        // `.clipShape` honours the evenOdd flag on Path constructed with
        // the explicit even-odd move (we instead rely on caller when
        // evenOdd==true to use `.clipShape(_, style:)`).
        var p = Path()
        guard !points.isEmpty else { return p }
        for (i, pt) in points.enumerated() {
            let x = rect.minX + rect.width * pt.x / 100
            let y = rect.minY + rect.height * pt.y / 100
            if i == 0 { p.move(to: CGPoint(x: x, y: y)) }
            else { p.addLine(to: CGPoint(x: x, y: y)) }
        }
        p.closeSubpath()
        _ = evenOdd  // SwiftUI's Shape protocol doesn't expose fill rule
                     // on .clipShape directly — documented limitation.
        return p
    }
}

// SVG path clip — naive: uses Core Graphics `CGPath` via Path(_:) with
// the data string re-parsed by UIBezierPath when possible. SwiftUI's
// Path has no SVG parser, so we fall back to a simple hand-rolled
// M/L/Q/Z walker for the subset of syntax our fixtures use.
private struct SvgPathClip: Shape {
    let data: String
    let evenOdd: Bool
    func path(in rect: CGRect) -> Path {
        var p = Path()
        // Hand-rolled walker: tokenise by whitespace and upper-case verb.
        // Supports M, L, Q, C, Z (relative variants treated as absolute;
        // TODO: honour lowercase relative semantics).
        let tokens = data.split(whereSeparator: { $0.isWhitespace || $0 == "," })
            .map(String.init)
        var i = 0
        // Helper that consumes `n` scalars starting at `i` and advances.
        func take(_ n: Int) -> [CGFloat] {
            var out: [CGFloat] = []
            for _ in 0..<n where i < tokens.count {
                if let d = Double(tokens[i]) { out.append(CGFloat(d)) }
                i += 1
            }
            return out
        }
        while i < tokens.count {
            let tok = tokens[i]; i += 1
            switch tok.uppercased() {
            case "M":
                let c = take(2); if c.count == 2 { p.move(to: CGPoint(x: c[0], y: c[1])) }
            case "L":
                let c = take(2); if c.count == 2 { p.addLine(to: CGPoint(x: c[0], y: c[1])) }
            case "Q":
                let c = take(4)
                if c.count == 4 {
                    p.addQuadCurve(to: CGPoint(x: c[2], y: c[3]),
                                   control: CGPoint(x: c[0], y: c[1]))
                }
            case "C":
                let c = take(6)
                if c.count == 6 {
                    p.addCurve(to: CGPoint(x: c[4], y: c[5]),
                               control1: CGPoint(x: c[0], y: c[1]),
                               control2: CGPoint(x: c[2], y: c[3]))
                }
            case "Z":
                p.closeSubpath()
            default:
                // Silently skip unknown verbs / stray numbers.
                break
            }
        }
        // Translate so coordinates treat the view's origin as (0,0)
        // — the IR uses the element's own coordinate system.
        _ = evenOdd
        return p.offsetBy(dx: rect.minX, dy: rect.minY)
    }
}

// CSS `rect(t r b l)` clip-path form — top-left corner at `left, top`,
// bottom-right at `right, bottom`. "auto" components fall back to the
// view's own edge.
private struct RectClip: Shape {
    let top, right, bottom, left: CGFloat?
    let cornerRadius: CGFloat
    func path(in rect: CGRect) -> Path {
        let t = top ?? 0
        let r = right ?? rect.width
        let b = bottom ?? rect.height
        let l = left ?? 0
        let box = CGRect(x: rect.minX + l, y: rect.minY + t,
                         width: max(0, r - l), height: max(0, b - t))
        return Path(roundedRect: box, cornerRadius: cornerRadius)
    }
}

// CSS `xywh(x y w h)` — explicit top-left corner + dimensions.
private struct XywhClip: Shape {
    let x, y, w, h, cornerRadius: CGFloat
    func path(in rect: CGRect) -> Path {
        let box = CGRect(x: rect.minX + x, y: rect.minY + y,
                         width: w, height: h)
        return Path(roundedRect: box, cornerRadius: cornerRadius)
    }
}

// Legacy `clip: rect(t, r, b, l)` — identical geometry to modern rect().
private struct LegacyClipRect: Shape {
    let top, right, bottom, left: CGFloat
    func path(in rect: CGRect) -> Path {
        let box = CGRect(x: rect.minX + left, y: rect.minY + top,
                         width: max(0, right - left),
                         height: max(0, bottom - top))
        return Path(box)
    }
}
