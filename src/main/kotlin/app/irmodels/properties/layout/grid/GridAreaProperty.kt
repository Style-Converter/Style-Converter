package app.irmodels.properties.layout.grid

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class GridAreaProperty(
    val value: GridAreaValue
) : IRProperty {
    override val propertyName = "grid-area"

    @Serializable
    sealed interface GridAreaValue {
        @Serializable
        data class Auto(val unit: Unit = Unit) : GridAreaValue

        @Serializable
        data class AreaName(val name: String) : GridAreaValue

        @Serializable
        data class Lines(
            val rowStart: GridLine,
            val columnStart: GridLine,
            val rowEnd: GridLine,
            val columnEnd: GridLine
        ) : GridAreaValue
    }
}
