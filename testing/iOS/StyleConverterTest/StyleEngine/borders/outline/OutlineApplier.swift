//
//  OutlineApplier.swift
//  StyleEngine/borders/outline — Phase 5.
//
//  Paints the CSS outline OUTSIDE the element's border box using a
//  SwiftUI `.overlay` whose geometry is expanded past the element
//  bounds (negative padding on all sides). Follows the border radius
//  when one is present, otherwise draws a rectangle — matches the
//  Android applier's behaviour.
//
//  Style keywords: solid / dashed / dotted / double are rendered by a
//  Canvas stroke. `groove/ridge/inset/outset` degrade to solid (same
//  compromise as Android).
//

// SwiftUI for Canvas/Shape overlays.
import SwiftUI

struct OutlineApplier: ViewModifier {
    // Outline config. Nil → identity.
    let config: OutlineConfig?
    // Forwarded element radius so the outline follows rounded corners.
    let radius: BorderRadiusConfig?

    func body(content: Content) -> some View {
        guard let cfg = config, cfg.hasOutline else { return AnyView(content) }
        // Distance from the border box edge to the outline centreline.
        // width/2 so the stroke fills width × band, +offset for the CSS
        // `outline-offset` gap. Negative offsets pull inward — SwiftUI
        // handles that naturally via `.padding` with negative values.
        let outer = cfg.width / 2 + cfg.offset
        // Colour resolves to the environment foreground when IR said
        // `currentColor`. `.primary` matches the dark-mode default used
        // by the test harness (preferredColorScheme(.dark)).
        let colour = cfg.color ?? .primary
        // Scale up the original radius by the outer distance so the
        // outline curve stays parallel to the element's rounded corners.
        let outlineRadius = grownRadius(radius, by: outer)
        return AnyView(
            content.overlay(
                OutlineShape(radius: outlineRadius, style: cfg.style,
                             strokeWidth: cfg.width, colour: colour)
                    // Expand the overlay past the element's bounds via a
                    // negative padding — this is the SwiftUI equivalent of
                    // Android's `drawBehind { drawRect(-offset, ...) }`.
                    .padding(-outer - cfg.width / 2)
                    // Disable hit-testing so the outline never swallows
                    // taps on content behind it.
                    .allowsHitTesting(false)
            )
        )
    }

    // Grow each corner radius by `amount`. A zero base radius stays zero
    // so rectangular elements keep rectangular outlines.
    private func grownRadius(_ base: BorderRadiusConfig?,
                             by amount: CGFloat) -> BorderRadiusConfig {
        guard let b = base, b.hasAny else { return BorderRadiusConfig() }
        var r = BorderRadiusConfig()
        let g: (BorderRadiusCorner) -> BorderRadiusCorner = { c in
            BorderRadiusCorner(x: c.x > 0 ? c.x + amount : 0,
                               y: c.y > 0 ? c.y + amount : 0)
        }
        r.topLeft     = g(b.topLeft)
        r.topRight    = g(b.topRight)
        r.bottomRight = g(b.bottomRight)
        r.bottomLeft  = g(b.bottomLeft)
        return r
    }
}

// Private Shape-based drawer. Keeps the OutlineApplier body short.
private struct OutlineShape: View {
    let radius: BorderRadiusConfig
    let style: BorderStyleValue
    let strokeWidth: CGFloat
    let colour: Color

    var body: some View {
        // Every path (solid / dashed / dotted / double) uses the same
        // Shape outline so the corner radius is consistent. We vary the
        // stroke style (or double up the stroke) per CSS style keyword.
        let shape = BorderRadiusShape(radius: radius)
        switch style {
        case .dashed:
            shape.stroke(colour, style: StrokeStyle(lineWidth: strokeWidth,
                                                    dash: [strokeWidth * 3,
                                                           strokeWidth * 2]))
        case .dotted:
            shape.stroke(colour, style: StrokeStyle(lineWidth: strokeWidth,
                                                    lineCap: .round,
                                                    dash: [0.01,
                                                           strokeWidth * 2]))
        case .double where strokeWidth >= 3:
            // Two concentric outlines — outer full width at 1/3, gap, then
            // inner at 1/3. Using `.strokeBorder` on successively more
            // inset copies of the shape keeps the bands parallel.
            let band = strokeWidth / 3
            ZStack {
                shape.strokeBorder(colour, lineWidth: band)
                BorderRadiusShape(radius: radius, inset: strokeWidth * 2 / 3)
                    .strokeBorder(colour, lineWidth: band)
            }
        default:
            shape.stroke(colour, lineWidth: strokeWidth)
        }
    }
}

// View chain helper.
extension View {
    func engineOutline(_ config: OutlineConfig?,
                       radius: BorderRadiusConfig? = nil) -> some View {
        modifier(OutlineApplier(config: config, radius: radius))
    }
}
