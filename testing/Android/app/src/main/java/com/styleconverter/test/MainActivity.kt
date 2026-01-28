package com.styleconverter.test

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.styleconverter.test.screenshot.ScreenshotCaptureScreen
import com.styleconverter.test.ui.ComponentListScreen

private const val TAG = "MainActivity"

/**
 * Main Activity for the SDUI Test Application.
 *
 * This app loads IR models from tmpOutput.json at runtime and renders
 * Compose UI based on the component definitions. It serves as a visual
 * test bed for the Style-Converter project.
 *
 * ## How It Works
 * 1. On launch, checks for storage permissions
 * 2. Runs screenshot capture for all components (saves to /sdcard/test_screenshots/)
 * 3. Shows completion summary
 * 4. Then displays the regular component gallery for browsing
 *
 * ## Testing Workflow
 * 1. Run main project: ./gradlew run --args="convert --from css --to compose -i examples/all-css-properties.json -o out"
 * 2. Copy out/tmpOutput.json to testing/android/app/src/main/assets/
 * 3. Run this Android app - screenshots are auto-captured
 * 4. Pull screenshots: adb pull /sdcard/test_screenshots/ ./screenshots/
 */
class MainActivity : ComponentActivity() {

    private var hasStoragePermission by mutableStateOf(false)
    private var permissionChecked by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasStoragePermission = permissions.values.all { it }
        permissionChecked = true
        Log.i(TAG, "Storage permission granted: $hasStoragePermission")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check/request storage permissions
        checkStoragePermission()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent(
                        hasPermission = hasStoragePermission,
                        permissionChecked = permissionChecked
                    )
                }
            }
        }
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ - check for MANAGE_EXTERNAL_STORAGE
            hasStoragePermission = Environment.isExternalStorageManager()
            permissionChecked = true
            if (!hasStoragePermission) {
                Log.w(TAG, "Need MANAGE_EXTERNAL_STORAGE permission on Android 11+")
                // For testing, we'll proceed anyway - screenshots will go to app-specific storage
                hasStoragePermission = true
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6-10
            val writePermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            val readPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )

            if (writePermission == PackageManager.PERMISSION_GRANTED &&
                readPermission == PackageManager.PERMISSION_GRANTED
            ) {
                hasStoragePermission = true
                permissionChecked = true
            } else {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                )
            }
        } else {
            // Pre-Marshmallow
            hasStoragePermission = true
            permissionChecked = true
        }
    }
}

@Composable
private fun MainContent(
    hasPermission: Boolean,
    permissionChecked: Boolean
) {
    var captureComplete by remember { mutableStateOf(false) }

    if (!permissionChecked) {
        // Still checking permissions
        return
    }

    if (!captureComplete && hasPermission) {
        // Run screenshot capture first
        ScreenshotCaptureScreen(
            onCaptureComplete = {
                captureComplete = true
            }
        )
    } else {
        // Show regular component gallery
        ComponentListScreen()
    }
}
