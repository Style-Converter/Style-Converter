//
//  LineHeightApplier.swift
//  StyleEngine/typography/line — Phase 6.
//

import Foundation

enum LineHeightApplier {
    static func contribute(_ cfg: LineHeightConfig?, into agg: inout TypographyAggregate) {
        guard let px = cfg?.px else { return }
        // Store the raw line-box height; TypographyApplier subtracts the
        // font size before calling `.lineSpacing(_:)`.
        agg.lineHeightPx = px
        agg.touched = true
    }
}
