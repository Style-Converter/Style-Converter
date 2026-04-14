package com.styleconverter.test.style.appearance.borders.radius

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Configuration for border radius on all four corners.
 *
 * Uses start/end naming to align with Compose's RTL-aware naming convention.
 * - topStart = top-left in LTR, top-right in RTL
 * - topEnd = top-right in LTR, top-left in RTL
 * - bottomEnd = bottom-right in LTR, bottom-left in RTL
 * - bottomStart = bottom-left in LTR, bottom-right in RTL
 */
data class BorderRadiusConfig(
    val topStart: Dp = 0.dp,
    val topEnd: Dp = 0.dp,
    val bottomEnd: Dp = 0.dp,
    val bottomStart: Dp = 0.dp
) {
    /**
     * Check if any corner has a radius.
     */
    val hasRadius: Boolean get() = topStart > 0.dp || topEnd > 0.dp || bottomEnd > 0.dp || bottomStart > 0.dp

    /**
     * Check if all corners have the same radius.
     */
    val isUniform: Boolean get() = topStart == topEnd && topEnd == bottomEnd && bottomEnd == bottomStart

    companion object {
        val NONE = BorderRadiusConfig()
    }
}
