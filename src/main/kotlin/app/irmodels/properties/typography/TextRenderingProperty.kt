package app.irmodels.properties.typography

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `text-rendering` property.
 *
 * ## CSS Property
 * **Syntax**: `text-rendering: auto | optimizeSpeed | optimizeLegibility | geometricPrecision`
 *
 * ## Description
 * Provides information to the rendering engine about what to optimize for when rendering text.
 * This is a hint to the browser and may not be respected.
 *
 * ## Examples
 * ```kotlin
 * TextRenderingProperty(rendering = TextRendering.Auto)
 * TextRenderingProperty(rendering = TextRendering.OptimizeLegibility)
 * TextRenderingProperty(rendering = TextRendering.GeometricPrecision)
 * ```
 *
 * ## Platform Support
 * - **CSS**: Full support (rendering hint)
 * - **Compose**: No explicit control
 * - **SwiftUI**: No explicit control
 *
 * @property rendering The text rendering optimization hint
 * @see [MDN text-rendering](https://developer.mozilla.org/en-US/docs/Web/CSS/text-rendering)
 */
@Serializable
data class TextRenderingProperty(
    val rendering: TextRendering
) : IRProperty {
    override val propertyName = "text-rendering"
}

/**
 * Represents text-rendering values.
 */
@Serializable
enum class TextRendering {
    /**
     * Browser makes educated guesses about when to optimize for speed,
     * legibility, and geometric precision.
     */
    AUTO,

    /**
     * Browser emphasizes rendering speed over legibility and geometric precision.
     * Disables kerning and ligatures.
     */
    OPTIMIZE_SPEED,

    /**
     * Browser emphasizes legibility over rendering speed and geometric precision.
     * Enables kerning and optional ligatures.
     */
    OPTIMIZE_LEGIBILITY,

    /**
     * Browser emphasizes geometric precision over rendering speed and legibility.
     * Useful for SVG text that should scale smoothly.
     */
    GEOMETRIC_PRECISION
}
