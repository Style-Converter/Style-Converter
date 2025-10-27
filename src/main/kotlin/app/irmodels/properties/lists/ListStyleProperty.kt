package app.irmodels.properties.lists

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `list-style` property.
 *
 * ## CSS Property
 * **Syntax**: `list-style: <list-style-type> <list-style-position> <list-style-image>`
 *
 * ## Description
 * Shorthand property for setting list-style-type, list-style-position, and list-style-image.
 *
 * ## Examples
 * ```kotlin
 * ListStyleProperty(
 *     type = "disc",
 *     position = "inside",
 *     image = "none"
 * )
 * ```
 *
 * @property type List style type (optional)
 * @property position List style position (optional)
 * @property image List style image (optional)
 * @see [MDN list-style](https://developer.mozilla.org/en-US/docs/Web/CSS/list-style)
 */
@Serializable
data class ListStyleProperty(
    val type: String? = null,
    val position: String? = null,
    val image: String? = null
) : IRProperty {
    override val propertyName = "list-style"
}
