package app.logic.compose.converters

import app.irmodels.IRProperty
import app.irmodels.IRLength
import app.irmodels.IRColor
import app.irmodels.IRKeyword
import app.irmodels.PaddingValue
import app.irmodels.MarginValue
import app.irmodels.BorderWidthValue
import app.irmodels.properties.color.*
import app.irmodels.properties.spacing.*
import app.irmodels.properties.typography.*
import app.irmodels.properties.borders.*
import app.irmodels.properties.layout.position.*
import app.irmodels.properties.layout.flexbox.*
import app.parsing.css.properties.GenericProperty

/**
 * Helper to extract typed values from specific IRProperty classes.
 * Bridges the gap between the new specific property types and the generator's needs.
 */
object PropertyValueExtractor {

    /**
     * Extract IRLength from a property
     */
    fun getLength(property: IRProperty): IRLength? {
        return when (property) {
            // Spacing - Padding
            is PaddingTopProperty -> when (val v = property.padding) {
                is PaddingValue.Length -> v.value
                is PaddingValue.Percentage -> null // TODO: Convert percentage
                else -> null
            }
            is PaddingRightProperty -> when (val v = property.padding) {
                is PaddingValue.Length -> v.value
                is PaddingValue.Percentage -> null
                else -> null
            }
            is PaddingBottomProperty -> when (val v = property.padding) {
                is PaddingValue.Length -> v.value
                is PaddingValue.Percentage -> null
                else -> null
            }
            is PaddingLeftProperty -> when (val v = property.padding) {
                is PaddingValue.Length -> v.value
                is PaddingValue.Percentage -> null
                else -> null
            }

            // Spacing - Margin
            is MarginTopProperty -> when (val v = property.margin) {
                is MarginValue.Length -> v.value
                is MarginValue.Percentage -> null
                is MarginValue.Auto -> null
                else -> null
            }
            is MarginRightProperty -> when (val v = property.margin) {
                is MarginValue.Length -> v.value
                is MarginValue.Percentage -> null
                is MarginValue.Auto -> null
                else -> null
            }
            is MarginBottomProperty -> when (val v = property.margin) {
                is MarginValue.Length -> v.value
                is MarginValue.Percentage -> null
                is MarginValue.Auto -> null
                else -> null
            }
            is MarginLeftProperty -> when (val v = property.margin) {
                is MarginValue.Length -> v.value
                is MarginValue.Percentage -> null
                is MarginValue.Auto -> null
                else -> null
            }

            // Typography - Font Size (use normalized pixels when available)
            is FontSizeProperty -> property.size.pixels?.let { IRLength.fromPx(it) }

            // Borders - Width (all use shared BorderWidthValue with normalized pixels)
            is BorderTopWidthProperty -> IRLength.fromPx(property.width.pixels)
            is BorderRightWidthProperty -> IRLength.fromPx(property.width.pixels)
            is BorderBottomWidthProperty -> IRLength.fromPx(property.width.pixels)
            is BorderLeftWidthProperty -> IRLength.fromPx(property.width.pixels)

            // Border Radius - extract horizontal length
            is BorderTopLeftRadiusProperty -> property.horizontal
            is BorderTopRightRadiusProperty -> property.horizontal
            is BorderBottomRightRadiusProperty -> property.horizontal
            is BorderBottomLeftRadiusProperty -> property.horizontal

            // Generic fallback - try to parse from raw value
            is GenericProperty -> null // TODO: Could parse rawValue if needed

            else -> null
        }
    }

    /**
     * Extract IRColor from a property
     */
    fun getColor(property: IRProperty): IRColor? {
        return when (property) {
            is ColorProperty -> property.color
            is BackgroundColorProperty -> property.color
            is BorderTopColorProperty -> property.color
            is BorderRightColorProperty -> property.color
            is BorderBottomColorProperty -> property.color
            is BorderLeftColorProperty -> property.color
            is GenericProperty -> null // TODO: Could parse rawValue
            else -> null
        }
    }

    /**
     * Extract keyword string from a property
     */
    fun getKeyword(property: IRProperty): String? {
        return when (property) {
            is FontWeightProperty -> {
                // Use normalized numeric value if available, otherwise original
                property.numericValue?.toString()
                    ?: when (val orig = property.original) {
                        is FontWeightProperty.FontWeightOriginal.Numeric -> orig.value.toString()
                        is FontWeightProperty.FontWeightOriginal.Keyword -> orig.keyword
                    }
            }
            is TextAlignProperty -> property.alignment.name.lowercase()
            is DisplayProperty -> property.value.name.lowercase()
            is PositionProperty -> property.value.name.lowercase()
            is GenericProperty -> property.rawValue // Use raw value for unparsed properties
            else -> null
        }
    }

    /**
     * Get raw string value from a property (useful for GenericProperty)
     */
    fun getRawValue(property: IRProperty): String? {
        return when (property) {
            is GenericProperty -> property.rawValue
            else -> null
        }
    }

    /**
     * Extract opacity value (0.0-1.0) from a property
     */
    fun getOpacity(property: IRProperty): Double? {
        return when (property) {
            is OpacityProperty -> property.value.alpha
            else -> null
        }
    }

    /**
     * Convert IRColor to hex string (without #).
     * Uses normalized sRGB values when available, falls back to representation parsing.
     */
    fun colorToHexString(color: IRColor): String {
        // Use normalized sRGB if available (handles all static color formats)
        color.srgb?.let { srgb ->
            val r = (srgb.r * 255).toInt().coerceIn(0, 255)
            val g = (srgb.g * 255).toInt().coerceIn(0, 255)
            val b = (srgb.b * 255).toInt().coerceIn(0, 255)
            return "%02X%02X%02X".format(r, g, b)
        }

        // Fallback for dynamic colors (color-mix, light-dark, currentColor, var())
        return when (val repr = color.representation) {
            is IRColor.ColorRepresentation.Transparent -> "00000000"
            is IRColor.ColorRepresentation.CurrentColor -> "000000" // Runtime-dependent
            is IRColor.ColorRepresentation.ColorMix -> "000000" // Runtime-dependent
            is IRColor.ColorRepresentation.LightDark -> "000000" // Theme-dependent
            is IRColor.ColorRepresentation.RelativeColor -> "000000" // May contain var()
            else -> "000000" // Unexpected case
        }
    }

    /**
     * Convert IRColor to hex string with alpha (AARRGGBB format).
     * Uses normalized sRGB values when available.
     */
    fun colorToHexStringWithAlpha(color: IRColor): String {
        // Use normalized sRGB if available
        color.srgb?.let { srgb ->
            val a = (srgb.a * 255).toInt().coerceIn(0, 255)
            val r = (srgb.r * 255).toInt().coerceIn(0, 255)
            val g = (srgb.g * 255).toInt().coerceIn(0, 255)
            val b = (srgb.b * 255).toInt().coerceIn(0, 255)
            return "%02X%02X%02X%02X".format(a, r, g, b)
        }

        // Fallback for dynamic colors
        return when (color.representation) {
            is IRColor.ColorRepresentation.Transparent -> "00000000"
            else -> "FF000000" // Default black with full opacity
        }
    }

    /**
     * Get alpha value (0.0-1.0) from IRColor.
     * Uses normalized sRGB when available.
     */
    fun colorAlpha(color: IRColor): Double {
        return color.srgb?.a ?: when (color.representation) {
            is IRColor.ColorRepresentation.Transparent -> 0.0
            else -> 1.0
        }
    }
}
