package com.styleconverter.test.screenshot

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream

/**
 * Manages screenshot capture and storage for component testing.
 *
 * On Android 11+, screenshots are saved to app-specific external files directory:
 *   /sdcard/Android/data/com.styleconverter.test/files/test_screenshots/
 *
 * On older Android versions, screenshots are saved to:
 *   /sdcard/test_screenshots/
 *
 * Both locations are accessible via adb pull.
 */
class ScreenshotManager(private val context: Context) {

    companion object {
        private const val TAG = "ScreenshotManager"
        private const val SCREENSHOT_DIR = "test_screenshots"
    }

    private val screenshotDir: File by lazy {
        val dir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ - use app-specific external files directory
            File(context.getExternalFilesDir(null), SCREENSHOT_DIR)
        } else {
            // Older Android - use shared external storage
            File(Environment.getExternalStorageDirectory(), SCREENSHOT_DIR)
        }
        dir.also {
            if (!it.exists()) {
                val created = it.mkdirs()
                Log.i(TAG, "Created screenshot directory: ${it.absolutePath}, success: $created")
            }
        }
    }

    /**
     * Clears all existing screenshots from the test_screenshots folder.
     * Call this at app startup before capturing new screenshots.
     */
    fun clearScreenshots(): Int {
        var deletedCount = 0
        try {
            if (screenshotDir.exists()) {
                screenshotDir.listFiles()?.forEach { file ->
                    if (file.isFile && (file.extension == "png" || file.extension == "jpg")) {
                        if (file.delete()) {
                            deletedCount++
                        }
                    }
                }
            }
            Log.i(TAG, "Cleared $deletedCount existing screenshots from ${screenshotDir.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing screenshots: ${e.message}")
        }
        return deletedCount
    }

    /**
     * Saves a bitmap as a PNG screenshot.
     *
     * @param bitmap The bitmap to save
     * @param componentName The component name (used for filename)
     * @param index The component index (for ordering)
     * @return The saved file, or null if saving failed
     */
    fun saveScreenshot(bitmap: Bitmap, componentName: String, index: Int): File? {
        return try {
            // Ensure directory exists
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs()
            }

            // Sanitize filename
            val safeName = componentName.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val filename = String.format("%03d_%s.png", index, safeName)
            val file = File(screenshotDir, filename)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            Log.i(TAG, "Saved screenshot: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error saving screenshot for $componentName: ${e.message}", e)
            null
        }
    }

    /**
     * Gets the screenshot directory path.
     */
    fun getScreenshotPath(): String = screenshotDir.absolutePath

    /**
     * Gets the adb pull command to retrieve screenshots.
     */
    fun getAdbPullCommand(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            "adb pull /sdcard/Android/data/com.styleconverter.test/files/$SCREENSHOT_DIR/ ./screenshots/"
        } else {
            "adb pull /sdcard/$SCREENSHOT_DIR/ ./screenshots/"
        }
    }

    /**
     * Checks if we have write permission to the screenshot directory.
     */
    fun hasWritePermission(): Boolean {
        return try {
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs()
            }
            // Try to create a test file to verify write access
            val testFile = File(screenshotDir, ".test_write")
            val canWrite = testFile.createNewFile()
            if (canWrite) {
                testFile.delete()
            }
            canWrite || screenshotDir.canWrite()
        } catch (e: Exception) {
            Log.e(TAG, "Permission check failed: ${e.message}")
            false
        }
    }

    /**
     * Gets the count of screenshots in the directory.
     */
    fun getScreenshotCount(): Int {
        return screenshotDir.listFiles()?.count {
            it.isFile && it.extension == "png"
        } ?: 0
    }
}
