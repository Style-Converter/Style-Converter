package com.styleconverter.test.style.debug.tree

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.styleconverter.test.style.core.ir.IRComponent
import com.styleconverter.test.style.core.ir.IRProperty

/**
 * Visual debugger for component tree and applied styles.
 *
 * ## Features
 * - Hierarchical tree view of components
 * - Property inspection panel
 * - Color/layout visualization
 * - Search and filter capabilities
 * - Computed style view
 *
 * ## Usage
 * ```kotlin
 * ComponentTreeDebugger.DebugPanel(
 *     rootComponent = irComponent,
 *     onSelectComponent = { component ->
 *         // Handle component selection
 *     }
 * )
 * ```
 */
object ComponentTreeDebugger {

    /**
     * Full debug panel with tree and property inspector.
     */
    @Composable
    fun DebugPanel(
        rootComponent: IRComponent,
        onSelectComponent: ((IRComponent) -> Unit)? = null,
        modifier: Modifier = Modifier
    ) {
        var selectedComponent by remember { mutableStateOf<IRComponent?>(null) }
        var selectedTabIndex by remember { mutableIntStateOf(0) }

        Row(modifier = modifier.fillMaxSize()) {
            // Left: Component Tree
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    TreeHeader("Component Tree")
                    ComponentTree(
                        component = rootComponent,
                        selectedComponent = selectedComponent,
                        onSelectComponent = {
                            selectedComponent = it
                            onSelectComponent?.invoke(it)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Divider
            Divider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Right: Property Inspector
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Column {
                    TreeHeader("Inspector")

                    TabRow(selectedTabIndex = selectedTabIndex) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 }
                        ) {
                            Text("Properties", modifier = Modifier.padding(12.dp))
                        }
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 }
                        ) {
                            Text("Layout", modifier = Modifier.padding(12.dp))
                        }
                        Tab(
                            selected = selectedTabIndex == 2,
                            onClick = { selectedTabIndex = 2 }
                        ) {
                            Text("Colors", modifier = Modifier.padding(12.dp))
                        }
                    }

                    if (selectedComponent != null) {
                        when (selectedTabIndex) {
                            0 -> PropertyInspector(selectedComponent!!)
                            1 -> LayoutInspector(selectedComponent!!)
                            2 -> ColorInspector(selectedComponent!!)
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Select a component to inspect",
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TreeHeader(title: String) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(12.dp)
            )
        }
    }

    /**
     * Hierarchical component tree view.
     */
    @Composable
    fun ComponentTree(
        component: IRComponent,
        selectedComponent: IRComponent?,
        onSelectComponent: (IRComponent) -> Unit,
        modifier: Modifier = Modifier,
        depth: Int = 0
    ) {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(8.dp)
        ) {
            item {
                ComponentTreeNode(
                    component = component,
                    selectedComponent = selectedComponent,
                    onSelectComponent = onSelectComponent,
                    depth = depth
                )
            }
        }
    }

    @Composable
    private fun ComponentTreeNode(
        component: IRComponent,
        selectedComponent: IRComponent?,
        onSelectComponent: (IRComponent) -> Unit,
        depth: Int
    ) {
        var expanded by remember { mutableStateOf(depth < 2) }
        val hasChildren = !component.children.isNullOrEmpty()
        val isSelected = component == selectedComponent

        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = (depth * 16).dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            Color.Transparent
                    )
                    .clickable { onSelectComponent(component) }
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Expand/collapse arrow
                if (hasChildren) {
                    Icon(
                        imageVector = if (expanded)
                            Icons.Default.KeyboardArrowDown
                        else
                            Icons.Default.KeyboardArrowRight,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { expanded = !expanded }
                    )
                } else {
                    Spacer(modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Component type indicator
                ComponentTypeIndicator(component)

                Spacer(modifier = Modifier.width(8.dp))

                // Component name and ID
                Column {
                    Text(
                        text = component.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "#${component.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Property count badge
                PropertyCountBadge(component.properties.size)
            }

            // Children
            AnimatedVisibility(
                visible = expanded && hasChildren,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    component.children?.forEach { child ->
                        ComponentTreeNode(
                            component = child,
                            selectedComponent = selectedComponent,
                            onSelectComponent = onSelectComponent,
                            depth = depth + 1
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ComponentTypeIndicator(component: IRComponent) {
        val layoutType = detectLayoutType(component)
        val (color, label) = when (layoutType) {
            LayoutType.GRID -> Color(0xFF4CAF50) to "G"
            LayoutType.FLEX_ROW -> Color(0xFF2196F3) to "R"
            LayoutType.FLEX_COLUMN -> Color(0xFF9C27B0) to "C"
            LayoutType.BLOCK -> Color(0xFF757575) to "B"
        }

        Box(
            modifier = Modifier
                .size(20.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    @Composable
    private fun PropertyCountBadge(count: Int) {
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }

    /**
     * Property inspector panel.
     */
    @Composable
    fun PropertyInspector(component: IRComponent) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Group properties by category
            val grouped = component.properties.groupBy { categorizeProperty(it) }

            grouped.forEach { (category, properties) ->
                item {
                    PropertyCategoryHeader(category)
                }
                items(properties) { property ->
                    PropertyRow(property)
                }
            }
        }
    }

    @Composable
    private fun PropertyCategoryHeader(category: String) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
    }

    @Composable
    private fun PropertyRow(property: IRProperty) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = property.type,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF1565C0),
                    modifier = Modifier.weight(0.4f)
                )

                Text(
                    text = property.data?.toString()?.take(100) ?: "null",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = Color.DarkGray,
                    modifier = Modifier.weight(0.6f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    /**
     * Layout visualization inspector.
     */
    @Composable
    fun LayoutInspector(component: IRComponent) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Layout Type
            val layoutType = detectLayoutType(component)
            Text(
                text = "Layout Type: ${layoutType.name}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Box model visualization
            BoxModelVisualization(component)

            Spacer(modifier = Modifier.height(16.dp))

            // Dimensions
            DimensionSection(component)
        }
    }

    @Composable
    private fun BoxModelVisualization(component: IRComponent) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFFF5DEB3)), // margin color
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(160.dp)
                    .background(Color(0xFFFFE0B2)), // border color
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(120.dp)
                        .background(Color(0xFFC8E6C9)), // padding color
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(80.dp)
                            .background(Color(0xFFBBDEFB)), // content color
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Content", color = Color.DarkGray)
                    }
                }
            }
        }

        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(Color(0xFFF5DEB3), "Margin")
            LegendItem(Color(0xFFFFE0B2), "Border")
            LegendItem(Color(0xFFC8E6C9), "Padding")
            LegendItem(Color(0xFFBBDEFB), "Content")
        }
    }

    @Composable
    private fun LegendItem(color: Color, label: String) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(color, RoundedCornerShape(2.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }

    @Composable
    private fun DimensionSection(component: IRComponent) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Dimensions", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))

                val widthProp = component.properties.find { it.type == "Width" }
                val heightProp = component.properties.find { it.type == "Height" }

                DimensionRow("Width", widthProp?.data?.toString() ?: "auto")
                DimensionRow("Height", heightProp?.data?.toString() ?: "auto")
            }
        }
    }

    @Composable
    private fun DimensionRow(label: String, value: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.Gray)
            Text(value, fontFamily = FontFamily.Monospace)
        }
    }

    /**
     * Color values inspector.
     */
    @Composable
    fun ColorInspector(component: IRComponent) {
        val colorProperties = component.properties.filter {
            it.type.contains("Color", ignoreCase = true) ||
            it.type.contains("Background", ignoreCase = true)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (colorProperties.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No color properties", color = Color.Gray)
                    }
                }
            } else {
                items(colorProperties) { property ->
                    ColorPropertyCard(property)
                }
            }
        }
    }

    @Composable
    private fun ColorPropertyCard(property: IRProperty) {
        val color = extractColorFromProperty(property)

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color preview
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color ?: Color.Transparent, RoundedCornerShape(4.dp))
                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = property.type,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = property.data?.toString() ?: "null",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }

    // Helper functions

    private enum class LayoutType {
        GRID, FLEX_ROW, FLEX_COLUMN, BLOCK
    }

    private fun detectLayoutType(component: IRComponent): LayoutType {
        val display = component.properties.find { it.type == "Display" }?.data?.toString()
        val flexDirection = component.properties.find { it.type == "FlexDirection" }?.data?.toString()

        return when {
            display?.contains("grid") == true -> LayoutType.GRID
            display?.contains("flex") == true && flexDirection?.contains("row") == true -> LayoutType.FLEX_ROW
            display?.contains("flex") == true -> LayoutType.FLEX_COLUMN
            else -> LayoutType.BLOCK
        }
    }

    private fun categorizeProperty(property: IRProperty): String {
        return when {
            property.type.contains("Color", ignoreCase = true) ||
            property.type.contains("Background", ignoreCase = true) -> "Colors"

            property.type.contains("Width", ignoreCase = true) ||
            property.type.contains("Height", ignoreCase = true) ||
            property.type.contains("Size", ignoreCase = true) -> "Dimensions"

            property.type.contains("Margin", ignoreCase = true) ||
            property.type.contains("Padding", ignoreCase = true) ||
            property.type.contains("Gap", ignoreCase = true) -> "Spacing"

            property.type.contains("Border", ignoreCase = true) ||
            property.type.contains("Outline", ignoreCase = true) -> "Borders"

            property.type.contains("Font", ignoreCase = true) ||
            property.type.contains("Text", ignoreCase = true) ||
            property.type.contains("Letter", ignoreCase = true) ||
            property.type.contains("Line", ignoreCase = true) -> "Typography"

            property.type.contains("Display", ignoreCase = true) ||
            property.type.contains("Flex", ignoreCase = true) ||
            property.type.contains("Grid", ignoreCase = true) ||
            property.type.contains("Align", ignoreCase = true) ||
            property.type.contains("Justify", ignoreCase = true) -> "Layout"

            property.type.contains("Transform", ignoreCase = true) ||
            property.type.contains("Rotate", ignoreCase = true) ||
            property.type.contains("Scale", ignoreCase = true) ||
            property.type.contains("Translate", ignoreCase = true) -> "Transforms"

            property.type.contains("Animation", ignoreCase = true) ||
            property.type.contains("Transition", ignoreCase = true) -> "Animations"

            property.type.contains("Filter", ignoreCase = true) ||
            property.type.contains("Shadow", ignoreCase = true) ||
            property.type.contains("Opacity", ignoreCase = true) -> "Effects"

            else -> "Other"
        }
    }

    private fun extractColorFromProperty(property: IRProperty): Color? {
        val data = property.data?.toString() ?: return null

        // Try to extract sRGB values
        val srgbMatch = Regex("""r=(\d+\.?\d*),\s*g=(\d+\.?\d*),\s*b=(\d+\.?\d*)""").find(data)
        if (srgbMatch != null) {
            val r = srgbMatch.groupValues[1].toFloatOrNull() ?: return null
            val g = srgbMatch.groupValues[2].toFloatOrNull() ?: return null
            val b = srgbMatch.groupValues[3].toFloatOrNull() ?: return null
            return Color(r, g, b)
        }

        // Try hex color
        val hexMatch = Regex("""#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{3})""").find(data)
        if (hexMatch != null) {
            return try {
                Color(android.graphics.Color.parseColor(hexMatch.value))
            } catch (e: Exception) {
                null
            }
        }

        return null
    }
}
