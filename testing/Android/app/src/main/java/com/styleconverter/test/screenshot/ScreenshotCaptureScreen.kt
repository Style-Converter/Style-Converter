package com.styleconverter.test.screenshot

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.styleconverter.test.style.core.ir.IRComponent
import com.styleconverter.test.style.core.ir.IRDocument
import com.styleconverter.test.style.core.renderer.ComponentRenderer
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

private const val TAG = "ScreenshotCapture"

/**
 * Screen that automatically captures screenshots of each component.
 *
 * On launch:
 * 1. Clears existing screenshots
 * 2. Loads components from tmpOutput.json
 * 3. Renders each component one by one
 * 4. Captures and saves screenshot for each
 * 5. Shows completion summary
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

    // Graphics layer for capturing
    val graphicsLayer = rememberGraphicsLayer()
    var shouldCapture by remember { mutableStateOf(false) }

    // Load JSON and clear existing screenshots
    LaunchedEffect(Unit) {
        try {
            // Clear existing screenshots first
            val deleted = screenshotManager.clearScreenshots()
            Log.i(TAG, "Cleared $deleted existing screenshots")

            // Check write permission
            if (!screenshotManager.hasWritePermission()) {
                error = "No write permission for screenshots directory"
                capturePhase = CapturePhase.ERROR
                return@LaunchedEffect
            }

            // Load JSON
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

    // Capture logic - triggered after component renders
    LaunchedEffect(shouldCapture, currentIndex) {
        if (shouldCapture && document != null && currentIndex >= 0 && currentIndex < document!!.components.size) {
            delay(300) // Wait for render to complete

            try {
                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                val component = document!!.components[currentIndex]
                val file = screenshotManager.saveScreenshot(bitmap, component.name, currentIndex)

                if (file != null) {
                    capturedCount++
                    Log.i(TAG, "Captured: ${component.name} -> ${file.absolutePath}")
                } else {
                    failedCount++
                    Log.e(TAG, "Failed to save: ${component.name}")
                }
            } catch (e: Exception) {
                failedCount++
                Log.e(TAG, "Capture error for component $currentIndex", e)
            }

            shouldCapture = false

            // Move to next component
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
            .background(Color(0xFFF5F5F5))
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
                            graphicsLayer = graphicsLayer,
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

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Preparing screenshot capture...", color = Color.Gray)
        }
    }
}

@Composable
private fun ErrorView(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Capture Error",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFFB71C1C)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    color = Color(0xFFD32F2F),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun CaptureView(
    component: IRComponent,
    currentIndex: Int,
    totalCount: Int,
    graphicsLayer: androidx.compose.ui.graphics.layer.GraphicsLayer,
    onRendered: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Progress header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp,
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Capturing Screenshots",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / totalCount },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${currentIndex + 1} / $totalCount - ${component.name}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        // Component render area (captured)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }
                    drawLayer(graphicsLayer)
                },
            contentAlignment = Alignment.Center
        ) {
            ComponentCaptureCard(
                component = component,
                onRendered = onRendered
            )
        }
    }
}

@Composable
private fun ComponentCaptureCard(
    component: IRComponent,
    onRendered: () -> Unit
) {
    // Signal that we're ready for capture after composition
    LaunchedEffect(component.id) {
        delay(100) // Give time for layout
        onRendered()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Component info header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = component.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = component.id,
                        fontSize = 12.sp,
                        color = Color(0xFF888888)
                    )
                }
                Surface(
                    color = Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${component.properties.size} props",
                        fontSize = 11.sp,
                        color = Color(0xFF1976D2),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Rendered component preview
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
                color = Color(0xFFFAFAFA),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ComponentRenderer.RenderComponent(component)
                }
            }

            // Property list
            if (component.properties.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Properties: ${component.properties.take(5).joinToString { it.type }}${if (component.properties.size > 5) "..." else ""}",
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
        Card(
            modifier = Modifier.padding(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Capture Complete!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "$capturedCount screenshots captured",
                    fontSize = 16.sp
                )
                if (failedCount > 0) {
                    Text(
                        text = "$failedCount failed",
                        fontSize = 14.sp,
                        color = Color(0xFFD32F2F)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Saved to:\n$screenshotPath",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Pull with:\n$adbPullCommand",
                    fontSize = 11.sp,
                    color = Color(0xFF666666)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onDismiss) {
                    Text("Continue to Gallery")
                }
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
