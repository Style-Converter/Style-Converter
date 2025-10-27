package app.irmodels.properties.columns

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class ColumnWidthProperty(
    val width: ColumnWidth
) : IRProperty {
    override val propertyName = "column-width"

    @Serializable
    sealed interface ColumnWidth {
        @Serializable
        data class Auto(val unit: kotlin.Unit = kotlin.Unit) : ColumnWidth

        @Serializable
        data class LengthValue(val length: IRLength) : ColumnWidth
    }
}
