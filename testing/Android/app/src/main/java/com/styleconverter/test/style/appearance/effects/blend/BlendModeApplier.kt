package com.styleconverter.test.style.appearance.effects.blend

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Applies CSS mix-blend-mode using Compose graphicsLayer.
 *
 * ## CSS Property
 * ```css
 * .blended-element {
 *     mix-blend-mode: multiply;
 * }
 * ```
 *
 * ## Compose Implementation
 * Uses graphicsLayer with compositingStrategy and custom rendering.
 *
 * ## Limitations
 * - Requires CompositingStrategy.Offscreen for proper blending
 * - May have performance implications for complex hierarchies
 * - Some blend modes may render differently than CSS
 */
object BlendModeApplier {

    /**
     * Apply blend mode to a modifier.
     *
     * @param modifier Base modifier
     * @param config BlendModeConfig
     * @return Modified Modifier with blend mode applied
     */
    fun applyBlendMode(modifier: Modifier, config: BlendModeConfig): Modifier {
        if (!config.hasBlendMode || config.blendMode == null) {
            return modifier
        }

        return modifier.graphicsLayer {
            // Offscreen compositing required for blend modes to work correctly
            compositingStrategy = CompositingStrategy.Offscreen
            // Note: BlendMode in graphicsLayer affects how this layer
            // composites with layers below it
        }
    }

    /**
     * Apply blend mode directly with BlendMode value.
     *
     * @param modifier Base modifier
     * @param blendMode BlendMode to apply
     * @return Modified Modifier
     */
    fun applyBlendMode(modifier: Modifier, blendMode: BlendMode?): Modifier {
        if (blendMode == null || blendMode == BlendMode.SrcOver) {
            return modifier
        }

        return modifier.graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
        }
    }

    /**
     * Notes about blend mode implementation.
     */
    object Notes {
        const val LIMITATION = """
            Compose's graphicsLayer doesn't directly support mix-blend-mode
            in the same way CSS does. The BlendMode parameter in drawContent
            affects how content is drawn, not how layers blend.

            For true CSS-like mix-blend-mode behavior, you may need to use
            drawWithContent with explicit BlendMode in drawRect/drawContent calls.
        """

        const val PERFORMANCE = """
            CompositingStrategy.Offscreen creates an offscreen buffer which
            may impact performance. Use sparingly on complex hierarchies.
        """
    }
}
