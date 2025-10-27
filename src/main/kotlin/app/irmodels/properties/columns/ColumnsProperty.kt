package app.irmodels.properties.columns

import app.irmodels.IRProperty
import app.irmodels.IRLength
import app.irmodels.IRNumber
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `columns` property.
 *
 * ## CSS Property
 * **Syntax**: `columns: <column-width> || <column-count>`
 *
 * ## Description
 * Shorthand property for setting column-width and column-count simultaneously.
 * Defines the ideal width and/or number of columns in a multi-column layout.
 *
 * ## Examples
 * ```kotlin
 * // Set both width and count
 * ColumnsProperty(
 *     width = ColumnWidthValue.Length(IRLength(200.0, LengthUnit.PX)),
 *     count = ColumnCountValue.Count(IRNumber(3.0))
 * )
 *
 * // Auto width with fixed count
 * ColumnsProperty(
 *     width = ColumnWidthValue.Auto,
 *     count = ColumnCountValue.Count(IRNumber(3.0))
 * )
 * ```
 *
 * @property width Column width value (optional)
 * @property count Column count value (optional)
 * @see [MDN columns](https://developer.mozilla.org/en-US/docs/Web/CSS/columns)
 */
@Serializable
data class ColumnsProperty(
    val width: ColumnWidthValue? = null,
    val count: ColumnCountValue? = null
) : IRProperty {
    override val propertyName = "columns"
}

/**
 * Represents column-width values.
 */
@Serializable
sealed interface ColumnWidthValue {
    @Serializable
    data class Length(val value: IRLength) : ColumnWidthValue

    @Serializable
    data object Auto : ColumnWidthValue
}

/**
 * Represents column-count values.
 */
@Serializable
sealed interface ColumnCountValue {
    @Serializable
    data class Count(val value: IRNumber) : ColumnCountValue

    @Serializable
    data object Auto : ColumnCountValue
}
