package app.parsing.css.properties.parsers.primitiveParsers

import app.*

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

        // Nested functions
        parse(trimmed, insideColorMix)?.let { return IRFunctionArg(raw = trimmed, function = it) }

        // Trailing percentage (e.g., rgb(...) 25%) for color-mix
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

        // Operators
        if (trimmed in operators) return IRFunctionArg(raw = trimmed, keyword = IRKeyword(trimmed))

        // Numbers or parenthesized calc expressions
        parseCalcNumberOrExpression(trimmed)?.let { return IRFunctionArg(raw = trimmed, length = it) }

        // Primitive types
        LengthParser.parse(trimmed)?.let { return IRFunctionArg(raw = trimmed, length = it) }

        // Named colors inside color-mix become keywords
        if (insideColorMix) {
            KeywordParser.parse(trimmed)?.let { return IRFunctionArg(raw = trimmed, keyword = it) }
        }

        // Regular parsing for colors
        if (!trimmed.startsWith("inset(")) {
            ColorParser.parse(trimmed)?.let { return IRFunctionArg(raw = trimmed, color = it) }
        }

        // Keywords for other cases
        KeywordParser.parse(trimmed)?.let { return IRFunctionArg(raw = trimmed, keyword = it) }

        UrlParser.parse(trimmed)?.let { return IRFunctionArg(raw = trimmed, url = it) }

        return IRFunctionArg(raw = trimmed)
    }

    private fun parseCalcNumberOrExpression(token: String): IRLength? {
        val trimmed = token.trim()
        val numberRegex = Regex("""^[+-]?\d+(\.\d+)?(px|em|rem|%|vh|vw|vmin|vmax|ch|Q|turn)?$""")
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
        val args = mutableListOf<String>()
        val current = StringBuilder()
        val contextStack = mutableListOf<Char>()

        fun pushCurrent() {
            val s = current.toString().trim()
            if (s.isNotEmpty()) args.add(s)
            current.clear()
        }

        var i = 0
        while (i < inner.length) {
            val ch = inner[i]

            when {
                ch == '\'' && contextStack.lastOrNull() != '"' -> {
                    if (contextStack.lastOrNull() == '\'') contextStack.removeAt(contextStack.lastIndex)
                    else contextStack.add('\'')
                    current.append(ch)
                }
                ch == '"' && contextStack.lastOrNull() != '\'' -> {
                    if (contextStack.lastOrNull() == '"') contextStack.removeAt(contextStack.lastIndex)
                    else contextStack.add('"')
                    current.append(ch)
                }
                ch == '(' -> {
                    val start = i
                    var depth = 1
                    i++
                    while (i < inner.length && depth > 0) {
                        if (inner[i] == '(') depth++
                        if (inner[i] == ')') depth--
                        i++
                    }
                    current.append(inner.substring(start, i))
                    i--
                }
                ch == ',' && contextStack.isEmpty() -> pushCurrent()
                allowOperators && contextStack.isEmpty() -> {
                    var matchedOp: String? = null
                    for (op in operators.sortedByDescending { it.length }) {
                        if (inner.startsWith(op, i)) {
                            matchedOp = op
                            break
                        }
                    }
                    if (matchedOp != null) {
                        pushCurrent()
                        args.add(matchedOp)
                        i += matchedOp.length - 1
                    } else {
                        current.append(ch)
                    }
                }
                else -> current.append(ch)
            }
            i++
        }

        pushCurrent()

        val cleaned = mutableListOf<String>()
        var idx = 0
        while (idx < args.size) {
            val token = args[idx]
            if ((token == "+" || token == "-") && idx + 1 < args.size) {
                val next = args[idx + 1]
                if (next.matches(Regex("""^\d.*""")) || next.startsWith("(")) {
                    cleaned.add(token + next)
                    idx += 2
                    continue
                }
            }
            cleaned.add(token)
            idx++
        }

        return cleaned.filter { it.isNotEmpty() }
    }
}
