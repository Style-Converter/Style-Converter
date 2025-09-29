package app.parsing.css

import kotlinx.serialization.Serializable

/**
 * Represents a CSS property value in JSON format
 */
@Serializable
data class CssPropertyValue(
    val value: String
)

/**
 * Represents a CSS selector in JSON format
 */
@Serializable
data class CssSelector(
    val selector: String,
    val properties: Map<String, CssPropertyValue>
)

/**
 * Represents a CSS media query in JSON format
 */
@Serializable
data class CssMedia(
    val query: String,
    val properties: Map<String, CssPropertyValue>
)

/**
 * Represents a CSS component in JSON format
 */
@Serializable
data class CssComponent(
    val properties: Map<String, CssPropertyValue>? = null,
    val selectors: List<CssSelector>? = null,
    val media: List<CssMedia>? = null
)

/**
 * Root document containing all CSS components in JSON format
 */
@Serializable
data class CssComponents(
    val components: Map<String, CssComponent>
)