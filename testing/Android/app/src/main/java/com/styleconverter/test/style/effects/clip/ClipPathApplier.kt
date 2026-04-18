package com.styleconverter.test.style.effects.clip

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.min

/**
 * Applies CSS clip-path styling to Compose modifiers.
 *
 * Converts [ClipPathConfig] shapes into Compose [Shape] implementations
 * and applies them using [Modifier.clip].
 *
 * ## Supported Shapes
 * - Circle: Full support with radius keywords
 * - Ellipse: Full support with radius keywords
 * - Inset: Full support including border-radius
 * - Polygon: Full support for arbitrary polygons
 * - Path: Limited support (fallback to rectangle)
 *
 * ## Compose Implementation
 * Compose's clip modifier uses [Shape] to define clipping regions.
 * Custom shapes are created by implementing [Shape.createOutline].
 */
object ClipPathApplier {

    /**
     * Apply clip-path to modifier.
     *
     * @param modifier The modifier to apply clipping to.
     * @param config The clip-path configuration.
     * @return Modified modifier with clipping applied.
     */
    fun applyClipPath(modifier: Modifier, config: ClipPathConfig): Modifier {
        val shape = config.shape ?: return modifier

        val composeShape = when (shape) {
            is ClipShape.Circle -> createCircleShape(shape)
            is ClipShape.Ellipse -> createEllipseShape(shape)
            is ClipShape.Inset -> createInsetShape(shape)
            is ClipShape.Polygon -> createPolygonShape(shape)
            is ClipShape.Path -> createPathShape(shape)
        }

        return modifier.clip(composeShape)
    }

    /**
     * Create a Compose Shape for circle clip-path.
     */
    private fun createCircleShape(circle: ClipShape.Circle): Shape {
        return object : Shape {
            override fun createOutline(
                size: Size,
                layoutDirection: LayoutDirection,
                density: Density
            ): Outline {
                val centerX = size.width * (circle.centerX / 100f)
                val centerY = size.height * (circle.centerY / 100f)

                val radius = resolveCircleRadius(
                    circle.radius,
                    size,
                    centerX,
                    centerY,
                    density
                )

                val path = Path().apply {
                    addOval(
                        Rect(
                            left = centerX - radius,
                            top = centerY - radius,
                            right = centerX + radius,
                            bottom = centerY + radius
                        )
                    )
                }

                return Outline.Generic(path)
            }
        }
    }

    /**
     * Resolve circle radius based on radius type and element size.
     */
    private fun resolveCircleRadius(
        radius: ClipRadius,
        size: Size,
        centerX: Float,
        centerY: Float,
        density: Density
    ): Float {
        return when (radius) {
            is ClipRadius.Fixed -> with(density) { radius.dp.toPx() }
            is ClipRadius.Percentage -> min(size.width, size.height) * (radius.percent / 100f)
            ClipRadius.ClosestSide -> min(
                min(centerX, size.width - centerX),
                min(centerY, size.height - centerY)
            )
            ClipRadius.FarthestSide -> maxOf(
                maxOf(centerX, size.width - centerX),
                maxOf(centerY, size.height - centerY)
            )
        }
    }

    /**
     * Create a Compose Shape for ellipse clip-path.
     */
    private fun createEllipseShape(ellipse: ClipShape.Ellipse): Shape {
        return object : Shape {
            override fun createOutline(
                size: Size,
                layoutDirection: LayoutDirection,
                density: Density
            ): Outline {
                val centerX = size.width * (ellipse.centerX / 100f)
                val centerY = size.height * (ellipse.centerY / 100f)

                val radiusX = resolveEllipseRadius(
                    ellipse.radiusX,
                    size,
                    centerX,
                    centerY,
                    density,
                    isHorizontal = true
                )
                val radiusY = resolveEllipseRadius(
                    ellipse.radiusY,
                    size,
                    centerX,
                    centerY,
                    density,
                    isHorizontal = false
                )

                val path = Path().apply {
                    addOval(
                        Rect(
                            left = centerX - radiusX,
                            top = centerY - radiusY,
                            right = centerX + radiusX,
                            bottom = centerY + radiusY
                        )
                    )
                }

                return Outline.Generic(path)
            }
        }
    }

    /**
     * Resolve ellipse radius based on radius type, axis, and element size.
     */
    private fun resolveEllipseRadius(
        radius: ClipRadius,
        size: Size,
        centerX: Float,
        centerY: Float,
        density: Density,
        isHorizontal: Boolean
    ): Float {
        return when (radius) {
            is ClipRadius.Fixed -> with(density) { radius.dp.toPx() }
            is ClipRadius.Percentage -> {
                if (isHorizontal) size.width * (radius.percent / 100f)
                else size.height * (radius.percent / 100f)
            }
            ClipRadius.ClosestSide -> {
                if (isHorizontal) min(centerX, size.width - centerX)
                else min(centerY, size.height - centerY)
            }
            ClipRadius.FarthestSide -> {
                if (isHorizontal) maxOf(centerX, size.width - centerX)
                else maxOf(centerY, size.height - centerY)
            }
        }
    }

    /**
     * Create a Compose Shape for inset clip-path.
     */
    private fun createInsetShape(inset: ClipShape.Inset): Shape {
        return object : Shape {
            override fun createOutline(
                size: Size,
                layoutDirection: LayoutDirection,
                density: Density
            ): Outline {
                val top = with(density) { inset.top.toPx() }
                val right = with(density) { inset.right.toPx() }
                val bottom = with(density) { inset.bottom.toPx() }
                val left = with(density) { inset.left.toPx() }
                val radius = with(density) { inset.borderRadius.toPx() }

                val path = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(
                                left = left,
                                top = top,
                                right = size.width - right,
                                bottom = size.height - bottom
                            ),
                            radiusX = radius,
                            radiusY = radius
                        )
                    )
                }

                return Outline.Generic(path)
            }
        }
    }

    /**
     * Create a Compose Shape for polygon clip-path.
     */
    private fun createPolygonShape(polygon: ClipShape.Polygon): Shape {
        return object : Shape {
            override fun createOutline(
                size: Size,
                layoutDirection: LayoutDirection,
                density: Density
            ): Outline {
                val path = Path().apply {
                    if (polygon.points.isNotEmpty()) {
                        val first = polygon.points.first()
                        moveTo(
                            size.width * (first.first / 100f),
                            size.height * (first.second / 100f)
                        )

                        polygon.points.drop(1).forEach { (x, y) ->
                            lineTo(
                                size.width * (x / 100f),
                                size.height * (y / 100f)
                            )
                        }

                        close()
                    }
                }

                return Outline.Generic(path)
            }
        }
    }

    /**
     * Create a Compose Shape for SVG path clip-path.
     *
     * Parses SVG path data string (d attribute) into a Compose Path.
     * Supports all standard SVG path commands: M, L, H, V, C, S, Q, T, A, Z.
     */
    private fun createPathShape(pathShape: ClipShape.Path): Shape {
        return object : Shape {
            override fun createOutline(
                size: Size,
                layoutDirection: LayoutDirection,
                density: Density
            ): Outline {
                // Parse the SVG path data
                val parsedPath = SvgPathParser.parse(pathShape.svgPath)

                if (parsedPath != null) {
                    // Scale the path to fit the element size if needed
                    // SVG paths are often defined in a viewBox coordinate system
                    return Outline.Generic(parsedPath)
                }

                // Fallback: return full rectangle (no clipping)
                val fallbackPath = Path().apply {
                    addRect(Rect(0f, 0f, size.width, size.height))
                }
                return Outline.Generic(fallbackPath)
            }
        }
    }

    /**
     * Create a clip shape directly from configuration.
     *
     * Useful when you need the Shape without applying it to a modifier.
     *
     * @param config The clip-path configuration.
     * @return The Compose Shape, or null if no clip path is configured.
     */
    fun createShape(config: ClipPathConfig): Shape? {
        val shape = config.shape ?: return null

        return when (shape) {
            is ClipShape.Circle -> createCircleShape(shape)
            is ClipShape.Ellipse -> createEllipseShape(shape)
            is ClipShape.Inset -> createInsetShape(shape)
            is ClipShape.Polygon -> createPolygonShape(shape)
            is ClipShape.Path -> createPathShape(shape)
        }
    }
}
