package com.styleconverter.test.style.effects.blend

// Applies CSS mix-blend-mode and background-blend-mode using Compose drawWithContent +
// an explicit Paint with the requested BlendMode. graphicsLayer alone cannot express
// "composite this subtree against the parent using BlendMode X" — drawWithContent +
// saveLayer (via drawIntoCanvas) is the canonical workaround on Compose.

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Applies CSS mix-blend-mode using Compose drawing primitives.
 *
 * ## CSS Property
 * ```css
 * .blended-element {
 *     mix-blend-mode: multiply;
 * }
 * ```
 *
 * ## Compose Implementation
 * Uses drawWithContent + Paint(blendMode) wrapped in Canvas.saveLayer so the
 * subtree composites against whatever was drawn below with the target blend
 * mode. An offscreen graphicsLayer is used underneath so antialiased content
 * is captured before blending.
 *
 * ## Limitations
 * - CSS mix-blend-mode requires the sibling underneath to already be drawn
 *   in the same compositing group. In Compose this is usually the parent Box.
 * - Some CSS modes (e.g. plus-darker, color-dodge) map imperfectly on older
 *   Android versions — the BlendMode enum is available on API 29+ for every
 *   mode we emit; older APIs fall back to Compose's default SrcOver.
 * - `mix-blend-mode: normal` is a no-op (SrcOver already is Compose default).
 */
object BlendModeApplier {

    /**
     * Apply blend mode to a modifier using the config wrapper.
     *
     * @param modifier Base modifier
     * @param config BlendModeConfig
     * @return Modified Modifier with blend mode applied
     */
    fun applyBlendMode(modifier: Modifier, config: BlendModeConfig): Modifier {
        // No blend mode, or SrcOver (normal) — return as-is. hasBlendMode already
        // filters SrcOver, but we double-check for safety.
        if (!config.hasBlendMode || config.blendMode == null) {
            return modifier
        }
        return applyBlendMode(modifier, config.blendMode)
    }

    /**
     * Apply blend mode directly with BlendMode value.
     *
     * @param modifier Base modifier
     * @param blendMode BlendMode to apply
     * @return Modified Modifier
     */
    fun applyBlendMode(modifier: Modifier, blendMode: BlendMode?): Modifier {
        // SrcOver (which CSS "normal" maps to) is Compose's default — skip.
        if (blendMode == null || blendMode == BlendMode.SrcOver) {
            return modifier
        }
        // Step 1: put the child content in its own offscreen layer so the blend
        // operation has a finished bitmap to work with (avoids partial alpha
        // artifacts at edges). CompositingStrategy.Offscreen does exactly that.
        return modifier
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            // Step 2: wrap the draw pass in a saveLayer that carries the target
            // blend mode. Everything drawn between saveLayer and restore will
            // composite against the canvas below using this BlendMode.
            .drawWithContent {
                drawIntoCanvas { canvas ->
                    // Paint carries the blend mode for the saveLayer call.
                    val paint = Paint().apply { this.blendMode = blendMode }
                    // saveLayer takes a rect + paint; size.toRect() covers the
                    // whole drawing area. A matching restore() is issued below
                    // to pop the layer once content is drawn.
                    canvas.saveLayer(
                        bounds = androidx.compose.ui.geometry.Rect(
                            left = 0f,
                            top = 0f,
                            right = size.width,
                            bottom = size.height
                        ),
                        paint = paint
                    )
                    // Draw child content into the layer we just pushed.
                    drawContent()
                    // Pop the layer — this is where the actual blending happens:
                    // the saveLayer's paint.blendMode is used to composite our
                    // buffered draws back onto the underlying canvas.
                    canvas.restore()
                }
            }
    }

    /**
     * Notes about blend mode implementation.
     */
    object Notes {
        const val LIMITATION = """
            CSS mix-blend-mode blends this element with what is painted behind
            it (its stacking-context siblings). In Compose, that means whatever
            was drawn into the parent Box before this modifier runs. The
            drawWithContent+saveLayer approach above is the closest analog.
        """

        const val PERFORMANCE = """
            CompositingStrategy.Offscreen allocates a bitmap buffer per element
            using a non-normal blend mode. Avoid on hot paths with many
            blended children.
        """
    }
}
