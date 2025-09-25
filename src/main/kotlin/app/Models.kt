package app

import kotlinx.serialization.Serializable

/**
 * Represents an RGBA color value with integer RGB components (0-255) and double alpha (0.0-1.0)
 */
@Serializable
data class ColorRgba(
    val r: Int,
    val g: Int,
    val b: Int,
    val a: Double
)

/**
 * property captured from CSS-like inputs
 */
@Serializable
data class PropertyIR(
    val name: String,
    val sizeName: String? = null,
    val numericSizeValue: Double? = null,
    val stringSizeValue: String? = null,
    val color: ColorRgba? = null,
    val value: String? = null
)

/**
 * Represents a selector in the intermediate representation
 * @param when The condition for the selector (e.g., ":hover", ":focus")
 * @param styles The styles to apply when the selector is matched
 */
@Serializable
data class SelectorIR(
    val condition: String,
    val styles: MutableList<PropertyIR>
)

/**
 * Represents a media query in the intermediate representation
 * @param query The media query string (e.g., "(min-width: 768px)")
 * @param styles The styles to apply when the media query is matched
 */
@Serializable
data class MediaIR(
    val query: String,
    val styles: MutableList<PropertyIR>
)

/**
 * Represents a component in the intermediate representation
 * @param name Component name/identifier
 * @param base Base style properties
 * @param selectors List of conditional selectors (e.g., hover, focus states)
 * @param media List of media query definitions with their own selectors
 */
@Serializable
data class ComponentIR(
    val name: String,
    val styles: MutableList<PropertyIR>,
    val selectors: List<SelectorIR>,
    val media: List<MediaIR>
)

/**
 * Root document containing all components in intermediate representation format
 */
@Serializable
data class DocumentIR(
    val components: List<ComponentIR>
)