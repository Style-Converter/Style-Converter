package app.parsing.css.properties.longhand

import app.*
import app.parsing.css.properties.longhand.primitiveParsers.*
import app.parsing.css.properties.tokenizers.*

// ===== TOKENIZER ALIASES =====
val BT = BracketTokenizer
val CT = CommaTokenizer
val FT = FunctionTokenizer
val OT = OperatorTokenizer
val QT = QuoteTokenizer
val ST = SlashTokenizer
val WT = WhitespaceTokenizer

// ===== PARSER ALIASES =====
// Parser enums for clean configuration syntax
enum class ParserType {
    CP,  // ColorParser
    FP,  // FunctionParser  
    KP,  // KeywordParser
    LP,  // LengthParser
    SP,  // ShadowParser
    UP   // UrlParser
}

// Shorthand constants for parser types
val CP = ParserType.CP
val FP = ParserType.FP
val KP = ParserType.KP
val LP = ParserType.LP
val SP = ParserType.SP
val UP = ParserType.UP

/**
 * Internal configuration for how to parse a specific CSS property.
 */
data class PropertyParsingConfig(
    val tokenizers: List<Tokenizer>,
    val parserOrder: List<(List<String>) -> Primitive?>
)

/**
 * Builder function for PropertyParsingConfig with clean syntax.
 * Pass tokenizers first, then parser types.
 * Example: PropertyParsingConfig(WT, ST, LP, CP, KP)
 */
fun PropertyParsingConfig(vararg items: Any): PropertyParsingConfig {
    val tokenizers = mutableListOf<Tokenizer>()
    val parserTypes = mutableListOf<ParserType>()
    
    // Separate tokenizers from parser types
    for (item in items) {
        when (item) {
            is Tokenizer -> tokenizers.add(item)
            is ParserType -> parserTypes.add(item)
        }
    }
    
    // Convert parser types to actual parser functions
    val parserOrder = parserTypes.map { type ->
        when (type) {
            ParserType.CP -> { tokens: List<String> -> ColorParser.parse(tokens)?.let { Primitive.Color(it) } }
            ParserType.FP -> { tokens: List<String> -> FunctionParser.parse(tokens.firstOrNull() ?: "")?.let { Primitive.Length(IRLength(function = it)) } }
            ParserType.KP -> { tokens: List<String> -> KeywordParser.parse(tokens)?.let { Primitive.Keyword(it) } }
            ParserType.LP -> { tokens: List<String> -> LengthParser.parse(tokens)?.let { Primitive.Length(it) } }
            ParserType.SP -> { tokens: List<String> -> ShadowParser.parse(tokens)?.let { Primitive.Shadow(it) } }
            ParserType.UP -> { tokens: List<String> -> UrlParser.parse(tokens)?.let { Primitive.Url(it) } }
        }
    }
    
    return PropertyParsingConfig(
        tokenizers = tokenizers,
        parserOrder = parserOrder
    )
}

/**
 * Extension function for clean regex-to-config syntax
 */
infix fun Regex.to(config: PropertyParsingConfig): Pair<Regex, PropertyParsingConfig> = this to config

/**
 * Registry that maps CSS property name patterns to their parsing configurations.
 * Each pattern specifies which tokenizers and primitive parsers should be used for that property.
 */
object PropertyParserRegistry {

    // Default configuration: whitespace tokenization with standard parser order
    private val defaultConfig = PropertyParsingConfig(WT, LP, UP, CP, KP)

    // Registry of property patterns to their parsing configurations
    private val configs = mutableMapOf<Regex, PropertyParsingConfig>()

    // Cache for property name to config lookups (reduces regex matching overhead)
    private val configCache = mutableMapOf<String, PropertyParsingConfig>()

    init {
        // ===== SHADOWS =====
        // Shadow properties: comma-separated shadows, then whitespace tokenize each
        Regex(".*shadow.*") register PropertyParsingConfig(CT, WT, SP)

        // ===== BORDERS =====
        // Note: Border shorthands (border, border-top, etc.) are expanded by ShorthandRegistry
        // These configs are for the longhand properties only

        // Border width properties: lengths or keywords (thin, medium, thick)
        Regex("^border(-top|-right|-bottom|-left)?-width$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^border-(block|inline)(-start|-end)?-width$") register PropertyParsingConfig(WT, LP, KP)

        // Border style properties: keywords only
        Regex("^border(-top|-right|-bottom|-left)?-style$") register PropertyParsingConfig(WT, KP)
        Regex("^border-(block|inline)(-start|-end)?-style$") register PropertyParsingConfig(WT, KP)

        // Border color properties: can be multiple colors (shorthand 1-4 values)
        Regex("^border(-top|-right|-bottom|-left)?-color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^border-(block|inline)(-start|-end)?-color$") register PropertyParsingConfig(WT, CP, KP)

        // Border radius: lengths and percentages, possibly with slash for elliptical
        Regex("^border-radius$") register PropertyParsingConfig(ST, WT, LP, KP)
        Regex("^border-(top|bottom)-(left|right)-radius$") register PropertyParsingConfig(ST, WT, LP, FP, KP)
        Regex("^border-(start|end)-(start|end)-radius$") register PropertyParsingConfig(ST, WT, LP, FP, KP)

        // Border image properties
        Regex("^border-image-source$") register PropertyParsingConfig(WT, UP, FP, KP)
        Regex("^border-image-slice$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^border-image-width$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^border-image-outset$") register PropertyParsingConfig(WT, LP)
        Regex("^border-image-repeat$") register PropertyParsingConfig(WT, KP)

        // Border utility properties
        Regex("^border-collapse$") register PropertyParsingConfig(WT, KP)
        Regex("^border-spacing$") register PropertyParsingConfig(WT, LP)

        // ===== OUTLINES =====
        // Note: Outline shorthand is expanded by ShorthandRegistry
        Regex("^outline-width$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^outline-style$") register PropertyParsingConfig(WT, KP)
        Regex("^outline-color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^outline-offset$") register PropertyParsingConfig(WT, LP)

        // ===== BACKGROUNDS =====
        // Note: Background shorthand will be handled separately (complex, deferred)

        // Background image: comma-separated list of images/gradients
        Regex("^background-image$") register PropertyParsingConfig(CT, WT, FP, UP, KP)

        // Background position: x/y with slash separator, comma-separated layers
        Regex("^background-position$") register PropertyParsingConfig(CT, ST, WT, LP, KP)
        Regex("^background-position-x$") register PropertyParsingConfig(CT, WT, LP, FP, KP)
        Regex("^background-position-y$") register PropertyParsingConfig(CT, WT, LP, FP, KP)

        // Background size: comma-separated, can have width/height pairs
        Regex("^background-size$") register PropertyParsingConfig(CT, WT, LP, KP)

        // Background repeat: comma-separated, can have x/y pairs
        Regex("^background-repeat$") register PropertyParsingConfig(CT, WT, KP)

        // Background attachment, origin, clip: comma-separated keywords
        Regex("^background-attachment$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^background-origin$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^background-clip$") register PropertyParsingConfig(CT, WT, KP)

        // Background color: single color value
        Regex("^background-color$") register PropertyParsingConfig(WT, CP, KP)

        // Background blend mode: comma-separated keywords
        Regex("^background-blend-mode$") register PropertyParsingConfig(CT, WT, KP)

        // ===== MASKS =====
        Regex("^mask$") register PropertyParsingConfig(CT, WT, FP, UP, LP, KP)
        Regex("^mask-image$") register PropertyParsingConfig(CT, WT, FP, UP, KP)
        Regex("^mask-position$") register PropertyParsingConfig(CT, ST, WT, LP, KP)
        Regex("^mask-position-x$") register PropertyParsingConfig(CT, WT, LP, KP)
        Regex("^mask-position-y$") register PropertyParsingConfig(CT, WT, LP, KP)
        Regex("^mask-size$") register PropertyParsingConfig(CT, WT, LP, KP)
        Regex("^mask-repeat$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^mask-origin$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^mask-clip$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^mask-mode$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^mask-type$") register PropertyParsingConfig(WT, KP)

        // Mask border properties
        Regex("^mask-border$") register PropertyParsingConfig(ST, WT, UP, LP, KP)
        Regex("^mask-border-source$") register PropertyParsingConfig(WT, UP, FP, KP)
        Regex("^mask-border-slice$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^mask-border-width$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^mask-border-outset$") register PropertyParsingConfig(WT, LP)
        Regex("^mask-border-repeat$") register PropertyParsingConfig(WT, KP)
        Regex("^mask-border-mode$") register PropertyParsingConfig(WT, KP)

        // Webkit mask properties
        Regex("^-webkit-mask-box-image$") register PropertyParsingConfig(WT, UP, LP, KP)
        Regex("^-webkit-mask-position-x$") register PropertyParsingConfig(CT, WT, LP, KP)
        Regex("^-webkit-mask-position-y$") register PropertyParsingConfig(CT, WT, LP, KP)
        Regex("^-webkit-mask-repeat-x$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-mask-repeat-y$") register PropertyParsingConfig(WT, KP)

        // ===== FILTERS =====
        Regex("^filter$") register PropertyParsingConfig(WT, FP)
        Regex("^backdrop-filter$") register PropertyParsingConfig(WT, FP)

        // ===== SPACING =====
        // Note: Margin/Padding shorthands are expanded by ShorthandRegistry
        Regex("^(margin|padding)-(top|right|bottom|left)$") register PropertyParsingConfig(WT, LP, FP, KP)
        Regex("^(margin|padding)-(block|inline)(-start|-end)?$") register PropertyParsingConfig(WT, LP, FP, KP)

        // Gap properties for flexbox/grid
        // Note: Gap shorthand is expanded by ShorthandRegistry
        Regex("^(row|column)-gap$") register PropertyParsingConfig(WT, LP, KP)

        // ===== SIZING =====
        Regex("^(width|height|min-width|min-height|max-width|max-height)$") register PropertyParsingConfig(WT, LP, FP, KP)
        Regex("^(inline|block)-size$") register PropertyParsingConfig(WT, LP, FP, KP)
        Regex("^(min|max)-(inline|block)-size$") register PropertyParsingConfig(WT, LP, FP, KP)

        // ===== TYPOGRAPHY =====
        // Font family: comma-separated, preserve quoted strings
        Regex("^font-family$") register PropertyParsingConfig(CT, WT, KP)

        // Font size: length or keyword
        Regex("^font-size$") register PropertyParsingConfig(WT, LP, FP, KP)

        // Font weight: number or keyword
        Regex("^font-weight$") register PropertyParsingConfig(WT, LP, KP)

        // Font style, variant, stretch: keywords
        Regex("^font-style$") register PropertyParsingConfig(WT, KP)
        Regex("^font-variant$") register PropertyParsingConfig(WT, KP)
        Regex("^font-stretch$") register PropertyParsingConfig(WT, KP)

        // Line height: unitless number, length, or percentage
        Regex("^line-height$") register PropertyParsingConfig(WT, LP, FP, KP)

        // Letter and word spacing
        Regex("^letter-spacing$") register PropertyParsingConfig(WT, LP, FP, KP)
        Regex("^word-spacing$") register PropertyParsingConfig(WT, LP, FP, KP)

        // Text properties
        Regex("^text-align$") register PropertyParsingConfig(WT, KP)
        Regex("^text-decoration$") register PropertyParsingConfig(WT, KP, CP)
        Regex("^text-decoration-line$") register PropertyParsingConfig(WT, KP)
        Regex("^text-decoration-color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^text-decoration-style$") register PropertyParsingConfig(WT, KP)
        Regex("^text-decoration-thickness$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^text-transform$") register PropertyParsingConfig(WT, KP)
        Regex("^text-indent$") register PropertyParsingConfig(WT, LP, FP, KP)
        Regex("^text-overflow$") register PropertyParsingConfig(WT, KP)
        Regex("^white-space$") register PropertyParsingConfig(WT, KP)
        Regex("^word-break$") register PropertyParsingConfig(WT, KP)
        Regex("^word-wrap$") register PropertyParsingConfig(WT, KP)

        // ===== COLORS =====
        Regex("^color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^opacity$") register PropertyParsingConfig(WT, LP)

        // ===== LAYOUT =====
        Regex("^display$") register PropertyParsingConfig(WT, KP)
        Regex("^position$") register PropertyParsingConfig(WT, KP)
        Regex("^(top|right|bottom|left)$") register PropertyParsingConfig(WT, LP, FP, KP)
        Regex("^(inset|inset-block|inset-inline)(-start|-end)?$") register PropertyParsingConfig(WT, LP, FP, KP)
        Regex("^z-index$") register PropertyParsingConfig(WT, LP, KP)

        // Flexbox
        // Note: Flex and flex-flow shorthands are expanded by ShorthandRegistry
        Regex("^flex-direction$") register PropertyParsingConfig(WT, KP)
        Regex("^flex-wrap$") register PropertyParsingConfig(WT, KP)
        Regex("^flex-grow$") register PropertyParsingConfig(WT, LP)
        Regex("^flex-shrink$") register PropertyParsingConfig(WT, LP)
        Regex("^flex-basis$") register PropertyParsingConfig(WT, LP, FP, KP)
        Regex("^justify-content$") register PropertyParsingConfig(WT, KP)
        Regex("^align-items$") register PropertyParsingConfig(WT, KP)
        Regex("^align-self$") register PropertyParsingConfig(WT, KP)
        Regex("^align-content$") register PropertyParsingConfig(WT, KP)
        Regex("^order$") register PropertyParsingConfig(WT, LP)

        // Grid
        Regex("^grid$") register PropertyParsingConfig(ST, WT, LP, FP, KP)
        Regex("^grid-template$") register PropertyParsingConfig(ST, WT, LP, KP)
        Regex("^grid-template-rows$") register PropertyParsingConfig(WT, LP, FP, KP)
        Regex("^grid-template-columns$") register PropertyParsingConfig(WT, LP, FP, KP)
        Regex("^grid-template-areas$") register PropertyParsingConfig(WT, KP)
        Regex("^grid-auto-rows$") register PropertyParsingConfig(WT, LP, FP, KP)
        Regex("^grid-auto-columns$") register PropertyParsingConfig(WT, LP, FP, KP)
        Regex("^grid-auto-flow$") register PropertyParsingConfig(WT, KP)
        Regex("^grid-(row|column)$") register PropertyParsingConfig(ST, WT, LP, KP)
        Regex("^grid-(row|column)-(start|end)$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^grid-area$") register PropertyParsingConfig(ST, WT, LP, KP)

        // ===== TRANSFORMS =====
        Regex("^transform$") register PropertyParsingConfig(WT, FP)
        Regex("^transform-origin$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^transform-style$") register PropertyParsingConfig(WT, KP)

        // ===== TRANSITIONS & ANIMATIONS =====
        Regex("^transition$") register PropertyParsingConfig(CT, WT, LP, FP, KP)
        Regex("^transition-property$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^transition-duration$") register PropertyParsingConfig(CT, WT, LP)
        Regex("^transition-timing-function$") register PropertyParsingConfig(CT, WT, FP, KP)
        Regex("^transition-delay$") register PropertyParsingConfig(CT, WT, LP)

        Regex("^animation$") register PropertyParsingConfig(CT, WT, LP, FP, KP)
        Regex("^animation-name$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^animation-duration$") register PropertyParsingConfig(CT, WT, LP)
        Regex("^animation-timing-function$") register PropertyParsingConfig(CT, WT, FP, KP)
        Regex("^animation-delay$") register PropertyParsingConfig(CT, WT, LP)
        Regex("^animation-iteration-count$") register PropertyParsingConfig(CT, WT, LP, KP)
        Regex("^animation-direction$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^animation-fill-mode$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^animation-play-state$") register PropertyParsingConfig(CT, WT, KP)

        // ===== OVERFLOW & CLIPPING =====
        // Note: Overflow shorthand is expanded by ShorthandRegistry
        Regex("^overflow-(x|y)$") register PropertyParsingConfig(WT, KP)
        Regex("^clip$") register PropertyParsingConfig(WT, FP, LP, KP)
        Regex("^clip-path$") register PropertyParsingConfig(WT, FP, UP, KP)

        // ===== ADDITIONAL TEXT PROPERTIES =====
        Regex("^text-align-last$") register PropertyParsingConfig(WT, KP)
        Regex("^text-justify$") register PropertyParsingConfig(WT, KP)
        Regex("^text-rendering$") register PropertyParsingConfig(WT, KP)
        Regex("^text-shadow$") register PropertyParsingConfig(CT, WT, SP)
        Regex("^text-size-adjust$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^text-emphasis-style$") register PropertyParsingConfig(WT, KP)
        Regex("^text-emphasis-color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^text-emphasis-position$") register PropertyParsingConfig(WT, KP)
        Regex("^text-wrap-mode$") register PropertyParsingConfig(WT, KP)
        Regex("^text-wrap-style$") register PropertyParsingConfig(WT, KP)
        Regex("^text-underline-position$") register PropertyParsingConfig(WT, KP)
        Regex("^text-underline-offset$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^text-combine-upright$") register PropertyParsingConfig(WT, KP)
        Regex("^text-orientation$") register PropertyParsingConfig(WT, KP)
        Regex("^vertical-align$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^writing-mode$") register PropertyParsingConfig(WT, KP)
        Regex("^direction$") register PropertyParsingConfig(WT, KP)
        Regex("^unicode-bidi$") register PropertyParsingConfig(WT, KP)
        Regex("^tab-size$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^hyphens$") register PropertyParsingConfig(WT, KP)
        Regex("^overflow-wrap$") register PropertyParsingConfig(WT, KP)
        Regex("^line-break$") register PropertyParsingConfig(WT, KP)
        Regex("^white-space-collapse$") register PropertyParsingConfig(WT, KP)
        Regex("^text-spacing-trim$") register PropertyParsingConfig(WT, KP)
        Regex("^hanging-punctuation$") register PropertyParsingConfig(WT, KP)
        Regex("^hyphenate-character$") register PropertyParsingConfig(WT, KP)
        Regex("^hyphenate-limit-chars$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^text-decoration-skip-ink$") register PropertyParsingConfig(WT, KP)

        // Font variant sub-properties
        Regex("^font-variant-ligatures$") register PropertyParsingConfig(WT, KP)
        Regex("^font-variant-caps$") register PropertyParsingConfig(WT, KP)
        Regex("^font-variant-numeric$") register PropertyParsingConfig(WT, KP)
        Regex("^font-variant-alternates$") register PropertyParsingConfig(WT, KP)
        Regex("^font-variant-east-asian$") register PropertyParsingConfig(WT, KP)
        Regex("^font-variant-position$") register PropertyParsingConfig(WT, KP)
        Regex("^font-variant-emoji$") register PropertyParsingConfig(WT, KP)
        Regex("^font-synthesis-weight$") register PropertyParsingConfig(WT, KP)
        Regex("^font-synthesis-style$") register PropertyParsingConfig(WT, KP)
        Regex("^font-synthesis-small-caps$") register PropertyParsingConfig(WT, KP)
        Regex("^font-synthesis-position$") register PropertyParsingConfig(WT, KP)
        Regex("^font-optical-sizing$") register PropertyParsingConfig(WT, KP)
        Regex("^font-kerning$") register PropertyParsingConfig(WT, KP)
        Regex("^font-feature-settings$") register PropertyParsingConfig(WT, KP)
        Regex("^font-variation-settings$") register PropertyParsingConfig(WT, KP)
        Regex("^font-palette$") register PropertyParsingConfig(WT, KP)
        Regex("^font-size-adjust$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^font-language-override$") register PropertyParsingConfig(WT, KP)

        // ===== COLUMNS (additional) =====
        Regex("^column-span$") register PropertyParsingConfig(WT, KP)
        Regex("^column-fill$") register PropertyParsingConfig(WT, KP)

        // ===== LIST & COUNTER =====
        Regex("^list-style-type$") register PropertyParsingConfig(WT, KP)
        Regex("^list-style-position$") register PropertyParsingConfig(WT, KP)
        Regex("^list-style-image$") register PropertyParsingConfig(WT, UP, KP)
        Regex("^counter-reset$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^counter-increment$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^counter-set$") register PropertyParsingConfig(WT, LP, KP)

        // ===== TABLE =====
        Regex("^table-layout$") register PropertyParsingConfig(WT, KP)
        Regex("^empty-cells$") register PropertyParsingConfig(WT, KP)
        Regex("^caption-side$") register PropertyParsingConfig(WT, KP)

        // ===== SCROLL PROPERTIES =====
        Regex("^scroll-behavior$") register PropertyParsingConfig(WT, KP)
        Regex("^scroll-snap-type$") register PropertyParsingConfig(WT, KP)
        Regex("^scroll-snap-align$") register PropertyParsingConfig(WT, KP)
        Regex("^scroll-snap-stop$") register PropertyParsingConfig(WT, KP)
        Regex("^scroll-margin-(top|right|bottom|left)$") register PropertyParsingConfig(WT, LP)
        Regex("^scroll-margin-(block|inline)(-start|-end)?$") register PropertyParsingConfig(WT, LP)
        Regex("^scroll-padding-(top|right|bottom|left)$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^scroll-padding-(block|inline)(-start|-end)?$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^overscroll-behavior$") register PropertyParsingConfig(WT, KP)
        Regex("^overscroll-behavior-(x|y)$") register PropertyParsingConfig(WT, KP)
        Regex("^overscroll-behavior-(block|inline)$") register PropertyParsingConfig(WT, KP)

        // Scroll timeline
        Regex("^scroll-timeline$") register PropertyParsingConfig(WT, KP)
        Regex("^scroll-timeline-name$") register PropertyParsingConfig(WT, KP)
        Regex("^scroll-timeline-axis$") register PropertyParsingConfig(WT, KP)
        Regex("^view-timeline$") register PropertyParsingConfig(WT, KP)
        Regex("^view-timeline-name$") register PropertyParsingConfig(WT, KP)
        Regex("^view-timeline-axis$") register PropertyParsingConfig(WT, KP)
        Regex("^view-timeline-inset$") register PropertyParsingConfig(WT, LP, KP)

        // ===== CONTAINER QUERIES =====
        Regex("^container$") register PropertyParsingConfig(WT, KP)
        Regex("^container-name$") register PropertyParsingConfig(WT, KP)
        Regex("^container-type$") register PropertyParsingConfig(WT, KP)
        Regex("^contain$") register PropertyParsingConfig(WT, KP)
        Regex("^content-visibility$") register PropertyParsingConfig(WT, KP)

        // ===== INTERACTIVITY =====
        Regex("^resize$") register PropertyParsingConfig(WT, KP)
        Regex("^touch-action$") register PropertyParsingConfig(WT, KP)
        Regex("^caret-color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^accent-color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^appearance$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-appearance$") register PropertyParsingConfig(WT, KP)
        Regex("^-moz-appearance$") register PropertyParsingConfig(WT, KP)

        // ===== IMAGE & OBJECT =====
        Regex("^object-fit$") register PropertyParsingConfig(WT, KP)
        Regex("^object-position$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^image-rendering$") register PropertyParsingConfig(WT, KP)
        Regex("^image-orientation$") register PropertyParsingConfig(WT, LP, KP)

        // ===== SHAPES & FLOAT =====
        Regex("^shape-outside$") register PropertyParsingConfig(WT, FP, UP, KP)
        Regex("^shape-margin$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^shape-image-threshold$") register PropertyParsingConfig(WT, LP)
        Regex("^float$") register PropertyParsingConfig(WT, KP)
        Regex("^clear$") register PropertyParsingConfig(WT, KP)

        // ===== 3D TRANSFORMS =====
        Regex("^perspective$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^perspective-origin$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^backface-visibility$") register PropertyParsingConfig(WT, KP)
        Regex("^transform-box$") register PropertyParsingConfig(WT, KP)

        // ===== BLEND & COMPOSITE =====
        Regex("^isolation$") register PropertyParsingConfig(WT, KP)
        Regex("^mix-blend-mode$") register PropertyParsingConfig(WT, KP)

        // ===== PERFORMANCE & OPTIMIZATION =====
        Regex("^will-change$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^aspect-ratio$") register PropertyParsingConfig(WT, LP, KP)

        // ===== SVG PROPERTIES =====
        Regex("^fill$") register PropertyParsingConfig(WT, CP, UP, KP)
        Regex("^stroke$") register PropertyParsingConfig(WT, CP, UP, KP)
        Regex("^stroke-width$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^stroke-dasharray$") register PropertyParsingConfig(CT, WT, LP, KP)
        Regex("^stroke-dashoffset$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^stroke-linecap$") register PropertyParsingConfig(WT, KP)
        Regex("^stroke-linejoin$") register PropertyParsingConfig(WT, KP)
        Regex("^stroke-miterlimit$") register PropertyParsingConfig(WT, LP)
        Regex("^fill-opacity$") register PropertyParsingConfig(WT, LP)
        Regex("^stroke-opacity$") register PropertyParsingConfig(WT, LP)
        Regex("^fill-rule$") register PropertyParsingConfig(WT, KP)
        Regex("^clip-rule$") register PropertyParsingConfig(WT, KP)
        Regex("^color-interpolation$") register PropertyParsingConfig(WT, KP)
        Regex("^color-interpolation-filters$") register PropertyParsingConfig(WT, KP)
        Regex("^dominant-baseline$") register PropertyParsingConfig(WT, KP)
        Regex("^alignment-baseline$") register PropertyParsingConfig(WT, KP)
        Regex("^baseline-shift$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^stop-color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^stop-opacity$") register PropertyParsingConfig(WT, LP)
        Regex("^flood-color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^flood-opacity$") register PropertyParsingConfig(WT, LP)
        Regex("^lighting-color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^marker-start$") register PropertyParsingConfig(WT, UP, KP)
        Regex("^marker-mid$") register PropertyParsingConfig(WT, UP, KP)
        Regex("^marker-end$") register PropertyParsingConfig(WT, UP, KP)
        Regex("^paint-order$") register PropertyParsingConfig(WT, KP)
        Regex("^vector-effect$") register PropertyParsingConfig(WT, KP)
        Regex("^shape-rendering$") register PropertyParsingConfig(WT, KP)
        Regex("^text-anchor$") register PropertyParsingConfig(WT, KP)
        Regex("^glyph-orientation-vertical$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^glyph-orientation-horizontal$") register PropertyParsingConfig(WT, LP, KP)

        // ===== PRINTING =====
        Regex("^page-break-before$") register PropertyParsingConfig(WT, KP)
        Regex("^page-break-after$") register PropertyParsingConfig(WT, KP)
        Regex("^page-break-inside$") register PropertyParsingConfig(WT, KP)
        Regex("^break-before$") register PropertyParsingConfig(WT, KP)
        Regex("^break-after$") register PropertyParsingConfig(WT, KP)
        Regex("^break-inside$") register PropertyParsingConfig(WT, KP)
        Regex("^orphans$") register PropertyParsingConfig(WT, LP)
        Regex("^widows$") register PropertyParsingConfig(WT, LP)

        // ===== MISC =====
        Regex("^cursor$") register PropertyParsingConfig(CT, WT, UP, KP)
        Regex("^content$") register PropertyParsingConfig(WT, UP, FP, KP)
        Regex("^quotes$") register PropertyParsingConfig(WT, KP)
        Regex("^visibility$") register PropertyParsingConfig(WT, KP)
        Regex("^pointer-events$") register PropertyParsingConfig(WT, KP)
        Regex("^user-select$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-user-select$") register PropertyParsingConfig(WT, KP)
        Regex("^-moz-user-select$") register PropertyParsingConfig(WT, KP)

        // ===== ADDITIONAL LAYOUT & DISPLAY =====
        Regex("^box-sizing$") register PropertyParsingConfig(WT, KP)
        Regex("^vertical-align-last$") register PropertyParsingConfig(WT, LP, KP)

        // ===== ADDITIONAL OVERFLOW =====
        Regex("^overflow-anchor$") register PropertyParsingConfig(WT, KP)
        Regex("^overflow-block$") register PropertyParsingConfig(WT, KP)
        Regex("^overflow-inline$") register PropertyParsingConfig(WT, KP)
        Regex("^overflow-clip-margin$") register PropertyParsingConfig(WT, LP, KP)

        // ===== RUBY =====
        Regex("^ruby-position$") register PropertyParsingConfig(WT, KP)
        Regex("^ruby-align$") register PropertyParsingConfig(WT, KP)
        Regex("^ruby-merge$") register PropertyParsingConfig(WT, KP)

        // ===== WEBKIT-SPECIFIC =====
        Regex("^-webkit-line-clamp$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-box-orient$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-tap-highlight-color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^-webkit-text-fill-color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^-webkit-text-stroke$") register PropertyParsingConfig(WT, LP, CP, KP)
        Regex("^-webkit-text-stroke-width$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-text-stroke-color$") register PropertyParsingConfig(WT, CP, KP)

        // ===== MOZ-SPECIFIC =====
        Regex("^-moz-osx-font-smoothing$") register PropertyParsingConfig(WT, KP)

        // ===== OFFSET & MOTION PATH =====
        Regex("^offset-path$") register PropertyParsingConfig(WT, FP, UP, KP)
        Regex("^offset-distance$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^offset-rotate$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^offset-anchor$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^offset-position$") register PropertyParsingConfig(WT, LP, KP)

        // ===== BASELINE & ALIGNMENT =====
        Regex("^justify-items$") register PropertyParsingConfig(WT, KP)
        Regex("^justify-self$") register PropertyParsingConfig(WT, KP)

        // ===== SIZING ADDITIONAL =====
        Regex("^contain-intrinsic-size$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^contain-intrinsic-width$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^contain-intrinsic-height$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^contain-intrinsic-block-size$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^contain-intrinsic-inline-size$") register PropertyParsingConfig(WT, LP, KP)

        // ===== COLUMNS EXTENDED =====
        Regex("^columns$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^column-width$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^column-count$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^column-gap$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^column-rule$") register PropertyParsingConfig(WT, LP, CP, KP)
        Regex("^column-rule-width$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^column-rule-style$") register PropertyParsingConfig(WT, KP)
        Regex("^column-rule-color$") register PropertyParsingConfig(WT, CP, KP)

        // ===== ANIMATION EXTENDED =====
        Regex("^animation-timeline$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^animation-range$") register PropertyParsingConfig(CT, WT, LP, KP)
        Regex("^animation-range-start$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^animation-range-end$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^animation-composition$") register PropertyParsingConfig(CT, WT, KP)

        // ===== TRANSITION EXTENDED =====
        Regex("^transition-behavior$") register PropertyParsingConfig(CT, WT, KP)

        // ===== VIEW TRANSITIONS =====
        Regex("^view-transition-name$") register PropertyParsingConfig(WT, KP)

        // ===== ANCHOR POSITIONING =====
        Regex("^anchor-name$") register PropertyParsingConfig(WT, KP)
        Regex("^anchor-scope$") register PropertyParsingConfig(WT, KP)
        Regex("^position-anchor$") register PropertyParsingConfig(WT, KP)
        Regex("^position-area$") register PropertyParsingConfig(WT, KP)
        Regex("^position-try$") register PropertyParsingConfig(WT, KP)
        Regex("^position-try-options$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^position-try-order$") register PropertyParsingConfig(WT, KP)
        Regex("^position-visibility$") register PropertyParsingConfig(WT, KP)
        Regex("^inset-area$") register PropertyParsingConfig(WT, KP)

        // ===== MASONRY LAYOUT =====
        Regex("^masonry-auto-flow$") register PropertyParsingConfig(WT, KP)

        // ===== TEXT EXTENDED =====
        Regex("^initial-letter$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^initial-letter-align$") register PropertyParsingConfig(WT, KP)
        Regex("^line-clamp$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^text-box-edge$") register PropertyParsingConfig(WT, KP)
        Regex("^text-box-trim$") register PropertyParsingConfig(WT, KP)

        // ===== COLOR EXTENDED =====
        Regex("^color-scheme$") register PropertyParsingConfig(WT, KP)
        Regex("^forced-color-adjust$") register PropertyParsingConfig(WT, KP)
        Regex("^print-color-adjust$") register PropertyParsingConfig(WT, KP)
        Regex("^color-adjust$") register PropertyParsingConfig(WT, KP)

        // ===== SCROLL EXTENDED =====
        Regex("^scroll-snap-margin$") register PropertyParsingConfig(WT, LP)
        Regex("^scroll-snap-margin-top$") register PropertyParsingConfig(WT, LP)
        Regex("^scroll-snap-margin-right$") register PropertyParsingConfig(WT, LP)
        Regex("^scroll-snap-margin-bottom$") register PropertyParsingConfig(WT, LP)
        Regex("^scroll-snap-margin-left$") register PropertyParsingConfig(WT, LP)
        Regex("^scroll-snap-margin-(block|inline)(-start|-end)?$") register PropertyParsingConfig(WT, LP)

        // ===== TOUCH & POINTER =====
        Regex("^touch-action-delay$") register PropertyParsingConfig(WT, KP)
        Regex("^input-security$") register PropertyParsingConfig(WT, KP)

        // ===== SCROLLBAR STYLING =====
        Regex("^scrollbar-width$") register PropertyParsingConfig(WT, KP)
        Regex("^scrollbar-color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^scrollbar-gutter$") register PropertyParsingConfig(WT, KP)

        // ===== FIELD SIZING =====
        Regex("^field-sizing$") register PropertyParsingConfig(WT, KP)

        // ===== OVERLAY & POPOVER =====
        Regex("^overlay$") register PropertyParsingConfig(WT, KP)

        // ===== HYPHENATION EXTENDED =====
        Regex("^hyphenate-limit-lines$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^hyphenate-limit-last$") register PropertyParsingConfig(WT, KP)
        Regex("^hyphenate-limit-zone$") register PropertyParsingConfig(WT, LP, KP)

        // ===== MATH =====
        Regex("^math-style$") register PropertyParsingConfig(WT, KP)
        Regex("^math-shift$") register PropertyParsingConfig(WT, KP)
        Regex("^math-depth$") register PropertyParsingConfig(WT, LP, KP)

        // ===== PAGE =====
        Regex("^page$") register PropertyParsingConfig(WT, KP)
        Regex("^size$") register PropertyParsingConfig(WT, LP, KP)

        // ===== WEBKIT EXTENDED =====
        Regex("^-webkit-font-smoothing$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-overflow-scrolling$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-print-color-adjust$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-mask$") register PropertyParsingConfig(CT, WT, UP, LP, KP)
        Regex("^-webkit-mask-image$") register PropertyParsingConfig(CT, WT, UP, FP, KP)
        Regex("^-webkit-mask-size$") register PropertyParsingConfig(CT, WT, LP, KP)
        Regex("^-webkit-mask-clip$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^-webkit-mask-origin$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^-webkit-mask-composite$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^-webkit-backdrop-filter$") register PropertyParsingConfig(WT, FP)

        // ===== MOZ EXTENDED =====
        Regex("^-moz-font-feature-settings$") register PropertyParsingConfig(WT, KP)
        Regex("^-moz-font-language-override$") register PropertyParsingConfig(WT, KP)

        // ===== MS EXTENDED =====
        Regex("^-ms-overflow-style$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-text-size-adjust$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-ms-interpolation-mode$") register PropertyParsingConfig(WT, KP)

        // ===== EXPERIMENTAL & FUTURE =====
        Regex("^reading-flow$") register PropertyParsingConfig(WT, KP)
        Regex("^interpolate-size$") register PropertyParsingConfig(WT, KP)

        // ===== CLIPPING & MASKING EXTENDED =====
        Regex("^clip-path-geometry-box$") register PropertyParsingConfig(WT, KP)
        Regex("^mask-composite$") register PropertyParsingConfig(CT, WT, KP)

        // ===== BORDER EXTENDED =====
        Regex("^border-boundary$") register PropertyParsingConfig(WT, KP)
        Regex("^border-end-end-radius$") register PropertyParsingConfig(ST, WT, LP, KP)
        Regex("^border-end-start-radius$") register PropertyParsingConfig(ST, WT, LP, KP)
        Regex("^border-start-end-radius$") register PropertyParsingConfig(ST, WT, LP, KP)
        Regex("^border-start-start-radius$") register PropertyParsingConfig(ST, WT, LP, KP)

        // ===== COUNTER & MARKERS =====
        Regex("^marker-side$") register PropertyParsingConfig(WT, KP)

        // ===== FILTER EXTENDED =====
        Regex("^color-interpolation-filters$") register PropertyParsingConfig(WT, KP)

        // ===== FLEX EXTENDED =====
        Regex("^flex-basis-auto$") register PropertyParsingConfig(WT, KP)

        // ===== FONT EXTENDED =====
        Regex("^font-display$") register PropertyParsingConfig(WT, KP)
        Regex("^font-min-size$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^font-max-size$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^line-height-step$") register PropertyParsingConfig(WT, LP, KP)

        // ===== IMAGE EXTENDED =====
        Regex("^image-resolution$") register PropertyParsingConfig(WT, LP, KP)

        // ===== LAYOUT EXTENDED =====
        Regex("^float-defer$") register PropertyParsingConfig(WT, KP)
        Regex("^float-offset$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^float-reference$") register PropertyParsingConfig(WT, KP)
        Regex("^wrap-flow$") register PropertyParsingConfig(WT, KP)
        Regex("^wrap-through$") register PropertyParsingConfig(WT, KP)

        // ===== LOGICAL PROPERTIES EXTENDED =====
        Regex("^block-size$") register PropertyParsingConfig(WT, LP, FP, KP)
        Regex("^inline-size$") register PropertyParsingConfig(WT, LP, FP, KP)

        // ===== OVERFLOW EXTENDED =====
        Regex("^text-overflow-ellipsis$") register PropertyParsingConfig(WT, KP)
        Regex("^text-overflow-mode$") register PropertyParsingConfig(WT, KP)
        Regex("^line-clamp-color$") register PropertyParsingConfig(WT, CP, KP)

        // ===== REGIONS =====
        Regex("^flow-into$") register PropertyParsingConfig(WT, KP)
        Regex("^flow-from$") register PropertyParsingConfig(WT, KP)
        Regex("^region-fragment$") register PropertyParsingConfig(WT, KP)

        // ===== SHAPES EXTENDED =====
        Regex("^shape-padding$") register PropertyParsingConfig(WT, LP, KP)

        // ===== SVG EXTENDED =====
        Regex("^buffered-rendering$") register PropertyParsingConfig(WT, KP)
        Regex("^color-rendering$") register PropertyParsingConfig(WT, KP)
        Regex("^enable-background$") register PropertyParsingConfig(WT, KP)
        Regex("^image-rendering-quality$") register PropertyParsingConfig(WT, KP)
        Regex("^kerning$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^text-rendering-quality$") register PropertyParsingConfig(WT, KP)

        // ===== TEXT EXTENDED =====
        Regex("^line-grid$") register PropertyParsingConfig(WT, KP)
        Regex("^line-snap$") register PropertyParsingConfig(WT, KP)
        Regex("^string-set$") register PropertyParsingConfig(WT, KP)
        Regex("^text-align-all$") register PropertyParsingConfig(WT, KP)
        Regex("^text-autospace$") register PropertyParsingConfig(WT, KP)
        Regex("^text-group-align$") register PropertyParsingConfig(WT, KP)
        Regex("^text-space-collapse$") register PropertyParsingConfig(WT, KP)
        Regex("^text-space-trim$") register PropertyParsingConfig(WT, KP)
        Regex("^word-space-transform$") register PropertyParsingConfig(WT, KP)
        Regex("^wrap-after$") register PropertyParsingConfig(WT, KP)
        Regex("^wrap-before$") register PropertyParsingConfig(WT, KP)
        Regex("^wrap-inside$") register PropertyParsingConfig(WT, KP)

        // ===== TRANSFORM EXTENDED =====
        Regex("^rotate$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^scale$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^translate$") register PropertyParsingConfig(WT, LP, KP)

        // ===== WEBKIT EXTENDED MORE =====
        Regex("^-webkit-align-content$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-align-items$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-align-self$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-animation$") register PropertyParsingConfig(CT, WT, LP, FP, KP)
        Regex("^-webkit-animation-delay$") register PropertyParsingConfig(CT, WT, LP)
        Regex("^-webkit-animation-direction$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^-webkit-animation-duration$") register PropertyParsingConfig(CT, WT, LP)
        Regex("^-webkit-animation-fill-mode$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^-webkit-animation-iteration-count$") register PropertyParsingConfig(CT, WT, LP, KP)
        Regex("^-webkit-animation-name$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^-webkit-animation-play-state$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^-webkit-animation-timing-function$") register PropertyParsingConfig(CT, WT, FP, KP)
        Regex("^-webkit-backface-visibility$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-background-clip$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^-webkit-background-origin$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^-webkit-background-size$") register PropertyParsingConfig(CT, WT, LP, KP)
        Regex("^-webkit-border-bottom-left-radius$") register PropertyParsingConfig(ST, WT, LP, KP)
        Regex("^-webkit-border-bottom-right-radius$") register PropertyParsingConfig(ST, WT, LP, KP)
        Regex("^-webkit-border-horizontal-spacing$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-border-image$") register PropertyParsingConfig(ST, WT, UP, LP, KP)
        Regex("^-webkit-border-radius$") register PropertyParsingConfig(ST, WT, LP, KP)
        Regex("^-webkit-border-top-left-radius$") register PropertyParsingConfig(ST, WT, LP, KP)
        Regex("^-webkit-border-top-right-radius$") register PropertyParsingConfig(ST, WT, LP, KP)
        Regex("^-webkit-border-vertical-spacing$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-box-align$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-box-decoration-break$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-box-direction$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-box-flex$") register PropertyParsingConfig(WT, LP)
        Regex("^-webkit-box-flex-group$") register PropertyParsingConfig(WT, LP)
        Regex("^-webkit-box-lines$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-box-ordinal-group$") register PropertyParsingConfig(WT, LP)
        Regex("^-webkit-box-pack$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-box-reflect$") register PropertyParsingConfig(WT, LP, UP, KP)
        Regex("^-webkit-box-shadow$") register PropertyParsingConfig(CT, WT, SP)
        Regex("^-webkit-clip-path$") register PropertyParsingConfig(WT, FP, UP, KP)
        Regex("^-webkit-column-break-after$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-column-break-before$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-column-break-inside$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-column-count$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-column-gap$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-column-rule$") register PropertyParsingConfig(WT, LP, CP, KP)
        Regex("^-webkit-column-rule-color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^-webkit-column-rule-style$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-column-rule-width$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-column-span$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-column-width$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-columns$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-filter$") register PropertyParsingConfig(WT, FP)
        Regex("^-webkit-flex$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-flex-basis$") register PropertyParsingConfig(WT, LP, FP, KP)
        Regex("^-webkit-flex-direction$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-flex-flow$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-flex-grow$") register PropertyParsingConfig(WT, LP)
        Regex("^-webkit-flex-shrink$") register PropertyParsingConfig(WT, LP)
        Regex("^-webkit-flex-wrap$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-justify-content$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-logical-height$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-logical-width$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-margin-after$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-margin-before$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-margin-end$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-margin-start$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-max-logical-height$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-max-logical-width$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-min-logical-height$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-min-logical-width$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-opacity$") register PropertyParsingConfig(WT, LP)
        Regex("^-webkit-order$") register PropertyParsingConfig(WT, LP)
        Regex("^-webkit-padding-after$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-padding-before$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-padding-end$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-padding-start$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-perspective$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-perspective-origin$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-text-emphasis$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^-webkit-text-emphasis-color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^-webkit-text-emphasis-position$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-text-emphasis-style$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-text-security$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-transform$") register PropertyParsingConfig(WT, FP)
        Regex("^-webkit-transform-origin$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-webkit-transform-style$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-transition$") register PropertyParsingConfig(CT, WT, LP, FP, KP)
        Regex("^-webkit-transition-delay$") register PropertyParsingConfig(CT, WT, LP)
        Regex("^-webkit-transition-duration$") register PropertyParsingConfig(CT, WT, LP)
        Regex("^-webkit-transition-property$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^-webkit-transition-timing-function$") register PropertyParsingConfig(CT, WT, FP, KP)
        Regex("^-webkit-user-drag$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-user-modify$") register PropertyParsingConfig(WT, KP)
        Regex("^-webkit-writing-mode$") register PropertyParsingConfig(WT, KP)

        // ===== MOZ EXTENDED MORE =====
        Regex("^-moz-animation$") register PropertyParsingConfig(CT, WT, LP, FP, KP)
        Regex("^-moz-animation-delay$") register PropertyParsingConfig(CT, WT, LP)
        Regex("^-moz-animation-direction$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^-moz-animation-duration$") register PropertyParsingConfig(CT, WT, LP)
        Regex("^-moz-animation-fill-mode$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^-moz-animation-iteration-count$") register PropertyParsingConfig(CT, WT, LP, KP)
        Regex("^-moz-animation-name$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^-moz-animation-play-state$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^-moz-animation-timing-function$") register PropertyParsingConfig(CT, WT, FP, KP)
        Regex("^-moz-backface-visibility$") register PropertyParsingConfig(WT, KP)
        Regex("^-moz-border-end$") register PropertyParsingConfig(WT, LP, CP, KP)
        Regex("^-moz-border-end-color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^-moz-border-end-style$") register PropertyParsingConfig(WT, KP)
        Regex("^-moz-border-end-width$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-moz-border-image$") register PropertyParsingConfig(ST, WT, UP, LP, KP)
        Regex("^-moz-border-radius$") register PropertyParsingConfig(ST, WT, LP, KP)
        Regex("^-moz-border-radius-bottomleft$") register PropertyParsingConfig(ST, WT, LP, KP)
        Regex("^-moz-border-radius-bottomright$") register PropertyParsingConfig(ST, WT, LP, KP)
        Regex("^-moz-border-radius-topleft$") register PropertyParsingConfig(ST, WT, LP, KP)
        Regex("^-moz-border-radius-topright$") register PropertyParsingConfig(ST, WT, LP, KP)
        Regex("^-moz-border-start$") register PropertyParsingConfig(WT, LP, CP, KP)
        Regex("^-moz-border-start-color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^-moz-border-start-style$") register PropertyParsingConfig(WT, KP)
        Regex("^-moz-border-start-width$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-moz-box-align$") register PropertyParsingConfig(WT, KP)
        Regex("^-moz-box-direction$") register PropertyParsingConfig(WT, KP)
        Regex("^-moz-box-flex$") register PropertyParsingConfig(WT, LP)
        Regex("^-moz-box-ordinal-group$") register PropertyParsingConfig(WT, LP)
        Regex("^-moz-box-orient$") register PropertyParsingConfig(WT, KP)
        Regex("^-moz-box-pack$") register PropertyParsingConfig(WT, KP)
        Regex("^-moz-box-shadow$") register PropertyParsingConfig(CT, WT, SP)
        Regex("^-moz-box-sizing$") register PropertyParsingConfig(WT, KP)
        Regex("^-moz-column-count$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-moz-column-fill$") register PropertyParsingConfig(WT, KP)
        Regex("^-moz-column-gap$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-moz-column-rule$") register PropertyParsingConfig(WT, LP, CP, KP)
        Regex("^-moz-column-rule-color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^-moz-column-rule-style$") register PropertyParsingConfig(WT, KP)
        Regex("^-moz-column-rule-width$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-moz-column-width$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-moz-columns$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-moz-hyphens$") register PropertyParsingConfig(WT, KP)
        Regex("^-moz-perspective$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-moz-perspective-origin$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-moz-tab-size$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-moz-text-align-last$") register PropertyParsingConfig(WT, KP)
        Regex("^-moz-text-decoration-color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^-moz-text-decoration-line$") register PropertyParsingConfig(WT, KP)
        Regex("^-moz-text-decoration-style$") register PropertyParsingConfig(WT, KP)
        Regex("^-moz-transform$") register PropertyParsingConfig(WT, FP)
        Regex("^-moz-transform-origin$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-moz-transform-style$") register PropertyParsingConfig(WT, KP)
        Regex("^-moz-transition$") register PropertyParsingConfig(CT, WT, LP, FP, KP)
        Regex("^-moz-transition-delay$") register PropertyParsingConfig(CT, WT, LP)
        Regex("^-moz-transition-duration$") register PropertyParsingConfig(CT, WT, LP)
        Regex("^-moz-transition-property$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^-moz-transition-timing-function$") register PropertyParsingConfig(CT, WT, FP, KP)
        Regex("^-moz-user-focus$") register PropertyParsingConfig(WT, KP)
        Regex("^-moz-user-input$") register PropertyParsingConfig(WT, KP)
        Regex("^-moz-user-modify$") register PropertyParsingConfig(WT, KP)

        // ===== MS EXTENDED MORE =====
        Regex("^-ms-accelerator$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-block-progression$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-content-zoom-chaining$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-content-zoom-limit$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-ms-content-zoom-limit-max$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-ms-content-zoom-limit-min$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-ms-content-zoom-snap$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-content-zoom-snap-points$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-ms-content-zoom-snap-type$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-content-zooming$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-filter$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-flex$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-ms-flex-align$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-flex-direction$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-flex-flow$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-flex-item-align$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-flex-line-pack$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-flex-negative$") register PropertyParsingConfig(WT, LP)
        Regex("^-ms-flex-order$") register PropertyParsingConfig(WT, LP)
        Regex("^-ms-flex-pack$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-flex-positive$") register PropertyParsingConfig(WT, LP)
        Regex("^-ms-flex-preferred-size$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-ms-flex-wrap$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-flow-from$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-flow-into$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-grid-column$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-ms-grid-column-align$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-grid-column-span$") register PropertyParsingConfig(WT, LP)
        Regex("^-ms-grid-columns$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-ms-grid-row$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-ms-grid-row-align$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-grid-row-span$") register PropertyParsingConfig(WT, LP)
        Regex("^-ms-grid-rows$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-ms-high-contrast-adjust$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-hyphenate-limit-chars$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-ms-hyphenate-limit-lines$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-ms-hyphenate-limit-zone$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-ms-hyphens$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-ime-align$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-overflow-x$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-overflow-y$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-scroll-chaining$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-scroll-limit$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-ms-scroll-limit-x-max$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-ms-scroll-limit-x-min$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-ms-scroll-limit-y-max$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-ms-scroll-limit-y-min$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-ms-scroll-rails$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-scroll-snap-points-x$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-ms-scroll-snap-points-y$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-ms-scroll-snap-type$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-scroll-snap-x$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-ms-scroll-snap-y$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-ms-scroll-translation$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-scrollbar-arrow-color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^-ms-scrollbar-base-color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^-ms-scrollbar-darkshadow-color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^-ms-scrollbar-face-color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^-ms-scrollbar-highlight-color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^-ms-scrollbar-shadow-color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^-ms-scrollbar-track-color$") register PropertyParsingConfig(WT, CP, KP)
        Regex("^-ms-text-autospace$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-touch-select$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-transform$") register PropertyParsingConfig(WT, FP)
        Regex("^-ms-transform-origin$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-ms-user-select$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-word-break$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-word-wrap$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-wrap-flow$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-wrap-margin$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-ms-wrap-through$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-writing-mode$") register PropertyParsingConfig(WT, KP)
        Regex("^-ms-zoom$") register PropertyParsingConfig(WT, LP, KP)

        // ===== O EXTENDED (Opera) =====
        Regex("^-o-object-fit$") register PropertyParsingConfig(WT, KP)
        Regex("^-o-object-position$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-o-tab-size$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-o-text-overflow$") register PropertyParsingConfig(WT, KP)
        Regex("^-o-transform$") register PropertyParsingConfig(WT, FP)
        Regex("^-o-transform-origin$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^-o-transition$") register PropertyParsingConfig(CT, WT, LP, FP, KP)
        Regex("^-o-transition-delay$") register PropertyParsingConfig(CT, WT, LP)
        Regex("^-o-transition-duration$") register PropertyParsingConfig(CT, WT, LP)
        Regex("^-o-transition-property$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^-o-transition-timing-function$") register PropertyParsingConfig(CT, WT, FP, KP)

        // ===== ADDITIONAL MISSING STANDARD PROPERTIES =====
        // Box decoration
        Regex("^box-decoration-break$") register PropertyParsingConfig(WT, KP)

        // Positioned layout
        Regex("^position-fallback$") register PropertyParsingConfig(WT, KP)

        // Grid extended
        Regex("^grid-auto-track$") register PropertyParsingConfig(WT, LP, FP, KP)

        // Alignment extended
        Regex("^align-tracks$") register PropertyParsingConfig(WT, KP)
        Regex("^justify-tracks$") register PropertyParsingConfig(WT, KP)

        // Text decoration extended
        Regex("^text-decoration-skip$") register PropertyParsingConfig(WT, KP)
        Regex("^text-underline-position$") register PropertyParsingConfig(WT, KP)

        // Font loading
        Regex("^font-named-instance$") register PropertyParsingConfig(WT, KP)

        // Logical properties that may be missing
        Regex("^max-block-size$") register PropertyParsingConfig(WT, LP, FP, KP)
        Regex("^max-inline-size$") register PropertyParsingConfig(WT, LP, FP, KP)
        Regex("^min-block-size$") register PropertyParsingConfig(WT, LP, FP, KP)
        Regex("^min-inline-size$") register PropertyParsingConfig(WT, LP, FP, KP)

        // Scroll extended
        Regex("^scroll-start$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^scroll-start-x$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^scroll-start-y$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^scroll-start-block$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^scroll-start-inline$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^scroll-start-target$") register PropertyParsingConfig(WT, KP)
        Regex("^scroll-start-target-x$") register PropertyParsingConfig(WT, KP)
        Regex("^scroll-start-target-y$") register PropertyParsingConfig(WT, KP)
        Regex("^scroll-start-target-block$") register PropertyParsingConfig(WT, KP)
        Regex("^scroll-start-target-inline$") register PropertyParsingConfig(WT, KP)

        // Margin trim
        Regex("^margin-trim$") register PropertyParsingConfig(WT, KP)

        // Nav direction
        Regex("^nav-up$") register PropertyParsingConfig(WT, KP)
        Regex("^nav-down$") register PropertyParsingConfig(WT, KP)
        Regex("^nav-left$") register PropertyParsingConfig(WT, KP)
        Regex("^nav-right$") register PropertyParsingConfig(WT, KP)

        // Baseline source
        Regex("^baseline-source$") register PropertyParsingConfig(WT, KP)

        // Speak
        Regex("^speak$") register PropertyParsingConfig(WT, KP)
        Regex("^speak-as$") register PropertyParsingConfig(WT, KP)

        // Voice properties
        Regex("^voice-balance$") register PropertyParsingConfig(WT, KP)
        Regex("^voice-duration$") register PropertyParsingConfig(WT, LP)
        Regex("^voice-family$") register PropertyParsingConfig(CT, WT, KP)
        Regex("^voice-pitch$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^voice-range$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^voice-rate$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^voice-stress$") register PropertyParsingConfig(WT, KP)
        Regex("^voice-volume$") register PropertyParsingConfig(WT, LP, KP)

        // Appearance variants
        Regex("^appearance-variant$") register PropertyParsingConfig(WT, KP)

        // Bookmark
        Regex("^bookmark-label$") register PropertyParsingConfig(WT, KP)
        Regex("^bookmark-level$") register PropertyParsingConfig(WT, LP)
        Regex("^bookmark-state$") register PropertyParsingConfig(WT, KP)

        // Footnote
        Regex("^footnote-display$") register PropertyParsingConfig(WT, KP)
        Regex("^footnote-policy$") register PropertyParsingConfig(WT, KP)

        // Running
        Regex("^running$") register PropertyParsingConfig(WT, FP, KP)

        // Copy into
        Regex("^copy-into$") register PropertyParsingConfig(WT, KP)

        // Continue
        Regex("^continue$") register PropertyParsingConfig(WT, KP)

        // Cue (audio cues)
        Regex("^cue$") register PropertyParsingConfig(WT, UP, KP)
        Regex("^cue-before$") register PropertyParsingConfig(WT, UP, KP)
        Regex("^cue-after$") register PropertyParsingConfig(WT, UP, KP)

        // Pause (speech)
        Regex("^pause$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^pause-before$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^pause-after$") register PropertyParsingConfig(WT, LP, KP)

        // Rest (speech)
        Regex("^rest$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^rest-before$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^rest-after$") register PropertyParsingConfig(WT, LP, KP)

        // Presentation level
        Regex("^presentation-level$") register PropertyParsingConfig(WT, LP, KP)

        // Azimuth (spatial audio)
        Regex("^azimuth$") register PropertyParsingConfig(WT, LP, KP)

        // Elevation (spatial audio)
        Regex("^elevation$") register PropertyParsingConfig(WT, LP, KP)

        // Pitch (deprecated audio)
        Regex("^pitch$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^pitch-range$") register PropertyParsingConfig(WT, LP)

        // Richness (deprecated audio)
        Regex("^richness$") register PropertyParsingConfig(WT, LP)

        // Speech rate (deprecated)
        Regex("^speech-rate$") register PropertyParsingConfig(WT, LP, KP)

        // Stress (deprecated audio)
        Regex("^stress$") register PropertyParsingConfig(WT, LP)

        // Volume (deprecated audio)
        Regex("^volume$") register PropertyParsingConfig(WT, LP, KP)

        // Page
        Regex("^bleed$") register PropertyParsingConfig(WT, LP, KP)
        Regex("^marks$") register PropertyParsingConfig(WT, KP)

        // Widows/orphans extended
        Regex("^max-lines$") register PropertyParsingConfig(WT, LP, KP)

        // String handling
        Regex("^bookmark-target$") register PropertyParsingConfig(WT, UP, KP)

        // Leader
        Regex("^leader$") register PropertyParsingConfig(WT, KP)

        // Property not yet covered
        Regex("^dominant-baseline-adjust$") register PropertyParsingConfig(WT, LP, KP)
    }

    /**
     * Register a property pattern with its parsing configuration.
     * Use the infix syntax: Regex("pattern") register PropertyParsingConfig(WT, LP, CP)
     */
    private infix fun Regex.register(config: PropertyParsingConfig) {
        configs[this] = config
    }

    /**
     * Helper function to register properties with vendor prefixes.
     * Registers the base property plus variants with -webkit-, -moz-, -ms-, and -o- prefixes.
     *
     * @param baseName The base property name (without prefix)
     * @param config The parsing configuration to use for all variants
     * @param prefixes Which prefixes to register (defaults to all)
     */
    private fun registerWithPrefixes(
        baseName: String,
        config: PropertyParsingConfig,
        prefixes: List<String> = listOf("webkit", "moz", "ms", "o")
    ) {
        // Register base property
        Regex("^$baseName$") register config

        // Register prefixed variants
        for (prefix in prefixes) {
            Regex("^-$prefix-$baseName$") register config
        }
    }

    /**
     * Helper function to register properties with directional suffixes.
     * Example: "margin" with config creates patterns for margin-top, margin-right, margin-bottom, margin-left.
     *
     * @param baseName The base property name
     * @param config The parsing configuration
     * @param directions The directional suffixes (defaults to top, right, bottom, left)
     */
    private fun registerWithDirections(
        baseName: String,
        config: PropertyParsingConfig,
        directions: List<String> = listOf("top", "right", "bottom", "left")
    ) {
        val pattern = "^$baseName-(${directions.joinToString("|")})$"
        Regex(pattern) register config
    }

    /**
     * Helper function to register properties with logical property suffixes.
     * Example: "margin" with config creates patterns for margin-block-start, margin-block-end, etc.
     *
     * @param baseName The base property name
     * @param config The parsing configuration
     */
    private fun registerLogicalProperties(
        baseName: String,
        config: PropertyParsingConfig
    ) {
        Regex("^$baseName-(block|inline)(-start|-end)?$") register config
    }

    /**
     * Find the parsing configuration for a given property name.
     * Returns the first matching pattern's config, or default config if no match.
     * Results are cached to avoid repeated regex matching.
     */
    fun findConfig(propertyName: String): PropertyParsingConfig {
        // Check cache first
        configCache[propertyName]?.let { return it }

        // Search through registered patterns
        for ((regex, config) in configs) {
            if (regex.matches(propertyName)) {
                configCache[propertyName] = config
                return config
            }
        }

        // Cache and return default config
        configCache[propertyName] = defaultConfig
        return defaultConfig
    }

    /**
     * Create a CssPropertyParser for a given property name using its configuration.
     */
    fun find(propertyName: String): CssPropertyParser {
        val config = findConfig(propertyName)
        return ConfigurablePropertyParser(config)
    }
}

/**
 * Helper function to accumulate parsed primitives into the appropriate lists.
 */
private fun accumulatePrimitive(
    primitive: Primitive,
    lengths: MutableList<IRLength>,
    colors: MutableList<IRColor>,
    urls: MutableList<IRUrl>,
    keywords: MutableList<IRKeyword>,
    shadows: MutableList<IRShadow>
) {
    when (primitive) {
        is Primitive.Length -> lengths.add(primitive.value)
        is Primitive.Color -> colors.add(primitive.value)
        is Primitive.Url -> urls.add(primitive.value)
        is Primitive.Keyword -> keywords.add(primitive.value)
        is Primitive.Shadow -> shadows.add(primitive.value)
        Primitive.Unknown -> { /* skip */ }
    }
}

/**
 * A property parser that uses a provided configuration to tokenize and parse property values.
 */
private class ConfigurablePropertyParser(
    private val config: PropertyParsingConfig
) : CssPropertyParser {
    
    override fun parse(propertyName: String, value: Any): IRProperty {
        val lengths = mutableListOf<IRLength>()
        val colors = mutableListOf<IRColor>()
        val urls = mutableListOf<IRUrl>()
        val keywords = mutableListOf<IRKeyword>()
        val shadows = mutableListOf<IRShadow>()
        val raw = value.toString()

        // Tokenize hierarchically - returns List<List<String>>
        val groups = CompositeTokenizer.tokenize(raw, config.tokenizers)
        
        // Parse each group
        for (tokens in groups) {
            // Try to parse with ALL tokens from this group together
            val primitive = parsePrimitive(tokens, config.parserOrder)
            accumulatePrimitive(primitive, lengths, colors, urls, keywords, shadows)
            
            if (primitive is Primitive.Unknown) {
                // If parsing the group as a whole failed, try each token individually
                for (token in tokens) {
                    val tokenPrimitive = parsePrimitive(listOf(token), config.parserOrder)
                    accumulatePrimitive(tokenPrimitive, lengths, colors, urls, keywords, shadows)
                }
            }
        }

        return IRProperty(
            propertyName = propertyName,
            lengths = lengths,
            colors = colors,
            urls = urls,
            keywords = keywords,
            shadows = shadows,
            raw = raw
        )
    }
    
    private fun parsePrimitive(tokens: List<String>, order: List<(List<String>) -> Primitive?>): Primitive {
        for (parser in order) {
            parser(tokens)?.let { return it }
        }
        return Primitive.Unknown
    }
}