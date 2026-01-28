package app.logic.compose

import app.irmodels.IRDocument
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Main entry point for Compose output generation
 */
fun generateCompose(ir: IRDocument): JsonObject {
    // Build the Compose document from IR components
    val composeDocument = SimpleComposeBuilder.buildComponents(ir)

    // Serialize to JSON
    val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    val jsonString = json.encodeToString(composeDocument)
    return json.parseToJsonElement(jsonString).jsonObject
}