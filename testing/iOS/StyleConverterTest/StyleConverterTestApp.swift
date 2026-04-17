//
//  StyleConverterTestApp.swift
//  StyleConverterTest
//
//  App entry point. Wraps the gallery in a 390×844 phone-frame view to
//  match the web and Android testing viewports pixel-for-pixel.
//

import SwiftUI

@main
struct StyleConverterTestApp: App {
    // Phase 1 primitive extractor self-test. DEBUG-only — zero release cost.
    // See testing/iOS/StyleConverterTest/StyleEngine/core/types/CoreTypesSelfTest.swift
    init() {
        #if DEBUG
        CoreTypesSelfTest.run()
        // Phase 2 — spacing extractors + resolver sanity check.
        SpacingSelfTest.run()
        // Phase 3 — sizing extractor + applier resolver sanity check.
        SizingSelfTest.run()
        // Phase 4 — colour + background + blend + isolation extractors.
        ColorBackgroundSelfTest.run()
        #endif
    }

    var body: some Scene {
        WindowGroup {
            RootView()
                .preferredColorScheme(.dark)
        }
    }
}

/// Outer dark background + inner 390×844 frame matching
/// `testing/Android/.../MainActivity.kt` and `testing/web`.
struct RootView: View {
    var body: some View {
        ZStack {
            // Outer: matches web's body background (#111)
            Color(red: 0x11 / 255.0, green: 0x11 / 255.0, blue: 0x11 / 255.0)
                .ignoresSafeArea()

            // Inner: 390×844 phone frame matching web's #root
            ContentView()
                .frame(width: 390, height: 844)
                .background(
                    Color(red: 0x1A / 255.0, green: 0x1A / 255.0, blue: 0x2E / 255.0)
                )
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(Color.white.opacity(0.15), lineWidth: 1)
                )
        }
    }
}
