package app.parsing.css.properties.shorthand

/**
 * Registry of CSS shorthand properties and their expanders.
 * When a shorthand property is encountered, it's expanded into longhand properties
 * before being passed to the property parsers.
 */
object ShorthandRegistry {

    private val expanders = mutableMapOf<String, ShorthandExpander>()

    init {
        // ===== SPACING =====
        register("margin", MarginExpander)
        register("padding", PaddingExpander)
        register("scroll-margin", ScrollMarginExpander)
        register("scroll-padding", ScrollPaddingExpander)

        // Logical spacing
        register("margin-block", MarginBlockExpander)
        register("margin-inline", MarginInlineExpander)
        register("padding-block", PaddingBlockExpander)
        register("padding-inline", PaddingInlineExpander)
        register("scroll-margin-block", ScrollMarginBlockExpander)
        register("scroll-margin-inline", ScrollMarginInlineExpander)
        register("scroll-padding-block", ScrollPaddingBlockExpander)
        register("scroll-padding-inline", ScrollPaddingInlineExpander)

        // ===== BORDERS =====
        register("border", BorderShorthandExpander)
        register("border-top", BorderTopExpander)
        register("border-right", BorderRightExpander)
        register("border-bottom", BorderBottomExpander)
        register("border-left", BorderLeftExpander)
        register("border-width", BorderWidthExpander)
        register("border-style", BorderStyleExpander)
        register("border-color", BorderColorExpander)
        register("border-radius", BorderRadiusExpander)

        // Logical borders
        register("border-block", BorderBlockExpander)
        register("border-inline", BorderInlineExpander)
        register("border-block-start", BorderBlockStartExpander)
        register("border-block-end", BorderBlockEndExpander)
        register("border-inline-start", BorderInlineStartExpander)
        register("border-inline-end", BorderInlineEndExpander)
        register("border-block-width", BorderBlockWidthExpander)
        register("border-block-style", BorderBlockStyleExpander)
        register("border-block-color", BorderBlockColorExpander)
        register("border-inline-width", BorderInlineWidthExpander)
        register("border-inline-style", BorderInlineStyleExpander)
        register("border-inline-color", BorderInlineColorExpander)

        // ===== OUTLINE =====
        register("outline", OutlineExpander)

        // ===== FLEXBOX =====
        register("flex", FlexExpander)
        register("flex-flow", FlexFlowExpander)

        // ===== GRID =====
        register("gap", GapExpander)
        register("place-items", PlaceItemsExpander)
        register("place-content", PlaceContentExpander)
        register("place-self", PlaceSelfExpander)
        register("grid", GridExpander)
        register("grid-template", GridTemplateExpander)
        register("grid-row", GridRowExpander)
        register("grid-column", GridColumnExpander)
        register("grid-area", GridAreaExpander)

        // ===== POSITIONING =====
        register("inset", InsetExpander)
        register("inset-block", InsetBlockExpander)
        register("inset-inline", InsetInlineExpander)

        // ===== OVERFLOW =====
        register("overflow", OverflowExpander)

        // ===== TYPOGRAPHY =====
        register("font", FontExpander)
        register("text-decoration", TextDecorationExpander)

        // ===== LIST =====
        register("list-style", ListStyleExpander)

        // ===== COLUMNS =====
        register("columns", ColumnsExpander)
        register("column-rule", ColumnRuleExpander)

        // ===== BACKGROUND & MASK =====
        register("background", BackgroundExpander)
        register("background-position", BackgroundPositionExpander)
        register("mask", MaskExpander)

        // ===== BORDER IMAGE & MASK BORDER =====
        register("border-image", BorderImageExpander)
        register("mask-border", MaskBorderExpander)

        // ===== ANIMATIONS & TRANSITIONS =====
        register("transition", TransitionExpander)
        register("animation", AnimationExpander)

        // ===== TEXT & FONTS =====
        register("text-emphasis", TextEmphasisExpander)
        register("text-wrap", TextWrapExpander)
        register("font-synthesis", FontSynthesisExpander)
        register("font-variant", FontVariantExpander)

        // ===== SCROLL =====
        register("scroll-timeline", ScrollTimelineExpander)
        register("overscroll-behavior", OverscrollBehaviorExpander)

        // ===== SVG =====
        register("marker", MarkerExpander)

        // ===== CONTAINER QUERIES =====
        register("container", ContainerExpander)

        // ===== MISC =====
        register("offset", OffsetExpander)
        register("all", AllExpander)
    }

    /**
     * Register a shorthand property with its expander.
     */
    private fun register(propertyName: String, expander: ShorthandExpander) {
        expanders[propertyName] = expander
    }

    /**
     * Check if a property is a shorthand property.
     */
    fun isShorthand(propertyName: String): Boolean {
        return expanders.containsKey(propertyName)
    }

    /**
     * Expand a shorthand property into its longhand properties.
     * Returns a map of longhand property names to their values.
     * Returns empty map if the property is not a shorthand or expansion fails.
     */
    fun expand(propertyName: String, value: String): Map<String, String> {
        val expander = expanders[propertyName] ?: return emptyMap()
        return expander.expand(value)
    }

    /**
     * Expand all shorthand properties in a map of properties.
     * Returns a new map with shorthands expanded to longhands.
     */
    fun expandAll(properties: Map<String, String>): Map<String, String> {
        val result = mutableMapOf<String, String>()

        for ((name, value) in properties) {
            if (isShorthand(name)) {
                // Expand shorthand and add all longhand properties
                val expanded = expand(name, value)
                result.putAll(expanded)
            } else {
                // Keep longhand as-is
                result[name] = value
            }
        }

        return result
    }
}
