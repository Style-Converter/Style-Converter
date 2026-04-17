//
//  ScreenshotManager.swift
//  StyleConverterTest
//
//  Saves per-component screenshots to the app's Documents directory using
//  SwiftUI's ImageRenderer (iOS 16+). The test-ios.sh script pulls them
//  out of the simulator with `xcrun simctl get_app_container`.
//
//  Mirrors testing/Android/.../screenshot/ScreenshotManager.kt.
//

import SwiftUI
import UIKit

enum ScreenshotManager {

    /// Directory: <Documents>/test_screenshots/
    static var directory: URL {
        let docs = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        let dir  = docs.appendingPathComponent("test_screenshots", isDirectory: true)
        try? FileManager.default.createDirectory(at: dir, withIntermediateDirectories: true)
        return dir
    }

    /// Reset capture directory.
    static func reset() {
        let fm = FileManager.default
        try? fm.removeItem(at: directory)
        try? fm.createDirectory(at: directory, withIntermediateDirectories: true)
    }

    /// Save a UIImage as `{index}_{componentName}.png`.
    static func save(image: UIImage, index: Int, name: String) {
        let sanitized = name
            .replacingOccurrences(of: "/", with: "_")
            .replacingOccurrences(of: " ", with: "_")
        let filename = String(format: "%03d_%@.png", index, sanitized)
        let url = directory.appendingPathComponent(filename)
        if let data = image.pngData() {
            try? data.write(to: url)
        }
    }

    /// Render a SwiftUI view to UIImage at 1x scale to match the Android
    /// emulator's 160dpi baseline (1pt == 1px). That keeps per-component
    /// captures the same pixel dimensions across platforms.
    @MainActor
    static func render<V: View>(_ view: V) -> UIImage? {
        let renderer = ImageRenderer(content: view)
        renderer.scale = 1.0
        return renderer.uiImage
    }
}
