package com.styleconverter.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF9FAFB)
                ) {
                    ComposeTestScreen()
                }
            }
        }
    }
}

@Composable
fun ComposeTestScreen() {
    val context = LocalContext.current
    var composeDocument by remember { mutableStateOf<ComposeDocument?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val jsonString = context.assets.open("composeOutput.json")
                .bufferedReader()
                .use { it.readText() }

            val json = Json { ignoreUnknownKeys = true }
            composeDocument = json.decodeFromString(jsonString)
        } catch (e: Exception) {
            error = "Failed to load JSON: ${e.message}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "Compose Style Converter - Visual Test",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            color = Color(0xFF111827)
        )

        when {
            error != null -> {
                ErrorMessage(error!!)
            }
            composeDocument == null -> {
                LoadingMessage()
            }
            else -> {
                ComponentsList(composeDocument!!)
            }
        }
    }
}

@Composable
fun ErrorMessage(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Error",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB91C1C)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = Color(0xFFDC2626)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Make sure composeOutput.json is in the assets folder",
                fontSize = 12.sp,
                color = Color(0xFF991B1B)
            )
        }
    }
}

@Composable
fun LoadingMessage() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ComponentsList(document: ComposeDocument) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        document.components.forEach { component ->
            ComponentCard(component)
        }

        // Footer with count
        Text(
            text = "Total components: ${document.components.size}",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )
    }
}

@Composable
fun ComponentCard(component: ComposeComponent) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Component title
            Text(
                text = "Component: ${component.name}",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Base modifiers
            if (component.baseModifiers.isNotEmpty()) {
                InfoSection(
                    title = "Base Modifiers:",
                    items = component.baseModifiers,
                    backgroundColor = Color(0xFFF3F4F6)
                )
            }

            // States
            if (component.states.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "States:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E40AF)
                )
                component.states.forEach { state ->
                    StateSection(state)
                }
            }

            // Responsive
            if (component.responsive.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Responsive:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF047857)
                )
                component.responsive.forEach { responsive ->
                    ResponsiveSection(responsive)
                }
            }

            // Live Component Preview
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Live Preview (Generated at Runtime):",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B7280)
            )
            Spacer(modifier = Modifier.height(8.dp))
            RuntimeComponentPreview(component)
        }
    }
}

@Composable
fun InfoSection(title: String, items: List<String>, backgroundColor: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151)
            )
            Spacer(modifier = Modifier.height(4.dp))
            items.forEach { item ->
                Text(
                    text = "• $item",
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun StateSection(state: ComposeState) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = Color(0xFFDEEBFF),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = ":${state.name}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1E40AF)
            )
            state.modifiers.forEach { modifier ->
                Text(
                    text = "  • $modifier",
                    fontSize = 10.sp,
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
fun ResponsiveSection(responsive: ComposeResponsive) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = Color(0xFFD1FAE5),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "@media (${responsive.condition})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF047857)
            )
            if (responsive.modifiers.isNotEmpty()) {
                responsive.modifiers.forEach { modifier ->
                    Text(
                        text = "  • $modifier",
                        fontSize = 10.sp,
                        color = Color(0xFF059669),
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            } else {
                Text(
                    text = "  (no modifiers)",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
fun RuntimeComponentPreview(component: ComposeComponent) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        color = Color(0xFFF9FAFB),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Render component at runtime from JSON modifiers
            RuntimeComponent(component)
        }
    }
}

