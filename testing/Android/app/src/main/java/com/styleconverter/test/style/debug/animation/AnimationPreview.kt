package com.styleconverter.test.style.debug.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.styleconverter.test.style.interactive.animations.AnimationConfig
import com.styleconverter.test.style.interactive.animations.AnimationDirection
import com.styleconverter.test.style.interactive.animations.AnimationFillMode
import com.styleconverter.test.style.interactive.animations.AnimationIterationCount
import com.styleconverter.test.style.interactive.animations.AnimationPlayState
import com.styleconverter.test.style.interactive.animations.CSSKeyframes
import com.styleconverter.test.style.interactive.animations.Keyframe
import com.styleconverter.test.style.interactive.animations.TimingFunctionConfig
import kotlin.math.pow

/**
 * Simplified keyframe config for preview purposes.
 * Bridges between our preview system and the actual CSSKeyframes model.
 */
data class KeyframeConfig(
    val name: String,
    val frames: List<KeyframeFrame>
) {
    companion object {
        fun fromCSSKeyframes(keyframes: CSSKeyframes): KeyframeConfig {
            return KeyframeConfig(
                name = keyframes.name,
                frames = keyframes.keyframes.map { kf ->
                    KeyframeFrame(
                        percentage = kf.percentage,
                        properties = buildMap {
                            kf.opacity?.let { put("opacity", it.toString()) }
                            kf.translateX?.let { put("translateX", it.toString()) }
                            kf.translateY?.let { put("translateY", it.toString()) }
                            kf.rotateZ?.let { put("rotate", it.toString()) }
                            kf.scaleX?.let { put("scale", it.toString()) }
                        }
                    )
                }
            )
        }
    }
}

data class KeyframeFrame(
    val percentage: Float,
    val properties: Map<String, String>
)

/**
 * Animation preview system for debugging CSS animations in Compose.
 *
 * ## Features
 * - Visual preview of animations with real-time controls
 * - Timeline scrubbing to inspect specific frames
 * - Timing function curve visualization
 * - Keyframe breakdown display
 * - Play/pause/restart controls
 * - Speed adjustment
 *
 * ## Usage
 * ```kotlin
 * AnimationPreview.AnimationDebugPanel(
 *     config = animationConfig,
 *     keyframes = keyframeConfig
 * )
 * ```
 */
object AnimationPreview {

    /**
     * Complete animation debug panel with all controls.
     * Accepts CSSKeyframes for compatibility.
     */
    @Composable
    fun AnimationDebugPanel(
        config: AnimationConfig,
        cssKeyframes: CSSKeyframes?,
        modifier: Modifier = Modifier
    ) {
        AnimationDebugPanel(
            config = config,
            keyframes = cssKeyframes?.let { KeyframeConfig.fromCSSKeyframes(it) },
            modifier = modifier
        )
    }

    /**
     * Complete animation debug panel with all controls.
     */
    @Composable
    fun AnimationDebugPanel(
        config: AnimationConfig,
        keyframes: KeyframeConfig? = null,
        modifier: Modifier = Modifier
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Animation Preview",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Animation Info Card
            AnimationInfoCard(config)

            Spacer(modifier = Modifier.height(16.dp))

            // Live Preview with Controls
            AnimationPreviewWithControls(config, keyframes)

            Spacer(modifier = Modifier.height(16.dp))

            // Timing Function Visualization
            if (config.timingFunctions.isNotEmpty()) {
                TimingFunctionCard(config.getTimingFunction(0))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Keyframe Breakdown
            if (keyframes != null) {
                KeyframeBreakdownCard(keyframes)
            }
        }
    }

    /**
     * Card showing animation configuration info.
     */
    @Composable
    fun AnimationInfoCard(config: AnimationConfig) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Configuration", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(8.dp))

                InfoRow("Names", config.names.joinToString(", ").ifEmpty { "none" })
                InfoRow("Duration", "${config.getDuration(0)}ms")
                InfoRow("Delay", "${config.getDelay(0)}ms")
                InfoRow("Timing", config.getTimingFunction(0).original ?: "ease")
                InfoRow("Iterations", when (val count = config.getIterationCount(0)) {
                    is AnimationIterationCount.Infinite -> "infinite"
                    is AnimationIterationCount.Count -> count.value.toString()
                })
                InfoRow("Direction", config.getDirection(0).name.lowercase())
                InfoRow("Fill Mode", config.getFillMode(0).name.lowercase())
                InfoRow("Play State", config.getPlayState(0).name.lowercase())
            }
        }
    }

    @Composable
    private fun InfoRow(label: String, value: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }

    /**
     * Live animation preview with playback controls.
     */
    @Composable
    fun AnimationPreviewWithControls(
        config: AnimationConfig,
        keyframes: KeyframeConfig? = null
    ) {
        var isPlaying by remember { mutableStateOf(true) }
        var speed by remember { mutableFloatStateOf(1f) }
        var manualProgress by remember { mutableFloatStateOf(0f) }
        var useManualProgress by remember { mutableStateOf(false) }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Live Preview", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(16.dp))

                // Preview Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPlaying && !useManualProgress) {
                        AnimatedPreviewElement(
                            config = config,
                            keyframes = keyframes,
                            speed = speed
                        )
                    } else {
                        ManualPreviewElement(
                            progress = if (useManualProgress) manualProgress else 0f,
                            keyframes = keyframes
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Timeline Scrubber
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Timeline", modifier = Modifier.width(80.dp))
                    Slider(
                        value = manualProgress,
                        onValueChange = {
                            manualProgress = it
                            useManualProgress = true
                            isPlaying = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Text("${(manualProgress * 100).toInt()}%", modifier = Modifier.width(50.dp))
                }

                // Speed Control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Speed", modifier = Modifier.width(80.dp))
                    Slider(
                        value = speed,
                        onValueChange = { speed = it },
                        valueRange = 0.1f..3f,
                        modifier = Modifier.weight(1f)
                    )
                    Text("${String.format("%.1f", speed)}x", modifier = Modifier.width(50.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Play Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            isPlaying = !isPlaying
                            useManualProgress = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isPlaying) "Pause" else "Play")
                    }

                    Button(
                        onClick = {
                            manualProgress = 0f
                            useManualProgress = false
                            isPlaying = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Restart")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Manual Control")
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = useManualProgress,
                        onCheckedChange = {
                            useManualProgress = it
                            if (it) isPlaying = false
                        }
                    )
                }
            }
        }
    }

    /**
     * Animated preview element that applies keyframe animations.
     */
    @Composable
    private fun AnimatedPreviewElement(
        config: AnimationConfig,
        keyframes: KeyframeConfig?,
        speed: Float
    ) {
        val duration = (config.getDuration(0) / speed).toInt()
        val repeatMode = when (config.getDirection(0)) {
            AnimationDirection.ALTERNATE, AnimationDirection.ALTERNATE_REVERSE -> RepeatMode.Reverse
            else -> RepeatMode.Restart
        }

        val infiniteTransition = rememberInfiniteTransition(label = "preview")

        val progress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = duration.coerceAtLeast(100),
                    easing = LinearEasing
                ),
                repeatMode = repeatMode
            ),
            label = "progress"
        )

        PreviewElement(progress, keyframes)
    }

    /**
     * Manual preview element controlled by slider.
     */
    @Composable
    private fun ManualPreviewElement(
        progress: Float,
        keyframes: KeyframeConfig?
    ) {
        PreviewElement(progress, keyframes)
    }

    /**
     * The actual preview element with transforms applied.
     */
    @Composable
    private fun PreviewElement(
        progress: Float,
        keyframes: KeyframeConfig?
    ) {
        // Interpolate transform values based on progress
        val rotation = interpolateKeyframeValue(progress, keyframes, "rotate") ?: (progress * 360f)
        val scale = interpolateKeyframeValue(progress, keyframes, "scale") ?: 1f
        val translateX = interpolateKeyframeValue(progress, keyframes, "translateX") ?: 0f
        val translateY = interpolateKeyframeValue(progress, keyframes, "translateY") ?: 0f
        val opacity = interpolateKeyframeValue(progress, keyframes, "opacity") ?: 1f
        val backgroundColor = interpolateColor(progress, keyframes)

        Box(
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer {
                    rotationZ = rotation
                    scaleX = scale
                    scaleY = scale
                    translationX = translateX
                    translationY = translateY
                    alpha = opacity
                }
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor)
                .border(2.dp, Color.DarkGray, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${(progress * 100).toInt()}%",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    /**
     * Interpolate a numeric value from keyframes.
     */
    private fun interpolateKeyframeValue(
        progress: Float,
        keyframes: KeyframeConfig?,
        property: String
    ): Float? {
        if (keyframes == null) return null

        val frames = keyframes.frames.sortedBy { it.percentage }
        if (frames.isEmpty()) return null

        val targetPercent = progress * 100f

        // Find surrounding keyframes
        var fromFrame = frames.first()
        var toFrame = frames.last()

        for (i in 0 until frames.size - 1) {
            if (frames[i].percentage <= targetPercent && frames[i + 1].percentage >= targetPercent) {
                fromFrame = frames[i]
                toFrame = frames[i + 1]
                break
            }
        }

        val fromValue = fromFrame.properties[property]?.toFloatOrNull() ?: return null
        val toValue = toFrame.properties[property]?.toFloatOrNull() ?: return null

        val rangePercent = toFrame.percentage - fromFrame.percentage
        if (rangePercent <= 0) return fromValue

        val localProgress = (targetPercent - fromFrame.percentage) / rangePercent

        return fromValue + (toValue - fromValue) * localProgress
    }

    /**
     * Interpolate background color from keyframes.
     */
    private fun interpolateColor(progress: Float, keyframes: KeyframeConfig?): Color {
        if (keyframes == null) return Color(0xFF6200EE)

        // Default gradient animation for demo
        val hue = progress * 360f
        return Color.hsv(hue, 0.7f, 0.9f)
    }

    /**
     * Card showing timing function curve visualization.
     */
    @Composable
    fun TimingFunctionCard(timing: TimingFunctionConfig) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Timing Function", style = MaterialTheme.typography.titleMedium)
                Text(
                    timing.original ?: "cubic-bezier",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bezier curve visualization
                if (timing.cubicBezier != null) {
                    BezierCurveVisualization(timing.cubicBezier)
                } else if (timing.stepsCount != null) {
                    StepsVisualization(timing.stepsCount, timing.stepsPosition)
                }
            }
        }
    }

    /**
     * Visual representation of cubic-bezier curve.
     */
    @Composable
    fun BezierCurveVisualization(controlPoints: List<Double>) {
        val x1 = controlPoints.getOrElse(0) { 0.0 }.toFloat()
        val y1 = controlPoints.getOrElse(1) { 0.0 }.toFloat()
        val x2 = controlPoints.getOrElse(2) { 1.0 }.toFloat()
        val y2 = controlPoints.getOrElse(3) { 1.0 }.toFloat()

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            val padding = 20f
            val width = size.width - padding * 2
            val height = size.height - padding * 2

            // Draw grid
            val gridColor = Color.LightGray.copy(alpha = 0.5f)
            for (i in 0..4) {
                val x = padding + (width * i / 4)
                val y = padding + (height * i / 4)
                drawLine(gridColor, Offset(x, padding), Offset(x, padding + height))
                drawLine(gridColor, Offset(padding, y), Offset(padding + width, y))
            }

            // Draw linear reference
            drawLine(
                Color.Gray.copy(alpha = 0.3f),
                Offset(padding, padding + height),
                Offset(padding + width, padding),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
            )

            // Draw bezier curve
            val path = Path()
            val steps = 50
            for (i in 0..steps) {
                val t = i.toFloat() / steps
                val bezierY = cubicBezier(t, 0f, y1, y2, 1f)

                val x = padding + t * width
                val y = padding + height - bezierY * height

                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            drawPath(
                path,
                Color(0xFF6200EE),
                style = Stroke(width = 3f)
            )

            // Draw control points
            val cp1x = padding + x1 * width
            val cp1y = padding + height - y1 * height
            val cp2x = padding + x2 * width
            val cp2y = padding + height - y2 * height

            // Control point lines
            drawLine(Color.Red.copy(alpha = 0.5f), Offset(padding, padding + height), Offset(cp1x, cp1y), strokeWidth = 1f)
            drawLine(Color.Blue.copy(alpha = 0.5f), Offset(padding + width, padding), Offset(cp2x, cp2y), strokeWidth = 1f)

            // Control points
            drawCircle(Color.Red, 6f, Offset(cp1x, cp1y))
            drawCircle(Color.Blue, 6f, Offset(cp2x, cp2y))
        }

        // Control point values
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("P1: (${"%.2f".format(x1)}, ${"%.2f".format(y1)})", color = Color.Red)
            Text("P2: (${"%.2f".format(x2)}, ${"%.2f".format(y2)})", color = Color.Blue)
        }
    }

    /**
     * Visual representation of steps timing.
     */
    @Composable
    fun StepsVisualization(count: Int, position: String?) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            val padding = 20f
            val width = size.width - padding * 2
            val height = size.height - padding * 2
            val stepWidth = width / count

            val isJumpStart = position == "start" || position == "jump-start"

            for (i in 0 until count) {
                val x1 = padding + i * stepWidth
                val x2 = padding + (i + 1) * stepWidth
                val yFrom = if (isJumpStart) {
                    padding + height - (i + 1).toFloat() / count * height
                } else {
                    padding + height - i.toFloat() / count * height
                }
                val yTo = padding + height - (i + 1).toFloat() / count * height

                // Horizontal line
                drawLine(Color(0xFF6200EE), Offset(x1, yFrom), Offset(x2, yFrom), strokeWidth = 2f)
                // Vertical jump
                if (i < count - 1 || isJumpStart) {
                    drawLine(Color(0xFF6200EE), Offset(x2, yFrom), Offset(x2, yTo), strokeWidth = 2f)
                }
            }
        }

        Text(
            "steps($count, ${position ?: "end"})",
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }

    /**
     * Card showing keyframe breakdown.
     */
    @Composable
    fun KeyframeBreakdownCard(keyframes: KeyframeConfig) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Keyframes: ${keyframes.name}", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(8.dp))

                keyframes.frames.sortedBy { it.percentage }.forEach { frame ->
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "${frame.percentage.toInt()}%",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFF6200EE)
                    )

                    frame.properties.forEach { (prop, value) ->
                        Row(
                            modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                        ) {
                            Text("$prop: ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text(value, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    /**
     * Cubic bezier calculation.
     */
    private fun cubicBezier(t: Float, p0: Float, p1: Float, p2: Float, p3: Float): Float {
        val oneMinusT = 1 - t
        return oneMinusT.pow(3) * p0 +
                3 * oneMinusT.pow(2) * t * p1 +
                3 * oneMinusT * t.pow(2) * p2 +
                t.pow(3) * p3
    }
}
