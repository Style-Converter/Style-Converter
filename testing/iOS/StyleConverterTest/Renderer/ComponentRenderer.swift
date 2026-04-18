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
            // Phase 7 step 4: apply per-child positioning after container
            // selection so absolute/relative offsets stack on top of the
            // fully-styled element. Identity when position/zindex unset.
            PositionApplier.apply(
                AnyView(layoutContainer(style: style).applyStyle(style)),
                aggregate: style.layout7
            )
        }
    }

    // MARK: - Container selection

    @ViewBuilder
    private func layoutContainer(style: ComponentStyle) -> some View {
        // Phase 2: resolve gap via the new GapApplier. Row gap for vertical
        // stacks, column gap for horizontal. Zero when no gap/row/col-gap
        // is set on the IR.
        let gap = GapApplier.resolve(style.spacing.gap, context: style.spacing.context)
        // Phase 7 step 4: detect absolute/fixed positioned children —
        // their parent must be a ZStack(alignment: .topLeading) so
        // offsets resolve against the upper-left corner.
        let needsZStack: Bool = {
            guard let kids = component.children else { return false }
            let aggs: [LayoutAggregate?] = kids.map { LayoutExtractor.extract(from: $0.properties) }
            return PositionApplier.needsZStackWrap(forChildren: aggs)
        }()
        // Phase 7 step 3: grid container selection. Runs ahead of the
        // flex-wrap branch so grid containers with wrap hints still
        // route to the LazyVGrid / Grid path.
        let gridKind: ContainerDecision.ContainerKind? = {
            guard let agg = style.layout7 else { return nil }
            return GridApplier.containerKind(for: agg)
        }()
        // Phase 7 step 2: route flex containers through FlexboxApplier's
        // FlowLayout when `flex-wrap: wrap|wrap-reverse` is set.
        if let kind = gridKind {
            // Grid path — LazyVGrid / LazyHGrid / iOS 16 Grid.
            gridContainer(kind: kind, style: style, gap: gap)
        } else if needsZStack {
            // Any non-grid parent with absolute/fixed children wraps in a
            // top-leading ZStack so PositionApplier's .offset calls anchor
            // to the correct corner.
            ZStack(alignment: .topLeading) {
                contentOrPlaceholder(style: style)
            }
        } else if let layoutAgg = style.layout7,
           layoutAgg.display == .flex,
           layoutAgg.flexWrap == .wrap || layoutAgg.flexWrap == .wrapReverse {
            FlowLayout(
                horizontalSpacing: gap.column,
                verticalSpacing: gap.row
            ) {
                contentOrPlaceholder(style: style)
            }
        } else {
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
    }

    // MARK: - Grid container (Phase 7 step 3)

    /// Render a grid container — routes to LazyVGrid / LazyHGrid for plain
    /// track-list grids or to iOS 16's Grid/GridRow for template-areas
    /// grids. Spanning within LazyVGrid isn't supported by SwiftUI, so
    /// template-areas components go through the Grid path.
    @ViewBuilder
    private func gridContainer(
        kind: ContainerDecision.ContainerKind,
        style: ComponentStyle,
        gap: (row: CGFloat, column: CGFloat)
    ) -> some View {
        let agg = style.layout7  // safe — gridKind only fires when non-nil
        switch kind {
        case .lazyVGrid:
            // Map GridTemplateColumns → [GridItem] via GridApplier.
            let items = GridApplier.gridItems(
                for: agg?.gridTemplateColumns,
                columnGap: gap.column
            )
            LazyVGrid(columns: items, spacing: gap.row) {
                contentOrPlaceholder(style: style)
            }
        case .lazyHGrid:
            // Column auto-flow — rows drive the LazyHGrid layout.
            let items = GridApplier.gridItems(
                for: agg?.gridTemplateRows,
                columnGap: gap.row
            )
            LazyHGrid(rows: items, spacing: gap.column) {
                contentOrPlaceholder(style: style)
            }
        case .grid:
            // iOS 16+ Grid for template-areas grids. We emit one GridRow
            // per template-areas row; each cell renders the first child
            // whose grid-area name matches the cell. Unnamed cells ("."),
            // or areas with no matching child, render as empty space.
            // TODO: this is a pragmatic mapping — it doesn't yet handle
            // spanned cells across adjacent rows (would need gridCellMerge).
            templateAreasGrid(style: style, gap: gap)
        default:
            // Fallback — vertical stack. Keeps the switch exhaustive.
            VStack(alignment: .leading, spacing: gap.row) {
                contentOrPlaceholder(style: style)
            }
        }
    }

    /// Render a template-areas grid using iOS 16 Grid / GridRow. Each
    /// IRComponent child with `grid-area: <name>` lands in every cell
    /// whose area matches that name. Without a match the cell is a
    /// transparent spacer so the track layout still resolves.
    @ViewBuilder
    private func templateAreasGrid(
        style: ComponentStyle,
        gap: (row: CGFloat, column: CGFloat)
    ) -> some View {
        let areas = style.layout7?.gridTemplateAreas ?? []
        let children = component.children ?? []
        // Precompute a (name → child) lookup so cell rendering is O(1).
        // A child without an explicit grid-area falls back to its name
        // field — matches the "children named after the area" pattern
        // in grid-template-areas.json fixtures.
        let byArea: [String: IRComponent] = Dictionary(
            uniqueKeysWithValues: children.map { c -> (String, IRComponent) in
                // Extract grid-area name from the child's IRProperty list.
                for p in c.properties where p.type == "GridArea" {
                    if let line = GridExtractor.parseGridLine(p.data),
                       let n = line.name {
                        return (n, c)
                    }
                }
                // Fall back on the component's own name — test fixtures
                // use matching names for areas + children.
                return (c.name, c)
            }
        )
        // Build the grid. `Grid` is iOS 16+ (matches deployment target).
        Grid(horizontalSpacing: gap.column, verticalSpacing: gap.row) {
            ForEach(Array(areas.enumerated()), id: \.offset) { _, row in
                GridRow {
                    ForEach(Array(row.enumerated()), id: \.offset) { _, cellName in
                        if cellName == "." {
                            // Empty cell — transparent placeholder keeps
                            // track widths consistent.
                            Color.clear
                        } else if let child = byArea[cellName] {
                            ComponentRenderer(component: child)
                        } else {
                            // Named but no matching child — still reserve
                            // the cell so the grid stays rectangular.
                            Color.clear
                        }
                    }
                }
            }
        }
    }

    // MARK: - Content

    @ViewBuilder
    private func contentOrPlaceholder(style: ComponentStyle) -> some View {
        if let rawChildren = component.children, !rawChildren.isEmpty {
            // Phase 7 step 2: sort children by CSS `order` BEFORE rendering.
            // SwiftUI has no runtime analogue, so the reordering happens
            // at build time. When no child carries Order, this is a no-op.
            let children = FlexboxApplier.sorted(rawChildren)
            // Parent aggregate for flex-child decoration. Nil fallback
            // keeps us on the legacy-layout path when Phase 7 has not
            // touched this component.
            let parentAgg = style.layout7
            ForEach(Array(children.enumerated()), id: \.offset) { _, child in
                // Build the child's aggregate once so FlexChildModifier
                // can read align-self / flex-basis / flex-grow without
                // re-parsing. This is a duplicated pass over the child's
                // property list, but it's cheap (string-compare loop).
                let childAgg: LayoutAggregate? = {
                    guard parentAgg?.display == .flex else { return nil }
                    var a = LayoutAggregate()
                    FlexboxExtractor.extract(from: child.properties, into: &a)
                    return a.touched ? a : nil
                }()
                if let ca = childAgg, let pa = parentAgg {
                    ComponentRenderer(component: child)
                        .modifier(FlexboxApplier.childModifier(for: ca, parent: pa))
                } else {
                    ComponentRenderer(component: child)
                }
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
