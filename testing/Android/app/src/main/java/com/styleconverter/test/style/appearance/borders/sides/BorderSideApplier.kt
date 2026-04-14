package com.styleconverter.test.style.appearance.borders.sides

import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.styleconverter.test.style.core.types.ValueExtractors.LineStyle

/**
 * Applies border styling to a Modifier.
 *
 * For uniform borders, uses the simple Modifier.border().
 * For non-uniform borders, draws each side individually using drawBehind.
 */
object BorderSideApplier {

    /**
     * Apply border styling based on the configuration.
     *
     * @param modifier The modifier to apply borders to.
     * @param config The border configuration for all sides.
     * @return Modified modifier with borders applied.
     */
    fun applyBorders(modifier: Modifier, config: AllBordersConfig): Modifier {
        if (!config.hasBorders) return modifier

        // For uniform SOLID borders, use simple border modifier (fast path)
        // Non-solid styles (dashed, dotted) must use the drawBehind path with PathEffect
        val topStyle = config.top.style ?: LineStyle.SOLID
        if (config.isUniform && config.top.hasBorder && topStyle == LineStyle.SOLID) {
            val width = config.top.width ?: 1.dp
            val color = config.top.color ?: Color.Black
            return modifier.border(width, color)
        }

        // For non-uniform borders, draw each side individually
        return modifier.drawBehind {
            val topWidth = config.top.width?.toPx() ?: 0f
            val endWidth = config.end.width?.toPx() ?: 0f
            val bottomWidth = config.bottom.width?.toPx() ?: 0f
            val startWidth = config.start.width?.toPx() ?: 0f

            // Draw top border
            if (topWidth > 0 && config.top.style != LineStyle.NONE && config.top.style != LineStyle.HIDDEN) {
                val style = config.top.style ?: LineStyle.SOLID
                val pathEffect = getPathEffect(style, topWidth)
                val cap = if (style == LineStyle.DOTTED) StrokeCap.Round else StrokeCap.Butt

                if (style == LineStyle.DOUBLE && topWidth >= 3) {
                    drawDoubleLine(
                        color = config.top.color ?: Color.Black,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        totalWidth = topWidth,
                        isHorizontal = true
                    )
                } else {
                    drawLine(
                        color = config.top.color ?: Color.Black,
                        start = Offset(0f, topWidth / 2),
                        end = Offset(size.width, topWidth / 2),
                        strokeWidth = topWidth,
                        pathEffect = pathEffect,
                        cap = cap
                    )
                }
            }

            // Draw end (right) border
            if (endWidth > 0 && config.end.style != LineStyle.NONE && config.end.style != LineStyle.HIDDEN) {
                val style = config.end.style ?: LineStyle.SOLID
                val pathEffect = getPathEffect(style, endWidth)
                val cap = if (style == LineStyle.DOTTED) StrokeCap.Round else StrokeCap.Butt

                if (style == LineStyle.DOUBLE && endWidth >= 3) {
                    drawDoubleLine(
                        color = config.end.color ?: Color.Black,
                        start = Offset(size.width, 0f),
                        end = Offset(size.width, size.height),
                        totalWidth = endWidth,
                        isHorizontal = false
                    )
                } else {
                    drawLine(
                        color = config.end.color ?: Color.Black,
                        start = Offset(size.width - endWidth / 2, 0f),
                        end = Offset(size.width - endWidth / 2, size.height),
                        strokeWidth = endWidth,
                        pathEffect = pathEffect,
                        cap = cap
                    )
                }
            }

            // Draw bottom border
            if (bottomWidth > 0 && config.bottom.style != LineStyle.NONE && config.bottom.style != LineStyle.HIDDEN) {
                val style = config.bottom.style ?: LineStyle.SOLID
                val pathEffect = getPathEffect(style, bottomWidth)
                val cap = if (style == LineStyle.DOTTED) StrokeCap.Round else StrokeCap.Butt

                if (style == LineStyle.DOUBLE && bottomWidth >= 3) {
                    drawDoubleLine(
                        color = config.bottom.color ?: Color.Black,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        totalWidth = bottomWidth,
                        isHorizontal = true
                    )
                } else {
                    drawLine(
                        color = config.bottom.color ?: Color.Black,
                        start = Offset(0f, size.height - bottomWidth / 2),
                        end = Offset(size.width, size.height - bottomWidth / 2),
                        strokeWidth = bottomWidth,
                        pathEffect = pathEffect,
                        cap = cap
                    )
                }
            }

            // Draw start (left) border
            if (startWidth > 0 && config.start.style != LineStyle.NONE && config.start.style != LineStyle.HIDDEN) {
                val style = config.start.style ?: LineStyle.SOLID
                val pathEffect = getPathEffect(style, startWidth)
                val cap = if (style == LineStyle.DOTTED) StrokeCap.Round else StrokeCap.Butt

                if (style == LineStyle.DOUBLE && startWidth >= 3) {
                    drawDoubleLine(
                        color = config.start.color ?: Color.Black,
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        totalWidth = startWidth,
                        isHorizontal = false
                    )
                } else {
                    drawLine(
                        color = config.start.color ?: Color.Black,
                        start = Offset(startWidth / 2, 0f),
                        end = Offset(startWidth / 2, size.height),
                        strokeWidth = startWidth,
                        pathEffect = pathEffect,
                        cap = cap
                    )
                }
            }
        }
    }

    /**
     * Get PathEffect for dashed and dotted styles.
     */
    private fun getPathEffect(style: LineStyle, width: Float): PathEffect? {
        return when (style) {
            LineStyle.DASHED -> PathEffect.dashPathEffect(floatArrayOf(width * 3, width * 2))
            LineStyle.DOTTED -> PathEffect.dashPathEffect(floatArrayOf(width, width))
            else -> null
        }
    }

    /**
     * Draw a double line (two lines with a gap between them).
     */
    private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDoubleLine(
        color: Color,
        start: Offset,
        end: Offset,
        totalWidth: Float,
        isHorizontal: Boolean
    ) {
        val lineWidth = totalWidth / 3

        if (isHorizontal) {
            // First line (outer)
            drawLine(
                color = color,
                start = Offset(start.x, start.y + lineWidth / 2),
                end = Offset(end.x, end.y + lineWidth / 2),
                strokeWidth = lineWidth,
                cap = StrokeCap.Butt
            )
            // Second line (inner)
            drawLine(
                color = color,
                start = Offset(start.x, start.y + totalWidth - lineWidth / 2),
                end = Offset(end.x, end.y + totalWidth - lineWidth / 2),
                strokeWidth = lineWidth,
                cap = StrokeCap.Butt
            )
        } else {
            // First line (outer)
            drawLine(
                color = color,
                start = Offset(start.x + lineWidth / 2, start.y),
                end = Offset(end.x + lineWidth / 2, end.y),
                strokeWidth = lineWidth,
                cap = StrokeCap.Butt
            )
            // Second line (inner)
            drawLine(
                color = color,
                start = Offset(start.x + totalWidth - lineWidth / 2, start.y),
                end = Offset(end.x + totalWidth - lineWidth / 2, end.y),
                strokeWidth = lineWidth,
                cap = StrokeCap.Butt
            )
        }
    }
}
