//
//  ColorApplier.swift
//  StyleEngine/color — Phase 4.
//
//  ViewModifier that paints the `background-color` half of a ColorConfig.
//  The `foreground` half is consumed by the text renderer in
//  ComponentRenderer (via `ComponentStyle.text.color`) — painting it on
//  the container would tint every child view, which isn't what CSS
//  `color` means. So this applier is background-only.
//
//  Dynamic colour fallbacks: when ColorValue is `.dynamic` (e.g.
//  color-mix, light-dark) we currently fall through to `.unknown` and
//  skip the paint. A later phase can introduce an environment-resolving
//  pass that hands a resolved sRGB down via PreferenceKey. The
//  "degrade gracefully" rule says skip > crash.
//

// SwiftUI for Color + ViewModifier.
import SwiftUI

// Attach via the `.engineBackgroundColor(_:)` extension below so
// StyleBuilder.applyStyle reads as a single chained modifier per family.
struct ColorApplier: ViewModifier {
    // The config from ColorExtractor. Nil means "no color family entries"
    // — body hits the identity branch and the modifier is a zero-cost
    // wrapper.
    let config: ColorConfig?
    // Phase 4 bridge: the existing BorderConfig carries corner radii we
    // need to clip the `.background(...)` to. Optional so callers can
    // pass `nil` (tests, fixtures without borders). When absent we
    // paint a plain rectangle.
    let border: BorderConfig?

    func body(content: Content) -> some View {
        // Fast path: nothing to paint at all.
        guard let cfg = config, let bg = cfg.background else {
            return AnyView(content)
        }

        // Resolve the ColorValue through the Phase 1 bridge. `.dynamic`
        // and `.unknown` map to nil here — we leave the environment alone
        // rather than paint a wrong colour.
        guard let swiftColor = bg.toSwiftUIColor() else {
            return AnyView(content)
        }

        // Rounded-corner aware painting — mirrors the legacy
        // BackgroundModifier behaviour so the switch-over is pixel
        // equivalent for existing fixtures. When no border radius is
        // configured we use `.background(Color)` (cheapest path).
        if let b = border, b.hasAnyRadius {
            let r: CGFloat = b.hasUniformRadius
                ? b.uniformRadius
                : max(b.topLeftRadius, b.topRightRadius,
                      b.bottomLeftRadius, b.bottomRightRadius)
            return AnyView(
                content.background(
                    RoundedRectangle(cornerRadius: r, style: .continuous)
                        .fill(swiftColor)
                )
            )
        }
        // Plain rectangle path.
        return AnyView(content.background(swiftColor))
    }
}

// View extension — public call surface. `config` may be nil because
// StyleBuilder forwards ComponentStyle's optional config unconditionally
// so the render chain stays flat.
extension View {
    // Chain helper. Mirrors `.engineSpacingPadding` in the spacing module.
    // `border` carries corner radii (shared with BorderModifier) so the
    // background paints inside rounded corners exactly once.
    func engineBackgroundColor(_ config: ColorConfig?,
                               border: BorderConfig? = nil) -> some View {
        modifier(ColorApplier(config: config, border: border))
    }
}
