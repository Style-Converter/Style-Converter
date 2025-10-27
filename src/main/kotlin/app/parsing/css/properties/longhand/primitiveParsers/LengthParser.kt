package app.parsing.css.properties.longhand.primitiveParsers

import app.IRLength
import app.parsing.css.properties.longhand.primitiveParsers.FunctionParser

object LengthParser {

    // Precompiled regex for number + unit
    private val numberUnitRegex = Regex(
        """^\s*(-?\d*\.?\d+)\s*(px|em|rem|vh|vw|vmin|vmax|ch|ex|fr|q|in|cm|mm|pt|pc|%|turn|deg|rad|grad)\s*$""",
        RegexOption.IGNORE_CASE
    )

    private val numberRegex = Regex("""^\s*-?\d*\.?\d+\s*$""")

    /** Directly supported CSS functions returning a length-like value */
    private val lengthFunctions = setOf(
        "calc", "clamp", "min", "max", "fit-content", "var", "env"
    )

    fun parse(tokens: List<String>): IRLength? {
        // Use only the first token
        val token = tokens.firstOrNull() ?: return null
        val t = token.trim()

        // 1️⃣ Function-based lengths (explicit list only)
        val fn = FunctionParser.parse(t)
        if (fn != null && lengthFunctions.contains(fn.name)) {
            return IRLength(function = fn)
        }

        // 2️⃣ Number + unit
        numberUnitRegex.matchEntire(t)?.let {
            return IRLength(value = it.groupValues[1].toDoubleOrNull(), unit = it.groupValues[2])
        }

        // 3️⃣ Plain number
        if (numberRegex.matches(t)) {
            return IRLength(value = t.toDoubleOrNull(), unit = null)
        }

        // 4️⃣ Zero shortcut (explicit unit=null)
        if (t == "0") {
            return IRLength(value = 0.0, unit = null)
        }

        // All keywords (thin, auto…) now go to KeywordParser
        return null
    }
}
