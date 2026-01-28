package com.styleconverter.test.style.core.ir

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Root document containing all components.
 */
@Serializable
data class IRDocument(
    val components: List<IRComponent>
)

/**
 * A single UI component with its styles.
 *
 * @property id Unique identifier for SDUI (e.g., "button-001")
 * @property name Component type/class name (e.g., "Button", "Card")
 * @property properties List of CSS properties as IR
 * @property selectors State-based styles (hover, focus, etc.)
 * @property media Responsive breakpoint styles
 * @property children Nested child components for containers
 */
@Serializable
data class IRComponent(
    val id: String,
    val name: String,
    val properties: List<IRProperty> = emptyList(),
    val selectors: List<IRSelector> = emptyList(),
    val media: List<IRMedia> = emptyList(),
    val children: List<IRComponent>? = null
)

/**
 * A CSS property in IR format.
 *
 * Uses generic JsonElement for data to handle all 446+ property types flexibly.
 * Specific property handling is done in StyleApplier.
 */
@Serializable
data class IRProperty(
    val type: String,
    val data: JsonElement
)

/**
 * Pseudo-class selector styles (e.g., :hover, :focus).
 */
@Serializable
data class IRSelector(
    val condition: String,
    val properties: List<IRProperty>
)

/**
 * Media query styles (e.g., min-width: 768px).
 */
@Serializable
data class IRMedia(
    val query: String,
    val properties: List<IRProperty>
)
