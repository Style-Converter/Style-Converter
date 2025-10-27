package app.irmodels

import kotlinx.serialization.Serializable

/**
 * Common value types used across CSS property representations in the IR model.
 *
 * ## Purpose
 * These data classes provide type-safe representations of CSS value types.
 * They serve as building blocks for property-specific implementations.
 *
 * ## Design Philosophy
 * - **Precise Type Constraints**: Each type accepts only valid CSS values
 * - **Platform Agnostic**: Represents values in a neutral format
 * - **Fully Serializable**: Can be serialized to/from JSON for IR persistence
 * - **No Validation Logic**: Parsers handle validation before creating these types
 */

/**
 * Represents a length value with unit.
 *
 * CSS lengths can be absolute (px, pt, cm, etc.) or relative (em, rem, %, vw, vh, etc.).
 *
 * ## Examples
 * - `IRLength(16.0, LengthUnit.PX)` represents "16px"
 * - `IRLength(1.5, LengthUnit.EM)` represents "1.5em"
 * - `IRLength(100.0, LengthUnit.PERCENT)` represents "100%"
 *
 * ## Supported Units
 * - **Absolute**: PX, PT, CM, MM, IN, PC
 * - **Relative to font**: EM, REM, EX, CH
 * - **Viewport relative**: VW, VH, VMIN, VMAX
 * - **Percentage**: PERCENT (relative to parent/context)
 * - **Grid**: FR (fraction of available space in CSS Grid)
 * - **Platform**: DP, SP (Android density-independent pixels)
 *
 * @property value The numeric value of the length
 * @property unit The unit of measurement
 */
@Serializable
data class IRLength(
    val value: Double,
    val unit: LengthUnit
) {
    enum class LengthUnit {
        /** Pixels - absolute unit (web: 1px = 1/96th inch, mobile: varies by density) */
        PX,

        /** Density-independent Pixels - Android absolute unit (1dp = 1px at 160dpi) */
        DP,

        /** Scale-independent Pixels - Android font unit (like dp but scales with user font preference) */
        SP,

        /** Relative to element's font-size (e.g., 2em = 2x current font size) */
        EM,

        /** Relative to root element's font-size (typically <html>) */
        REM,

        /** Percentage relative to parent or containing context */
        PERCENT,

        /** 1% of viewport width */
        VW,

        /** 1% of viewport height */
        VH,

        /** Minimum of VW and VH */
        VMIN,

        /** Maximum of VW and VH */
        VMAX,

        /** Points - absolute unit (1pt = 1/72nd inch) */
        PT,

        /** Centimeters - absolute unit */
        CM,

        /** Millimeters - absolute unit */
        MM,

        /** Inches - absolute unit */
        IN,

        /** Picas - absolute unit (1pc = 12pt) */
        PC,

        /** Relative to font's x-height (height of lowercase 'x') */
        EX,

        /** Relative to width of '0' glyph in element's font */
        CH,

        /** Fraction of available space in CSS Grid layout */
        FR
    }
}

/**
 * Represents a color value in various formats.
 *
 * CSS supports multiple color formats: hex, rgb/rgba, hsl/hsla, named colors, and special keywords.
 *
 * ## Examples
 * - `IRColor(ColorRepresentation.Hex("#FF0000"))` represents red in hex
 * - `IRColor(ColorRepresentation.RGB(255, 0, 0, 1.0))` represents red in RGB
 * - `IRColor(ColorRepresentation.HSL(0.0, 100.0, 50.0, 1.0))` represents red in HSL
 * - `IRColor(ColorRepresentation.Named("red"))` represents the named color "red"
 * - `IRColor(ColorRepresentation.CurrentColor())` represents the special "currentColor" keyword
 * - `IRColor(ColorRepresentation.Transparent())` represents transparent
 *
 * @property representation The specific color format and value
 */
@Serializable
data class IRColor(
    val representation: ColorRepresentation
) {
    @Serializable
    sealed interface ColorRepresentation {
        /**
         * Hexadecimal color notation.
         * @property value Hex string including '#' (e.g., "#FF0000" or "#F00")
         */
        @Serializable
        data class Hex(val value: String) : ColorRepresentation

        /**
         * RGB/RGBA color notation.
         * @property r Red channel (0-255)
         * @property g Green channel (0-255)
         * @property b Blue channel (0-255)
         * @property a Alpha/opacity channel (0.0-1.0)
         */
        @Serializable
        data class RGB(val r: Int, val g: Int, val b: Int, val a: Double = 1.0) : ColorRepresentation

        /**
         * HSL/HSLA color notation.
         * @property h Hue in degrees (0-360)
         * @property s Saturation percentage (0-100)
         * @property l Lightness percentage (0-100)
         * @property a Alpha/opacity channel (0.0-1.0)
         */
        @Serializable
        data class HSL(val h: Double, val s: Double, val l: Double, val a: Double = 1.0) : ColorRepresentation

        /**
         * Named color (e.g., "red", "blue", "transparent").
         * @property name CSS color name
         */
        @Serializable
        data class Named(val name: String) : ColorRepresentation

        /**
         * Special keyword "currentColor" - inherits the text color.
         */
        @Serializable
        data class CurrentColor(val unit: Unit = Unit) : ColorRepresentation

        /**
         * Special keyword "transparent" - fully transparent color.
         */
        @Serializable
        data class Transparent(val unit: Unit = Unit) : ColorRepresentation
    }
}

/**
 * Represents a CSS keyword value.
 *
 * Generic keyword type for properties with simple string values.
 * Prefer using enums in specific property classes for type safety.
 *
 * @property value The keyword string (e.g., "auto", "inherit", "initial")
 */
@Serializable
data class IRKeyword(
    val value: String
)

/**
 * Represents a URL reference.
 *
 * Used for properties like background-image, border-image-source, etc.
 *
 * ## Examples
 * - `IRUrl("https://example.com/image.png")` for external resources
 * - `IRUrl("data:image/png;base64,...", isDataUrl = true)` for data URLs
 *
 * @property url The URL string
 * @property isDataUrl True if this is a data URL (base64 encoded data)
 */
@Serializable
data class IRUrl(
    val url: String,
    val isDataUrl: Boolean = false
)

/**
 * Represents a percentage value.
 *
 * Percentages are relative to a reference value (usually parent element's dimension).
 *
 * ## Examples
 * - `IRPercentage(50.0)` represents "50%"
 * - `IRPercentage(100.0)` represents "100%"
 *
 * @property value The percentage value (0-100, though can exceed 100)
 */
@Serializable
data class IRPercentage(
    val value: Double
)

/**
 * Represents a unitless number.
 *
 * Used for properties like opacity, flex-grow, flex-shrink, z-index, line-height, etc.
 *
 * ## Examples
 * - `IRNumber(0.5)` for opacity: 0.5
 * - `IRNumber(1.0)` for flex-grow: 1
 * - `IRNumber(1.5)` for line-height: 1.5
 *
 * @property value The numeric value
 */
@Serializable
data class IRNumber(
    val value: Double
)

/**
 * Represents an angle value with unit.
 *
 * Used for transforms (rotate), gradients, etc.
 *
 * ## Examples
 * - `IRAngle(45.0, AngleUnit.DEG)` represents "45deg"
 * - `IRAngle(3.14159, AngleUnit.RAD)` represents "3.14159rad"
 * - `IRAngle(0.5, AngleUnit.TURN)` represents "0.5turn" (180 degrees)
 *
 * ## Supported Units
 * - **DEG**: Degrees (0-360)
 * - **RAD**: Radians (0-2π)
 * - **GRAD**: Gradians (0-400)
 * - **TURN**: Turns/rotations (0-1 = full circle)
 *
 * @property value The numeric angle value
 * @property unit The angle unit
 */
@Serializable
data class IRAngle(
    val value: Double,
    val unit: AngleUnit
) {
    enum class AngleUnit {
        /** Degrees (360deg = full circle) */
        DEG,

        /** Radians (2π rad = full circle) */
        RAD,

        /** Gradians (400grad = full circle) */
        GRAD,

        /** Turns (1turn = full circle) */
        TURN
    }
}

/**
 * Represents a time duration with unit.
 *
 * Used for animations, transitions, and delays.
 *
 * ## Examples
 * - `IRTime(0.3, TimeUnit.S)` represents "0.3s" (300 milliseconds)
 * - `IRTime(300.0, TimeUnit.MS)` represents "300ms"
 *
 * ## Supported Units
 * - **S**: Seconds
 * - **MS**: Milliseconds
 *
 * @property value The numeric time value
 * @property unit The time unit
 */
@Serializable
data class IRTime(
    val value: Double,
    val unit: TimeUnit
) {
    enum class TimeUnit {
        /** Seconds */
        S,

        /** Milliseconds */
        MS
    }
}
