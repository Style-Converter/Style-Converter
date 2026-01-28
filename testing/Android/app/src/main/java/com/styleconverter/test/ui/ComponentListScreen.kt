package com.styleconverter.test.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.styleconverter.test.style.core.ir.IRComponent
import com.styleconverter.test.style.core.ir.IRDocument
import com.styleconverter.test.style.core.renderer.ComponentRenderer
import kotlinx.serialization.json.Json

/**
 * Main screen displaying all components from the IR document.
 *
 * Loads tmpOutput.json from assets at runtime and renders each component
 * using the SDUI ComponentRenderer.
 */
@Composable
fun ComponentListScreen() {
    val context = LocalContext.current
    var document by remember { mutableStateOf<IRDocument?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Load JSON on first composition
    LaunchedEffect(Unit) {
        try {
            val jsonString = context.assets.open("tmpOutput.json")
                .bufferedReader()
                .use { it.readText() }

            val json = Json { ignoreUnknownKeys = true }
            document = json.decodeFromString<IRDocument>(jsonString)
        } catch (e: Exception) {
            error = "Failed to load IR: ${e.message}"
            e.printStackTrace()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        Header(
            componentCount = document?.components?.size ?: 0,
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it }
        )

        when {
            error != null -> ErrorCard(error!!)
            document == null -> LoadingIndicator()
            else -> {
                val filteredComponents = document!!.components.filter {
                    searchQuery.isEmpty() ||
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.id.contains(searchQuery, ignoreCase = true)
                }
                ComponentList(filteredComponents)
            }
        }
    }
}

@Composable
private fun Header(
    componentCount: Int,
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 4.dp,
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "SDUI Component Viewer",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = "$componentCount components loaded",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(top = 4.dp)
            )

            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search components...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                singleLine = true
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Error Loading IR",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB71C1C)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = Color(0xFFD32F2F),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Make sure tmpOutput.json is in assets/",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading IR document...", color = Color.Gray)
        }
    }
}

@Composable
private fun ComponentList(components: List<IRComponent>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(components, key = { it.id }) { component ->
            ComponentCard(component)
        }

        // Footer
        item {
            Text(
                text = "Showing ${components.size} components",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        }
    }
}

@Composable
private fun ComponentCard(component: IRComponent) {
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
                // Property count badge
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

            // Property list (collapsed by default)
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
