package app.parsing.css.properties.longhand.primitiveParsers

import app.*

object ColorParser {

    // Hex codes (#FFF, #123456, #11223344)
    private val hexRegex = Regex("""^#([0-9a-fA-F]{3,8})$""")

    /** Directly supported CSS functions that return colors */
    private val colorFunctions = setOf(
        "rgb", "rgba", "hsl", "hsla", "hwb", "lab", "lch",
        "oklab", "oklch", "color", "color-mix", "device-cmyk",
        "linear-gradient", "radial-gradient", "conic-gradient",
        "repeating-linear-gradient", "repeating-radial-gradient", "repeating-conic-gradient"
    )

    fun parse(tokens: List<String>): IRColor? {
        // Use only the first token
        val token = tokens.firstOrNull() ?: return null
        val trimmed = token.trim()

        // 1️⃣ Hex codes
        hexRegex.matchEntire(trimmed)?.let {
            return IRColor(raw = trimmed)
        }

        // 2️⃣ Function-based colors (only our explicit list)
        val fn = FunctionParser.parse(trimmed)
        if (fn != null && colorFunctions.contains(fn.name)) {
            return IRColor(raw = trimmed, function = fn)
        }

        // 3️⃣ Named colors handled by KeywordParser externally
        return null
    }
}