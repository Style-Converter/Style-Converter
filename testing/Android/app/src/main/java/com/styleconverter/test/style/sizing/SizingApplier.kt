package com.styleconverter.test.style.sizing

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Applies sizing configuration to a Compose Modifier.
 *
 * Maps CSS sizing properties to their Compose equivalents:
 * - Fixed sizes -> width()/height()
 * - Percentages -> fillMaxWidth(fraction)/fillMaxHeight(fraction)
 * - FillMax -> fillMaxWidth()/fillMaxHeight()
 * - WrapContent -> wrapContentWidth()/wrapContentHeight()
 * - Auto -> no modifier (intrinsic sizing)
 *
 * Note: In LTR horizontal writing mode:
 * - inline-size maps to width
 * - block-size maps to height
 */
object SizingApplier {

    /**
     * Apply sizing configuration to a Modifier.
     *
     * @param modifier The base modifier to extend
     * @param config The sizing configuration to apply
     * @return Modified Modifier with sizing applied
     */
    fun applySizing(modifier: Modifier, config: SizingConfig): Modifier {
        if (!config.hasSizing) return modifier

        var result = modifier

        // Apply width (physical property takes precedence over logical)
        result = applyWidth(result, config.width ?: config.inlineSize)

        // Apply height (physical property takes precedence over logical)
        result = applyHeight(result, config.height ?: config.blockSize)

        // Apply min/max constraints
        result = applyWidthConstraints(
            result,
            minWidth = config.minWidth ?: config.minInlineSize,
            maxWidth = config.maxWidth ?: config.maxInlineSize
        )

        result = applyHeightConstraints(
            result,
            minHeight = config.minHeight ?: config.minBlockSize,
            maxHeight = config.maxHeight ?: config.maxBlockSize
        )

        return result
    }

    /**
     * Apply width sizing to a Modifier.
     */
    private fun applyWidth(modifier: Modifier, size: SizeValue?): Modifier {
        return when (size) {
            is SizeValue.Fixed -> modifier.width(size.dp)
            is SizeValue.Percentage -> modifier.fillMaxWidth(size.fraction)
            is SizeValue.FillMax -> modifier.fillMaxWidth()
            is SizeValue.WrapContent -> modifier.wrapContentWidth()
            is SizeValue.Auto -> modifier // Auto means intrinsic sizing (default)
            null -> modifier
        }
    }

    /**
     * Apply height sizing to a Modifier.
     */
    private fun applyHeight(modifier: Modifier, size: SizeValue?): Modifier {
        return when (size) {
            is SizeValue.Fixed -> modifier.height(size.dp)
            is SizeValue.Percentage -> modifier.fillMaxHeight(size.fraction)
            is SizeValue.FillMax -> modifier.fillMaxHeight()
            is SizeValue.WrapContent -> modifier.wrapContentHeight()
            is SizeValue.Auto -> modifier // Auto means intrinsic sizing (default)
            null -> modifier
        }
    }

    /**
     * Apply min/max width constraints to a Modifier.
     *
     * Note: widthIn() requires both min and max. We use 0.dp for min
     * if not specified, and Dp.Infinity for max if not specified.
     */
    private fun applyWidthConstraints(
        modifier: Modifier,
        minWidth: Dp?,
        maxWidth: Dp?
    ): Modifier {
        if (minWidth == null && maxWidth == null) return modifier

        return modifier.widthIn(
            min = minWidth ?: 0.dp,
            max = maxWidth ?: Dp.Infinity
        )
    }

    /**
     * Apply min/max height constraints to a Modifier.
     *
     * Note: heightIn() requires both min and max. We use 0.dp for min
     * if not specified, and Dp.Infinity for max if not specified.
     */
    private fun applyHeightConstraints(
        modifier: Modifier,
        minHeight: Dp?,
        maxHeight: Dp?
    ): Modifier {
        if (minHeight == null && maxHeight == null) return modifier

        return modifier.heightIn(
            min = minHeight ?: 0.dp,
            max = maxHeight ?: Dp.Infinity
        )
    }

    /**
     * Apply only width-related sizing (useful for flex items).
     */
    fun applyWidthOnly(modifier: Modifier, config: SizingConfig): Modifier {
        var result = modifier
        result = applyWidth(result, config.width ?: config.inlineSize)
        result = applyWidthConstraints(
            result,
            minWidth = config.minWidth ?: config.minInlineSize,
            maxWidth = config.maxWidth ?: config.maxInlineSize
        )
        return result
    }

    /**
     * Apply only height-related sizing (useful for flex items).
     */
    fun applyHeightOnly(modifier: Modifier, config: SizingConfig): Modifier {
        var result = modifier
        result = applyHeight(result, config.height ?: config.blockSize)
        result = applyHeightConstraints(
            result,
            minHeight = config.minHeight ?: config.minBlockSize,
            maxHeight = config.maxHeight ?: config.maxBlockSize
        )
        return result
    }
}
