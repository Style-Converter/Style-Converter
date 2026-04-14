package com.styleconverter.test.screenshot

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.PixelCopy
import android.view.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.styleconverter.test.style.core.ir.IRComponent
import com.styleconverter.test.style.core.ir.IRDocument
import com.styleconverter.test.style.core.renderer.ComponentRenderer
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.math.roundToInt

private const val TAG = "ScreenshotCapture"

// ── Color tokens (matching web & ComponentListScreen) ────────────────────────
private val BgColor = Color(0xFF1A1A2E)
private val CardBg = Color(0x08FFFFFF)
private val CardHeaderBg = Color(0x0DFFFFFF)
private val CardFooterBg = Color(0x33000000)
private val BorderColor = Color(0x1AFFFFFF)
private val HeaderBg = Color(0x4D000000)
private val TextWhite = Color.White
private val TextSubtitle = Color(0xFF888888)
private val TextIndex = Color(0xFF666666)
private val TextPropCount = Color(0xFF666666)

/**
 * Screen that automatically captures screenshots of each component.
 *
 * Uses PixelCopy API to capture the fully-composited rendered frame,
 * preserving ALL visual effects: transforms, filters, clips, alpha/opacity.
 */
@Composable
fun ScreenshotCaptureScreen(
    onCaptureComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val screenshotManager = remember { ScreenshotManager(context) }

    var document by remember { mutableStateOf<IRDocument?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentIndex by remember { mutableIntStateOf(-1) }
    var capturePhase by remember { mutableStateOf(CapturePhase.LOADING) }
    var capturedCount by remember { mutableIntStateOf(0) }
    var failedCount by remember { mutableIntStateOf(0) }

    // PixelCopy capture state
    var shouldCapture by remember { mutableStateOf(false) }
    var cardBoundsInWindow by remember { mutableStateOf<Rect?>(null) }

    // Get the Activity window for PixelCopy
    val activity = context as? android.app.Activity
    val window = activity?.window

    LaunchedEffect(Unit) {
        try {
            val deleted = screenshotManager.clearScreenshots()
            Log.i(TAG, "Cleared $deleted existing screenshots")

            if (!screenshotManager.hasWritePermission()) {
                error = "No write permission for screenshots directory"
                capturePhase = CapturePhase.ERROR
                return@LaunchedEffect
            }

            val jsonString = context.assets.open("tmpOutput.json")
                .bufferedReader()
                .use { it.readText() }

            val json = Json { ignoreUnknownKeys = true }
            document = json.decodeFromString<IRDocument>(jsonString)

            Log.i(TAG, "Loaded ${document?.components?.size ?: 0} components")
            capturePhase = CapturePhase.CAPTURING
            currentIndex = 0
        } catch (e: Exception) {
            error = "Failed to load IR: ${e.message}"
            capturePhase = CapturePhase.ERROR
            Log.e(TAG, "Error loading document", e)
        }
    }

    // Capture logic using PixelCopy
    LaunchedEffect(shouldCapture, currentIndex) {
        if (shouldCapture && document != null && currentIndex >= 0 && currentIndex < document!!.components.size) {
            delay(400) // Wait for rendering + compositing

            try {
                val component = document!!.components[currentIndex]
                val bounds = cardBoundsInWindow

                val bitmap = if (window != null && bounds != null && bounds.width() > 0 && bounds.height() > 0) {
                    captureWithPixelCopy(window, bounds)
                } else {
                    null
                }

                if (bitmap != null) {
                    val file = screenshotManager.saveScreenshot(bitmap, component.name, currentIndex)
                    if (file != null) {
                        capturedCount++
                        Log.i(TAG, "Captured: ${component.name} -> ${file.absolutePath}")
                    } else {
                        failedCount++
                        Log.e(TAG, "Failed to save: ${component.name}")
                    }
                } else {
                    failedCount++
                    Log.e(TAG, "PixelCopy failed for: ${component.name}")
                }
            } catch (e: Exception) {
                failedCount++
                Log.e(TAG, "Capture error for component $currentIndex", e)
            }

            shouldCapture = false

            if (currentIndex < document!!.components.size - 1) {
                currentIndex++
            } else {
                capturePhase = CapturePhase.COMPLETE
                Log.i(TAG, "Capture complete: $capturedCount captured, $failedCount failed")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        when (capturePhase) {
            CapturePhase.LOADING -> LoadingView()
            CapturePhase.ERROR -> ErrorView(error ?: "Unknown error")
            CapturePhase.CAPTURING -> {
                document?.let { doc ->
                    if (currentIndex >= 0 && currentIndex < doc.components.size) {
                        CaptureView(
                            component = doc.components[currentIndex],
                            currentIndex = currentIndex,
                            totalCount = doc.components.size,
                            onCardPositioned = { bounds -> cardBoundsInWindow = bounds },
                            onRendered = { shouldCapture = true }
                        )
                    }
                }
            }
            CapturePhase.COMPLETE -> CompleteView(
                capturedCount = capturedCount,
                failedCount = failedCount,
                screenshotPath = screenshotManager.getScreenshotPath(),
                adbPullCommand = screenshotManager.getAdbPullCommand(),
                onDismiss = onCaptureComplete
            )
        }
    }
}

/**
 * Capture a region of the window using PixelCopy API.
 * This captures the fully-composited hardware-rendered frame,
 * including ALL visual effects (transforms, filters, clips, alpha).
 */
private suspend fun captureWithPixelCopy(window: Window, bounds: Rect): Bitmap? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null

    val bitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888)

    return suspendCancellableCoroutine { continuation ->
        try {
            PixelCopy.request(
                window,
                bounds,
                bitmap,
                { result ->
                    if (result == PixelCopy.SUCCESS) {
                        continuation.resume(bitmap)
                    } else {
                        Log.e(TAG, "PixelCopy failed with result: $result")
                        continuation.resume(null)
                    }
                },
                Handler(Looper.getMainLooper())
            )
        } catch (e: Exception) {
            Log.e(TAG, "PixelCopy exception", e)
            continuation.resume(null)
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color(0xFF60A5FA))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Preparing screenshot capture...", color = TextSubtitle)
        }
    }
}

@Composable
private fun ErrorView(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Capture Error",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color(0xFFF87171)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = Color(0xFFF87171),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun CaptureView(
    component: IRComponent,
    currentIndex: Int,
    totalCount: Int,
    onCardPositioned: (Rect) -> Unit,
    onRendered: () -> Unit
) {
    val density = LocalDensity.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Progress header (dark theme)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderBg)
                .drawBottomBorder(1.dp, BorderColor)
                .padding(12.dp)
        ) {
            Text(
                text = "Capturing Screenshots",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / totalCount },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF60A5FA),
                trackColor = Color(0x33FFFFFF)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${currentIndex + 1} / $totalCount - ${component.name}",
                fontSize = 14.sp,
                color = TextSubtitle
            )
        }

        // Component render area — captured via PixelCopy
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            ComponentCaptureCard(
                component = component,
                index = currentIndex,
                onPositioned = { posInWindow, widthPx, heightPx ->
                    onCardPositioned(Rect(
                        posInWindow.x.roundToInt(),
                        posInWindow.y.roundToInt(),
                        (posInWindow.x + widthPx).roundToInt(),
                        (posInWindow.y + heightPx).roundToInt()
                    ))
                },
                onRendered = onRendered
            )
        }
    }
}

@Composable
private fun ComponentCaptureCard(
    component: IRComponent,
    index: Int,
    onPositioned: (androidx.compose.ui.geometry.Offset, Float, Float) -> Unit,
    onRendered: () -> Unit
) {
    LaunchedEffect(component.id) {
        delay(100)
        onRendered()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(BgColor)
            .background(CardBg)
            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
            .onGloballyPositioned { coords ->
                val pos = coords.positionInWindow()
                onPositioned(pos, coords.size.width.toFloat(), coords.size.height.toFloat())
            }
    ) {
        // Card Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardHeaderBg)
                .drawBottomBorder(1.dp, BorderColor)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "#${index + 1}",
                fontSize = 12.sp,
                color = TextIndex,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = component.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextWhite,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = component.id,
                fontSize = 11.sp,
                color = TextIndex,
                fontFamily = FontFamily.Monospace
            )
        }

        // Card Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            ComponentRenderer.RenderComponent(component)
        }

        // Card Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawTopBorder(1.dp, BorderColor)
                .background(CardFooterBg)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val childInfo = if (component.children != null && component.children!!.isNotEmpty()) {
                ", ${component.children!!.size} children"
            } else ""
            Text(
                text = "${component.properties.size} props$childInfo",
                fontSize = 12.sp,
                color = TextPropCount
            )
            // "Show Props" button matching web gallery card footer
            Box(
                modifier = Modifier
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Show Props",
                    fontSize = 11.sp,
                    color = Color(0xFF888888)
                )
            }
        }
    }
}

@Composable
private fun CompleteView(
    capturedCount: Int,
    failedCount: Int,
    screenshotPath: String,
    adbPullCommand: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CardBg)
                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Capture Complete!",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = Color(0xFF22C55E)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$capturedCount screenshots captured",
                fontSize = 16.sp,
                color = TextWhite
            )
            if (failedCount > 0) {
                Text(
                    text = "$failedCount failed",
                    fontSize = 14.sp,
                    color = Color(0xFFF87171)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Saved to:\n$screenshotPath",
                fontSize = 12.sp,
                color = TextSubtitle
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Pull with:\n$adbPullCommand",
                fontSize = 11.sp,
                color = TextIndex
            )
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .background(Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Continue to Gallery",
                    color = Color(0xFF60A5FA),
                    fontSize = 14.sp
                )
            }
        }
    }
}

private enum class CapturePhase {
    LOADING,
    CAPTURING,
    COMPLETE,
    ERROR
}

// ── Border drawing modifiers ─────────────────────────────────────────────────

private fun Modifier.drawBottomBorder(width: Dp, color: Color): Modifier =
    this.then(
        Modifier.drawWithContent {
            drawContent()
            drawLine(
                color = color,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = width.toPx()
            )
        }
    )

private fun Modifier.drawTopBorder(width: Dp, color: Color): Modifier =
    this.then(
        Modifier.drawWithContent {
            drawContent()
            drawLine(
                color = color,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = width.toPx()
            )
        }
    )
