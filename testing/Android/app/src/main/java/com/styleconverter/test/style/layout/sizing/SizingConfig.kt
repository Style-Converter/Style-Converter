package com.styleconverter.test.style.layout.sizing

import androidx.compose.ui.unit.Dp

/**
 * Configuration for CSS sizing properties.
 *
 * Handles width, height, min/max constraints and logical properties
 * (block-size, inline-size) for writing mode support.
 */
data class SizingConfig(
    val width: SizeValue? = null,
    val height: SizeValue? = null,
    val minWidth: Dp? = null,
    val maxWidth: Dp? = null,
    val minHeight: Dp? = null,
    val maxHeight: Dp? = null,
    // Logical sizes (for writing mode support)
    val blockSize: SizeValue? = null,
    val inlineSize: SizeValue? = null,
    val minBlockSize: Dp? = null,
    val maxBlockSize: Dp? = null,
    val minInlineSize: Dp? = null,
    val maxInlineSize: Dp? = null
) {
    /**
     * Returns true if any sizing property is defined.
     */
    val hasSizing: Boolean
        get() = width != null || height != null ||
                minWidth != null || maxWidth != null ||
                minHeight != null || maxHeight != null ||
                blockSize != null || inlineSize != null ||
                minBlockSize != null || maxBlockSize != null ||
                minInlineSize != null || maxInlineSize != null

    /**
     * Returns true if any width-related property is defined.
     */
    val hasWidthConstraints: Boolean
        get() = width != null || minWidth != null || maxWidth != null ||
                inlineSize != null || minInlineSize != null || maxInlineSize != null

    /**
     * Returns true if any height-related property is defined.
     */
    val hasHeightConstraints: Boolean
        get() = height != null || minHeight != null || maxHeight != null ||
                blockSize != null || minBlockSize != null || maxBlockSize != null
}

/**
 * Represents a CSS size value which can be a fixed length, percentage,
 * or a keyword like auto, fill, or fit-content.
 */
sealed interface SizeValue {
    /**
     * Fixed size in Dp (from px, pt, em, rem, etc.)
     */
    data class Fixed(val dp: Dp) : SizeValue

    /**
     * Percentage of parent dimension (0.0-1.0 fraction)
     */
    data class Percentage(val fraction: Float) : SizeValue

    /**
     * Fill maximum available space (100% of parent)
     * Maps to CSS: max-content, fill-available, 100%
     */
    data object FillMax : SizeValue

    /**
     * Size to content (intrinsic sizing)
     * Maps to CSS: min-content, fit-content
     */
    data object WrapContent : SizeValue

    /**
     * Auto sizing (default behavior)
     * Maps to CSS: auto
     */
    data object Auto : SizeValue
}
