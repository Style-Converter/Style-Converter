package app.parsing.css.selectors

import app.IRSelector
import app.IRProperty
import app.parsing.css.CssSelector
import app.parsing.css.properties.PropertiesParser

fun parseSelectors(selectors: List<CssSelector>?): List<IRSelector> {
    if (selectors == null) return emptyList()
    return selectors.map { sel ->
        val props = PropertiesParser.parse(sel.properties)
        IRSelector(condition = sel.selector, properties = props)
    }
}