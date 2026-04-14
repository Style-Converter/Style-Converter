package app.irmodels

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*

/**
 * Represents a CSS pseudo-class selector (e.g., :hover, :active, :focus) with its properties.
 *
 * ## Purpose
 * Captures conditional styles that apply based on element state or user interaction.
 *
 * ## Examples
 * - `:hover` - Mouse hover state
 * - `:active` - Active/pressed state
 * - `:focus` - Focused state
 * - `:disabled` - Disabled state
 *
 * @property condition The selector condition (e.g., "hover", "active", "focus", "disabled")
 * @property properties List of CSS properties that apply when the condition is met
 */
@Serializable
data class IRSelector(
    val condition: String,
    val properties: MutableList<@Serializable(with = IRPropertySerializer::class) IRProperty>
)

/**
 * Represents a CSS media query with its properties.
 *
 * ## Purpose
 * Captures responsive styles that apply based on screen size, device type, or other media features.
 *
 * ## Examples
 * - `(min-width: 768px)` - Tablet and larger screens
 * - `(max-width: 480px)` - Mobile screens
 * - `(prefers-color-scheme: dark)` - Dark mode preference
 *
 * @property query The media query string (e.g., "(min-width: 768px)")
 * @property properties List of CSS properties that apply when the query matches
 */
@Serializable
data class IRMedia(
    val query: String,
    val properties: MutableList<@Serializable(with = IRPropertySerializer::class) IRProperty>
)

/**
 * Represents a single UI component with its styles, selectors, and media queries.
 *
 * ## Purpose
 * Encapsulates all styling information for one component, including:
 * - Base properties (default styles)
 * - Selector-based properties (state-dependent styles like :hover)
 * - Media query properties (responsive styles)
 * - Nested children (for SDUI container components)
 *
 * ## Architecture
 * A component maps to:
 * - **Compose**: A `@Composable` function
 * - **SwiftUI**: A `View` struct
 * - **CSS**: A CSS class or element
 *
 * ## SDUI Support
 * The `id` and `children` fields enable Server-Driven UI:
 * - `id` uniquely identifies each component instance
 * - `children` allows nesting for container layouts (Grid, Flex, Stack)
 *
 * @property id Unique identifier for this component instance (e.g., "button-001", "card-123")
 * @property name The component type/class (e.g., "Button", "Card", "LoginScreen")
 * @property properties Base CSS properties that always apply
 * @property selectors Conditional properties based on pseudo-class selectors
 * @property media Responsive properties based on media queries
 * @property children Nested child components (null for leaf components)
 */
@Serializable(with = IRComponentSerializer::class)
data class IRComponent(
    val id: String,
    val name: String,
    val properties: MutableList<@Serializable(with = IRPropertySerializer::class) IRProperty>,
    val selectors: List<IRSelector> = emptyList(),
    val media: List<IRMedia> = emptyList(),
    val children: List<IRComponent>? = null
)

/**
 * Custom serializer for IRComponent that omits empty/null fields.
 *
 * Serialization strategy:
 * - `id` and `name` are always included
 * - `properties` is always included
 * - `selectors` is omitted if empty
 * - `media` is omitted if empty
 * - `children` is omitted if null or empty
 *
 * Note: Uses simple descriptor since we serialize directly to JSON.
 * The children field is handled dynamically in serialize().
 */
object IRComponentSerializer : KSerializer<IRComponent> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("IRComponent") {
        element<String>("id")
        element<String>("name")
        element<List<IRProperty>>("properties")
        element<List<IRSelector>>("selectors", isOptional = true)
        element<List<IRMedia>>("media", isOptional = true)
        // Children descriptor omitted to avoid circular reference; handled dynamically in serialize()
    }

    override fun serialize(encoder: Encoder, value: IRComponent) {
        require(encoder is JsonEncoder) { "This serializer only works with JSON" }
        val json = encoder.json

        val element = buildJsonObject {
            put("id", value.id)
            put("name", value.name)
            put("properties", json.encodeToJsonElement(
                kotlinx.serialization.builtins.ListSerializer(IRPropertySerializer),
                value.properties
            ))
            // Only include selectors if non-empty
            if (value.selectors.isNotEmpty()) {
                put("selectors", json.encodeToJsonElement(
                    kotlinx.serialization.builtins.ListSerializer(IRSelector.serializer()),
                    value.selectors
                ))
            }
            // Only include media if non-empty
            if (value.media.isNotEmpty()) {
                put("media", json.encodeToJsonElement(
                    kotlinx.serialization.builtins.ListSerializer(IRMedia.serializer()),
                    value.media
                ))
            }
            // Only include children if non-null and non-empty
            if (!value.children.isNullOrEmpty()) {
                put("children", json.encodeToJsonElement(
                    kotlinx.serialization.builtins.ListSerializer(IRComponentSerializer),
                    value.children
                ))
            }
        }

        encoder.encodeJsonElement(element)
    }

    override fun deserialize(decoder: Decoder): IRComponent {
        require(decoder is JsonDecoder) { "This serializer only works with JSON" }
        val obj = decoder.decodeJsonElement().jsonObject

        return IRComponent(
            id = obj["id"]?.jsonPrimitive?.content ?: "",
            name = obj["name"]!!.jsonPrimitive.content,
            properties = mutableListOf(), // Deserialization not fully implemented
            selectors = emptyList(),
            media = emptyList(),
            children = null
        )
    }
}

/**
 * Root IR model representing an entire style document.
 *
 * ## Purpose
 * The top-level container for the Intermediate Representation of a complete style definition.
 * This is the output of the parsing phase and the input to the generation phase.
 *
 * ## Processing Pipeline
 * ```
 * JSON Input
 *   ↓
 * CSS Parsing (CssParsing.kt)
 *   ↓
 * IRDocument (this class)
 *   ↓
 * Code Generation (ComposeGenerator, SwiftUIGenerator, etc.)
 *   ↓
 * Platform Output
 * ```
 *
 * ## Usage Example
 * ```kotlin
 * val document = IRDocument(
 *     components = listOf(
 *         IRComponent(
 *             name = "Button",
 *             properties = mutableListOf(
 *                 PaddingTopProperty(IRLength(16f, LengthUnit.DP)),
 *                 BackgroundColorProperty(IRColor.Hex("#007AFF"))
 *             ),
 *             selectors = listOf(
 *                 IRSelector(
 *                     condition = "hover",
 *                     properties = mutableListOf(
 *                         BackgroundColorProperty(IRColor.Hex("#0051D5"))
 *                     )
 *                 )
 *             ),
 *             media = listOf(
 *                 IRMedia(
 *                     query = "(min-width: 768px)",
 *                     properties = mutableListOf(
 *                         PaddingTopProperty(IRLength(20f, LengthUnit.DP))
 *                     )
 *                 )
 *             )
 *         )
 *     )
 * )
 * ```
 *
 * @property components List of all components in the document
 */
@Serializable
data class IRDocument(
    val components: List<IRComponent>
)
