package com.styleconverter.test.style.appearance.effects.clip

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Configuration for CSS clip-path property.
 *
 * Clip-path creates a clipping region that determines which parts
 * of an element are visible. Parts outside the clipping region are hidden.
 *
 * ## Supported Shapes
 * - [ClipShape.Circle] - Circular clipping region
 * - [ClipShape.Ellipse] - Elliptical clipping region
 * - [ClipShape.Inset] - Rectangular inset with optional border-radius
 * - [ClipShape.Polygon] - Arbitrary polygon defined by points
 * - [ClipShape.Path] - SVG path data (limited support)
 *
 * ## Example
 * ```kotlin
 * val config = ClipPathConfig(
 *     shape = ClipShape.Circle(
 *         radius = ClipRadius.Percentage(50f),
 *         centerX = 50f,
 *         centerY = 50f
 *     )
 * )
 * ```
 */
data class ClipPathConfig(
    val shape: ClipShape? = null
) {
    /** True if there is a clip path to apply. */
    val hasClipPath: Boolean get() = shape != null
}

/**
 * Sealed interface representing CSS clip-path shape functions.
 *
 * Each shape maps to a CSS basic-shape function:
 * - `circle()` -> [Circle]
 * - `ellipse()` -> [Ellipse]
 * - `inset()` -> [Inset]
 * - `polygon()` -> [Polygon]
 * - `path()` -> [Path]
 */
sealed interface ClipShape {

    /**
     * Circular clipping region.
     *
     * CSS: `clip-path: circle(radius at centerX centerY)`
     *
     * @param radius The radius of the circle (fixed, percentage, or keyword)
     * @param centerX Horizontal center position as percentage (0-100)
     * @param centerY Vertical center position as percentage (0-100)
     */
    data class Circle(
        val radius: ClipRadius = ClipRadius.ClosestSide,
        val centerX: Float = 50f,
        val centerY: Float = 50f
    ) : ClipShape

    /**
     * Elliptical clipping region.
     *
     * CSS: `clip-path: ellipse(radiusX radiusY at centerX centerY)`
     *
     * @param radiusX Horizontal radius
     * @param radiusY Vertical radius
     * @param centerX Horizontal center position as percentage (0-100)
     * @param centerY Vertical center position as percentage (0-100)
     */
    data class Ellipse(
        val radiusX: ClipRadius = ClipRadius.ClosestSide,
        val radiusY: ClipRadius = ClipRadius.ClosestSide,
        val centerX: Float = 50f,
        val centerY: Float = 50f
    ) : ClipShape

    /**
     * Rectangular inset clipping region with optional border-radius.
     *
     * CSS: `clip-path: inset(top right bottom left round radius)`
     *
     * @param top Inset from top edge
     * @param right Inset from right edge
     * @param bottom Inset from bottom edge
     * @param left Inset from left edge
     * @param borderRadius Corner radius for rounded rectangle
     */
    data class Inset(
        val top: Dp = 0.dp,
        val right: Dp = 0.dp,
        val bottom: Dp = 0.dp,
        val left: Dp = 0.dp,
        val borderRadius: Dp = 0.dp
    ) : ClipShape

    /**
     * Polygon clipping region defined by a series of points.
     *
     * CSS: `clip-path: polygon(x1 y1, x2 y2, ...)`
     *
     * @param points List of (x%, y%) coordinate pairs (0-100 range)
     */
    data class Polygon(
        val points: List<Pair<Float, Float>>
    ) : ClipShape

    /**
     * SVG path clipping region.
     *
     * CSS: `clip-path: path('M...')`
     *
     * Note: Full SVG path parsing is complex. This is a simplified
     * implementation that may not support all path commands.
     *
     * @param svgPath SVG path data string
     */
    data class Path(
        val svgPath: String
    ) : ClipShape
}

/**
 * Represents a radius value for circle/ellipse clip shapes.
 *
 * CSS clip-path radius can be:
 * - Fixed length (e.g., `50px`)
 * - Percentage of reference box (e.g., `50%`)
 * - Keyword (`closest-side` or `farthest-side`)
 */
sealed interface ClipRadius {

    /**
     * Fixed length radius.
     * @param dp The radius in density-independent pixels
     */
    data class Fixed(val dp: Dp) : ClipRadius

    /**
     * Percentage radius relative to the reference box.
     * @param percent Percentage value (0-100)
     */
    data class Percentage(val percent: Float) : ClipRadius

    /**
     * Radius extends to the closest side of the reference box.
     * For circle: minimum of distances to all sides from center.
     * For ellipse: minimum horizontal/vertical distance separately.
     */
    data object ClosestSide : ClipRadius

    /**
     * Radius extends to the farthest side of the reference box.
     * For circle: maximum of distances to all sides from center.
     * For ellipse: maximum horizontal/vertical distance separately.
     */
    data object FarthestSide : ClipRadius
}
