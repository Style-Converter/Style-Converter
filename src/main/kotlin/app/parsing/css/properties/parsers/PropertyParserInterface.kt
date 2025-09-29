package app.parsing.css.properties.parsers

import app.IRProperty

/**
 * All property parsers implement this.
 * They get a property name + value and return an IRProperty.
 */
interface CssPropertyParser {
    fun parse(propertyName: String, value: Any): IRProperty
}