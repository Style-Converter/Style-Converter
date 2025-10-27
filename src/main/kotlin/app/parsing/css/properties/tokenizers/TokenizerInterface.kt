package app.parsing.css.properties.tokenizers

/**
 * Tokenizes CSS property values while respecting context (parentheses, brackets, quotes).
 */
interface Tokenizer {
    fun tokenize(input: String): List<String>
}

/**
 * Unified delimiter-based tokenizer that handles all common split cases.
 * Eliminates code duplication across WhitespaceTokenizer, CommaTokenizer, etc.
 */
class DelimiterTokenizer(
    private val shouldSplit: (Char) -> Boolean,
    private val respectParens: Boolean = true,
    private val respectBrackets: Boolean = false
) : Tokenizer {
    
    override fun tokenize(input: String): List<String> {
        val tokens = mutableListOf<String>()
        val current = StringBuilder()
        var parenDepth = 0
        var bracketDepth = 0
        var quoteChar: Char? = null

        fun push() {
            val trimmed = current.toString().trim()
            if (trimmed.isNotEmpty()) {
                tokens.add(trimmed)
            }
            current.setLength(0)
        }

        for (c in input) {
            when {
                // Quote handling
                (c == '"' || c == '\'') -> {
                    if (quoteChar == null) quoteChar = c
                    else if (quoteChar == c) quoteChar = null
                    current.append(c)
                }
                
                quoteChar != null -> current.append(c)
                
                // Track nesting
                c == '(' -> { if (respectParens) parenDepth++; current.append(c) }
                c == ')' -> { if (respectParens) parenDepth--; current.append(c) }
                c == '[' -> { if (respectBrackets) bracketDepth++; current.append(c) }
                c == ']' -> { if (respectBrackets) bracketDepth--; current.append(c) }
                
                // Split on delimiter when not nested
                shouldSplit(c) && parenDepth == 0 && bracketDepth == 0 -> push()
                
                else -> current.append(c)
            }
        }
        push()
        return tokens
    }
}

// ===== TOKENIZER INSTANCES =====
val WhitespaceTokenizer = DelimiterTokenizer({ it.isWhitespace() })
val CommaTokenizer = DelimiterTokenizer({ it == ',' })
val SlashTokenizer = DelimiterTokenizer({ it == '/' })
val BracketTokenizer = DelimiterTokenizer({ false }, respectBrackets = true)
val FunctionTokenizer = DelimiterTokenizer({ false }, respectParens = true)
val QuoteTokenizer = DelimiterTokenizer({ it == '"' || it == '\'' })

// Operator tokenizer needs special handling for multi-char operators
object OperatorTokenizer : Tokenizer {
    private val operators = listOf("<=", ">=", "==", "!=", "+", "-", "*", "/")
    
    override fun tokenize(input: String): List<String> {
        val tokens = mutableListOf<String>()
        val current = StringBuilder()
        var parenDepth = 0
        var quoteChar: Char? = null
        var i = 0
        
        fun push() {
            val trimmed = current.toString().trim()
            if (trimmed.isNotEmpty()) {
                tokens.add(trimmed)
                current.setLength(0)
            }
        }
        
        while (i < input.length) {
            val c = input[i]
            when {
                (c == '"' || c == '\'') -> {
                    if (quoteChar == null) quoteChar = c
                    else if (quoteChar == c) quoteChar = null
                    current.append(c)
                    i++
                }
                quoteChar != null -> { current.append(c); i++ }
                c == '(' -> { parenDepth++; current.append(c); i++ }
                c == ')' -> { parenDepth--; current.append(c); i++ }
                parenDepth == 0 -> {
                    val matched = operators.firstOrNull { input.startsWith(it, i) }
                    if (matched != null) {
                        push()
                        tokens.add(matched)
                        i += matched.length
                    } else {
                        current.append(c)
                        i++
                    }
                }
                else -> { current.append(c); i++ }
            }
        }
        push()
        return tokens
    }
}