package com.styleconverter.test

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset

/**
 * Runtime component builder that creates actual Composables from JSON data
 */
@Composable
fun RuntimeComponent(component: ComposeComponent) {
    // Build base modifier from strings
    val baseModifier = buildModifierChain(component.baseModifiers)

    // Determine the container type from the composableCode
    val containerType = detectContainerType(component.composableCode)

    when (containerType) {
        ContainerType.ROW -> {
            Row(modifier = baseModifier) {
                ComponentContent(component)
            }
        }
        ContainerType.COLUMN -> {
            Column(modifier = baseModifier) {
                ComponentContent(component)
            }
        }
        else -> {
            Box(modifier = baseModifier) {
                ComponentContent(component)
            }
        }
    }
}

@Composable
fun ComponentContent(component: ComposeComponent) {
    // Display 3 child boxes like the web environment (vertical column)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xCCFFFFFF), RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Child ${index + 1}",
                    fontSize = 12.sp,
                    color = Color(0xFF616161)
                )
            }
        }
    }
}

enum class ContainerType {
    BOX, ROW, COLUMN
}

fun detectContainerType(code: String): ContainerType {
    return when {
        code.contains("Row(") -> ContainerType.ROW
        code.contains("Column(") -> ContainerType.COLUMN
        else -> ContainerType.BOX
    }
}

@Composable
fun buildModifierChain(modifiers: List<String>): Modifier {
    var result: Modifier = Modifier

    modifiers.forEach { modStr ->
        // Skip comments
        if (modStr.startsWith("/*")) return@forEach

        result = result.then(parseModifier(modStr))
    }

    return result
}

@Composable
fun parseModifier(modStr: String): Modifier {
    return when {
        modStr.startsWith("padding(") -> parsePadding(modStr)
        modStr.startsWith("background(") -> parseBackground(modStr)
        modStr.startsWith("clip(") -> parseClip(modStr)
        modStr.startsWith("border(") -> parseBorder(modStr)
        modStr.startsWith("shadow(") -> parseShadow(modStr)
        modStr.startsWith("alpha(") -> parseAlpha(modStr)
        modStr.startsWith("width(") -> parseWidth(modStr)
        modStr.startsWith("height(") -> parseHeight(modStr)
        modStr.startsWith("fillMaxWidth(") -> parseFillMaxWidth(modStr)
        modStr.startsWith("fillMaxHeight(") -> parseFillMaxHeight(modStr)
        modStr.startsWith("widthIn(") -> parseWidthIn(modStr)
        modStr.startsWith("heightIn(") -> parseHeightIn(modStr)
        modStr.startsWith("scale(") -> parseScale(modStr)
        modStr.startsWith("rotate(") -> parseRotate(modStr)
        modStr.startsWith("drawBehind") -> parseDrawBehind(modStr)
        modStr.startsWith("drawWithContent") -> parseDrawWithContent(modStr)
        else -> Modifier
    }
}

// Modifier parsers
fun parsePadding(modStr: String): Modifier {
    return try {
        if (modStr.contains("top =")) {
            val top = extractValue(modStr, "top = (\\d+)\\.dp")
            val end = extractValue(modStr, "end = (\\d+)\\.dp")
            val bottom = extractValue(modStr, "bottom = (\\d+)\\.dp")
            val start = extractValue(modStr, "start = (\\d+)\\.dp")
            Modifier.padding(start = start.dp, top = top.dp, end = end.dp, bottom = bottom.dp)
        } else {
            val value = extractValue(modStr, "padding\\((\\d+)\\.dp\\)")
            Modifier.padding(value.dp)
        }
    } catch (e: Exception) {
        Modifier
    }
}

fun parseBackground(modStr: String): Modifier {
    return try {
        val color = extractColor(modStr)
        if (color != null) Modifier.background(color) else Modifier
    } catch (e: Exception) {
        Modifier
    }
}

fun parseClip(modStr: String): Modifier {
    return try {
        if (modStr.contains("RoundedCornerShape")) {
            if (modStr.contains("topStart =")) {
                val topStart = extractValue(modStr, "topStart = (\\d+)\\.dp")
                val topEnd = extractValue(modStr, "topEnd = (\\d+)\\.dp")
                val bottomEnd = extractValue(modStr, "bottomEnd = (\\d+)\\.dp")
                val bottomStart = extractValue(modStr, "bottomStart = (\\d+)\\.dp")
                Modifier.clip(RoundedCornerShape(
                    topStart = topStart.dp,
                    topEnd = topEnd.dp,
                    bottomEnd = bottomEnd.dp,
                    bottomStart = bottomStart.dp
                ))
            } else {
                val radius = extractValue(modStr, "RoundedCornerShape\\((\\d+)\\.dp\\)")
                Modifier.clip(RoundedCornerShape(radius.dp))
            }
        } else {
            Modifier
        }
    } catch (e: Exception) {
        Modifier
    }
}

fun parseBorder(modStr: String): Modifier {
    return try {
        val width = extractValue(modStr, "border\\((\\d+)\\.dp")
        val color = extractColor(modStr)
        if (color != null) Modifier.border(width.dp, color) else Modifier
    } catch (e: Exception) {
        Modifier
    }
}

fun parseShadow(modStr: String): Modifier {
    return try {
        val elevation = extractFloatValue(modStr, "shadow\\(([\\d.]+)\\.dp\\)")
        Modifier.shadow(elevation.dp)
    } catch (e: Exception) {
        Modifier
    }
}

fun parseAlpha(modStr: String): Modifier {
    return try {
        val alpha = extractFloatValue(modStr, "alpha\\(([\\d.]+)f\\)")
        Modifier.alpha(alpha)
    } catch (e: Exception) {
        Modifier
    }
}

fun parseWidth(modStr: String): Modifier {
    return try {
        val width = extractValue(modStr, "width\\((\\d+)\\.dp\\)")
        Modifier.width(width.dp)
    } catch (e: Exception) {
        Modifier
    }
}

fun parseHeight(modStr: String): Modifier {
    return try {
        val height = extractValue(modStr, "height\\((\\d+)\\.dp\\)")
        Modifier.height(height.dp)
    } catch (e: Exception) {
        Modifier
    }
}

fun parseFillMaxWidth(modStr: String): Modifier {
    return try {
        if (modStr.contains("f)")) {
            val fraction = extractFloatValue(modStr, "fillMaxWidth\\(([\\d.]+)f\\)")
            Modifier.fillMaxWidth(fraction)
        } else {
            Modifier.fillMaxWidth()
        }
    } catch (e: Exception) {
        Modifier.fillMaxWidth()
    }
}

fun parseFillMaxHeight(modStr: String): Modifier {
    return try {
        if (modStr.contains("f)")) {
            val fraction = extractFloatValue(modStr, "fillMaxHeight\\(([\\d.]+)f\\)")
            Modifier.fillMaxHeight(fraction)
        } else {
            Modifier.fillMaxHeight()
        }
    } catch (e: Exception) {
        Modifier.fillMaxHeight()
    }
}

fun parseWidthIn(modStr: String): Modifier {
    return try {
        when {
            modStr.contains("min =") && modStr.contains("max =") -> {
                val min = extractValue(modStr, "min = (\\d+)\\.dp")
                val max = extractValue(modStr, "max = (\\d+)\\.dp")
                Modifier.widthIn(min = min.dp, max = max.dp)
            }
            modStr.contains("min =") -> {
                val min = extractValue(modStr, "min = (\\d+)\\.dp")
                Modifier.widthIn(min = min.dp)
            }
            modStr.contains("max =") -> {
                val max = extractValue(modStr, "max = (\\d+)\\.dp")
                Modifier.widthIn(max = max.dp)
            }
            else -> Modifier
        }
    } catch (e: Exception) {
        Modifier
    }
}

fun parseHeightIn(modStr: String): Modifier {
    return try {
        when {
            modStr.contains("min =") && modStr.contains("max =") -> {
                val min = extractValue(modStr, "min = (\\d+)\\.dp")
                val max = extractValue(modStr, "max = (\\d+)\\.dp")
                Modifier.heightIn(min = min.dp, max = max.dp)
            }
            modStr.contains("min =") -> {
                val min = extractValue(modStr, "min = (\\d+)\\.dp")
                Modifier.heightIn(min = min.dp)
            }
            modStr.contains("max =") -> {
                val max = extractValue(modStr, "max = (\\d+)\\.dp")
                Modifier.heightIn(max = max.dp)
            }
            else -> Modifier
        }
    } catch (e: Exception) {
        Modifier
    }
}

fun parseScale(modStr: String): Modifier {
    return try {
        val scale = extractFloatValue(modStr, "scale\\(([\\d.]+)f\\)")
        Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
    } catch (e: Exception) {
        Modifier
    }
}

fun parseRotate(modStr: String): Modifier {
    return try {
        val angle = extractFloatValue(modStr, "rotate\\(([\\d.]+)f\\)")
        Modifier.graphicsLayer(rotationZ = angle)
    } catch (e: Exception) {
        Modifier
    }
}

fun parseDrawBehind(modStr: String): Modifier {
    return try {
        // Extract all drawLine calls from the drawBehind block
        val drawLinePattern = """drawLine\(Color\(0x([0-9A-Fa-f]+)\),\s*Offset\((.*?)\),\s*Offset\((.*?)\),\s*([\d.]+)\.dp\.toPx\(\)\)""".toRegex()
        val matches = drawLinePattern.findAll(modStr).toList()

        if (matches.isEmpty()) return Modifier

        return Modifier.drawBehind {
            matches.forEach { match ->
                try {
                    val colorHex = match.groupValues[1]
                    val start = match.groupValues[2]
                    val end = match.groupValues[3]
                    val strokeWidth = match.groupValues[4]

                    val color = Color(android.graphics.Color.parseColor("#$colorHex"))
                    val startOffset = parseOffsetExpression(start, size.width, size.height, this)
                    val endOffset = parseOffsetExpression(end, size.width, size.height, this)
                    val width = strokeWidth.toFloatOrNull()?.dp?.toPx() ?: 0f

                    drawLine(
                        color = color,
                        start = startOffset,
                        end = endOffset,
                        strokeWidth = width
                    )
                } catch (e: Exception) {
                    // Skip this drawLine if parsing fails
                }
            }
        }
    } catch (e: Exception) {
        Modifier
    }
}

fun parseDrawWithContent(modStr: String): Modifier {
    return try {
        // Extract all drawLine calls from the drawWithContent block (after drawContent())
        val drawLinePattern = """drawLine\(Color\(0x([0-9A-Fa-f]+)\),\s*Offset\((.*?)\),\s*Offset\((.*?)\),\s*([\d.]+)\.dp\.toPx\(\)\)""".toRegex()
        val matches = drawLinePattern.findAll(modStr).toList()

        if (matches.isEmpty()) return Modifier

        return Modifier.drawWithContent {
            // First draw the content (background, etc.)
            drawContent()

            // Then draw borders on top
            matches.forEach { match ->
                try {
                    val colorHex = match.groupValues[1]
                    val start = match.groupValues[2]
                    val end = match.groupValues[3]
                    val strokeWidth = match.groupValues[4]

                    val color = Color(android.graphics.Color.parseColor("#$colorHex"))
                    val startOffset = parseOffsetExpression(start, size.width, size.height, this)
                    val endOffset = parseOffsetExpression(end, size.width, size.height, this)
                    val width = strokeWidth.toFloatOrNull()?.dp?.toPx() ?: 0f

                    drawLine(
                        color = color,
                        start = startOffset,
                        end = endOffset,
                        strokeWidth = width
                    )
                } catch (e: Exception) {
                    // Skip this drawLine if parsing fails
                }
            }
        }
    } catch (e: Exception) {
        Modifier
    }
}

// Helper function to parse Offset expressions in drawBehind context
fun parseOffsetExpression(offsetStr: String, width: Float, height: Float, density: androidx.compose.ui.unit.Density): Offset {
    // Parse expressions like "0f, 2.dp.toPx() / 2" or "size.width, 2.dp.toPx() / 2"
    val parts = offsetStr.split(",").map { it.trim() }
    if (parts.size != 2) return Offset.Zero

    val x = evaluateSingleExpression(parts[0], width, height, density)
    val y = evaluateSingleExpression(parts[1], width, height, density)

    return Offset(x, y)
}

fun evaluateSingleExpression(expr: String, width: Float, height: Float, density: androidx.compose.ui.unit.Density): Float {
    return try {
        with(density) {
            when {
                expr == "0f" -> 0f
                expr.contains("size.width") -> {
                    // Handle "size.width - 1.dp.toPx() / 2" etc
                    if (expr.contains("-")) {
                        val dpValue = extractFloatValue(expr, """([\d.]+)\.dp\.toPx\(\)""")
                        val divisor = if (expr.contains("/")) extractFloatValue(expr, """/ ([\d.]+)""") else 1f
                        width - (dpValue.dp.toPx() / divisor)
                    } else {
                        width
                    }
                }
                expr.contains("size.height") -> {
                    // Handle "size.height - 0.dp.toPx() / 2" etc
                    if (expr.contains("-")) {
                        val dpValue = extractFloatValue(expr, """([\d.]+)\.dp\.toPx\(\)""")
                        val divisor = if (expr.contains("/")) extractFloatValue(expr, """/ ([\d.]+)""") else 1f
                        height - (dpValue.dp.toPx() / divisor)
                    } else {
                        height
                    }
                }
                expr.contains("dp.toPx()") -> {
                    // Handle "2.dp.toPx() / 2" etc
                    val dpValue = extractFloatValue(expr, """([\d.]+)\.dp\.toPx\(\)""")
                    val divisor = if (expr.contains("/")) extractFloatValue(expr, """/ ([\d.]+)""") else 1f
                    dpValue.dp.toPx() / divisor
                }
                else -> expr.replace("f", "").toFloatOrNull() ?: 0f
            }
        }
    } catch (e: Exception) {
        0f
    }
}

// Helper functions
fun extractValue(str: String, pattern: String): Int {
    val regex = pattern.toRegex()
    return regex.find(str)?.groupValues?.get(1)?.toIntOrNull() ?: 0
}

fun extractFloatValue(str: String, pattern: String): Float {
    val regex = pattern.toRegex()
    return regex.find(str)?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
}

fun extractColor(str: String): Color? {
    return try {
        val regex = """Color\(0x([0-9A-Fa-f]+)\)""".toRegex()
        val hex = regex.find(str)?.groupValues?.get(1) ?: return null
        Color(android.graphics.Color.parseColor("#$hex"))
    } catch (e: Exception) {
        null
    }
}
