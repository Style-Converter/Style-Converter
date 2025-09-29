package app.parsing.css.properties.parsers

import app.parsing.css.properties.parsers.CssPropertyParser
import app.parsing.css.properties.parsers.GenericPropertyParser

object PropertyParserRegistry {
    //private val parsers = mutableMapOf<Regex, CssPropertyParser>()

    //init {
    //    // Register specialized parsers by pattern:
    //    //register(Regex("^border(-.*)?$"), BorderParser)
    //    //register(Regex(".*shadow.*"), ShadowParser)
    //    //register(Regex("^background-position-(x|y)$"), PositionParser)
    //    //register(Regex("^font-style$"), FontStyleParser)
    //    private fun register(pattern: Regex, parser: CssPropertyParser) {
    //}
    //init {
    //    // Register specialized parsers by pattern:
    //    register(Regex("^border(-.*)?$"), BorderParser)
    //    register(Regex(".*shadow.*"), ShadowParser)
    //    register(Regex("^background-position-(x|y)$"), PositionParser)
    //    register(Regex("^font-style$"), FontStyleParser)
    //    // ... add more specialized parsers as needed
    //}

    //private fun register(pattern: Regex, parser: CssPropertyParser) {
    //    parsers[pattern] = parser
    //}

    fun find(propertyName: String): CssPropertyParser {
        //for ((regex, parser) in parsers) {
        //    if (regex.matches(propertyName)) return parser
        //}
        return GenericPropertyParser // fallback
    }
}