package com.styleconverter.test.style.appearance.borders.radius

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

/**
 * Applies border radius to a Modifier using RoundedCornerShape.
 */
object BorderRadiusApplier {

    /**
     * Apply border radius using clip modifier.
     *
     * @param modifier The modifier to apply radius to.
     * @param config The border radius configuration for all corners.
     * @return Modified modifier with border radius applied.
     */
    fun applyRadius(modifier: Modifier, config: BorderRadiusConfig): Modifier {
        if (!config.hasRadius) return modifier

        return modifier.clip(
            RoundedCornerShape(
                topStart = config.topStart,
                topEnd = config.topEnd,
                bottomEnd = config.bottomEnd,
                bottomStart = config.bottomStart
            )
        )
    }
}
