package com.styleconverter.test.style.effects.blend

import androidx.compose.ui.graphics.BlendMode

/**
 * Configuration for CSS mix-blend-mode property.
 *
 * ## CSS Property
 * ```css
 * .blended {
 *     mix-blend-mode: multiply;
 * }
 * ```
 *
 * ## Compose Mapping
 * Uses graphicsLayer with BlendMode parameter.
 */
data class BlendModeConfig(
    val blendMode: BlendMode? = null
) {
    val hasBlendMode: Boolean get() = blendMode != null && blendMode != BlendMode.SrcOver
}

/**
 * CSS mix-blend-mode values mapped to Compose BlendMode.
 */
object BlendModeMapping {

    fun fromCssValue(value: String): BlendMode? {
        return when (value.uppercase().replace("-", "_")) {
            "NORMAL" -> BlendMode.SrcOver
            "MULTIPLY" -> BlendMode.Multiply
            "SCREEN" -> BlendMode.Screen
            "OVERLAY" -> BlendMode.Overlay
            "DARKEN" -> BlendMode.Darken
            "LIGHTEN" -> BlendMode.Lighten
            "COLOR_DODGE", "COLORDODGE" -> BlendMode.ColorDodge
            "COLOR_BURN", "COLORBURN" -> BlendMode.ColorBurn
            "HARD_LIGHT", "HARDLIGHT" -> BlendMode.Hardlight
            "SOFT_LIGHT", "SOFTLIGHT" -> BlendMode.Softlight
            "DIFFERENCE" -> BlendMode.Difference
            "EXCLUSION" -> BlendMode.Exclusion
            "HUE" -> BlendMode.Hue
            "SATURATION" -> BlendMode.Saturation
            "COLOR" -> BlendMode.Color
            "LUMINOSITY" -> BlendMode.Luminosity
            "PLUS_LIGHTER", "PLUSLIGHTER" -> BlendMode.Plus
            else -> null
        }
    }
}
