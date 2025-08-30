package app.parsing

import app.*

/**
 * Parses a pixel value from a string, handling both "Npx" format and plain numeric values
 * @param s Input string (e.g., "16px", "16", "  8px  ")
 * @return Parsed pixel value as Double, or null if invalid
 */
fun parsePxFromString(s: String): Double? {
    val t = s.trim()
    if (t.endsWith("px")) return t.dropLast(2).toDoubleOrNull()
    return t.toDoubleOrNull()
}

/**
 * Parses a color string into ColorRgba format, supporting hex and rgb/rgba formats
 * @param input Color string (e.g., "#ff0000", "rgb(255,0,0)", "rgba(255,0,0,0.5)")
 * @return Parsed ColorRgba object, or null if the format is invalid or input is null
 */
fun parseColor(input: String?): ColorRgba? {
    if (input == null) return null
    val s = input.trim()
    
    // Parse hex format: #ffffff
    val hex = Regex("^#([0-9a-fA-F]{6})$")
    if (hex.matches(s)) {
        val v = s.substring(1)
        val r = v.substring(0, 2).toInt(16)
        val g = v.substring(2, 4).toInt(16)
        val b = v.substring(4, 6).toInt(16)
        return ColorRgba(r, g, b, 1.0)
    }
    
    // Parse rgb/rgba format: rgb(255,0,0) or rgba(255,0,0,0.5)
    val rgb = Regex("^rgba?\\((\\d+),\\s*(\\d+),\\s*(\\d+)(?:,\\s*(\\d*\\.?\\d+))?\\)$").find(s)
    if (rgb != null) {
        val r = rgb.groupValues[1].toInt().coerceIn(0, 255)
        val g = rgb.groupValues[2].toInt().coerceIn(0, 255)
        val b = rgb.groupValues[3].toInt().coerceIn(0, 255)
        val a = rgb.groupValues.getOrNull(4)?.takeIf { it.isNotEmpty() }?.toDoubleOrNull()?.coerceIn(0.0, 1.0) ?: 1.0
        return ColorRgba(r, g, b, a)
    }
    return null
}
