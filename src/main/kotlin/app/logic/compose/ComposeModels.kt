package app.logic.compose

import kotlinx.serialization.Serializable

/**
 * Represents a single Compose modifier in the chain
 */
@Serializable
data class ComposeModifier(
    val type: String,      // "padding", "background", "size", etc.
    val code: String       // The actual modifier code: "Modifier.padding(16.dp)"
)

/**
 * Represents a state variant (hover, pressed, focused, etc.)
 */
@Serializable
data class ComposeState(
    val name: String,           // "hover", "pressed", "focused"
    val modifiers: List<String> // List of modifier code strings for this state
)

/**
 * Represents a responsive variant for different screen sizes
 */
@Serializable
data class ComposeResponsive(
    val condition: String,      // "maxWidth: 768.dp", "minWidth: 1024.dp"
    val modifiers: List<String> // List of modifier code strings for this breakpoint
)

/**
 * Represents a complete Compose component
 */
@Serializable
data class ComposeComponent(
    val name: String,                           // Component name in PascalCase
    val composableCode: String,                 // Full @Composable function code
    val baseModifiers: List<String>,            // Base modifier chain as list
    val states: List<ComposeState> = emptyList(),         // Interactive states
    val responsive: List<ComposeResponsive> = emptyList() // Responsive variants
)

/**
 * Root document containing all Compose components
 */
@Serializable
data class ComposeDocument(
    val components: List<ComposeComponent>,
    val imports: List<String>  // Required imports for the generated code
)
