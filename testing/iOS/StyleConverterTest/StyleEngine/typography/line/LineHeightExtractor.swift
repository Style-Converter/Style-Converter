//
//  LineHeightExtractor.swift
//  StyleEngine/typography/line — Phase 6.
//

import Foundation

enum LineHeightProperty { static let name = "LineHeight" }

enum LineHeightExtractor {
    static func extract(from properties: [IRProperty]) -> LineHeightConfig? {
        var cfg = LineHeightConfig()
        var touched = false
        for prop in properties where prop.type == LineHeightProperty.name {
            touched = true
            // extractPx already handles {px:N}, raw numbers, and ints.
            // Unitless multipliers also arrive as raw numbers here — we
            // treat them as absolute because the parser multiplies them
            // against font-size upstream when it can resolve it.
            cfg.px = ValueExtractors.extractPx(prop.data)
        }
        return touched ? cfg : nil
    }
}
