package app

import app.parsing.parsing
import kotlinx.serialization.encodeToString
import app.logic.logic
import kotlinx.serialization.json.*
import java.io.File

/*
Purpose: Kotlin CLI entry that mirrors the MVP pipeline described in AiInstructions.
Usage: style-converter convert --from css|compose|swiftui --to compose,swiftui -i <input> -o <outDir>
MVP: Parse input JSON → normalize to IR → emit androidStyles.json and iosStyles.json with diagnostics.
*/

/**
 * Parses command line arguments into a key-value map
 * Supports both long (--flag) and short (-f) option formats
 * Flags without values are set to "true", flags with values store the next argument
 * @param args List of command line arguments
 * @return Map of flag names to their values
 */
private fun parseArgs(args: List<String>): Map<String, String> {
    val map = mutableMapOf<String, String>()
    var i = 0
    while (i < args.size) {
        val a = args[i]
        if (a.startsWith("--")) {
            // Long form flag (--flag or --flag value)
            if (i + 1 < args.size && !args[i + 1].startsWith("-")) { 
                map[a] = args[i + 1]; i += 2 
            } else { 
                map[a] = "true"; i += 1 
            }
        } else if (a.startsWith("-")) {
            // Short form flag (-f or -f value)
            if (i + 1 < args.size && !args[i + 1].startsWith("-")) { 
                map[a] = args[i + 1]; i += 2 
            } else { 
                map[a] = "true"; i += 1 
            }
        } else { 
            // Skip positional arguments
            i += 1 
        }
    }
    return map
}

private fun printUsage() {
    println("Usage: style-converter convert --from css|compose|swiftui --to css,compose,swiftui -i <input> -o <outDir>")
}

/**
 * Main entry point for the style-converter CLI application
 * Implements the MVP pipeline: Parse input JSON → normalize to IR → emit target platform code
 * 
 * Usage: style-converter convert --from json --to compose,swiftui -i <input> -o <outDir>
 * 
 * @param rawArgs Command line arguments array
 */
fun main(rawArgs: Array<String>) {
    val args = parseArgs(rawArgs.toList())
    val cmd = rawArgs.firstOrNull()
    
    if (cmd == "convert") {
        // Extract required command line arguments
        val inputPath = args["--input"] ?: args["-i"] ?: run { printUsage(); return }
        val fromRaw = args["--from"]?.lowercase() ?: run { printUsage(); return }
        val targetsRaw = args["--to"] ?: run { printUsage(); return }
        val outDir = args["--outDir"] ?: args["-o"] ?: "out"

        // Parse target platforms (comma or space separated)
        val targets = targetsRaw.split(Regex("[\\s,]+")).map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        
        // Parse input JSON and convert to intermediate representation
        val json = Json { ignoreUnknownKeys = true }
        val root = json.parseToJsonElement(File(inputPath).readText()).jsonObject

        // Route to the correct parser based on --from
        val allowedFrom = setOf("css", "compose", "swiftui")
        if (fromRaw !in allowedFrom) { printUsage(); return }
        val ir = parsing(root, fromRaw)
        val pretty = Json { prettyPrint = true }.encodeToString(ir)
        println("[style-converter] IR as JSON:\n$pretty")

        // Ensure output directory exists
        File(outDir).mkdirs()
        
        // Generate code for each target platform via logic layer
        val outputs = logic(ir, targets.toSet())
        for ((fileName, jsonObj) in outputs) {
            File(outDir, fileName).writeText(Json { prettyPrint = true }.encodeToString(JsonObject.serializer(), jsonObj))
            println("[style-converter] Wrote ${File(outDir, fileName).path}")
        }
    } else {
        // Show usage information for invalid commands
        printUsage()
    }
}