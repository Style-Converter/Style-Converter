package app.parsing.css.properties.longhand.primitiveParsers

import app.*
import app.parsing.css.properties.tokenizers.CommaTokenizer
import app.parsing.css.properties.tokenizers.OperatorTokenizer

object FunctionParser {
    private val functionRegex = Regex("""^([a-zA-Z\-_]+)\((.*)\)$""")
    private val operators = setOf("+", "-", "*", "/", "<=", ">=", "==", "!=")
    private val calcLikeFunctions = setOf("calc", "inset")

    fun parse(token: String, insideColorMix: Boolean = false): IRFunction? {
        val trimmed = token.trim()
        val match = functionRegex.matchEntire(trimmed) ?: return null

        val rawName = match.groupValues[1].trim()
        val name = rawName.lowercase()  // normalize function name
        val inner = match.groupValues[2].trim()

        // If this function is color-mix, mark context for nested args
        val newInsideColorMix = insideColorMix || name == "color-mix"

        val rawArgs = splitArgs(inner, allowOperators = name in calcLikeFunctions)
        val typedArgs = rawArgs.map { parseArg(it, newInsideColorMix) }
        return IRFunction(name = name, args = typedArgs)
    }

    private fun parseArg(arg: String, insideColorMix: Boolean): IRFunctionArg {
        val trimmed = arg.trim()

        // 1. Nested functions
        parse(trimmed, insideColorMix)?.let { return IRFunctionArg(raw = trimmed, function = it) }

        // 2. Function(...) 25% case
        val percentMatch = Regex("""^(.*?)([+-]?\d+(?:\.\d+)?%)$""").matchEntire(trimmed)
        if (percentMatch != null) {
            val mainPart = percentMatch.groupValues[1].trim()
            val percentPart = percentMatch.groupValues[2]
            parse(mainPart, insideColorMix)?.let { fn ->
                return IRFunctionArg(
                    raw = trimmed,
                    function = fn,
                    length = IRLength(value = percentPart.removeSuffix("%").toDouble(), unit = "%")
                )
            }
        }

        // 3. ✅ Keyword + length (e.g. "red 10px", "blue 2em", "green 50%")
        val keywordLengthRegex = Regex(
            """^([a-zA-Z][\w-]*)\s+([+-]?\d+(?:\.\d+)?(?:px|em|rem|%|vh|vw|vmin|vmax|ch|q|in|cm|mm|pt|pc|deg|rad|grad|turn)?)$""",
            RegexOption.IGNORE_CASE
        )
        val keywordLengthMatch = keywordLengthRegex.matchEntire(trimmed)
        if (keywordLengthMatch != null) {
            val keywordPart = keywordLengthMatch.groupValues[1]
            val lengthPart = keywordLengthMatch.groupValues[2]

            KeywordParser.parse(listOf(keywordPart))?.let { kw ->
                LengthParser.parse(listOf(lengthPart))?.let { len ->
                    return IRFunctionArg(
                        raw = trimmed,
                        keyword = kw,
                        length = len
                    )
                }
            }
        }

        // 4. Operators
        if (trimmed in operators) return IRFunctionArg(raw = trimmed, keyword = IRKeyword(trimmed))

        // 5. ✅ Keywords always first
        KeywordParser.parse(listOf(trimmed))?.let { return IRFunctionArg(raw = trimmed, keyword = it) }

        // 6. Numbers or parenthesized calc expressions
        parseCalcNumberOrExpression(trimmed)?.let { return IRFunctionArg(raw = trimmed, length = it) }

        // 7. Length values
        LengthParser.parse(listOf(trimmed))?.let { return IRFunctionArg(raw = trimmed, length = it) }

        // 8. Colors (rgb(), hex, etc.)
        if (!trimmed.startsWith("inset(")) {
            ColorParser.parse(listOf(trimmed))?.let { return IRFunctionArg(raw = trimmed, color = it) }
        }

        // 9. URLs
        UrlParser.parse(listOf(trimmed))?.let { return IRFunctionArg(raw = trimmed, url = it) }

        // 10. Fallback
        return IRFunctionArg(raw = trimmed)
    }

    private fun parseCalcNumberOrExpression(token: String): IRLength? {
        val trimmed = token.trim()
        val numberRegex = Regex("""^[+-]?\d+(\.\d+)?(px|em|rem|%|vh|vw|vmin|vmax|ch|q|turn|deg|rad|grad)?$""", RegexOption.IGNORE_CASE)
        if (numberRegex.matches(trimmed)) {
            val match = numberRegex.matchEntire(trimmed)!!
            val unit = match.groups[2]?.value
            val value = trimmed.replace(unit ?: "", "").toDouble()
            return IRLength(value = value, unit = unit)
        }

        if (trimmed.startsWith("(") && trimmed.endsWith(")")) {
            val inner = trimmed.substring(1, trimmed.length - 1)
            val args = splitArgs(inner, allowOperators = true).map { parseArg(it, insideColorMix = false) }
            return IRLength(function = IRFunction(name = "calc", args = args))
        }

        return null
    }

    private fun splitArgs(inner: String, allowOperators: Boolean = false): List<String> {
        // First split by top-level commas
        val byComma = CommaTokenizer.tokenize(inner)
        if (!allowOperators) return byComma

        // For calc-like functions, further split each chunk by operators, but preserve spacing
        val result = mutableListOf<String>()
        for (chunk in byComma) {
            val pieces = OperatorTokenizer.tokenize(chunk)
            // Merge unary +/- with immediate number or parenthesized expression
            var i = 0
            while (i < pieces.size) {
                val part = pieces[i]
                if ((part == "+" || part == "-") && i + 1 < pieces.size) {
                    val next = pieces[i + 1]
                    if (next.matches(Regex("""^\s*\d""")) || next.trim().startsWith("(")) {
                        result.add((part + next).trim())
                        i += 2
                        continue
                    }
                }
                result.add(part.trim())
                i++
            }
        }
        return result.filter { it.isNotEmpty() }
    }
}