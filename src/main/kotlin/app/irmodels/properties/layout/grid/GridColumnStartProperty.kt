package app.irmodels.properties.layout.grid

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class GridColumnStartProperty(
    val value: GridLine
) : IRProperty {
    override val propertyName = "grid-column-start"
}

@Serializable
sealed interface GridLine {
    @Serializable
    data class Auto(val unit: Unit = Unit) : GridLine

    @Serializable
    data class LineNumber(val number: Int) : GridLine

    @Serializable
    data class LineName(val name: String) : GridLine

    @Serializable
    data class Span(val count: Int) : GridLine

    @Serializable
    data class SpanName(val name: String) : GridLine
}
