//
//  LengthValue.swift
//  StyleConverterTest — StyleEngine/core/types
//
//  Sealed-style enum representing every CSS length shape documented by the
//  Phase 1 primitive fixtures. See:
//    examples/primitives/lengths-absolute.json      — px/pt/cm/mm/in/pc/Q
//    examples/primitives/lengths-viewport.json      — vw/vh/vmin/vmax/vi/vb + s/l/d variants
//    examples/primitives/lengths-font-relative.json — em/rem/ex/ch/cap/ic/lh/rlh
//    examples/primitives/lengths-container.json     — cqw/cqh/cqi/cqb/cqmin/cqmax
//    examples/primitives/lengths-intrinsic.json     — auto / min-content / max-content
//    examples/primitives/lengths-special.json       — percentage, fr, calc()
//
//  Intent: deterministic, enum-exhaustive Swift representation. Extractor
//  NEVER throws; returns `.unknown` on unparseable shapes.
//

import Foundation

// Every CSS length unit recognised across all primitive fixtures.
enum LengthUnit: String {
    // Absolute (lengths-absolute.json) — DP and SP are Android-only, treated as PX on iOS.
    case px, pt, cm, mm
    case inch = "in"  // `in` collides with Swift's `in` keyword — escape via raw value.
    case pc
    case q = "Q"      // Original CSS is lower-case `q`; serializer uppercases — accept both (see extractor).
    // Font-relative (lengths-font-relative.json) — cannot pre-resolve without font metrics.
    case em, rem, ex, ch, cap, ic, lh, rlh
    // Percentage (lengths-special.json) — separate IR envelope but folded here for uniformity.
    case percent
    // Classic viewport (lengths-viewport.json).
    case vw, vh, vmin, vmax, vi, vb
    // Small / Large / Dynamic viewport variants (lengths-viewport.json).
    case svw, svh, svmin, svmax, svi, svb
    case lvw, lvh, lvmin, lvmax, lvi, lvb
    case dvw, dvh, dvmin, dvmax, dvi, dvb
    // Container-query (lengths-container.json).
    case cqw, cqh, cqi, cqb, cqmin, cqmax
    // Grid fraction (lengths-special.json — inside grid-template-*).
    case fr
}

// Intrinsic sizing keywords (lengths-intrinsic.json). fit-content isn't
// consistently parsed by the converter today but we accept it defensively.
enum IntrinsicKind { case minContent, maxContent, fitContent }

// Canonical sealed-style representation. Consumers switch exhaustively.
enum LengthValue: Equatable {
    case exact(px: Double)                                 // Resolvable absolute value.
    case relative(value: Double, unit: LengthUnit, pxFallback: Double?)
    case auto                                              // `width: auto` etc.
    case intrinsic(kind: IntrinsicKind)
    case fraction(fr: Double)                              // Grid track sizing.
    case calc(expression: String)                          // Unresolved calc().
    case unknown
}

// Primary entry point. IRValue is the raw, recursive JSON blob.
// Shape dispatch is order-sensitive — more specific checks come first.
func extractLength(_ value: IRValue?) -> LengthValue {
    // Nil wire input ⇒ property was absent ⇒ unknown.
    guard let value = value else { return .unknown }

    switch value {
    // Bare string — intrinsic keywords arrive this way per fixture agent.
    case .string(let s):
        return lengthFromBareString(s)

    // Bare numeric — ambiguity: in sizing props these are pre-resolved px,
    // but in padding/margin IR the converter emits bare numbers to mean
    // percent (e.g. `padding: 10%` → `{"PaddingTop": 10.0}`). Callers that
    // know the context should prefer `extractLengthPercentDefault` below
    // when a bare number should be treated as percent. Default remains
    // `exact(px:)` for Phase 1 compatibility with sizing fixtures.
    case .double(let d): return .exact(px: d)
    case .int(let i):    return .exact(px: Double(i))

    // Object form — the overwhelmingly common case. Dispatched below.
    case .object(let o): return lengthFromObject(o)

    // Arrays / bools / null can never be a length.
    default: return .unknown
    }
}

// Normalise raw strings → intrinsic keywords (`auto`, `min-content`, ...).
private func lengthFromBareString(_ s: String) -> LengthValue {
    switch s.lowercased() {
    case "auto":        return .auto
    case "min-content": return .intrinsic(kind: .minContent)
    case "max-content": return .intrinsic(kind: .maxContent)
    case "fit-content": return .intrinsic(kind: .fitContent)
    default:            return .unknown
    }
}

// Phase 2 variant: same as `extractLength` but treats a bare numeric IRValue
// as a `%` relative value, which is how the spacing IR emits percentages
// (e.g. `padding-top: 10%` → the number `10.0` at the wire level). Every
// other shape behaves identically to `extractLength`.
func extractLengthPercentDefault(_ value: IRValue?) -> LengthValue {
    guard let value = value else { return .unknown }
    switch value {
    case .double(let d): return .relative(value: d, unit: .percent, pxFallback: nil)
    case .int(let i):    return .relative(value: Double(i), unit: .percent, pxFallback: nil)
    default:             return extractLength(value)
    }
}

// Object dispatch table — preserves documented IR quirks #1–#4.
private func lengthFromObject(_ o: [String: IRValue]) -> LengthValue {
    // Phase 2 quirk: calc expressions in spacing IR arrive as `{ "expr": "..." }`.
    // Distinct from the Phase 1 `{ "type": "calc", "expression": "..." }` form.
    if let expr = o["expr"]?.stringValue {
        return .calc(expression: expr)
    }

    // Quirk #4: grid-only `{ "fr": <n> }` shape (lengths-special.json).
    if let fr = o["fr"]?.doubleValue {
        return .fraction(fr: fr)
    }

    // Quirk #1 (two-tier wrapper): sizing props emit `{ "type":"length", "px":N, ... }`.
    // We peel the outer wrapper and recurse on the inner-flavoured subset so
    // the same logic handles both the raw `{ "px": N }` and the wrapped form.
    if let typeTag = o["type"]?.stringValue {
        switch typeTag.lowercased() {
        case "length":
            // Wrapper. Inner fields sit alongside "type". Recurse on minus-type copy.
            var inner = o
            inner.removeValue(forKey: "type")
            // Re-enter the object branch. We intentionally do NOT recurse on
            // `extractLength` at the IRValue layer because the object isn't
            // re-wrapped — we already have the dict.
            return lengthFromObject(inner)

        // Quirk #2: `{ "type":"percentage", "value": 50.0 }` on sizing props.
        case "percentage":
            if let v = o["value"]?.doubleValue {
                return .relative(value: v, unit: .percent, pxFallback: nil)
            }
            return .unknown

        // Sizing value-types sometimes arrive as `{ "type":"keyword", "value":"auto" }`.
        case "keyword":
            if let s = o["value"]?.stringValue {
                return lengthFromBareString(s)
            }
            return .unknown

        // Unresolved calc() arrives as `{ "type":"calc", "expression":"..." }` per fixture agent.
        case "calc":
            let expr = o["expression"]?.stringValue ?? ""
            return .calc(expression: expr)

        default:
            break // Fall through to legacy shapes below.
        }
    }

    // Quirk #1 (bottom half): raw shorthand / non-sizing props — `{ "px": N, "original"?: {...} }`.
    // Presence of a numeric `px` means the value is resolvable regardless of unit.
    if let px = o["px"]?.doubleValue {
        // If an `original` object exists, attach unit metadata for completeness.
        if let original = o["original"]?.objectValue,
           let unit = parseUnit(original["u"]?.stringValue),
           unit != .px {
            // Font-relative / viewport units occasionally ship with a resolved
            // `px`. Prefer the concrete pixels but keep the unit hint.
            return .relative(value: original["v"]?.doubleValue ?? px, unit: unit, pxFallback: px)
        }
        return .exact(px: px)
    }

    // Viewport / font-relative with no resolvable px — only `original` is populated.
    if let original = o["original"]?.objectValue,
       let v = original["v"]?.doubleValue,
       let unit = parseUnit(original["u"]?.stringValue) {
        // Percent can arrive via an `original` block when `type` was dropped.
        return .relative(value: v, unit: unit, pxFallback: nil)
    }

    // Legacy percent shape: `{ "percentage": 50 }` or `{ "pct": 50 }`.
    if let pct = (o["percentage"] ?? o["pct"])?.doubleValue {
        return .relative(value: pct, unit: .percent, pxFallback: nil)
    }

    // Legacy keyword shape: `{ "keyword": "auto" }`.
    if let kw = o["keyword"]?.stringValue {
        return lengthFromBareString(kw)
    }

    return .unknown
}

// Convert Kotlin-side `LengthUnit` enum name (uppercase) → Swift enum case.
// Returns nil when unrecognised — caller falls back to `.unknown`.
private func parseUnit(_ raw: String?) -> LengthUnit? {
    guard let raw = raw else { return nil }
    // Fixture agent documents unit tags as UPPERCASE. Match raw-value casing.
    switch raw.uppercased() {
    case "PX", "DP", "SP":              return .px  // DP/SP are Android-only; treat as points.
    case "PT":                          return .pt
    case "CM":                          return .cm
    case "MM":                          return .mm
    case "IN":                          return .inch
    case "PC":                          return .pc
    case "Q":                           return .q
    case "EM":                          return .em
    case "REM":                         return .rem
    case "EX":                          return .ex
    case "CH":                          return .ch
    case "CAP":                         return .cap
    case "IC":                          return .ic
    case "LH":                          return .lh
    case "RLH":                         return .rlh
    case "PERCENT", "PCT", "%":         return .percent
    case "VW":  return .vw  case "VH":  return .vh
    case "VMIN":return .vmin case "VMAX":return .vmax
    case "VI":  return .vi  case "VB":  return .vb
    case "SVW": return .svw case "SVH": return .svh
    case "SVMIN":return .svmin case "SVMAX":return .svmax
    case "SVI": return .svi case "SVB": return .svb
    case "LVW": return .lvw case "LVH": return .lvh
    case "LVMIN":return .lvmin case "LVMAX":return .lvmax
    case "LVI": return .lvi case "LVB": return .lvb
    case "DVW": return .dvw case "DVH": return .dvh
    case "DVMIN":return .dvmin case "DVMAX":return .dvmax
    case "DVI": return .dvi case "DVB": return .dvb
    case "CQW": return .cqw case "CQH": return .cqh
    case "CQI": return .cqi case "CQB": return .cqb
    case "CQMIN":return .cqmin case "CQMAX":return .cqmax
    case "FR":  return .fr
    default:    return nil
    }
}
