package app.logic.css

import app.irmodels.BorderWidthValue
import app.irmodels.IRAngle
import app.irmodels.IRColor
import app.irmodels.IRLength
import app.irmodels.IRTime
import app.irmodels.properties.animations.TimingFunction
import app.irmodels.properties.typography.FontSize
import app.irmodels.properties.typography.FontStretch
import app.irmodels.properties.typography.FontWeightProperty
import app.irmodels.properties.color.Opacity
import app.irmodels.properties.layout.position.ZIndex
import app.irmodels.properties.typography.LetterSpacing
import app.irmodels.properties.typography.LineHeight
import app.irmodels.properties.typography.WordSpacing

/**
 * CSS Regeneration Utilities
 *
 * Extension functions to convert IR values back to their original CSS format.
 * Uses the preserved original values to ensure round-trip fidelity.
 */

/**
 * Convert IRColor back to CSS string.
 * Preserves the original format (hex, rgb, hsl, oklch, named, etc.)
 */
fun IRColor.toCss(): String {
    return when (val repr = representation) {
        is IRColor.ColorRepresentation.Hex -> {
            val hex = repr.value
            if (hex.startsWith("#")) hex else "#$hex"
        }
        is IRColor.ColorRepresentation.RGB -> {
            if (repr.a < 1.0) {
                "rgba(${repr.r}, ${repr.g}, ${repr.b}, ${repr.a})"
            } else {
                "rgb(${repr.r}, ${repr.g}, ${repr.b})"
            }
        }
        is IRColor.ColorRepresentation.HSL -> {
            if (repr.a < 1.0) {
                "hsla(${repr.h}, ${repr.s}%, ${repr.l}%, ${repr.a})"
            } else {
                "hsl(${repr.h}, ${repr.s}%, ${repr.l}%)"
            }
        }
        is IRColor.ColorRepresentation.HWB -> {
            if (repr.alpha < 1.0) {
                "hwb(${repr.h} ${repr.w}% ${repr.b}% / ${repr.alpha})"
            } else {
                "hwb(${repr.h} ${repr.w}% ${repr.b}%)"
            }
        }
        is IRColor.ColorRepresentation.Lab -> {
            if (repr.alpha < 1.0) {
                "lab(${repr.l}% ${repr.a} ${repr.b} / ${repr.alpha})"
            } else {
                "lab(${repr.l}% ${repr.a} ${repr.b})"
            }
        }
        is IRColor.ColorRepresentation.LCH -> {
            if (repr.alpha < 1.0) {
                "lch(${repr.l}% ${repr.c} ${repr.h} / ${repr.alpha})"
            } else {
                "lch(${repr.l}% ${repr.c} ${repr.h})"
            }
        }
        is IRColor.ColorRepresentation.OKLab -> {
            if (repr.alpha < 1.0) {
                "oklab(${repr.l} ${repr.a} ${repr.b} / ${repr.alpha})"
            } else {
                "oklab(${repr.l} ${repr.a} ${repr.b})"
            }
        }
        is IRColor.ColorRepresentation.OKLCH -> {
            if (repr.alpha < 1.0) {
                "oklch(${repr.l}% ${repr.c} ${repr.h} / ${repr.alpha})"
            } else {
                "oklch(${repr.l}% ${repr.c} ${repr.h})"
            }
        }
        is IRColor.ColorRepresentation.Named -> repr.name
        is IRColor.ColorRepresentation.CurrentColor -> "currentColor"
        is IRColor.ColorRepresentation.Transparent -> "transparent"
        is IRColor.ColorRepresentation.ColorFunction -> {
            val valuesStr = repr.values.joinToString(" ")
            val alpha = if (repr.alpha < 1.0) " / ${repr.alpha}" else ""
            "color(${repr.colorSpace} $valuesStr$alpha)"
        }
        is IRColor.ColorRepresentation.ColorMix -> {
            val hue = repr.hueMethod?.let { " $it hue" } ?: ""
            val p1 = repr.percent1?.let { " ${it}%" } ?: ""
            val p2 = repr.percent2?.let { " ${it}%" } ?: ""
            "color-mix(in ${repr.colorSpace}$hue, ${repr.color1}$p1, ${repr.color2}$p2)"
        }
        is IRColor.ColorRepresentation.LightDark -> {
            "light-dark(${repr.lightColor}, ${repr.darkColor})"
        }
        is IRColor.ColorRepresentation.RelativeColor -> {
            // Reconstruct relative color syntax
            val componentsStr = repr.components.joinToString(" ")
            "${repr.function}(from ${repr.baseColor} $componentsStr)"
        }
    }
}

/**
 * Convert IRAngle back to CSS string.
 * Uses originalValue and originalUnit to preserve the input format.
 */
fun IRAngle.toCss(): String {
    return when (originalUnit) {
        IRAngle.AngleUnit.DEG -> "${formatNumber(originalValue)}deg"
        IRAngle.AngleUnit.RAD -> "${formatNumber(originalValue)}rad"
        IRAngle.AngleUnit.GRAD -> "${formatNumber(originalValue)}grad"
        IRAngle.AngleUnit.TURN -> "${formatNumber(originalValue)}turn"
    }
}

/**
 * Convert IRTime back to CSS string.
 * Uses originalValue and originalUnit to preserve the input format.
 */
fun IRTime.toCss(): String {
    return when (originalUnit) {
        IRTime.TimeUnit.S -> "${formatNumber(originalValue)}s"
        IRTime.TimeUnit.MS -> "${formatNumber(originalValue)}ms"
    }
}

/**
 * Convert IRLength back to CSS string.
 * Uses originalValue and originalUnit to preserve the input format.
 */
fun IRLength.toCss(): String {
    val value = formatNumber(originalValue)
    return when (originalUnit) {
        // Absolute units
        IRLength.LengthUnit.PX -> "${value}px"
        IRLength.LengthUnit.PT -> "${value}pt"
        IRLength.LengthUnit.CM -> "${value}cm"
        IRLength.LengthUnit.MM -> "${value}mm"
        IRLength.LengthUnit.IN -> "${value}in"
        IRLength.LengthUnit.PC -> "${value}pc"
        IRLength.LengthUnit.Q -> "${value}Q"
        // Android units
        IRLength.LengthUnit.DP -> "${value}dp"
        IRLength.LengthUnit.SP -> "${value}sp"
        // Font-relative units
        IRLength.LengthUnit.EM -> "${value}em"
        IRLength.LengthUnit.REM -> "${value}rem"
        IRLength.LengthUnit.EX -> "${value}ex"
        IRLength.LengthUnit.CH -> "${value}ch"
        IRLength.LengthUnit.LH -> "${value}lh"
        IRLength.LengthUnit.RLH -> "${value}rlh"
        IRLength.LengthUnit.IC -> "${value}ic"
        IRLength.LengthUnit.CAP -> "${value}cap"
        // Classic viewport units
        IRLength.LengthUnit.VW -> "${value}vw"
        IRLength.LengthUnit.VH -> "${value}vh"
        IRLength.LengthUnit.VMIN -> "${value}vmin"
        IRLength.LengthUnit.VMAX -> "${value}vmax"
        IRLength.LengthUnit.VI -> "${value}vi"
        IRLength.LengthUnit.VB -> "${value}vb"
        // Small viewport units
        IRLength.LengthUnit.SVW -> "${value}svw"
        IRLength.LengthUnit.SVH -> "${value}svh"
        IRLength.LengthUnit.SVB -> "${value}svb"
        IRLength.LengthUnit.SVI -> "${value}svi"
        IRLength.LengthUnit.SVMIN -> "${value}svmin"
        IRLength.LengthUnit.SVMAX -> "${value}svmax"
        // Large viewport units
        IRLength.LengthUnit.LVW -> "${value}lvw"
        IRLength.LengthUnit.LVH -> "${value}lvh"
        IRLength.LengthUnit.LVB -> "${value}lvb"
        IRLength.LengthUnit.LVI -> "${value}lvi"
        IRLength.LengthUnit.LVMIN -> "${value}lvmin"
        IRLength.LengthUnit.LVMAX -> "${value}lvmax"
        // Dynamic viewport units
        IRLength.LengthUnit.DVW -> "${value}dvw"
        IRLength.LengthUnit.DVH -> "${value}dvh"
        IRLength.LengthUnit.DVB -> "${value}dvb"
        IRLength.LengthUnit.DVI -> "${value}dvi"
        IRLength.LengthUnit.DVMIN -> "${value}dvmin"
        IRLength.LengthUnit.DVMAX -> "${value}dvmax"
        // Container query units
        IRLength.LengthUnit.CQW -> "${value}cqw"
        IRLength.LengthUnit.CQH -> "${value}cqh"
        IRLength.LengthUnit.CQI -> "${value}cqi"
        IRLength.LengthUnit.CQB -> "${value}cqb"
        IRLength.LengthUnit.CQMIN -> "${value}cqmin"
        IRLength.LengthUnit.CQMAX -> "${value}cqmax"
        // Other units
        IRLength.LengthUnit.PERCENT -> "${value}%"
        IRLength.LengthUnit.FR -> "${value}fr"
    }
}

/**
 * Convert FontWeightProperty back to CSS string.
 * Preserves the original format (numeric or keyword).
 */
fun FontWeightProperty.toCss(): String {
    return when (val orig = original) {
        is FontWeightProperty.FontWeightOriginal.Numeric -> orig.value.toString()
        is FontWeightProperty.FontWeightOriginal.Keyword -> orig.keyword
    }
}

/**
 * Convert TimingFunction back to CSS string.
 * Preserves the original format (keyword, cubic-bezier, steps, or linear).
 */
fun TimingFunction.toCss(): String {
    return when (val orig = original) {
        is TimingFunction.TimingOriginal.Keyword -> orig.keyword
        is TimingFunction.TimingOriginal.CubicBezier ->
            "cubic-bezier(${formatNumber(orig.x1)}, ${formatNumber(orig.y1)}, ${formatNumber(orig.x2)}, ${formatNumber(orig.y2)})"
        is TimingFunction.TimingOriginal.Steps -> {
            val posStr = orig.position?.let { ", $it" } ?: ""
            "steps(${orig.count}$posStr)"
        }
        is TimingFunction.TimingOriginal.Linear -> {
            val stopsStr = orig.stops.joinToString(", ") { stop ->
                if (stop.position != null) {
                    "${formatNumber(stop.value)} ${formatNumber(stop.position)}%"
                } else {
                    formatNumber(stop.value)
                }
            }
            "linear($stopsStr)"
        }
    }
}

/**
 * Convert BorderWidthValue back to CSS string.
 * Preserves the original format (keyword or length).
 */
fun BorderWidthValue.toCss(): String {
    return when (val orig = original) {
        is BorderWidthValue.BorderWidthOriginal.Keyword -> orig.keyword
        is BorderWidthValue.BorderWidthOriginal.Length -> orig.length.toCss()
    }
}

/**
 * Convert LineHeight back to CSS string.
 * Preserves the original format (normal, number, percentage, length, or expression).
 */
fun LineHeight.toCss(): String {
    return when (val orig = original) {
        is LineHeight.LineHeightOriginal.Normal -> "normal"
        is LineHeight.LineHeightOriginal.Number -> formatNumber(orig.value)
        is LineHeight.LineHeightOriginal.Percentage -> "${formatNumber(orig.value)}%"
        is LineHeight.LineHeightOriginal.Length -> orig.length.toCss()
        is LineHeight.LineHeightOriginal.Expression -> orig.expr
        is LineHeight.LineHeightOriginal.Keyword -> orig.keyword
    }
}

/**
 * Convert FontSize back to CSS string.
 * Preserves the original format (keyword, length, percentage, or expression).
 */
fun FontSize.toCss(): String {
    return when (val orig = original) {
        is FontSize.FontSizeOriginal.AbsoluteKeyword ->
            orig.keyword.name.lowercase().replace("_", "-")
        is FontSize.FontSizeOriginal.RelativeKeyword ->
            orig.keyword.name.lowercase()
        is FontSize.FontSizeOriginal.Length -> orig.length.toCss()
        is FontSize.FontSizeOriginal.Percentage -> "${formatNumber(orig.value)}%"
        is FontSize.FontSizeOriginal.Expression -> orig.expr
        is FontSize.FontSizeOriginal.GlobalKeyword -> orig.keyword
    }
}

/**
 * Convert FontStretch back to CSS string.
 * Preserves the original format (keyword or percentage).
 */
fun FontStretch.toCss(): String {
    return when (val orig = original) {
        is FontStretch.FontStretchOriginal.Keyword ->
            orig.keyword.name.lowercase().replace("_", "-")
        is FontStretch.FontStretchOriginal.Percentage -> "${formatNumber(orig.value)}%"
        is FontStretch.FontStretchOriginal.GlobalKeyword -> orig.keyword
    }
}

/**
 * Convert LetterSpacing back to CSS string.
 * Preserves the original format (normal or length).
 */
fun LetterSpacing.toCss(): String {
    return when (val orig = original) {
        is LetterSpacing.LetterSpacingOriginal.Normal -> "normal"
        is LetterSpacing.LetterSpacingOriginal.Length -> orig.length.toCss()
        is LetterSpacing.LetterSpacingOriginal.GlobalKeyword -> orig.keyword
    }
}

/**
 * Convert WordSpacing back to CSS string.
 * Preserves the original format (normal or length).
 */
fun WordSpacing.toCss(): String {
    return when (val orig = original) {
        is WordSpacing.WordSpacingOriginal.Normal -> "normal"
        is WordSpacing.WordSpacingOriginal.Length -> orig.length.toCss()
        is WordSpacing.WordSpacingOriginal.GlobalKeyword -> orig.keyword
    }
}

/**
 * Convert ZIndex back to CSS string.
 * Preserves the original format (auto, integer, or expression).
 */
fun ZIndex.toCss(): String {
    return when (val orig = original) {
        is ZIndex.ZIndexOriginal.Auto -> "auto"
        is ZIndex.ZIndexOriginal.Integer -> orig.value.toString()
        is ZIndex.ZIndexOriginal.Expression -> orig.expr
        is ZIndex.ZIndexOriginal.GlobalKeyword -> orig.keyword
    }
}

/**
 * Convert Opacity back to CSS string.
 * Preserves the original format (number or percentage).
 */
fun Opacity.toCss(): String {
    return when (val orig = original) {
        is Opacity.OpacityOriginal.Number -> formatNumber(orig.value)
        is Opacity.OpacityOriginal.Percentage -> "${formatNumber(orig.value)}%"
        is Opacity.OpacityOriginal.Expression -> orig.expr
        is Opacity.OpacityOriginal.GlobalKeyword -> orig.keyword
    }
}

/**
 * Format a number for CSS output.
 * Removes unnecessary decimal places (e.g., 1.0 -> "1", 0.5 -> "0.5")
 */
private fun formatNumber(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        value.toString()
    }
}
