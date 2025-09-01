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
 * Represents properties for one side of a border (top, right, bottom, or left)
 * @param widthPx Border width in pixels
 * @param color Border color as RGBA
 * @param style Border style (e.g., "solid", "none")
 */
@Serializable
data class BorderSide(
    var widthPx: Double? = null, 
    var color: ColorRgba? = null, 
    var style: String? = null
)

/**
 * Represents border radius values for all four corners in pixels
 */
@Serializable
data class BorderRadius(
    var topLeftPx: Double? = null, 
    var topRightPx: Double? = null, 
    var bottomRightPx: Double? = null, 
    var bottomLeftPx: Double? = null
)

/**
 * Intermediate representation of border properties including all sides and corner radius
 */
@Serializable
data class BorderIR(
    val top: BorderSide? = null, 
    val right: BorderSide? = null, 
    val bottom: BorderSide? = null, 
    val left: BorderSide? = null, 
    val radius: BorderRadius? = null
)

/**
 * Intermediate representation of typography properties
 */
@Serializable
data class TypographyIR(
    val fontSizePx: Double? = null
)

/**
 * Intermediate representation of spacing/margin properties
 */
@Serializable
data class SpacingIR(
    val marginTopPx: Double? = null,
    val marginRightPx: Double? = null,
    val marginBottomPx: Double? = null,
    val marginLeftPx: Double? = null,
    val paddingTopPx: Double? = null,
    val paddingRightPx: Double? = null,
    val paddingBottomPx: Double? = null,
    val paddingLeftPx: Double? = null
)

/**
 * Intermediate representation of size properties
 */
@Serializable
data class SizeIR(
    val widthPx: Double? = null
)

/**
 * Intermediate representation of layout properties
 */
@Serializable
data class LayoutIR(
    val display: String? = null
)

/**
 * Base intermediate representation containing all common style properties
 * This serves as the normalized format that can be converted to any target platform
 */
@Serializable
data class BaseIR(
    var backgroundColor: ColorRgba? = null,
    var opacity: Double? = null,
    var typography: TypographyIR? = null,
    var spacing: SpacingIR? = null,
    var size: SizeIR? = null,
    var layout: LayoutIR? = null,
    var border: BorderIR? = null,
    // Fallback bucket for properties not yet modeled in the IR
    var other: Map<String, String>? = null,
)

/**
 * Represents a selector in the intermediate representation
 * @param when The condition for the selector (e.g., ":hover", ":focus")
 * @param styles The styles to apply when the selector is matched
 */
@Serializable
data class SelectorIR(
    val condition: String,
    val styles: BaseIR
)


/**
 * Represents a media query in the intermediate representation
 * @param query The media query string (e.g., "(min-width: 768px)")
 * @param styles The styles to apply when the media query is matched
 */
@Serializable
data class MediaIR(
    val query: String,
    val styles: BaseIR
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
    val base: BaseIR,
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