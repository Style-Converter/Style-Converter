package app.irmodels.properties.layout.grid

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class GridTemplateAreasProperty(
    val value: GridTemplateAreas
) : IRProperty {
    override val propertyName = "grid-template-areas"

    @Serializable
    sealed interface GridTemplateAreas {
        @Serializable
        data class None(val unit: Unit = Unit) : GridTemplateAreas

        @Serializable
        data class Areas(val rows: List<List<String>>) : GridTemplateAreas
    }
}
