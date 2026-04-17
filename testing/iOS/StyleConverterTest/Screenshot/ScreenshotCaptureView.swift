//
//  ScreenshotCaptureView.swift
//  StyleConverterTest
//
//  Drives the first-launch capture phase: clears old screenshots, then walks
//  through every component in the document, renders it via ImageRenderer,
//  saves the PNG, and calls `onComplete` when done.
//
//  Mirrors Android's ScreenshotCaptureScreen.kt but without needing manual
//  permissions — iOS app sandbox writes are unconditional.
//

import SwiftUI

struct ScreenshotCaptureView: View {
    let document: IRDocument
    let onComplete: () -> Void

    @State private var captured = 0
    @State private var total = 0
    @State private var currentName = ""
    @State private var finished = false

    private var flat: [IRComponent] { flatten(document.components) }

    var body: some View {
        VStack(spacing: 16) {
            Text("Capturing component screenshots")
                .font(.system(size: 16, weight: .semibold))
                .foregroundColor(.white)

            if finished {
                VStack(spacing: 8) {
                    Text("✓ Captured \(captured) / \(total)")
                        .foregroundColor(.green)
                    Text("Pull from simulator:")
                        .font(.system(size: 12))
                        .foregroundColor(.gray)
                    Text("xcrun simctl get_app_container booted com.styleconverter.test data")
                        .font(.system(size: 11, design: .monospaced))
                        .foregroundColor(.white.opacity(0.7))
                        .multilineTextAlignment(.center)
                        .padding(8)
                        .background(Color.white.opacity(0.05))
                        .cornerRadius(4)
                    Text("test-all.sh and test-ios.sh do this automatically.")
                        .font(.system(size: 11))
                        .foregroundColor(.white.opacity(0.5))
                }
                Button("Continue to gallery", action: onComplete)
                    .buttonStyle(.borderedProminent)
            } else {
                ProgressView(value: Double(captured), total: Double(max(total, 1)))
                    .progressViewStyle(.linear)
                    .frame(maxWidth: 300)
                Text("\(captured) / \(total)")
                    .foregroundColor(.white.opacity(0.8))
                Text(currentName)
                    .font(.system(size: 12, design: .monospaced))
                    .foregroundColor(.gray)
                    .lineLimit(1)
            }
        }
        .padding(24)
        .onAppear(perform: startCapture)
    }

    private func startCapture() {
        ScreenshotManager.reset()
        total = flat.count
        captured = 0
        captureNext(index: 0)
    }

    /// Capture components one at a time via async dispatch. Each iteration
    /// yields to the main run loop so the progress UI updates.
    private func captureNext(index: Int) {
        guard index < flat.count else {
            finished = true
            // Auto-advance after a brief pause — matches Android behavior.
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
                onComplete()
            }
            return
        }

        let component = flat[index]
        currentName = component.name

        // Render the chromeless CaptureCanvas — 390 px wide, natural height,
        // solid #1A1A2E background, 16 px padding. Matches the Android and
        // web canvases exactly so captures are directly pixel-diffable.
        let canvas = CaptureCanvas(component: component)

        if let image = ScreenshotManager.render(canvas) {
            ScreenshotManager.save(image: image, index: index, name: component.name)
        }
        captured = index + 1

        DispatchQueue.main.asyncAfter(deadline: .now() + 0.02) {
            captureNext(index: index + 1)
        }
    }
}

// MARK: - Flatten

private func flatten(_ components: [IRComponent]) -> [IRComponent] {
    var out: [IRComponent] = []
    func walk(_ c: IRComponent) {
        out.append(c)
        c.children?.forEach(walk)
    }
    components.forEach(walk)
    return out
}
