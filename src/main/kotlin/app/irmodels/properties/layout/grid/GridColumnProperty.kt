package app.irmodels.properties.layout.grid

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `grid-column` property.
 *
 * ## CSS Property
 * **Syntax**: `grid-column: <grid-column-start> / <grid-column-end>`
 *
 * ## Description
 * Shorthand property for grid-column-start and grid-column-end.
 * Specifies a grid item's position within the grid column.
 *
 * ## Examples
 * ```kotlin
 * GridColumnProperty(
 *     start = "1",
 *     end = "3"
 * )
 * ```
 *
 * @property start Grid column start position (optional)
 * @property end Grid column end position (optional)
 * @see [MDN grid-column](https://developer.mozilla.org/en-US/docs/Web/CSS/grid-column)
 */
@Serializable
data class GridColumnProperty(
    val start: String? = null,
    val end: String? = null
) : IRProperty {
    override val propertyName = "grid-column"
}
