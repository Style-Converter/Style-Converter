package com.styleconverter.test.style.debug.performance

import android.os.SystemClock
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

/**
 * Performance profiling system for SDUI rendering.
 *
 * ## Features
 * - IR parsing time measurement
 * - Component render time tracking
 * - Memory usage monitoring
 * - Frame timing analysis
 * - Property extraction performance
 *
 * ## Usage
 * ```kotlin
 * // Start timing an operation
 * PerformanceProfiler.start("parseIR")
 *
 * // ... perform operation ...
 *
 * // End timing
 * PerformanceProfiler.end("parseIR")
 *
 * // Get report
 * val report = PerformanceProfiler.getReport()
 * ```
 */
object PerformanceProfiler {

    private val startTimes = ConcurrentHashMap<String, Long>()
    private val measurements = ConcurrentHashMap<String, MutableList<Long>>()
    private val componentRenderTimes = ConcurrentHashMap<String, MutableList<Long>>()
    private val memorySnapshots = mutableListOf<MemorySnapshot>()

    // Configuration
    var enabled = true
    var maxMeasurementsPerOperation = 100
    var logToConsole = false

    private const val TAG = "SDUIProfiler"

    /**
     * Start timing an operation.
     */
    fun start(operationName: String) {
        if (!enabled) return
        startTimes[operationName] = SystemClock.elapsedRealtimeNanos()
    }

    /**
     * End timing and record measurement.
     */
    fun end(operationName: String): Long {
        if (!enabled) return 0L

        val startTime = startTimes.remove(operationName) ?: return 0L
        val duration = SystemClock.elapsedRealtimeNanos() - startTime
        val durationMs = duration / 1_000_000.0

        val list = measurements.getOrPut(operationName) { mutableListOf() }
        synchronized(list) {
            list.add(duration)
            if (list.size > maxMeasurementsPerOperation) {
                list.removeAt(0)
            }
        }

        if (logToConsole) {
            Log.d(TAG, "$operationName: ${String.format("%.2f", durationMs)}ms")
        }

        return duration
    }

    /**
     * Measure a block of code.
     */
    inline fun <T> measure(operationName: String, block: () -> T): T {
        start(operationName)
        try {
            return block()
        } finally {
            end(operationName)
        }
    }

    /**
     * Record component render time.
     */
    fun recordComponentRender(componentId: String, durationNanos: Long) {
        if (!enabled) return

        val list = componentRenderTimes.getOrPut(componentId) { mutableListOf() }
        synchronized(list) {
            list.add(durationNanos)
            if (list.size > maxMeasurementsPerOperation) {
                list.removeAt(0)
            }
        }
    }

    /**
     * Take a memory snapshot.
     */
    fun snapshotMemory(label: String = "") {
        if (!enabled) return

        val runtime = Runtime.getRuntime()
        val snapshot = MemorySnapshot(
            timestamp = System.currentTimeMillis(),
            label = label,
            totalMemoryMB = runtime.totalMemory() / 1024.0 / 1024.0,
            freeMemoryMB = runtime.freeMemory() / 1024.0 / 1024.0,
            usedMemoryMB = (runtime.totalMemory() - runtime.freeMemory()) / 1024.0 / 1024.0,
            maxMemoryMB = runtime.maxMemory() / 1024.0 / 1024.0
        )

        synchronized(memorySnapshots) {
            memorySnapshots.add(snapshot)
            if (memorySnapshots.size > 50) {
                memorySnapshots.removeAt(0)
            }
        }
    }

    /**
     * Get performance report.
     */
    fun getReport(): PerformanceReport {
        val operationStats = measurements.map { (name, times) ->
            val timesCopy = synchronized(times) { times.toList() }
            val msValues = timesCopy.map { it / 1_000_000.0 }

            OperationStats(
                name = name,
                count = msValues.size,
                totalMs = msValues.sum(),
                averageMs = msValues.average(),
                minMs = msValues.minOrNull() ?: 0.0,
                maxMs = msValues.maxOrNull() ?: 0.0,
                p50Ms = percentile(msValues, 50),
                p95Ms = percentile(msValues, 95),
                p99Ms = percentile(msValues, 99)
            )
        }.sortedByDescending { it.totalMs }

        val componentStats = componentRenderTimes.map { (id, times) ->
            val timesCopy = synchronized(times) { times.toList() }
            val msValues = timesCopy.map { it / 1_000_000.0 }

            ComponentRenderStats(
                componentId = id,
                renderCount = msValues.size,
                averageMs = msValues.average(),
                maxMs = msValues.maxOrNull() ?: 0.0
            )
        }.sortedByDescending { it.averageMs }

        val memoryStats = synchronized(memorySnapshots) {
            memorySnapshots.toList()
        }

        return PerformanceReport(
            operationStats = operationStats,
            componentStats = componentStats.take(20),
            memorySnapshots = memoryStats.takeLast(10)
        )
    }

    /**
     * Clear all measurements.
     */
    fun reset() {
        startTimes.clear()
        measurements.clear()
        componentRenderTimes.clear()
        synchronized(memorySnapshots) {
            memorySnapshots.clear()
        }
    }

    private fun percentile(values: List<Double>, percentile: Int): Double {
        if (values.isEmpty()) return 0.0
        val sorted = values.sorted()
        val index = ((percentile / 100.0) * sorted.size).toInt().coerceIn(0, sorted.size - 1)
        return sorted[index]
    }

    // Data classes

    data class MemorySnapshot(
        val timestamp: Long,
        val label: String,
        val totalMemoryMB: Double,
        val freeMemoryMB: Double,
        val usedMemoryMB: Double,
        val maxMemoryMB: Double
    )

    data class OperationStats(
        val name: String,
        val count: Int,
        val totalMs: Double,
        val averageMs: Double,
        val minMs: Double,
        val maxMs: Double,
        val p50Ms: Double,
        val p95Ms: Double,
        val p99Ms: Double
    )

    data class ComponentRenderStats(
        val componentId: String,
        val renderCount: Int,
        val averageMs: Double,
        val maxMs: Double
    )

    data class PerformanceReport(
        val operationStats: List<OperationStats>,
        val componentStats: List<ComponentRenderStats>,
        val memorySnapshots: List<MemorySnapshot>
    )
}

/**
 * Composable performance profiler UI.
 */
@Composable
fun PerformanceProfilerPanel(
    modifier: Modifier = Modifier
) {
    var report by remember { mutableStateOf(PerformanceProfiler.getReport()) }

    // Refresh report periodically
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            report = PerformanceProfiler.getReport()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Memory Overview
        item {
            MemoryOverviewCard(report.memorySnapshots.lastOrNull())
        }

        // Operations
        item {
            Text(
                "Operations",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(report.operationStats.take(10)) { stats ->
            OperationStatsCard(stats)
        }

        // Slow Components
        if (report.componentStats.isNotEmpty()) {
            item {
                Text(
                    "Component Render Times",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(report.componentStats.take(5)) { stats ->
                ComponentStatsCard(stats)
            }
        }
    }
}

@Composable
private fun MemoryOverviewCard(snapshot: PerformanceProfiler.MemorySnapshot?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Memory Usage",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (snapshot != null) {
                val usagePercent = (snapshot.usedMemoryMB / snapshot.maxMemoryMB).toFloat()

                LinearProgressIndicator(
                    progress = { usagePercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = when {
                        usagePercent > 0.9 -> Color.Red
                        usagePercent > 0.7 -> Color(0xFFFFA000)
                        else -> Color(0xFF4CAF50)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${String.format("%.1f", snapshot.usedMemoryMB)} MB used",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "${String.format("%.1f", snapshot.maxMemoryMB)} MB max",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                Text("No data yet", color = Color.Gray)
            }
        }
    }
}

@Composable
private fun OperationStatsCard(stats: PerformanceProfiler.OperationStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stats.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )

                // Performance indicator
                val avgColor = when {
                    stats.averageMs > 16.67 -> Color.Red // > 1 frame at 60fps
                    stats.averageMs > 8.33 -> Color(0xFFFFA000) // > 0.5 frame
                    else -> Color(0xFF4CAF50)
                }

                Box(
                    modifier = Modifier
                        .background(avgColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "${String.format("%.2f", stats.averageMs)}ms",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Count", stats.count.toString())
                StatItem("Min", "${String.format("%.2f", stats.minMs)}ms")
                StatItem("Max", "${String.format("%.2f", stats.maxMs)}ms")
                StatItem("P95", "${String.format("%.2f", stats.p95Ms)}ms")
            }
        }
    }
}

@Composable
private fun ComponentStatsCard(stats: PerformanceProfiler.ComponentRenderStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    stats.componentId,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    "${stats.renderCount} renders",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Text(
                "avg ${String.format("%.2f", stats.averageMs)}ms",
                style = MaterialTheme.typography.bodyMedium,
                color = if (stats.averageMs > 16.67) Color.Red else Color.Gray
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}

/**
 * Composable wrapper that profiles render time.
 */
@Composable
fun ProfiledComponent(
    componentId: String,
    content: @Composable () -> Unit
) {
    val startTime = remember { SystemClock.elapsedRealtimeNanos() }

    content()

    DisposableEffect(Unit) {
        val duration = SystemClock.elapsedRealtimeNanos() - startTime
        PerformanceProfiler.recordComponentRender(componentId, duration)
        onDispose { }
    }
}

/**
 * Common profiling operation names.
 */
object ProfilerOperations {
    const val PARSE_IR = "parseIR"
    const val EXTRACT_PROPERTIES = "extractProperties"
    const val BUILD_MODIFIER = "buildModifier"
    const val RENDER_COMPONENT = "renderComponent"
    const val LOAD_IMAGE = "loadImage"
    const val EVALUATE_CALC = "evaluateCalc"
    const val RESOLVE_VARIABLE = "resolveVariable"
    const val APPLY_TRANSFORM = "applyTransform"
    const val APPLY_ANIMATION = "applyAnimation"
    const val APPLY_FILTER = "applyFilter"
}
