package app.parsing.css.properties.parsers.primitiveParsers

import app.IRLength
import app.parsing.css.properties.parsers.primitiveParsers.FunctionParser

object LengthParser {

    // Precompiled regex for number + unit
    private val numberUnitRegex = Regex(
        """^\s*(-?\d*\.?\d+)\s*(px|em|rem|vh|vw|vmin|vmax|ch|ex|fr|q|in|cm|mm|pt|pc|%|turn|deg|rad|grad)\s*$""",
        RegexOption.IGNORE_CASE
    )

    private val numberRegex = Regex("""^\s*-?\d*\.?\d+\s*$""")
    private val keywordLengths = setOf("thin", "medium", "thick", "auto")

    // Functions considered valid length functions
    private val lengthFunctions = setOf("calc", "clamp", "min", "max", "fit-content", "var", "env")

    fun parse(token: String): IRLength? {
        val t = token.trim()
        val lowerT = t.lowercase()

        // 1️⃣ Function-based lengths (support any future functions dynamically)
        FunctionParser.parse(t)?.takeIf { lengthFunctions.contains(it.name.lowercase()) }?.let { fn ->
            return IRLength(function = fn)
        }

        // 2️⃣ Keyword lengths
        if (keywordLengths.contains(lowerT)) return IRLength(value = null, unit = lowerT)

        // 3️⃣ Number + unit
        numberUnitRegex.matchEntire(t)?.let {
            return IRLength(value = it.groupValues[1].toDoubleOrNull(), unit = it.groupValues[2])
        }

        // 4️⃣ Plain number
        if (numberRegex.matches(t)) return IRLength(value = t.toDoubleOrNull(), unit = null)

        // 5️⃣ Zero shortcut (explicit unit=null)
        if (t == "0") return IRLength(value = 0.0, unit = null)

        return null
    }
}
