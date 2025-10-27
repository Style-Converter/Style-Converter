package app.irmodels.properties.layout.flexbox

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class DisplayProperty(
    val value: DisplayValue
) : IRProperty {
    override val propertyName = "display"

    enum class DisplayValue {
        NONE, BLOCK, INLINE, INLINE_BLOCK,
        FLEX, INLINE_FLEX,
        GRID, INLINE_GRID,
        TABLE, INLINE_TABLE, TABLE_ROW, TABLE_CELL,
        LIST_ITEM, CONTENTS
    }
}
