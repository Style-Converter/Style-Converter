//
//  StyleBuilder.swift
//  StyleConverterTest
//
//  Extracts SwiftUI-native values from a bag of IRProperties.
//
//  Scope = "basic rendering": layout/sizing/spacing/colors/borders/text.
//  Roughly mirrors the subset of Android's StyleApplier that's enough to
//  render most components recognizably. Unknown / unsupported properties
//  are silently ignored so decoding never fails.
//

import SwiftUI

// MARK: - Config structs

struct LayoutConfig {
    enum DisplayType { case block, inline, flexRow, flexColumn, grid, none }
    enum Justify { case flexStart, flexEnd, center, spaceBetween, spaceAround, spaceEvenly }
    enum Align   { case stretch, flexStart, flexEnd, center, baseline }
    enum Wrap    { case noWrap, wrap, wrapReverse }

    var display: DisplayType = .block
    var justify: Justify     = .flexStart
    var align: Align         = .stretch
    var wrap: Wrap           = .noWrap
    var rowGap: CGFloat      = 0
    var columnGap: CGFloat   = 0
}

// Legacy SizeConfig removed — Phase 3 migrated sizing to the engine-side
// SizeConfig under StyleEngine/sizing. The new type carries full
// LengthValues instead of pre-resolved CGFloats so percent / em / vw can
// defer resolution to the applier's GeometryReader. ComponentStyle.size
// now refers directly to the engine struct.

// Legacy spacing config retained as an empty shim so old references compile
// while the engine-based PaddingConfig/MarginConfig take over. Migrated-out
// properties (Padding*, Margin*) are handled via PaddingApplier / MarginApplier
// attached through the new `spacing: SpacingConfig` field on ComponentStyle.
struct SpacingConfig {
    // Phase 2 extractor outputs. Nil when not present in the IR.
    var padding: PaddingConfig? = nil
    var margin: MarginConfig? = nil
    var gap: GapConfig? = nil
    var marginTrim: MarginTrimConfig? = nil
    // Threaded render context. FontSize is resolved during extraction.
    var context: SpacingContext = SpacingContext()
}

struct BorderConfig {
    var topWidth: CGFloat = 0
    var rightWidth: CGFloat = 0
    var bottomWidth: CGFloat = 0
    var leftWidth: CGFloat = 0

    var topColor: Color?    = nil
    var rightColor: Color?  = nil
    var bottomColor: Color? = nil
    var leftColor: Color?   = nil

    var topLeftRadius: CGFloat     = 0
    var topRightRadius: CGFloat    = 0
    var bottomRightRadius: CGFloat = 0
    var bottomLeftRadius: CGFloat  = 0

    /// True when every side uses the same width & color — lets us use a simple stroke.
    var isUniform: Bool {
        topWidth == rightWidth && rightWidth == bottomWidth && bottomWidth == leftWidth
            && topColor == rightColor && rightColor == bottomColor && bottomColor == leftColor
    }

    var uniformWidth: CGFloat { topWidth }
    var uniformColor: Color?  { topColor }

    /// True when all four corner radii are the same.
    var hasUniformRadius: Bool {
        topLeftRadius == topRightRadius &&
        topRightRadius == bottomRightRadius &&
        bottomRightRadius == bottomLeftRadius
    }

    var uniformRadius: CGFloat { topLeftRadius }

    var hasAnyRadius: Bool {
        topLeftRadius != 0 || topRightRadius != 0 ||
        bottomRightRadius != 0 || bottomLeftRadius != 0
    }

    var hasAnyBorder: Bool {
        (topWidth != 0 && topColor != nil) ||
        (rightWidth != 0 && rightColor != nil) ||
        (bottomWidth != 0 && bottomColor != nil) ||
        (leftWidth != 0 && leftColor != nil)
    }
}

struct TextConfig {
    var color: Color?            = nil
    var fontSize: CGFloat?       = nil
    var fontWeight: Font.Weight? = nil
    var fontItalic: Bool         = false
    var letterSpacing: CGFloat?  = nil
    var lineHeight: CGFloat?     = nil
    var textAlign: TextAlignment = .leading
    var underline: Bool          = false
    var strikethrough: Bool      = false
}

struct EffectConfig {
    var opacity: CGFloat?   = nil
    var rotation: CGFloat?  = nil  // degrees
    var scale: CGFloat?     = nil
    var shadowColor: Color? = nil
    var shadowRadius: CGFloat? = nil
    var shadowX: CGFloat    = 0
    var shadowY: CGFloat    = 0
    var zIndex: Double?     = nil
}

/// Bundle of everything StyleBuilder extracts from a property list.
struct ComponentStyle {
    var layout: LayoutConfig    = LayoutConfig()
    var size: SizeConfig        = SizeConfig()
    var spacing: SpacingConfig  = SpacingConfig()
    var border: BorderConfig    = BorderConfig()
    var text: TextConfig        = TextConfig()
    var effect: EffectConfig    = EffectConfig()
    var backgroundColor: Color? = nil

    // Phase 4 — colour + background + blend + isolation family outputs.
    // All optional: nil means "no matching property in IR" so the
    // corresponding applier short-circuits to identity.
    var color: ColorConfig?                           = nil
    var opacity: OpacityConfig?                       = nil
    var accentColor: AccentColorConfig?               = nil
    var caretColor: CaretColorConfig?                 = nil
    var backgroundImage: BackgroundImageConfig?       = nil
    var backgroundSize: BackgroundSizeConfig?         = nil
    var backgroundPosition: BackgroundPositionConfig? = nil
    var backgroundRepeat: BackgroundRepeatConfig?     = nil
    var backgroundClip: BackgroundClipConfig?         = nil
    var backgroundOrigin: BackgroundOriginConfig?     = nil
    var backgroundAttachment: BackgroundAttachmentConfig? = nil
    var blend: BlendModeConfig?                       = nil
    var isolation: IsolationConfig?                   = nil
}

// MARK: - Builder

enum StyleBuilder {

    /// Consume a property list once, producing a populated ComponentStyle.
    static func build(from properties: [IRProperty]) -> ComponentStyle {
        var s = ComponentStyle()

        // Phase 2: extract FontSize first so SpacingContext has the right
        // pt value before padding/margin resolve em/rem. Default stays 16pt
        // per the CSS spec when no FontSize property is present.
        if let fs = properties.first(where: { $0.type == "FontSize" })
            .flatMap({ ValueExtractors.extractPx($0.data) }) {
            s.spacing.context.fontSizePx = Double(fs)
        }

        // Phase 2: extract each spacing family once via the new extractors.
        // Properties in `PropertyRegistry.migrated` are then skipped in the
        // legacy switch below, so there's no double-handling.
        s.spacing.padding    = PaddingExtractor.extract(from: properties)
        s.spacing.margin     = MarginExtractor.extract(from: properties)
        s.spacing.gap        = GapExtractor.extract(from: properties)
        s.spacing.marginTrim = MarginTrimExtractor.extract(from: properties)

        // Phase 3: sizing family. SizeExtractor.extract never returns nil —
        // it returns an empty config when no sizing props are present; the
        // applier short-circuits via `hasAny`. Every sizing prop name is in
        // PropertyRegistry.migrated so we don't double-dispatch below.
        s.size = SizeExtractor.extract(from: properties)

        // Phase 4: colour, background, blend, isolation. Each extractor
        // returns nil when its property wasn't in the IR, letting each
        // applier short-circuit cleanly. Every listed property type is in
        // PropertyRegistry.migrated so the legacy switch below skips them.
        s.color                = ColorExtractor.extract(from: properties)
        s.opacity              = OpacityExtractor.extract(from: properties)
        s.accentColor          = AccentColorExtractor.extract(from: properties)
        s.caretColor           = CaretColorExtractor.extract(from: properties)
        s.backgroundImage      = BackgroundImageExtractor.extract(from: properties)
        s.backgroundSize       = BackgroundSizeExtractor.extract(from: properties)
        s.backgroundPosition   = BackgroundPositionExtractor.extract(from: properties)
        s.backgroundRepeat     = BackgroundRepeatExtractor.extract(from: properties)
        s.backgroundClip       = BackgroundClipExtractor.extract(from: properties)
        s.backgroundOrigin     = BackgroundOriginExtractor.extract(from: properties)
        s.backgroundAttachment = BackgroundAttachmentExtractor.extract(from: properties)
        s.blend                = BlendModeExtractor.extract(from: properties)
        s.isolation            = IsolationExtractor.extract(from: properties)

        // Compatibility bridge — ComponentRenderer reads `text.color`
        // and `backgroundColor` directly (PlaceholderLabel uses the
        // latter to pick a contrasting text colour). Mirror the Phase 4
        // ColorConfig into these legacy fields. The legacy
        // BackgroundModifier was deleted so mirroring `backgroundColor`
        // no longer causes a double-paint.
        if let fg = s.color?.foreground?.toSwiftUIColor() {
            s.text.color = fg
        }
        if let bg = s.color?.background?.toSwiftUIColor() {
            s.backgroundColor = bg
        }
        // Phase 4: opacity is now painted by OpacityApplier. The legacy
        // EffectsModifier still reads `effect.opacity` — we deliberately
        // leave it nil so only one modifier applies (the new one). This
        // matches the "remove the legacy cases" instruction.

        for prop in properties {
            // Skip migrated properties — the spacing extractors above
            // have already consumed them. `contains` on a Set is O(1).
            if PropertyRegistry.migrated.contains(prop.type) { continue }

            switch prop.type {
            // ── Sizing ── migrated to StyleEngine/sizing (Phase 3). All
            // Width/Height/Min*/Max*/BlockSize/InlineSize/AspectRatio
            // flow through SizeExtractor above and are listed in
            // PropertyRegistry.migrated, so they never hit this switch.

            // ── Spacing ─── migrated to StyleEngine/spacing (Phase 2) ──

            // ── Colors ── migrated to StyleEngine/color (Phase 4) ───────
            // BackgroundColor + Color now flow through ColorExtractor /
            // ColorApplier. `text.color` is mirrored from ColorConfig
            // above so the text renderer keeps working. Both names are
            // listed in PropertyRegistry.migrated and never hit this
            // switch.

            // ── Borders: widths ─────────────────────────────────────────
            case "BorderTopWidth":    s.border.topWidth    = ValueExtractors.extractPx(prop.data) ?? 0
            case "BorderRightWidth":  s.border.rightWidth  = ValueExtractors.extractPx(prop.data) ?? 0
            case "BorderBottomWidth": s.border.bottomWidth = ValueExtractors.extractPx(prop.data) ?? 0
            case "BorderLeftWidth":   s.border.leftWidth   = ValueExtractors.extractPx(prop.data) ?? 0

            // ── Borders: colors ─────────────────────────────────────────
            case "BorderTopColor":    s.border.topColor    = ValueExtractors.extractColor(prop.data)
            case "BorderRightColor":  s.border.rightColor  = ValueExtractors.extractColor(prop.data)
            case "BorderBottomColor": s.border.bottomColor = ValueExtractors.extractColor(prop.data)
            case "BorderLeftColor":   s.border.leftColor   = ValueExtractors.extractColor(prop.data)

            // ── Borders: radius ─────────────────────────────────────────
            case "BorderTopLeftRadius", "BorderStartStartRadius":
                s.border.topLeftRadius     = ValueExtractors.extractPx(prop.data) ?? 0
            case "BorderTopRightRadius", "BorderStartEndRadius":
                s.border.topRightRadius    = ValueExtractors.extractPx(prop.data) ?? 0
            case "BorderBottomRightRadius", "BorderEndEndRadius":
                s.border.bottomRightRadius = ValueExtractors.extractPx(prop.data) ?? 0
            case "BorderBottomLeftRadius", "BorderEndStartRadius":
                s.border.bottomLeftRadius  = ValueExtractors.extractPx(prop.data) ?? 0

            // ── Typography ──────────────────────────────────────────────
            case "FontSize":
                s.text.fontSize = ValueExtractors.extractPx(prop.data)
            case "FontWeight":
                s.text.fontWeight = parseFontWeight(prop.data)
            case "FontStyle":
                if let kw = ValueExtractors.extractKeyword(prop.data)?.lowercased() {
                    s.text.fontItalic = (kw == "italic" || kw == "oblique")
                }
            case "LetterSpacing":
                s.text.letterSpacing = ValueExtractors.extractPx(prop.data)
            case "LineHeight":
                s.text.lineHeight = ValueExtractors.extractPx(prop.data)
            case "TextAlign":
                s.text.textAlign = parseTextAlign(prop.data)
            case "TextDecoration":
                let kw = ValueExtractors.extractKeyword(prop.data)?.lowercased() ?? ""
                if kw.contains("under") { s.text.underline = true }
                if kw.contains("line-through") || kw.contains("strike") { s.text.strikethrough = true }

            // ── Layout / display ────────────────────────────────────────
            case "Display":
                let kw = ValueExtractors.normalize(ValueExtractors.extractKeyword(prop.data))
                switch kw {
                case "FLEX", "INLINE_FLEX":           s.layout.display = .flexRow
                case "GRID":                          s.layout.display = .grid
                case "INLINE", "INLINE_BLOCK":        s.layout.display = .inline
                case "NONE":                          s.layout.display = .none
                default:                              s.layout.display = .block
                }
            case "FlexDirection":
                let kw = ValueExtractors.normalize(ValueExtractors.extractKeyword(prop.data))
                if (kw == "COLUMN" || kw == "COLUMN_REVERSE") && s.layout.display == .flexRow {
                    s.layout.display = .flexColumn
                }
            case "JustifyContent":
                s.layout.justify = parseJustify(prop.data)
            case "AlignItems":
                s.layout.align = parseAlign(prop.data)
            case "FlexWrap":
                let kw = ValueExtractors.normalize(ValueExtractors.extractKeyword(prop.data))
                s.layout.wrap = (kw == "WRAP") ? .wrap : (kw == "WRAP_REVERSE") ? .wrapReverse : .noWrap
            // Gap / RowGap / ColumnGap migrated — see GapExtractor.

            // ── Effects ─────────────────────────────────────────────────
            // Opacity migrated to StyleEngine/color (Phase 4) — see
            // OpacityApplier. Not handled here.
            case "Rotate":
                s.effect.rotation = ValueExtractors.extractDegrees(prop.data)
            case "Scale":
                s.effect.scale = ValueExtractors.extractFloat(prop.data)
            case "BoxShadow":
                applyBoxShadow(prop.data, to: &s.effect)
            case "ZIndex":
                s.effect.zIndex = ValueExtractors.extractFloat(prop.data).map(Double.init)

            default:
                break  // unsupported — silently skip
            }
        }

        return s
    }

    // MARK: - Parsing helpers

    private static func parseFontWeight(_ v: IRValue) -> Font.Weight? {
        if let n = ValueExtractors.extractInt(v) {
            switch n {
            case ..<200: return .ultraLight
            case ..<300: return .thin
            case ..<400: return .light
            case ..<500: return .regular
            case ..<600: return .medium
            case ..<700: return .semibold
            case ..<800: return .bold
            case ..<900: return .heavy
            default:     return .black
            }
        }
        switch ValueExtractors.extractKeyword(v)?.lowercased() {
        case "bold":    return .bold
        case "bolder":  return .heavy
        case "lighter": return .light
        case "normal":  return .regular
        default:        return nil
        }
    }

    private static func parseTextAlign(_ v: IRValue) -> TextAlignment {
        switch ValueExtractors.normalize(ValueExtractors.extractKeyword(v)) {
        case "CENTER":         return .center
        case "RIGHT", "END":   return .trailing
        default:               return .leading
        }
    }

    private static func parseJustify(_ v: IRValue) -> LayoutConfig.Justify {
        switch ValueExtractors.normalize(ValueExtractors.extractKeyword(v)) {
        case "CENTER":                return .center
        case "FLEX_END", "END":       return .flexEnd
        case "SPACE_BETWEEN":         return .spaceBetween
        case "SPACE_AROUND":          return .spaceAround
        case "SPACE_EVENLY":          return .spaceEvenly
        default:                      return .flexStart
        }
    }

    private static func parseAlign(_ v: IRValue) -> LayoutConfig.Align {
        switch ValueExtractors.normalize(ValueExtractors.extractKeyword(v)) {
        case "CENTER":                return .center
        case "FLEX_START", "START":   return .flexStart
        case "FLEX_END", "END":       return .flexEnd
        case "BASELINE":              return .baseline
        default:                      return .stretch
        }
    }

    /// Box-shadow IR is an array of shadow objects:
    ///   [ { "x": {"px": 5}, "y": {"px": 5}, "blur": {"px": 10},
    ///       "c": { "srgb": {...} }, "inset": false, "spread": {"px": 0} } ]
    /// We pick the first shadow and pull x/y/blur through the length extractor
    /// (values are wrapped in { "px": ... }, not bare numbers). Color key is
    /// "c" in this IR flavor; fall back to "color" if present.
    private static func applyBoxShadow(_ v: IRValue, to effect: inout EffectConfig) {
        var shadow: [String: IRValue]? = nil
        switch v {
        case .array(let arr): shadow = arr.first?.objectValue
        case .object(let o):  shadow = (o["shadows"]?.arrayValue?.first?.objectValue) ?? o
        default: break
        }
        guard let shadow = shadow else { return }

        // Inset shadows aren't supported by SwiftUI's .shadow(); skip them
        // rather than render a misleading outset.
        if shadow["inset"]?.boolValue == true { return }

        effect.shadowX = ValueExtractors.extractPx(shadow["x"] ?? shadow["offsetX"]) ?? 0
        effect.shadowY = ValueExtractors.extractPx(shadow["y"] ?? shadow["offsetY"]) ?? 0
        effect.shadowRadius = ValueExtractors.extractPx(shadow["blur"] ?? shadow["blurRadius"]) ?? 0
        effect.shadowColor = ValueExtractors.extractColor(shadow["c"] ?? shadow["color"])
            ?? Color.black.opacity(0.25)
    }
}

// MARK: - View modifier

extension View {
    /// Apply a ComponentStyle to this view. Does everything except layout-container
    /// choice (that's ComponentRenderer's job) and spacing-outside-border cases
    /// that need the caller to know container context.
    @ViewBuilder
    func applyStyle(_ style: ComponentStyle) -> some View {
        self
            // Phase 3 — sizing applied via SizeApplier. Uses the threaded
            // SpacingContext so em/rem/vw resolve against the same 390×844
            // canvas as padding/margin.
            .engineSizing(style.size, context: style.spacing.context)
            // Phase 2: padding / margin / margin-trim from StyleEngine/spacing.
            // Order matters — padding is inside the border (CSS box model),
            // margin is outside.
            .engineSpacingPadding(style.spacing.padding, context: style.spacing.context)
            // Phase 4 — painting chain. Order (from innermost outward):
            //   1. BackgroundImage: gradients sit behind solid colour so
            //      a BackgroundColor with translucency can tint them.
            //   2. BackgroundColor: solid paint, rounded-corner aware.
            //   3. BackgroundClip / Origin / Repeat / Attachment / Size:
            //      currently stubs (see per-file headers) — kept in the
            //      chain for future non-identity implementations.
            //   4. BackgroundPosition: stub today.
            .engineBackgroundImage(style.backgroundImage)
            .engineBackgroundColor(style.color, border: style.border)
            .engineBackgroundClip(style.backgroundClip)
            .engineBackgroundOrigin(style.backgroundOrigin)
            .engineBackgroundRepeat(style.backgroundRepeat)
            .engineBackgroundAttachment(style.backgroundAttachment)
            .engineBackgroundSize(style.backgroundSize)
            .engineBackgroundPosition(style.backgroundPosition)
            .modifier(BorderModifier(border: style.border))
            // Phase 4 — blend / isolation / opacity. `.blendMode`
            // applies to the whole element (including already-painted
            // backgrounds) so it must come after the paint chain.
            // `.compositingGroup` and `.opacity` follow so blending
            // composites into the isolated buffer before fading.
            .engineBlendMode(style.blend)
            .engineIsolation(style.isolation)
            .engineOpacity(style.opacity)
            // Phase 4 — accent/caret tint. Accent tints descendant
            // controls; caret is a stub. Both happily go anywhere.
            .engineAccentColor(style.accentColor)
            .engineCaretColor(style.caretColor)
            .modifier(EffectsModifier(effect: style.effect))
            .engineSpacingMargin(style.spacing.margin, context: style.spacing.context)
            .engineSpacingMarginTrim(style.spacing.marginTrim)
    }
}

// Phase 3: SizingModifier / ExactSizeModifier / AspectRatioModifier
// deleted — the engine-side SizeApplier replaces all three, and the
// wiring lives in `engineSizing(_:context:)` on View above.

// Phase 4: BackgroundModifier removed — BackgroundColor now paints via
// the new ColorApplier (StyleEngine/color/ColorApplier.swift), which
// replicates the rounded-corner-aware paint so the switch is pixel
// equivalent. The `backgroundColor: Color?` field on ComponentStyle is
// no longer wired to a modifier; it's kept as a compatibility mirror
// with .text.color for consumers that read it directly.

private struct BorderModifier: ViewModifier {
    let border: BorderConfig
    func body(content: Content) -> some View {
        if border.isUniform && border.hasAnyBorder,
           let color = border.uniformColor, border.uniformWidth > 0 {
            if border.hasAnyRadius {
                content.overlay(
                    roundedShape(border).stroke(color, lineWidth: border.uniformWidth)
                )
            } else {
                content.overlay(
                    Rectangle().stroke(color, lineWidth: border.uniformWidth)
                )
            }
        } else if border.hasAnyRadius {
            // Still apply corner radius clipping even without borders
            content.clipShape(roundedShape(border))
        } else {
            content
        }
    }
}

private struct EffectsModifier: ViewModifier {
    let effect: EffectConfig
    func body(content: Content) -> some View {
        content
            .modifier(OpacityMod(value: effect.opacity))
            .modifier(RotationMod(value: effect.rotation))
            .modifier(ScaleMod(value: effect.scale))
            .modifier(ShadowMod(effect: effect))
            .modifier(ZIndexMod(value: effect.zIndex))
    }
}

private struct OpacityMod: ViewModifier {
    let value: CGFloat?
    func body(content: Content) -> some View {
        if let v = value { content.opacity(v) } else { content }
    }
}

private struct RotationMod: ViewModifier {
    let value: CGFloat?
    func body(content: Content) -> some View {
        if let v = value { content.rotationEffect(.degrees(v)) } else { content }
    }
}

private struct ScaleMod: ViewModifier {
    let value: CGFloat?
    func body(content: Content) -> some View {
        if let v = value { content.scaleEffect(v) } else { content }
    }
}

private struct ShadowMod: ViewModifier {
    let effect: EffectConfig
    func body(content: Content) -> some View {
        if let radius = effect.shadowRadius, radius > 0 || effect.shadowColor != nil {
            content.shadow(
                color: effect.shadowColor ?? .black.opacity(0.25),
                radius: radius,
                x: effect.shadowX,
                y: effect.shadowY
            )
        } else {
            content
        }
    }
}

private struct ZIndexMod: ViewModifier {
    let value: Double?
    func body(content: Content) -> some View {
        if let z = value { content.zIndex(z) } else { content }
    }
}

// Shared shape helper — uses a single uniform radius when all four corners match,
// or the largest-matching RoundedRectangle otherwise (best-effort; mixed radii
// would need a custom Shape).
private func roundedShape(_ b: BorderConfig) -> some Shape {
    let r: CGFloat
    if b.hasUniformRadius {
        r = b.uniformRadius
    } else {
        r = max(b.topLeftRadius, b.topRightRadius, b.bottomLeftRadius, b.bottomRightRadius)
    }
    return RoundedRectangle(cornerRadius: r, style: .continuous)
}
