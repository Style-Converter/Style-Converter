//
//  ClipExtractor.swift
//  StyleEngine/effects/clip — Phase 8.
//
//  Parses the effects clip family into a single ClipConfig. Owned
//  properties: ClipPath, ClipRule, Clip (legacy), ClipPathGeometryBox
//  (geometry-box rides inside ClipPath today so this name is reserved
//  for future standalone use). See ClipPathPropertyParser.kt for the
//  full value flavour list.
//

import Foundation

enum ClipProperty {
    // Centralises owned property names so PropertyRegistry + self-test
    // can union from one spot.
    static let set: Set<String> = [
        "ClipPath", "ClipRule", "Clip", "ClipPathGeometryBox",
    ]
}

enum ClipExtractor {

    // Top-level — returns nil when no owned property was present.
    static func extract(from properties: [IRProperty]) -> ClipConfig? {
        var cfg = ClipConfig()
        for p in properties {
            switch p.type {
            case "ClipPath":             applyClipPath(p.data, into: &cfg)
            case "ClipRule":             applyRule(p.data, into: &cfg)
            case "Clip":                 applyLegacy(p.data, into: &cfg)
            case "ClipPathGeometryBox":  applyGeometryBox(p.data, into: &cfg)
            default: break
            }
        }
        return cfg.touched ? cfg : nil
    }

    // MARK: - ClipPath

    // IR shapes documented in examples/properties/effects/clip-path-*.
    private static func applyClipPath(_ data: IRValue, into cfg: inout ClipConfig) {
        // Bare string shapes: "none" or "#svgId".
        if let s = data.stringValue {
            if s == "none" { cfg.shape = .none; cfg.touched = true; return }
            if s.hasPrefix("#") {
                cfg.shape = .url(id: String(s.dropFirst()))
                cfg.touched = true
                return
            }
            return
        }
        guard case .object(let o) = data else { return }

        // Geometry-box-only (or box + shape combo).
        if let box = o["geometry-box"]?.stringValue {
            // When a shape is also present, parse it; the box tells us
            // which reference rectangle the shape's units are in — we
            // don't currently honour that and always use the view's own
            // frame (documented TODO).
            if let shape = o["shape"], case .object(let inner) = shape {
                cfg.shape = parseShape(inner)
            } else {
                cfg.shape = .geometryBoxOnly(box: box)
            }
            cfg.touched = true
            return
        }

        // Bare-shape forms.
        cfg.shape = parseShape(o)
        cfg.touched = true
    }

    // Dispatcher for `{ "type": "inset|circle|ellipse|polygon|path|rect|xywh", ... }`.
    private static func parseShape(_ o: [String: IRValue]) -> ClipShape? {
        guard let t = o["type"]?.stringValue else { return nil }
        switch t {
        case "inset":
            // Four side lengths + optional "round" corner radius.
            return .inset(top:    CGFloat(lenPx(o["t"]) ?? 0),
                          right:  CGFloat(lenPx(o["r"]) ?? 0),
                          bottom: CGFloat(lenPx(o["b"]) ?? 0),
                          left:   CGFloat(lenPx(o["l"]) ?? 0),
                          cornerRadius: CGFloat(lenPx(o["round"]) ?? 0))
        case "circle":
            // Two shapes: short form `{ "type":"circle","px": N }` and
            // verbose `{ "type":"circle","r": {...}, "pos":{ x, y } }`.
            if let px = o["px"]?.doubleValue {
                return .circle(radius: CGFloat(px), isRadiusPercent: false,
                               cx: 0.5, cy: 0.5)
            }
            let rBlob = o["r"]
            let (r, isPct) = extractRadius(rBlob)
            let (cx, cy) = extractPosition(o["pos"])
            return .circle(radius: CGFloat(r), isRadiusPercent: isPct,
                           cx: cx, cy: cy)
        case "ellipse":
            let (rx, rxPct) = extractRadius(o["rx"])
            let (ry, ryPct) = extractRadius(o["ry"])
            let (cx, cy) = extractPosition(o["pos"])
            return .ellipse(rx: CGFloat(rx), ry: CGFloat(ry),
                            isRxPercent: rxPct, isRyPercent: ryPct,
                            cx: cx, cy: cy)
        case "polygon":
            // Points array of `{ "x": N, "y": N }` in PERCENT units.
            guard let pts = o["points"]?.arrayValue else { return nil }
            let points = pts.compactMap { pv -> CGPoint? in
                guard case .object(let p) = pv,
                      let x = p["x"]?.doubleValue,
                      let y = p["y"]?.doubleValue else { return nil }
                return CGPoint(x: x, y: y)
            }
            return .polygon(points: points)
        case "path":
            return .path(data: o["d"]?.stringValue ?? "")
        case "rect":
            // top/right/bottom/left — "auto" allowed, else a length.
            return .rect(top:    autoOrPx(o["t"]),
                         right:  autoOrPx(o["r"]),
                         bottom: autoOrPx(o["b"]),
                         left:   autoOrPx(o["l"]),
                         cornerRadius: CGFloat(lenPx(o["round"]) ?? 0))
        case "xywh":
            return .xywh(x: CGFloat(lenPx(o["x"]) ?? 0),
                         y: CGFloat(lenPx(o["y"]) ?? 0),
                         w: CGFloat(lenPx(o["w"]) ?? 0),
                         h: CGFloat(lenPx(o["h"]) ?? 0),
                         cornerRadius: CGFloat(lenPx(o["round"]) ?? 0))
        default:
            return nil
        }
    }

    // Radius extraction — either `{ "px": N }` (absolute) or `{ "original": { "v": N, "u":"PERCENT"}}` (percent).
    private static func extractRadius(_ v: IRValue?) -> (Double, Bool) {
        guard let v = v, case .object(let o) = v else { return (0, false) }
        if let px = o["px"]?.doubleValue { return (px, false) }
        if let orig = o["original"]?.objectValue,
           let vv = orig["v"]?.doubleValue,
           orig["u"]?.stringValue == "PERCENT" { return (vv / 100.0, true) }
        return (0, false)
    }

    // Position extraction for circle/ellipse `at cx cy`. Returns unit-space.
    private static func extractPosition(_ v: IRValue?) -> (CGFloat, CGFloat) {
        guard let v = v, case .object(let o) = v else { return (0.5, 0.5) }
        // Each axis: length → nil fallback to 0.5 (no view metrics here),
        // or percent → fraction. We store length as a raw px-to-fraction
        // ratio by assuming 100px≈100% which is wrong; documented TODO.
        let x = axisFraction(o["x"]) ?? 0.5
        let y = axisFraction(o["y"]) ?? 0.5
        return (CGFloat(x), CGFloat(y))
    }

    private static func axisFraction(_ v: IRValue?) -> Double? {
        guard let v = v, case .object(let o) = v else { return nil }
        if let orig = o["original"]?.objectValue,
           let vv = orig["v"]?.doubleValue,
           orig["u"]?.stringValue == "PERCENT" { return vv / 100.0 }
        // Best-effort: a plain px turns into nil here → caller uses 0.5.
        return nil
    }

    // Rect allows literal "auto" in any component → nil; else a length.
    private static func autoOrPx(_ v: IRValue?) -> CGFloat? {
        if v?.stringValue == "auto" { return nil }
        return lenPx(v).map { CGFloat($0) }
    }

    // Small shared helper for `{ "px": N }` + nested wrapper shape.
    private static func lenPx(_ v: IRValue?) -> Double? {
        guard let v = v else { return nil }
        if case .object(let o) = v {
            if let d = o["px"]?.doubleValue { return d }
        }
        return v.doubleValue
    }

    // MARK: - ClipRule

    private static func applyRule(_ data: IRValue, into cfg: inout ClipConfig) {
        guard let s = data.stringValue else { return }
        // Qualify explicitly — `ClipRule.none` doesn't exist but a future
        // enum could add it. Explicit name prevents the Optional `.none`
        // pitfall from creeping in if the enum grows.
        switch s {
        case "NONZERO": cfg.rule = ClipRule.nonzero
        case "EVENODD": cfg.rule = ClipRule.evenodd
        default: return
        }
        cfg.touched = true
    }

    // MARK: - Legacy `clip`

    private static func applyLegacy(_ data: IRValue, into cfg: inout ClipConfig) {
        guard case .object(let o) = data else { return }
        switch o["type"]?.stringValue {
        case "auto":
            cfg.legacy = .auto
            cfg.touched = true
        case "rect":
            cfg.legacy = .rect(
                top:    CGFloat(lenPx(o["top"])    ?? 0),
                right:  CGFloat(lenPx(o["right"])  ?? 0),
                bottom: CGFloat(lenPx(o["bottom"]) ?? 0),
                left:   CGFloat(lenPx(o["left"])   ?? 0))
            cfg.touched = true
        default: break
        }
    }

    // MARK: - ClipPathGeometryBox (reserved; parser folds this into ClipPath today)

    private static func applyGeometryBox(_ data: IRValue, into cfg: inout ClipConfig) {
        // Only reached if the IR emits a standalone property — currently
        // the parser folds geometry-box into ClipPath. We touch the
        // config so the self-test can observe presence.
        if let s = data.stringValue {
            cfg.shape = .geometryBoxOnly(box: s.lowercased())
            cfg.touched = true
        }
    }
}
