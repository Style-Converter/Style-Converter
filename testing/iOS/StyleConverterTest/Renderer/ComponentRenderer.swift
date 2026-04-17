//
//  ComponentRenderer.swift
//  StyleConverterTest
//
//  Recursive SwiftUI renderer: IRComponent → View.
//
//  Picks the right layout container based on `Display` / `FlexDirection`, then
//  applies the rest of the style via StyleBuilder + .applyStyle(). Children
//  render recursively; leaves fall back to a placeholder label showing the
//  component's name.
//

import SwiftUI

struct ComponentRenderer: View {
    let component: IRComponent

    var body: some View {
        let style = StyleBuilder.build(from: component.properties)

        if style.layout.display == .none {
            EmptyView()
        } else {
            layoutContainer(style: style)
                .applyStyle(style)
        }
    }

    // MARK: - Container selection

    @ViewBuilder
    private func layoutContainer(style: ComponentStyle) -> some View {
        // Phase 2: resolve gap via the new GapApplier. Row gap for vertical
        // stacks, column gap for horizontal. Zero when no gap/row/col-gap
        // is set on the IR.
        let gap = GapApplier.resolve(style.spacing.gap, context: style.spacing.context)
        switch style.layout.display {
        case .flexRow:
            HStack(
                alignment: style.layout.align.verticalAlignment,
                spacing: gap.column
            ) {
                contentOrPlaceholder(style: style)
            }
        case .flexColumn:
            VStack(
                alignment: style.layout.align.horizontalAlignment,
                spacing: gap.row
            ) {
                contentOrPlaceholder(style: style)
            }
        case .grid:
            // Grid subset: render as an adaptive LazyVGrid with 2 columns for now.
            LazyVGrid(
                columns: [GridItem(.adaptive(minimum: 80), spacing: gap.column)],
                spacing: gap.row
            ) {
                contentOrPlaceholder(style: style)
            }
        case .inline:
            HStack(alignment: .firstTextBaseline, spacing: 4) {
                contentOrPlaceholder(style: style)
            }
        case .none:
            EmptyView()
        case .block:
            VStack(
                alignment: .leading,
                spacing: gap.row
            ) {
                contentOrPlaceholder(style: style)
            }
        }
    }

    // MARK: - Content

    @ViewBuilder
    private func contentOrPlaceholder(style: ComponentStyle) -> some View {
        if let children = component.children, !children.isEmpty {
            ForEach(Array(children.enumerated()), id: \.offset) { _, child in
                ComponentRenderer(component: child)
            }
        } else {
            PlaceholderLabel(
                name: component.name,
                color: style.text.color,
                textConfig: style.text,
                backgroundColor: style.backgroundColor
            )
        }
    }
}

// MARK: - Placeholder

/// Mirrors the web/Android placeholder: the component's name, centered, small,
/// with smart light/dark contrast based on background luminance.
private struct PlaceholderLabel: View {
    let name: String
    let color: Color?
    let textConfig: TextConfig
    let backgroundColor: Color?

    var body: some View {
        Text(name.replacingOccurrences(of: "_", with: " "))
            .font(font)
            .foregroundColor(resolvedColor)
            .multilineTextAlignment(textConfig.textAlign)
            .lineLimit(2)
            .padding(4)
    }

    private var font: Font {
        var f = Font.system(size: textConfig.fontSize ?? 11)
        if let w = textConfig.fontWeight { f = f.weight(w) }
        if textConfig.fontItalic { f = f.italic() }
        return f
    }

    private var resolvedColor: Color {
        if let c = color { return c }
        // Web default: inherit #eee @ 0.7 on dark bg; dark text on light bg.
        if let bg = backgroundColor,
           let components = bg.rgbComponents {
            let luminance = 0.299 * components.r + 0.587 * components.g + 0.114 * components.b
            return luminance > 0.6
                ? Color(white: 0.2).opacity(0.7)
                : Color(white: 0.93).opacity(0.7)
        }
        return Color(white: 0.93).opacity(0.7)
    }
}

// MARK: - Alignment bridges

private extension LayoutConfig.Align {
    var verticalAlignment: VerticalAlignment {
        switch self {
        case .flexStart: return .top
        case .flexEnd:   return .bottom
        case .center:    return .center
        case .baseline:  return .firstTextBaseline
        case .stretch:   return .center
        }
    }

    var horizontalAlignment: HorizontalAlignment {
        switch self {
        case .flexStart: return .leading
        case .flexEnd:   return .trailing
        case .center:    return .center
        case .baseline:  return .leading
        case .stretch:   return .leading
        }
    }
}

// MARK: - Color luminance helper

private extension Color {
    /// Reasonable sRGB component extraction via UIKit bridge. Fails for
    /// named / pattern colors but all our IR colors go through
    /// Color(.sRGB, ...) so this works for us.
    var rgbComponents: (r: Double, g: Double, b: Double, a: Double)? {
        let ui = UIColor(self)
        var r: CGFloat = 0, g: CGFloat = 0, b: CGFloat = 0, a: CGFloat = 0
        guard ui.getRed(&r, green: &g, blue: &b, alpha: &a) else { return nil }
        return (Double(r), Double(g), Double(b), Double(a))
    }
}
