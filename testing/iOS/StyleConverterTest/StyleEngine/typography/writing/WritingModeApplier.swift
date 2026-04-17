//
//  WritingModeApplier.swift
//  StyleEngine/typography/writing — Phase 6.
//
//  Flags vertical writing in the aggregate. TypographyApplier doesn't
//  emit a rotation today (would break measurement); the flag is a TODO.
//

import Foundation

enum WritingModeApplier {
    static func contribute(_ cfg: WritingModeConfig?, into agg: inout TypographyAggregate) {
        guard let v = cfg?.isVertical, v else { return }
        agg.verticalWritingMode = true
        // Not flipping `touched` — vertical text alone shouldn't trigger
        // the full modifier chain when no other typography prop is set.
    }
}
