package app.irmodels.properties.layout.grid

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `grid` property.
 *
 * ## CSS Property
 * **Syntax**: `grid: <grid-template-rows> / <grid-template-columns> | <grid-template-areas> | <grid-auto-rows> / <grid-auto-columns> / <grid-auto-flow>`
 *
 * ## Description
 * Shorthand property for setting grid-template-rows, grid-template-columns, grid-template-areas,
 * grid-auto-rows, grid-auto-columns, and grid-auto-flow in a single declaration.
 *
 * ## Examples
 * ```kotlin
 * GridProperty(
 *     templateRows = "100px 1fr",
 *     templateColumns = "200px 1fr 1fr",
 *     templateAreas = null,
 *     autoRows = null,
 *     autoColumns = null,
 *     autoFlow = null
 * )
 * ```
 *
 * @property templateRows Grid template rows (optional)
 * @property templateColumns Grid template columns (optional)
 * @property templateAreas Grid template areas (optional)
 * @property autoRows Grid auto rows (optional)
 * @property autoColumns Grid auto columns (optional)
 * @property autoFlow Grid auto flow (optional)
 * @see [MDN grid](https://developer.mozilla.org/en-US/docs/Web/CSS/grid)
 */
@Serializable
data class GridProperty(
    val templateRows: String? = null,
    val templateColumns: String? = null,
    val templateAreas: String? = null,
    val autoRows: String? = null,
    val autoColumns: String? = null,
    val autoFlow: String? = null
) : IRProperty {
    override val propertyName = "grid"
}
