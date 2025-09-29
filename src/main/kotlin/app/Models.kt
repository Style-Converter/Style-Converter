package app

import kotlinx.serialization.Serializable

/**
 * Represents a typed argument inside a CSS function.
 */
@Serializable
data class IRFunctionArg(
    val raw: String,
    val length: IRLength? = null,
    val keyword: IRKeyword? = null,
    val function: IRFunction? = null,
    val color: IRColor? = null,
    val url: IRUrl? = null
)

/**
 * A generic Intermediate Representation function, e.g. calc(), var(), min(), clamp(), color-mix().
 */
@Serializable
data class IRFunction(
    val name: String,
    val args: List<IRFunctionArg>
)

/**
 * Primitive Intermediate Representation value types
 */
@Serializable
data class IRLength(
    val value: Double? = null,
    val unit: String? = null,
    val function: IRFunction? = null
)

@Serializable
data class IRColor(
    val raw: String? = null,
    val function: IRFunction? = null
)

@Serializable
data class IRUrl(
    val url: String? = null,
    val function: IRFunction? = null
)

@Serializable
data class IRKeyword(
    val value: String
)

/**
 * Composite types
 */
@Serializable
data class IRShadow(
    val xOffset: IRLength? = null,
    val yOffset: IRLength? = null,
    val blur: IRLength? = null,
    val spread: IRLength? = null,
    val color: IRColor? = null,
    val inset: Boolean = false
)

/**
 * The main IR property.
 */
@Serializable
data class IRProperty(
    val propertyName: String,
    val lengths: List<IRLength> = emptyList(),
    val colors: List<IRColor> = emptyList(),
    val urls: List<IRUrl> = emptyList(),
    val keywords: List<IRKeyword> = emptyList(),
    val shadows: List<IRShadow> = emptyList(),
    val raw: String? = null
)

@Serializable
data class IRSelector(
    val condition: String,
    val properties: MutableList<IRProperty>
)

@Serializable
data class IRMedia(
    val query: String,
    val properties: MutableList<IRProperty>
)

@Serializable
data class IRComponent(
    val name: String,
    val properties: MutableList<IRProperty>,
    val selectors: List<IRSelector>,
    val media: List<IRMedia>
)

@Serializable
data class IRDocument(
    val components: List<IRComponent>
)