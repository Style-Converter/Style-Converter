package app.logic

import app.*
import app.logic.compose.generateCompose
import app.logic.swiftUI.generateSwiftUI
import app.logic.css.generateCss
import kotlinx.serialization.json.JsonObject

/**
 * Orchestrates final transformations from IR to platform-specific outputs.
 * This is the entry-point logic transformation: IR → outputs.
 */

fun logic(ir: DocumentIR, targets: Set<String>): Map<String, JsonObject> {
    val outputs = mutableMapOf<String, JsonObject>()
    if ("compose" in targets) {
        outputs["androidStyles.json"] = generateCompose(ir)
    }
    if ("swiftui" in targets) {
        outputs["iosStyles.json"] = generateSwiftUI(ir)
    }
    if ("css" in targets) {
        outputs["cssStyles.json"] = generateCss(ir)
    }
    return outputs
}