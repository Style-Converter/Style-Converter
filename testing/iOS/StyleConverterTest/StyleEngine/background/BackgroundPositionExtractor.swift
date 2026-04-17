//
//  BackgroundPositionExtractor.swift
//  StyleEngine/background — Phase 4.
//
//  Reads BackgroundPositionX and BackgroundPositionY into a single
//  BackgroundPositionConfig. IR axis shapes:
//    {type:"keyword", value:"LEFT"|"RIGHT"|"TOP"|"BOTTOM"|"CENTER"}
//    {type:"length",  px: N}
//    {type:"percentage", percentage: N}     // N is 0..100
//

import Foundation

enum BackgroundPositionProperty {
    // BackgroundPosition shorthand is pre-expanded upstream. We only
    // consume the two longhands here — listing them lets the registry
    // mark both as migrated.
    static let names: [String] = [
        "BackgroundPosition",
        "BackgroundPositionX",
        "BackgroundPositionY",
    ]
}

enum BackgroundPositionExtractor {

    // Extract both axes in one pass. Nil return when neither is present.
    static func extract(from properties: [IRProperty]) -> BackgroundPositionConfig? {
        var cfg = BackgroundPositionConfig()
        var touched = false
        for prop in properties {
            switch prop.type {
            case "BackgroundPositionX":
                cfg.x = parseAxis(prop.data); touched = true
            case "BackgroundPositionY":
                cfg.y = parseAxis(prop.data); touched = true
            case "BackgroundPosition":
                // Extremely rare: un-expanded shorthand. IR shape mirrors
                // position-x — treat as X-only and ignore if unparseable.
                if let parsed = parseAxis(prop.data) {
                    cfg.x = parsed; touched = true
                }
            default:
                break
            }
        }
        return touched ? cfg : nil
    }

    // Parse a single axis value. Unknown shapes return nil so we don't
    // misrender a "fake" position.
    private static func parseAxis(_ v: IRValue) -> BackgroundAxisPosition? {
        guard case .object(let o) = v else { return nil }
        let type = o["type"]?.stringValue?.lowercased()
        switch type {
        case "keyword":
            // Normalise to upper-case for switch friendliness at apply time.
            let raw = o["value"]?.stringValue ?? ""
            return .keyword(raw.uppercased())
        case "length":
            guard let px = o["px"]?.doubleValue else { return nil }
            return .px(px)
        case "percentage":
            guard let pct = o["percentage"]?.doubleValue else { return nil }
            return .percent(pct)
        default:
            return nil
        }
    }
}
