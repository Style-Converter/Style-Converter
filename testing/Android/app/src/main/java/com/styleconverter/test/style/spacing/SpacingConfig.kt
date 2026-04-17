package com.styleconverter.test.style.spacing

import androidx.compose.ui.unit.Dp

data class PaddingConfig(
    val top: Dp? = null,
    val end: Dp? = null,
    val bottom: Dp? = null,
    val start: Dp? = null,
    // Logical properties
    val blockStart: Dp? = null,
    val blockEnd: Dp? = null,
    val inlineStart: Dp? = null,
    val inlineEnd: Dp? = null
) {
    val hasPadding: Boolean get() = top != null || end != null || bottom != null || start != null ||
        blockStart != null || blockEnd != null || inlineStart != null || inlineEnd != null

    // Resolved values (physical or logical fallback)
    val resolvedTop: Dp? get() = top ?: blockStart
    val resolvedEnd: Dp? get() = end ?: inlineEnd
    val resolvedBottom: Dp? get() = bottom ?: blockEnd
    val resolvedStart: Dp? get() = start ?: inlineStart
}

data class MarginConfig(
    val top: Dp? = null,
    val end: Dp? = null,
    val bottom: Dp? = null,
    val start: Dp? = null,
    // Logical properties
    val blockStart: Dp? = null,
    val blockEnd: Dp? = null,
    val inlineStart: Dp? = null,
    val inlineEnd: Dp? = null
) {
    val hasMargin: Boolean get() = top != null || end != null || bottom != null || start != null ||
        blockStart != null || blockEnd != null || inlineStart != null || inlineEnd != null

    val resolvedTop: Dp? get() = top ?: blockStart
    val resolvedEnd: Dp? get() = end ?: inlineEnd
    val resolvedBottom: Dp? get() = bottom ?: blockEnd
    val resolvedStart: Dp? get() = start ?: inlineStart
}

data class GapConfig(
    val rowGap: Dp? = null,
    val columnGap: Dp? = null
) {
    val hasGap: Boolean get() = rowGap != null || columnGap != null
}
