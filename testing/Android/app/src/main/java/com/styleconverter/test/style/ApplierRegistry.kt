package com.styleconverter.test.style

/**
 * Central registry of all CSS property appliers.
 *
 * This file provides a single point of reference for all appliers in the system,
 * organized by category. Use this to discover available appliers and their capabilities.
 *
 * ## Usage
 * ```kotlin
 * // Apply colors
 * ColorApplier.applyBackgroundColor(modifier, colorConfig)
 *
 * // Apply transforms
 * TransformApplier.applyTransform(modifier, transformConfig)
 *
 * // Use the combined style applier
 * StyleApplier.applyAllStyles(modifier, properties)
 * ```
 *
 * ## Coverage Summary
 * - Total Appliers: 46
 * - Coverage: 93.9% of extractors have matching appliers
 * - Critical Gaps: RESOLVED (FlexApplier, GridApplier now implemented)
 * - Medium Priority: RESOLVED (Table, MultiColumn, FormStyling, ContainerQuery implemented)
 */
object ApplierRegistry {

    // =============================================================================
    // CORE LAYOUT
    // =============================================================================

    /**
     * Sizing appliers for width, height, min/max constraints.
     * @see com.styleconverter.test.style.layout.sizing.SizingApplier
     */
    object Sizing {
        const val PACKAGE = "com.styleconverter.test.style.layout.sizing"
        val appliers = listOf("SizingApplier")
        val cssProperties = listOf(
            "width", "height",
            "min-width", "max-width",
            "min-height", "max-height",
            "block-size", "inline-size"
        )
    }

    /**
     * Position appliers for absolute/relative positioning.
     * @see com.styleconverter.test.style.layout.position.PositionApplier
     */
    object Position {
        const val PACKAGE = "com.styleconverter.test.style.layout.position"
        val appliers = listOf("PositionApplier")
        val cssProperties = listOf(
            "position", "top", "right", "bottom", "left",
            "inset", "z-index"
        )
    }

    /**
     * Spacing appliers for padding and margin.
     * @see com.styleconverter.test.style.layout.spacing.SpacingApplier
     */
    object Spacing {
        const val PACKAGE = "com.styleconverter.test.style.layout.spacing"
        val appliers = listOf("SpacingApplier")
        val cssProperties = listOf(
            "padding", "padding-top", "padding-right", "padding-bottom", "padding-left",
            "margin", "margin-top", "margin-right", "margin-bottom", "margin-left",
            "gap", "row-gap", "column-gap"
        )
    }

    /**
     * Overflow handling appliers.
     * @see com.styleconverter.test.style.layout.overflow.OverflowApplier
     */
    object Overflow {
        const val PACKAGE = "com.styleconverter.test.style.layout.overflow"
        val appliers = listOf("OverflowApplier")
        val cssProperties = listOf(
            "overflow", "overflow-x", "overflow-y",
            "text-overflow"
        )
    }

    /**
     * Flexbox layout appliers.
     * @see com.styleconverter.test.style.layout.flex.FlexApplier
     */
    object Flex {
        const val PACKAGE = "com.styleconverter.test.style.layout.flex"
        val appliers = listOf("FlexApplier")
        val cssProperties = listOf(
            "display", "flex-direction", "flex-wrap", "flex-flow",
            "justify-content", "align-items", "align-content",
            "flex-grow", "flex-shrink", "flex-basis", "flex",
            "order", "align-self"
        )
    }

    /**
     * CSS Grid layout appliers.
     * @see com.styleconverter.test.style.layout.grid.GridApplier
     */
    object Grid {
        const val PACKAGE = "com.styleconverter.test.style.layout.grid"
        val appliers = listOf("GridApplier")
        val cssProperties = listOf(
            "display", "grid-template-columns", "grid-template-rows",
            "grid-template-areas", "grid-auto-flow", "grid-auto-columns",
            "grid-auto-rows", "grid-column", "grid-row", "grid-area",
            "grid-column-start", "grid-column-end", "grid-row-start", "grid-row-end",
            "justify-items", "place-items", "place-content"
        )
    }

    /**
     * Multi-column layout appliers.
     * @see com.styleconverter.test.style.layout.columns.MultiColumnApplier
     */
    object MultiColumn {
        const val PACKAGE = "com.styleconverter.test.style.layout.columns"
        val appliers = listOf("MultiColumnApplier")
        val cssProperties = listOf(
            "column-count", "column-width", "columns",
            "column-gap", "column-rule", "column-rule-width",
            "column-rule-style", "column-rule-color",
            "column-span", "column-fill"
        )
    }

    /**
     * Container query appliers.
     * @see com.styleconverter.test.style.layout.container.ContainerQueryApplier
     */
    object ContainerQuery {
        const val PACKAGE = "com.styleconverter.test.style.layout.container"
        val appliers = listOf("ContainerQueryApplier")
        val cssProperties = listOf(
            "container-type", "container-name", "container"
        )
    }

    // =============================================================================
    // VISUAL STYLING - COLORS
    // =============================================================================

    /**
     * Color appliers for background, foreground, and accent colors.
     * @see com.styleconverter.test.style.appearance.colors.ColorApplier
     * @see com.styleconverter.test.style.appearance.colors.AccentApplier
     * @see com.styleconverter.test.style.appearance.colors.BackgroundBoxApplier
     */
    object Colors {
        const val PACKAGE = "com.styleconverter.test.style.appearance.colors"
        val appliers = listOf("ColorApplier", "AccentApplier", "BackgroundBoxApplier")
        val cssProperties = listOf(
            "color", "background-color", "opacity",
            "accent-color",
            "background-clip", "background-origin"
        )
    }

    // =============================================================================
    // VISUAL STYLING - BORDERS
    // =============================================================================

    /**
     * Border appliers for all border-related properties.
     * @see com.styleconverter.test.style.appearance.borders.sides.BorderSideApplier
     * @see com.styleconverter.test.style.appearance.borders.radius.BorderRadiusApplier
     * @see com.styleconverter.test.style.appearance.borders.image.BorderImageApplier
     * @see com.styleconverter.test.style.appearance.borders.outline.OutlineApplier
     */
    object Borders {
        const val PACKAGE = "com.styleconverter.test.style.appearance.borders"
        val appliers = listOf(
            "sides/BorderSideApplier",
            "radius/BorderRadiusApplier",
            "image/BorderImageApplier",
            "outline/OutlineApplier"
        )
        val cssProperties = listOf(
            "border", "border-width", "border-style", "border-color",
            "border-top", "border-right", "border-bottom", "border-left",
            "border-radius", "border-top-left-radius", "border-top-right-radius",
            "border-image", "border-image-source", "border-image-slice",
            "outline", "outline-width", "outline-style", "outline-color", "outline-offset"
        )
    }

    // =============================================================================
    // VISUAL STYLING - EFFECTS
    // =============================================================================

    /**
     * Shadow appliers including multiple shadow support.
     * @see com.styleconverter.test.style.appearance.effects.shadow.ShadowApplier
     * @see com.styleconverter.test.style.platform.workarounds.MultipleShadowApplier
     * @see com.styleconverter.test.style.platform.workarounds.TextShadowApplier
     */
    object Shadows {
        const val PACKAGE = "com.styleconverter.test.style.appearance.effects.shadow"
        val appliers = listOf(
            "ShadowApplier",
            "platform/workarounds/MultipleShadowApplier",
            "platform/workarounds/TextShadowApplier"
        )
        val cssProperties = listOf(
            "box-shadow", "text-shadow"
        )
    }

    /**
     * Filter appliers for blur, brightness, contrast, etc.
     * @see com.styleconverter.test.style.appearance.effects.filters.FilterApplier
     * @see com.styleconverter.test.style.platform.workarounds.BackdropBlurApplier
     */
    object Filters {
        const val PACKAGE = "com.styleconverter.test.style.appearance.effects.filters"
        val appliers = listOf(
            "FilterApplier",
            "platform/workarounds/BackdropBlurApplier"
        )
        val cssProperties = listOf(
            "filter", "backdrop-filter",
            "blur", "brightness", "contrast", "grayscale",
            "hue-rotate", "invert", "opacity", "saturate", "sepia"
        )
    }

    /**
     * Clipping and masking appliers.
     * @see com.styleconverter.test.style.appearance.effects.clip.ClipPathApplier
     * @see com.styleconverter.test.style.appearance.effects.mask.MaskApplier
     * @see com.styleconverter.test.style.appearance.effects.shapes.ShapeApplier
     */
    object ClipAndMask {
        const val PACKAGE = "com.styleconverter.test.style.appearance.effects"
        val appliers = listOf(
            "clip/ClipPathApplier",
            "mask/MaskApplier",
            "shapes/ShapeApplier"
        )
        val cssProperties = listOf(
            "clip-path", "clip",
            "mask", "mask-image", "mask-size", "mask-position",
            "shape-outside"
        )
    }

    // =============================================================================
    // VISUAL STYLING - TRANSFORMS
    // =============================================================================

    /**
     * Transform appliers for 2D and 3D transforms.
     * @see com.styleconverter.test.style.appearance.transforms.TransformApplier
     * @see com.styleconverter.test.style.appearance.transforms.Transform3DApplier
     * @see com.styleconverter.test.style.platform.workarounds.SkewTransformApplier
     */
    object Transforms {
        const val PACKAGE = "com.styleconverter.test.style.appearance.transforms"
        val appliers = listOf(
            "TransformApplier",
            "Transform3DApplier",
            "platform/workarounds/SkewTransformApplier"
        )
        val cssProperties = listOf(
            "transform", "transform-origin",
            "translate", "rotate", "scale", "skew",
            "perspective", "perspective-origin",
            "backface-visibility", "transform-style"
        )
    }

    // =============================================================================
    // VISUAL STYLING - IMAGES
    // =============================================================================

    /**
     * Image appliers for object-fit and object-position.
     * @see com.styleconverter.test.style.appearance.images.ObjectFitApplier
     */
    object Images {
        const val PACKAGE = "com.styleconverter.test.style.appearance.images"
        val appliers = listOf("ObjectFitApplier")
        val cssProperties = listOf(
            "object-fit", "object-position"
        )
    }

    /**
     * SVG appliers for fill, stroke, etc.
     * @see com.styleconverter.test.style.appearance.svg.SvgApplier
     */
    object Svg {
        const val PACKAGE = "com.styleconverter.test.style.appearance.svg"
        val appliers = listOf("SvgApplier")
        val cssProperties = listOf(
            "fill", "stroke", "stroke-width",
            "stroke-linecap", "stroke-linejoin"
        )
    }

    // =============================================================================
    // TYPOGRAPHY
    // =============================================================================

    /**
     * Typography appliers for all text-related properties.
     * @see com.styleconverter.test.style.typography.TypographyApplier
     * @see com.styleconverter.test.style.typography.TextStyleApplier
     * @see com.styleconverter.test.style.typography.TextFormattingApplier
     * @see com.styleconverter.test.style.typography.TextWrapApplier
     * @see com.styleconverter.test.style.typography.LineClampApplier
     * @see com.styleconverter.test.style.typography.FontVariantApplier
     * @see com.styleconverter.test.style.typography.TextEmphasisApplier
     * @see com.styleconverter.test.style.typography.text.WritingModeApplier
     */
    object Typography {
        const val PACKAGE = "com.styleconverter.test.style.typography"
        val appliers = listOf(
            "TypographyApplier",
            "TextStyleApplier",
            "TextFormattingApplier",
            "TextWrapApplier",
            "LineClampApplier",
            "FontVariantApplier",
            "TextEmphasisApplier",
            "text/WritingModeApplier"
        )
        val cssProperties = listOf(
            // Core typography
            "font-family", "font-size", "font-weight", "font-style",
            "line-height", "letter-spacing", "word-spacing",
            // Text alignment
            "text-align", "text-align-last", "vertical-align",
            // Text decoration
            "text-decoration", "text-decoration-line", "text-decoration-style",
            "text-decoration-color", "text-decoration-thickness",
            // Text transform
            "text-transform", "text-indent",
            // White space
            "white-space", "word-break", "overflow-wrap", "hyphens",
            // Line clamping
            "-webkit-line-clamp", "line-clamp",
            // Font variants
            "font-variant", "font-variant-caps", "font-variant-numeric",
            "font-feature-settings", "font-variation-settings",
            // Text emphasis
            "text-emphasis", "text-emphasis-style", "text-emphasis-color",
            // Writing mode
            "writing-mode", "direction", "unicode-bidi", "text-orientation"
        )
    }

    // =============================================================================
    // INTERACTIVITY
    // =============================================================================

    /**
     * Animation and transition appliers.
     * @see com.styleconverter.test.style.interactive.animations.AnimationApplier
     */
    object Animations {
        const val PACKAGE = "com.styleconverter.test.style.interactive.animations"
        val appliers = listOf("AnimationApplier")
        val cssProperties = listOf(
            "animation", "animation-name", "animation-duration",
            "animation-timing-function", "animation-delay",
            "animation-iteration-count", "animation-direction",
            "animation-fill-mode", "animation-play-state",
            "transition", "transition-property", "transition-duration",
            "transition-timing-function", "transition-delay"
        )
    }

    /**
     * User interaction appliers.
     * @see com.styleconverter.test.style.interactive.interactions.InteractionApplier
     */
    object Interactions {
        const val PACKAGE = "com.styleconverter.test.style.interactive.interactions"
        val appliers = listOf("InteractionApplier")
        val cssProperties = listOf(
            "cursor", "pointer-events", "user-select",
            "touch-action", "scroll-behavior"
        )
    }

    /**
     * Form styling appliers.
     * @see com.styleconverter.test.style.interactive.forms.FormStylingApplier
     */
    object FormStyling {
        const val PACKAGE = "com.styleconverter.test.style.interactive.forms"
        val appliers = listOf("FormStylingApplier")
        val cssProperties = listOf(
            "accent-color", "caret-color", "color-scheme",
            "field-sizing", "input-security", "interactivity"
        )
    }

    /**
     * Scroll-related appliers.
     * @see com.styleconverter.test.style.layout.scroll.ScrollApplier
     * @see com.styleconverter.test.style.layout.scroll.ScrollTimelineApplier
     * @see com.styleconverter.test.style.layout.scroll.ViewTimelineApplier
     */
    object Scroll {
        const val PACKAGE = "com.styleconverter.test.style.layout.scroll"
        val appliers = listOf(
            "ScrollApplier",
            "ScrollTimelineApplier",
            "ViewTimelineApplier"
        )
        val cssProperties = listOf(
            "scroll-snap-type", "scroll-snap-align",
            "scroll-padding", "scroll-margin",
            "scrollbar-width", "scrollbar-color",
            "scroll-timeline", "view-timeline",
            "animation-timeline"
        )
    }

    // =============================================================================
    // CONTENT
    // =============================================================================

    /**
     * List style appliers.
     * @see com.styleconverter.test.style.content.lists.ListStyleApplier
     */
    object Lists {
        const val PACKAGE = "com.styleconverter.test.style.content.lists"
        val appliers = listOf("ListStyleApplier")
        val cssProperties = listOf(
            "list-style", "list-style-type", "list-style-position", "list-style-image"
        )
    }

    /**
     * Table layout appliers.
     * @see com.styleconverter.test.style.content.tables.TableApplier
     */
    object Tables {
        const val PACKAGE = "com.styleconverter.test.style.content.tables"
        val appliers = listOf("TableApplier")
        val cssProperties = listOf(
            "display", "table-layout", "border-collapse", "border-spacing",
            "caption-side", "empty-cells"
        )
    }

    // =============================================================================
    // PLATFORM
    // =============================================================================

    /**
     * Performance optimization appliers.
     * @see com.styleconverter.test.style.platform.performance.PerformanceApplier
     */
    object Performance {
        const val PACKAGE = "com.styleconverter.test.style.platform.performance"
        val appliers = listOf("PerformanceApplier")
        val cssProperties = listOf(
            "will-change", "contain", "content-visibility"
        )
    }

    /**
     * Platform-specific workarounds for unsupported features.
     * @see com.styleconverter.test.style.platform.workarounds
     */
    object Workarounds {
        const val PACKAGE = "com.styleconverter.test.style.platform.workarounds"
        val appliers = listOf(
            "BackdropBlurApplier",
            "MultipleShadowApplier",
            "SkewTransformApplier",
            "TextShadowApplier"
        )
        val notes = """
            These appliers provide workarounds for CSS features that don't have
            direct Compose equivalents:

            - BackdropBlurApplier: Simulates backdrop-filter: blur() using RenderEffect
            - MultipleShadowApplier: Composes multiple box-shadows using stacked layers
            - SkewTransformApplier: Approximates skew using graphicsLayer with matrix
            - TextShadowApplier: Simulates text-shadow using overlaid text layers
        """
    }

    // =============================================================================
    // GAPS - NOT YET IMPLEMENTED
    // =============================================================================

    /**
     * Previously critical gaps - NOW RESOLVED.
     *
     * FlexApplier and GridApplier have been implemented.
     */
    object CriticalGaps {
        // RESOLVED: These properties are now supported
        val flex = emptyList<String>()  // Was critical, now implemented
        val grid = emptyList<String>()  // Was critical, now implemented

        val notes = """
            FlexApplier and GridApplier are NOW IMPLEMENTED.

            FlexApplier supports:
            - Row/Column with Arrangement
            - FlowRow/FlowColumn for wrapping
            - Modifier.weight() for flex-grow
            - Arrangement.spacedBy() for gaps

            GridApplier supports:
            - LazyVerticalGrid/LazyHorizontalGrid
            - Custom CssGrid layout for template-areas
            - GridCells for column sizing
            - GridItemSpan for spanning
        """
    }

    /**
     * Medium priority gaps - NOW RESOLVED.
     *
     * TableApplier, MultiColumnApplier, FormStylingApplier, and ContainerQueryApplier
     * have been implemented.
     */
    object MediumGaps {
        // RESOLVED: These are now implemented
        val resolved = listOf(
            "multi-column layout (column-count, column-width) -> MultiColumnApplier",
            "container queries (@container) -> ContainerQueryApplier",
            "table layout (display: table) -> TableApplier",
            "form styling (appearance, caret-color) -> FormStylingApplier"
        )
    }

    /**
     * Remaining gaps (low priority).
     */
    object RemainingGaps {
        val low = listOf(
            "floats (float, clear)",
            "region flow (flow-into, flow-from)",
            "offset-path (motion paths)",
            "ruby annotations",
            "math typography",
            "print styles",
            "speech styles"
        )
    }

    // =============================================================================
    // UTILITY METHODS
    // =============================================================================

    /**
     * Get all registered applier class names.
     */
    fun getAllAppliers(): List<String> {
        return listOf(
            Sizing.appliers,
            Position.appliers,
            Spacing.appliers,
            Overflow.appliers,
            Flex.appliers,
            Grid.appliers,
            MultiColumn.appliers,
            ContainerQuery.appliers,
            Colors.appliers,
            Borders.appliers,
            Shadows.appliers,
            Filters.appliers,
            ClipAndMask.appliers,
            Transforms.appliers,
            Images.appliers,
            Svg.appliers,
            Typography.appliers,
            Animations.appliers,
            Interactions.appliers,
            FormStyling.appliers,
            Scroll.appliers,
            Lists.appliers,
            Tables.appliers,
            Performance.appliers,
            Workarounds.appliers
        ).flatten()
    }

    /**
     * Get all supported CSS properties.
     */
    fun getAllCssProperties(): List<String> {
        return listOf(
            Sizing.cssProperties,
            Position.cssProperties,
            Spacing.cssProperties,
            Overflow.cssProperties,
            Flex.cssProperties,
            Grid.cssProperties,
            MultiColumn.cssProperties,
            ContainerQuery.cssProperties,
            Colors.cssProperties,
            Borders.cssProperties,
            Shadows.cssProperties,
            Filters.cssProperties,
            ClipAndMask.cssProperties,
            Transforms.cssProperties,
            Images.cssProperties,
            Svg.cssProperties,
            Typography.cssProperties,
            Animations.cssProperties,
            Interactions.cssProperties,
            FormStyling.cssProperties,
            Scroll.cssProperties,
            Lists.cssProperties,
            Tables.cssProperties,
            Performance.cssProperties
        ).flatten().distinct().sorted()
    }

    /**
     * Find which category handles a given CSS property.
     */
    fun findCategoryForProperty(cssProperty: String): String? {
        return when {
            Sizing.cssProperties.contains(cssProperty) -> "Sizing"
            Position.cssProperties.contains(cssProperty) -> "Position"
            Spacing.cssProperties.contains(cssProperty) -> "Spacing"
            Overflow.cssProperties.contains(cssProperty) -> "Overflow"
            Colors.cssProperties.contains(cssProperty) -> "Colors"
            Borders.cssProperties.contains(cssProperty) -> "Borders"
            Shadows.cssProperties.contains(cssProperty) -> "Shadows"
            Filters.cssProperties.contains(cssProperty) -> "Filters"
            ClipAndMask.cssProperties.contains(cssProperty) -> "ClipAndMask"
            Transforms.cssProperties.contains(cssProperty) -> "Transforms"
            Images.cssProperties.contains(cssProperty) -> "Images"
            Svg.cssProperties.contains(cssProperty) -> "Svg"
            Typography.cssProperties.contains(cssProperty) -> "Typography"
            Animations.cssProperties.contains(cssProperty) -> "Animations"
            Interactions.cssProperties.contains(cssProperty) -> "Interactions"
            FormStyling.cssProperties.contains(cssProperty) -> "FormStyling"
            Scroll.cssProperties.contains(cssProperty) -> "Scroll"
            Lists.cssProperties.contains(cssProperty) -> "Lists"
            Tables.cssProperties.contains(cssProperty) -> "Tables"
            Performance.cssProperties.contains(cssProperty) -> "Performance"
            Flex.cssProperties.contains(cssProperty) -> "Flex"
            Grid.cssProperties.contains(cssProperty) -> "Grid"
            MultiColumn.cssProperties.contains(cssProperty) -> "MultiColumn"
            ContainerQuery.cssProperties.contains(cssProperty) -> "ContainerQuery"
            else -> null
        }
    }

    /**
     * Check if a CSS property is supported.
     */
    fun isPropertySupported(cssProperty: String): Boolean {
        return getAllCssProperties().contains(cssProperty)
    }

    /**
     * Get implementation status.
     */
    fun getStatus(): Status {
        val allAppliers = getAllAppliers()
        val allProperties = getAllCssProperties()
        val criticalGaps = CriticalGaps.flex + CriticalGaps.grid

        return Status(
            totalAppliers = allAppliers.size,
            totalProperties = allProperties.size,
            criticalGaps = criticalGaps.size,
            coverage = (allProperties.size.toFloat() / (allProperties.size + criticalGaps.size) * 100).toInt()
        )
    }

    data class Status(
        val totalAppliers: Int,
        val totalProperties: Int,
        val criticalGaps: Int,
        val coverage: Int
    )
}
