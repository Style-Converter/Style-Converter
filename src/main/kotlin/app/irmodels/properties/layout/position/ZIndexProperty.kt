package app.irmodels.properties.layout.position

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class ZIndexProperty(
    val value: ZIndex
) : IRProperty {
    override val propertyName = "z-index"

    @Serializable
    sealed interface ZIndex {
        @Serializable
        data class Auto(val unit: Unit = Unit) : ZIndex

        @Serializable
        data class Integer(val value: Int) : ZIndex
    }
}
