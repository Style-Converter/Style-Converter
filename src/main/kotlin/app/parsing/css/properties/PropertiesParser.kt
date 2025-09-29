package app.parsing.css.properties

import app.IRProperty
import app.parsing.css.CssPropertyValue
import app.parsing.css.properties.parsers.PropertyParserRegistry


object PropertiesParser {

	fun parse(properties: Map<String, CssPropertyValue>): MutableList<IRProperty> {
		val irProperties = mutableListOf<IRProperty>()
		for ((propertyName, value) in properties) {
			val parser = PropertyParserRegistry.find(propertyName)
			irProperties.add(parser.parse(propertyName, value.value))
		}
		return irProperties
	}

}