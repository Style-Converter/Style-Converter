package com.styleconverter.test.style.layout.spacing

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

object SpacingApplier {

    fun applyPadding(modifier: Modifier, config: PaddingConfig): Modifier {
        if (!config.hasPadding) return modifier

        val top = config.resolvedTop ?: 0.dp
        val end = config.resolvedEnd ?: 0.dp
        val bottom = config.resolvedBottom ?: 0.dp
        val start = config.resolvedStart ?: 0.dp

        return modifier.padding(start = start, top = top, end = end, bottom = bottom)
    }

    fun applyMargin(modifier: Modifier, config: MarginConfig): Modifier {
        if (!config.hasMargin) return modifier

        // Compose doesn't have margin - use offset for positioning effect
        val x = (config.resolvedStart ?: 0.dp) - (config.resolvedEnd ?: 0.dp)
        val y = (config.resolvedTop ?: 0.dp) - (config.resolvedBottom ?: 0.dp)

        return if (x.value != 0f || y.value != 0f) {
            modifier.offset(x = x, y = y)
        } else {
            modifier
        }
    }
}
