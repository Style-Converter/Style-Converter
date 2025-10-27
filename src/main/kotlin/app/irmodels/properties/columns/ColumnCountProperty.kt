package app.irmodels.properties.columns

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class ColumnCountProperty(
    val count: ColumnCount
) : IRProperty {
    override val propertyName = "column-count"

    @Serializable
    sealed interface ColumnCount {
        @Serializable
        data class Auto(val unit: kotlin.Unit = kotlin.Unit) : ColumnCount

        @Serializable
        data class Number(val value: IRNumber) : ColumnCount
    }
}
