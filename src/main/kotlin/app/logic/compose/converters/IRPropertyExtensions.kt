package app.logic.compose.converters

import app.irmodels.IRProperty
import app.irmodels.IRLength
import app.irmodels.IRColor
import app.irmodels.IRKeyword
import app.irmodels.properties.borders.BoxShadowProperty

/**
 * Backward compatibility extensions for IRProperty.
 * These provide the old .lengths, .colors, .keywords fields as computed properties
 * to minimize changes needed in SimpleComposeBuilder and other generators.
 *
 * This is a temporary bridge solution. Eventually, generators should work directly
 * with specific property types rather than using these generic accessors.
 */

/**
 * Get lengths from this property (backward compat)
 */
val IRProperty.lengths: List<IRLength>
    get() {
        val length = PropertyValueExtractor.getLength(this)
        return if (length != null) listOf(length) else emptyList()
    }

/**
 * Get colors from this property (backward compat)
 */
val IRProperty.colors: List<IRColor>
    get() {
        val color = PropertyValueExtractor.getColor(this)
        return if (color != null) listOf(color) else emptyList()
    }

/**
 * Get keywords from this property (backward compat)
 */
val IRProperty.keywords: List<IRKeyword>
    get() {
        val keyword = PropertyValueExtractor.getKeyword(this)
        return if (keyword != null) listOf(IRKeyword(keyword)) else emptyList()
    }

/**
 * Get raw string value from property (backward compat)
 */
val IRProperty.raw: String?
    get() = PropertyValueExtractor.getRawValue(this)

/**
 * Get urls from this property (backward compat)
 * TODO: Implement URL extraction when needed
 */
val IRProperty.urls: List<app.irmodels.IRUrl>
    get() = emptyList()

/**
 * Get shadows from this property (backward compat)
 */
val IRProperty.shadows: List<BoxShadowProperty.Shadow>
    get() = when (this) {
        is BoxShadowProperty -> when (val v = this.value) {
            is BoxShadowProperty.BoxShadowValue.Shadows -> v.list
            else -> emptyList()
        }
        else -> emptyList()
    }

/**
 * Get raw value from color (backward compat)
 */
val IRColor.raw: String
    get() = "#" + PropertyValueExtractor.colorToHexString(this)
