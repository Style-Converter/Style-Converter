//
//  LayoutSelfTest.swift
//  StyleEngine/layout — Phase 7, step 1 (scaffold only).
//
//  Launch-time asserts that confirm the 60 layout property type names are
//  present in `PropertyRegistry.migrated`. Mirrors `TypographySelfTest`:
//  runs at app init under `#if DEBUG`, prints PASS on success, and
//  `assertionFailure`s with a full list of any missing names so the
//  developer sees the drift the instant it happens.
//
//  This test is intentionally coarse in step 1 — it checks registration
//  only, not extractor behaviour, because no extractor exists yet. Steps
//  2-5 will add per-triplet checks in the same typography-style
//  `runFlexboxChecks()` / `runGridChecks()` / ... helper shape.
//

import Foundation

enum LayoutSelfTest {

    /// Entry point called once from `StyleConverterTestApp.init()` under
    /// `#if DEBUG`. Emits exactly one `print(...)` line whether pass or fail.
    static func run() {
        // Build the expected name set from the five grouped enums. Using
        // the public grouping enums (rather than a local literal) means
        // the self-test follows future additions automatically — any name
        // added to e.g. LayoutFlexboxProperty.set is checked here too.
        let expected = LayoutProperty.set

        // Sanity check — catch accidental count drift the second it lands.
        // The task spec locks this to 60; adjust only if the task changes.
        let expectedCount = 60
        var failures: [String] = []
        if expected.count != expectedCount {
            failures.append("Layout property count drift: expected \(expectedCount), got \(expected.count)")
        }

        // Per-name registry membership check. `PropertyRegistry.migrated`
        // unions LayoutProperty.set below, so every name MUST be present.
        let missing = expected.subtracting(PropertyRegistry.migrated).sorted()
        if !missing.isEmpty {
            failures.append("Missing from PropertyRegistry.migrated: \(missing.joined(separator: ", "))")
        }

        // Report — same PASS/FAIL shape as the other SelfTest modules so
        // the launch log reads consistently.
        if failures.isEmpty {
            print("[LayoutSelfTest] PASS — layout registry scaffolded")
        } else {
            print("[LayoutSelfTest] FAIL — \(failures.count) check(s) failed:")
            failures.forEach { print("  - \($0)") }
            // `assertionFailure` trips the debugger in DEBUG builds but
            // is a no-op in release — matches the Phase 1-6 convention.
            assertionFailure("[LayoutSelfTest] registration drift")
        }
    }
}
