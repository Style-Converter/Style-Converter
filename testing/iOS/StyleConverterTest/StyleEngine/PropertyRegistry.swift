// PropertyRegistry.swift
// iOS StyleEngine — Phase 0 scaffolding.
//
// Purpose: a dispatch shell that maps `IRProperty.type` strings to dedicated
// extractor functions once they are migrated out of the monolithic
// `StyleBuilder.build(from:)` path.
//
// In Phase 0 this registry is intentionally empty: every property still flows
// through the legacy `StyleBuilder`, and `isLegacy(_:)` returns `true` for
// every input. Future phases will add entries to `migrated` and wire real
// extractors here, without forking the renderer dispatch.
//
// See `CLAUDE.md` → *Per-property contract* for the migration rules.

// Foundation gives us `Set<String>`, the only type we need at this stage.
import Foundation

/// Maps `IRProperty.type` → a typed piece of the `ComponentStyle` output.
///
/// Phase 0 scaffold: all properties still flow through the legacy
/// `StyleBuilder.build(from:)` monolith; this registry exists so future
/// phases can migrate properties one at a time without forking the
/// dispatch path.
///
/// See `CLAUDE.md` → *Per-property contract* for the migration contract.
enum PropertyRegistry {

    // MARK: - Migration ledger

    /// Property-type names that have been migrated out of `StyleBuilder`
    /// and into dedicated `{Property}Extractor.swift` files under
    /// `StyleEngine/{category}/`. Empty in Phase 0; filled by later phases.
    ///
    /// When a property is added here, the renderer will route its IR through
    /// the corresponding extractor instead of the legacy `StyleBuilder` path.
    /// Until then, `isLegacy(_:)` returns `true` for every property type.
    static let migrated: Set<String> = [
        // Phase 2 — spacing family. Padding/Margin physical+logical, Gap
        // longhands, MarginTrim. See testing/iOS/.../StyleEngine/spacing/.
        "PaddingTop", "PaddingRight", "PaddingBottom", "PaddingLeft",
        "PaddingBlockStart", "PaddingBlockEnd",
        "PaddingInlineStart", "PaddingInlineEnd",
        "MarginTop", "MarginRight", "MarginBottom", "MarginLeft",
        "MarginBlockStart", "MarginBlockEnd",
        "MarginInlineStart", "MarginInlineEnd",
        "Gap", "RowGap", "ColumnGap",
        "MarginTrim",
        // Phase 3 — sizing family. Physical + logical sizing + aspect-ratio.
        // See testing/iOS/.../StyleEngine/sizing/.
        "Width", "Height",
        "MinWidth", "MaxWidth", "MinHeight", "MaxHeight",
        "AspectRatio",
        "BlockSize", "InlineSize",
        "MinBlockSize", "MaxBlockSize",
        "MinInlineSize", "MaxInlineSize",
    ]

    // MARK: - Query helpers

    /// Returns `true` when the given IR property type is still served by
    /// the legacy `StyleBuilder`. The renderer uses this during transition
    /// to decide whether to dispatch to `StyleBuilder.build(from:)` or to a
    /// migrated extractor. In Phase 0 this is always `true`.
    ///
    /// - Parameter propertyType: The IR property `type` field (e.g. `"width"`,
    ///   `"background-color"`). Compared against `migrated` verbatim.
    /// - Returns: `true` if the property has not been migrated yet.
    static func isLegacy(_ propertyType: String) -> Bool {
        // `contains` is O(1) on `Set<String>`; invert because `migrated` is
        // the positive set.
        !migrated.contains(propertyType)
    }

    /// Count of migrated properties — exposed for the coverage report /
    /// rollout dashboard. Zero in Phase 0.
    static var migratedCount: Int {
        // Set.count is O(1).
        migrated.count
    }
}
