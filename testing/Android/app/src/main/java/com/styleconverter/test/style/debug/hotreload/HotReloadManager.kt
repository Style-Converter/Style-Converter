package com.styleconverter.test.style.debug.hotreload

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.styleconverter.test.style.core.ir.IRComponent
import com.styleconverter.test.style.core.ir.IRDocument
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Hot reload manager for IR JSON files.
 *
 * Enables live updates of SDUI components during development by:
 * - Watching a file for changes (file-based)
 * - Polling a server endpoint (network-based)
 * - Loading from clipboard (manual trigger)
 *
 * ## Usage
 *
 * ### File-based hot reload (development with adb)
 * ```kotlin
 * // In your activity/app startup
 * HotReloadManager.startFileWatcher("/sdcard/ir-components.json")
 *
 * // In composable
 * val document by HotReloadManager.documentState.collectAsState()
 * ```
 *
 * ### Server-based hot reload
 * ```kotlin
 * HotReloadManager.startServerPolling("http://192.168.1.100:8080/ir-components.json")
 * ```
 *
 * ## ADB Push Workflow
 * ```bash
 * # Generate IR
 * ./gradlew run --args="convert --from css --to compose -i input.json -o out"
 *
 * # Push to device
 * adb push out/tmpOutput.json /sdcard/ir-components.json
 *
 * # Hot reload will pick up the change automatically
 * ```
 */
object HotReloadManager {

    private const val TAG = "HotReload"

    private val _documentState = MutableStateFlow<IRDocument?>(null)
    val documentState: StateFlow<IRDocument?> = _documentState.asStateFlow()

    private val _reloadCount = MutableStateFlow(0)
    val reloadCount: StateFlow<Int> = _reloadCount.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val _lastReloadTime = MutableStateFlow<Long>(0)
    val lastReloadTime: StateFlow<Long> = _lastReloadTime.asStateFlow()

    private val _isWatching = MutableStateFlow(false)
    val isWatching: StateFlow<Boolean> = _isWatching.asStateFlow()

    private var watchJob: Job? = null
    private var lastFileModified: Long = 0
    private var lastEtag: String? = null

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Configuration
    var pollIntervalMs = 1000L
    var enableDebugLog = true

    /**
     * Start watching a file for changes.
     */
    fun startFileWatcher(filePath: String) {
        stopWatching()

        _isWatching.value = true
        watchJob = CoroutineScope(Dispatchers.IO).launch {
            log("Starting file watcher: $filePath")

            while (isActive) {
                try {
                    val file = File(filePath)
                    if (file.exists()) {
                        val modified = file.lastModified()
                        if (modified != lastFileModified) {
                            lastFileModified = modified
                            log("File changed, reloading...")
                            loadFromFile(file)
                        }
                    }
                } catch (e: Exception) {
                    _lastError.value = "File watch error: ${e.message}"
                    log("File watch error: ${e.message}")
                }

                delay(pollIntervalMs)
            }
        }
    }

    /**
     * Start polling a server endpoint for changes.
     */
    fun startServerPolling(serverUrl: String) {
        stopWatching()

        _isWatching.value = true
        watchJob = CoroutineScope(Dispatchers.IO).launch {
            log("Starting server polling: $serverUrl")

            while (isActive) {
                try {
                    val result = fetchFromServer(serverUrl)
                    if (result != null) {
                        log("Server returned new content, reloading...")
                        parseAndUpdate(result)
                    }
                } catch (e: Exception) {
                    _lastError.value = "Server poll error: ${e.message}"
                    log("Server poll error: ${e.message}")
                }

                delay(pollIntervalMs)
            }
        }
    }

    /**
     * Stop watching for changes.
     */
    fun stopWatching() {
        watchJob?.cancel()
        watchJob = null
        _isWatching.value = false
        log("Stopped watching")
    }

    /**
     * Manually trigger a reload from file.
     */
    suspend fun reloadFromFile(filePath: String) {
        log("Manual reload from file: $filePath")
        loadFromFile(File(filePath))
    }

    /**
     * Manually trigger a reload from URL.
     */
    suspend fun reloadFromUrl(url: String) {
        log("Manual reload from URL: $url")
        val content = fetchFromServer(url, forceReload = true)
        if (content != null) {
            parseAndUpdate(content)
        }
    }

    /**
     * Load from raw JSON string.
     */
    suspend fun loadFromJson(jsonString: String) {
        log("Loading from JSON string (${jsonString.length} chars)")
        parseAndUpdate(jsonString)
    }

    /**
     * Load from app assets.
     */
    suspend fun loadFromAssets(context: Context, assetPath: String) {
        log("Loading from assets: $assetPath")
        withContext(Dispatchers.IO) {
            try {
                val content = context.assets.open(assetPath)
                    .bufferedReader()
                    .readText()
                parseAndUpdate(content)
            } catch (e: Exception) {
                _lastError.value = "Asset load error: ${e.message}"
                log("Asset load error: ${e.message}")
            }
        }
    }

    /**
     * Get a specific component by ID.
     */
    fun getComponent(id: String): IRComponent? {
        return findComponent(_documentState.value?.components, id)
    }

    /**
     * Get all component IDs.
     */
    fun getAllComponentIds(): List<String> {
        return collectComponentIds(_documentState.value?.components ?: emptyList())
    }

    // Private helpers

    private suspend fun loadFromFile(file: File) {
        withContext(Dispatchers.IO) {
            try {
                val content = file.readText()
                parseAndUpdate(content)
            } catch (e: Exception) {
                _lastError.value = "File read error: ${e.message}"
                log("File read error: ${e.message}")
            }
        }
    }

    private suspend fun fetchFromServer(url: String, forceReload: Boolean = false): String? {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                // Use ETag for efficient polling
                if (!forceReload && lastEtag != null) {
                    connection.setRequestProperty("If-None-Match", lastEtag)
                }

                val responseCode = connection.responseCode

                when (responseCode) {
                    304 -> {
                        // Not modified
                        null
                    }
                    200 -> {
                        lastEtag = connection.getHeaderField("ETag")
                        connection.inputStream.bufferedReader().readText()
                    }
                    else -> {
                        _lastError.value = "Server returned $responseCode"
                        null
                    }
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    private suspend fun parseAndUpdate(jsonContent: String) {
        withContext(Dispatchers.Default) {
            try {
                val document = json.decodeFromString<IRDocument>(jsonContent)

                _documentState.value = document
                _reloadCount.value++
                _lastReloadTime.value = System.currentTimeMillis()
                _lastError.value = null

                log("Loaded ${document.components.size} components")
            } catch (e: Exception) {
                _lastError.value = "Parse error: ${e.message}"
                log("Parse error: ${e.message}")
            }
        }
    }

    private fun findComponent(components: List<IRComponent>?, id: String): IRComponent? {
        if (components == null) return null

        for (component in components) {
            if (component.id == id) return component
            val found = findComponent(component.children, id)
            if (found != null) return found
        }
        return null
    }

    private fun collectComponentIds(components: List<IRComponent>): List<String> {
        val ids = mutableListOf<String>()
        for (component in components) {
            ids.add(component.id)
            ids.addAll(collectComponentIds(component.children ?: emptyList()))
        }
        return ids
    }

    private fun log(message: String) {
        if (enableDebugLog) {
            Log.d(TAG, message)
        }
    }

    /**
     * Debug info for the current state.
     */
    fun getDebugInfo(): HotReloadDebugInfo {
        return HotReloadDebugInfo(
            isWatching = _isWatching.value,
            componentCount = _documentState.value?.components?.size ?: 0,
            reloadCount = _reloadCount.value,
            lastReloadTime = _lastReloadTime.value,
            lastError = _lastError.value
        )
    }

    data class HotReloadDebugInfo(
        val isWatching: Boolean,
        val componentCount: Int,
        val reloadCount: Int,
        val lastReloadTime: Long,
        val lastError: String?
    )
}

/**
 * Composable that observes hot reload state.
 */
@Composable
fun rememberHotReloadDocument(): IRDocument? {
    var document by remember { mutableStateOf<IRDocument?>(null) }

    LaunchedEffect(Unit) {
        HotReloadManager.documentState.collect { doc ->
            document = doc
        }
    }

    return document
}

/**
 * Composable that observes a specific component.
 */
@Composable
fun rememberHotReloadComponent(componentId: String): IRComponent? {
    var component by remember { mutableStateOf<IRComponent?>(null) }

    LaunchedEffect(componentId) {
        HotReloadManager.documentState.collect {
            component = HotReloadManager.getComponent(componentId)
        }
    }

    return component
}

/**
 * Composable status indicator for hot reload.
 */
@Composable
fun HotReloadStatusIndicator(
    modifier: Modifier = Modifier
) {
    var debugInfo by remember { mutableStateOf(HotReloadManager.getDebugInfo()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            debugInfo = HotReloadManager.getDebugInfo()
        }
    }

    Row(
        modifier = modifier
            .background(
                if (debugInfo.lastError != null)
                    Color.Red.copy(alpha = 0.1f)
                else if (debugInfo.isWatching)
                    Color.Green.copy(alpha = 0.1f)
                else
                    Color.Gray.copy(alpha = 0.1f),
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Status dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    when {
                        debugInfo.lastError != null -> Color.Red
                        debugInfo.isWatching -> Color.Green
                        else -> Color.Gray
                    },
                    CircleShape
                )
        )

        Text(
            text = if (debugInfo.isWatching) "Watching" else "Stopped",
            style = MaterialTheme.typography.labelSmall
        )

        if (debugInfo.reloadCount > 0) {
            Text(
                text = "| ${debugInfo.reloadCount} reloads",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}
