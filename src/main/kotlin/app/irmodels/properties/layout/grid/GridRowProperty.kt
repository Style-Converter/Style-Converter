package app.irmodels.properties.layout.grid

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `grid-row` property.
 *
 * ## CSS Property
 * **Syntax**: `grid-row: <grid-row-start> / <grid-row-end>`
 *
 * ## Description
 * Shorthand property for grid-row-start and grid-row-end.
 * Specifies a grid item's position within the grid row.
 *
 * ## Examples
 * ```kotlin
 * GridRowProperty(
 *     start = "1",
 *     end = "3"
 * )
 * ```
 *
 * @property start Grid row start position (optional)
 * @property end Grid row end position (optional)
 * @see [MDN grid-row](https://developer.mozilla.org/en-US/docs/Web/CSS/grid-row)
 */
@Serializable
data class GridRowProperty(
    val start: String? = null,
    val end: String? = null
) : IRProperty {
    override val propertyName = "grid-row"
}
