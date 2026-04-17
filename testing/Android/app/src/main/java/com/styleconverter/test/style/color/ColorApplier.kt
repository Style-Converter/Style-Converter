package com.styleconverter.test.style.color

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import com.styleconverter.test.style.background.RepeatingGradientHelper
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Applies color-related styling to Compose Modifiers.
 *
 * ## Supported Features
 * - Solid background colors
 * - Linear gradients (including repeating)
 * - Radial gradients (including repeating)
 * - Conic/sweep gradients (repeating not fully supported)
 * - Opacity/alpha
 *
 * ## Limitations
 * - Multiple background layers: Only the first gradient is applied
 * - Image URLs: Not rendered (use Coil or similar for image loading)
 * - Repeating conic gradients: Compose sweepGradient doesn't support TileMode
 * - Gradient sizing: Uses approximated coordinates; actual size requires DrawScope
 *
 * ## Usage
 * ```kotlin
 * val config = ColorExtractor.extractColorConfig(properties)
 * val modifier = ColorApplier.applyColors(Modifier, config)
 * ```
 */
object ColorApplier {

    /**
     * Apply all color-related properties to a Modifier.
     *
     * Order of application:
     * 1. Background images (gradients) - drawn first (bottom layer)
     * 2. Solid background color - drawn on top of gradients
     * 3. Opacity - applied to the entire result
     *
     * @param modifier The base modifier to extend
     * @param config ColorConfig containing all color properties
     * @return Modified Modifier with color properties applied
     */
    fun applyColors(modifier: Modifier, config: ColorConfig): Modifier {
        var result = modifier

        // Apply background images (gradients) first - they form the bottom layer
        config.backgroundImages.forEach { bgImage ->
            result = applyBackgroundImage(result, bgImage, config)
        }

        // Apply solid background color on top of gradients
        config.backgroundColor?.let { color ->
            result = result.background(color)
        }

        // Apply opacity last - affects the entire element
        config.opacity?.let { alpha ->
            result = result.alpha(alpha.coerceIn(0f, 1f))
        }

        return result
    }

    /**
     * Apply a single background color to a Modifier.
     *
     * @param modifier The base modifier
     * @param color The background color
     * @return Modified Modifier with background color
     */
    fun applyBackgroundColor(modifier: Modifier, color: Color): Modifier {
        return modifier.background(color)
    }

    /**
     * Apply opacity to a Modifier.
     *
     * @param modifier The base modifier
     * @param alpha Opacity value (0.0-1.0)
     * @return Modified Modifier with alpha applied
     */
    fun applyOpacity(modifier: Modifier, alpha: Float): Modifier {
        return modifier.alpha(alpha.coerceIn(0f, 1f))
    }

    /**
     * Apply a background image (gradient or URL) to a Modifier.
     *
     * @param modifier Base modifier
     * @param image Background image config
     * @param config Full color config
     * @param size Optional size for gradient calculations
     */
    private fun applyBackgroundImage(
        modifier: Modifier,
        image: BackgroundImageConfig,
        config: ColorConfig,
        size: Size = Size(500f, 500f)
    ): Modifier {
        // Determine TileMode based on background-repeat
        val tileMode = when (config.backgroundRepeat) {
            BackgroundRepeatConfig.REPEAT,
            BackgroundRepeatConfig.REPEAT_X,
            BackgroundRepeatConfig.REPEAT_Y -> TileMode.Repeated
            BackgroundRepeatConfig.NO_REPEAT -> TileMode.Clamp
            BackgroundRepeatConfig.SPACE -> TileMode.Decal
            BackgroundRepeatConfig.ROUND -> TileMode.Repeated
        }

        return when (image) {
            is BackgroundImageConfig.LinearGradient -> {
                val brush = if (image.repeating) {
                    // Use the RepeatingGradientHelper for better repeating behavior
                    RepeatingGradientHelper.createRepeatingLinearGradient(
                        angle = image.angle,
                        colorStops = image.colorStops,
                        size = size
                    )
                } else {
                    createLinearGradientBrush(image, config.backgroundPosition, tileMode)
                }
                if (brush != null) modifier.background(brush) else modifier
            }
            is BackgroundImageConfig.RadialGradient -> {
                val brush = if (image.repeating) {
                    // Use the RepeatingGradientHelper for better repeating behavior
                    RepeatingGradientHelper.createRepeatingRadialGradient(
                        centerX = image.centerX,
                        centerY = image.centerY,
                        colorStops = image.colorStops,
                        size = size
                    )
                } else {
                    createRadialGradientBrush(image, config.backgroundPosition, tileMode)
                }
                if (brush != null) modifier.background(brush) else modifier
            }
            is BackgroundImageConfig.ConicGradient -> {
                val brush = if (image.repeating) {
                    // For repeating conic, we manually expand color stops
                    RepeatingGradientHelper.createRepeatingConicGradient(
                        centerX = image.centerX,
                        centerY = image.centerY,
                        startAngle = image.angle,
                        colorStops = image.colorStops,
                        size = size
                    )
                } else {
                    createSweepGradientBrush(image, config.backgroundPosition)
                }
                if (brush != null) modifier.background(brush) else modifier
            }
            is BackgroundImageConfig.Url -> modifier // Image URLs handled separately via BackgroundBoxApplier
            is BackgroundImageConfig.None -> modifier
        }
    }

    /**
     * Create a linear gradient Brush from configuration.
     *
     * CSS angle conversion:
     * - CSS: 0deg = to top (upward), 90deg = to right
     * - Compose: Uses start/end offsets
     *
     * @param gradient LinearGradient configuration
     * @param position Background position configuration
     * @param tileMode Tile mode for repeating
     * @return Brush for the gradient, or null if invalid
     */
    private fun createLinearGradientBrush(
        gradient: BackgroundImageConfig.LinearGradient,
        position: BackgroundPositionConfig = BackgroundPositionConfig(),
        tileMode: TileMode = TileMode.Clamp
    ): Brush? {
        if (gradient.colorStops.size < 2) return null

        // Convert CSS angle to radians
        // CSS: 0deg = to top, 90deg = to right
        // We need to convert to Compose coordinate system
        val angleRad = (90 - gradient.angle) * PI.toFloat() / 180f

        // Calculate gradient length for repeating gradients
        val gradientLength = if (tileMode == TileMode.Repeated) {
            // Use the last explicit position to determine repeat length
            val maxPos = gradient.colorStops.maxOfOrNull { it.position } ?: 1f
            maxPos.coerceIn(0.01f, 1f) * 500f
        } else {
            1000f // Large enough to cover most elements
        }

        // Apply background position offset
        val offsetX = position.x * gradientLength
        val offsetY = position.y * gradientLength

        // Calculate start and end points (normalized 0-1)
        val startX = (0.5f - cos(angleRad) * 0.5f) * gradientLength + offsetX * 0.1f
        val startY = (0.5f + sin(angleRad) * 0.5f) * gradientLength + offsetY * 0.1f
        val endX = (0.5f + cos(angleRad) * 0.5f) * gradientLength + offsetX * 0.1f
        val endY = (0.5f - sin(angleRad) * 0.5f) * gradientLength + offsetY * 0.1f

        // Build color stops array for Brush
        val colorStops = gradient.colorStops.map { it.position to it.color }.toTypedArray()

        return Brush.linearGradient(
            colorStops = colorStops,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            tileMode = tileMode
        )
    }

    /**
     * Create a radial gradient Brush from configuration.
     *
     * @param gradient RadialGradient configuration
     * @param position Background position configuration
     * @param tileMode Tile mode for repeating
     * @return Brush for the gradient, or null if invalid
     */
    private fun createRadialGradientBrush(
        gradient: BackgroundImageConfig.RadialGradient,
        position: BackgroundPositionConfig = BackgroundPositionConfig(),
        tileMode: TileMode = TileMode.Clamp
    ): Brush? {
        if (gradient.colorStops.size < 2) return null

        // Calculate radius for repeating gradients
        val radius = if (tileMode == TileMode.Repeated) {
            val maxPos = gradient.colorStops.maxOfOrNull { it.position } ?: 1f
            maxPos.coerceIn(0.01f, 1f) * 100f
        } else {
            Float.POSITIVE_INFINITY // Default: fill the shape
        }

        // Build color stops array
        val colorStops = gradient.colorStops.map { it.position to it.color }.toTypedArray()

        // Use gradient's center position, or fall back to background position
        val centerX = if (gradient.centerX != 0.5f) gradient.centerX else position.x
        val centerY = if (gradient.centerY != 0.5f) gradient.centerY else position.y

        // Note: For proper centering we'd need size, so use Unspecified for now
        // unless explicit positioning is provided
        val center = if (centerX == 0.5f && centerY == 0.5f) {
            Offset.Unspecified
        } else {
            // Approximate with a large size assumption
            Offset(centerX * 500f, centerY * 500f)
        }

        return Brush.radialGradient(
            colorStops = colorStops,
            center = center,
            radius = radius,
            tileMode = tileMode
        )
    }

    /**
     * Create a sweep (conic) gradient Brush from configuration.
     *
     * Note: Compose's sweepGradient does not support:
     * - TileMode (repeating gradients)
     * - Starting angle offset
     *
     * @param gradient ConicGradient configuration
     * @param position Background position configuration
     * @return Brush for the gradient, or null if invalid
     */
    private fun createSweepGradientBrush(
        gradient: BackgroundImageConfig.ConicGradient,
        position: BackgroundPositionConfig = BackgroundPositionConfig()
    ): Brush? {
        if (gradient.colorStops.size < 2) return null

        // Build color stops array
        val colorStops = gradient.colorStops.map { it.position to it.color }.toTypedArray()

        // Use gradient's center position, or fall back to background position
        val centerX = if (gradient.centerX != 0.5f) gradient.centerX else position.x
        val centerY = if (gradient.centerY != 0.5f) gradient.centerY else position.y

        // Note: sweepGradient doesn't support starting angle or TileMode
        val center = if (centerX == 0.5f && centerY == 0.5f) {
            Offset.Unspecified
        } else {
            Offset(centerX * 500f, centerY * 500f)
        }

        return Brush.sweepGradient(
            colorStops = colorStops,
            center = center
        )
    }
}
