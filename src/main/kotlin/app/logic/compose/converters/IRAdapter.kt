package app.logic.compose.converters

import app.*

/**
 * Adapts the IR property list format to a simplified map format for converters
 */
object IRAdapter {

    /**
     * Converts list of IRProperties to a Map<propertyName, firstValue>
     * Simplifies working with properties in converters
     */
    fun propertiesToMap(properties: List<IRProperty>): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()

        properties.forEach { prop ->
            val value: Any? = when {
                prop.lengths.isNotEmpty() -> prop.lengths.first()
                prop.colors.isNotEmpty() -> prop.colors.first()
                prop.keywords.isNotEmpty() -> prop.keywords.first()
                prop.shadows.isNotEmpty() -> prop.shadows.first()
                prop.urls.isNotEmpty() -> prop.urls.first()
                else -> null
            }

            map[prop.propertyName] = value
        }

        return map
    }

    /**
     * Gets a length value from a property list
     */
    fun getLength(properties: List<IRProperty>, propertyName: String): IRLength? {
        return properties.find { it.propertyName == propertyName }?.lengths?.firstOrNull()
    }

    /**
     * Gets a color value from a property list
     */
    fun getColor(properties: List<IRProperty>, propertyName: String): IRColor? {
        return properties.find { it.propertyName == propertyName }?.colors?.firstOrNull()
    }

    /**
     * Gets a keyword value from a property list
     */
    fun getKeyword(properties: List<IRProperty>, propertyName: String): IRKeyword? {
        return properties.find { it.propertyName == propertyName }?.keywords?.firstOrNull()
    }

    /**
     * Gets a shadow value from a property list
     */
    fun getShadow(properties: List<IRProperty>, propertyName: String): IRShadow? {
        return properties.find { it.propertyName == propertyName }?.shadows?.firstOrNull()
    }

    /**
     * Converts IRColor (which only has raw string) to a usable color code
     */
    fun colorToString(color: IRColor): String {
        // IRColor has raw string representation
        return color.raw ?: "transparent"
    }

    /**
     * Converts IRLength to Double value for calculations
     */
    fun lengthToDouble(length: IRLength?): Double {
        return length?.value ?: 0.0
    }
}
