//
//  LineHeightConfig.swift
//  StyleEngine/typography/line — Phase 6.
//
//  `line-height` accepts `normal`, a unitless multiplier, a length, or a
//  percentage. The CSS parser pre-resolves numerics to `{ px: N }` where
//  possible; unitless / %-forms that depend on the rendered font size
//  arrive as raw numbers that we treat as "absolute" at extract time.
//

import CoreGraphics

/// Explicit line-box height in points. Nil → inherit. SwiftUI's
/// `.lineSpacing(...)` expects the *extra* space, so TypographyApplier
/// subtracts the font size at emit time.
struct LineHeightConfig: Equatable { var px: CGFloat? = nil }
