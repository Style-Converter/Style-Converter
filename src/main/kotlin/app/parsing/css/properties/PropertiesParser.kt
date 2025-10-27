package app.parsing.css.properties

import app.IRProperty
import app.parsing.css.CssPropertyValue
import app.parsing.css.properties.longhand.PropertyParserRegistry
import app.parsing.css.properties.shorthand.ShorthandRegistry


object PropertiesParser {

	fun parse(properties: Map<String, CssPropertyValue>): MutableList<IRProperty> {
		// First, filter out invalid CSS properties
		val validProperties = properties.filter { (name, _) ->
			CssPropertyValidator.isValidProperty(name)
		}

		// Log removed properties (optional - can be removed if not needed)
		val invalidProperties = properties.keys.filter { !CssPropertyValidator.isValidProperty(it) }
		if (invalidProperties.isNotEmpty()) {
			// Optionally log or report invalid properties
			println("[CSS Validator] Removed invalid properties: ${invalidProperties.joinToString(", ")}")
		}

		// Then, expand all shorthand properties into longhand
		val expandedProperties = mutableMapOf<String, String>()

		for ((name, cssValue) in validProperties) {
			val value = cssValue.value
			if (ShorthandRegistry.isShorthand(name)) {
				// Expand shorthand into multiple longhand properties
				val expanded = ShorthandRegistry.expand(name, value)
				expandedProperties.putAll(expanded)
			} else {
				// Keep longhand as-is
				expandedProperties[name] = value
			}
		}

		// Finally, parse all longhand properties
		return expandedProperties.map { (name, value) ->
			PropertyParserRegistry.find(name).parse(name, value)
		}.toMutableList()
	}

}