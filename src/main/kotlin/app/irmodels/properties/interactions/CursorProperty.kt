package app.irmodels.properties.interactions

import app.irmodels.IRProperty
import app.irmodels.IRUrl
import kotlinx.serialization.Serializable

@Serializable
data class CursorProperty(
    val value: Cursor
) : IRProperty {
    override val propertyName = "cursor"

    @Serializable
    sealed interface Cursor {
        @Serializable
        data class Keyword(val value: CursorKeyword) : Cursor

        @Serializable
        data class Url(val url: IRUrl, val fallback: CursorKeyword?) : Cursor

        enum class CursorKeyword {
            AUTO, DEFAULT, NONE, CONTEXT_MENU, HELP, POINTER,
            PROGRESS, WAIT, CELL, CROSSHAIR, TEXT, VERTICAL_TEXT,
            ALIAS, COPY, MOVE, NO_DROP, NOT_ALLOWED, GRAB, GRABBING,
            ALL_SCROLL, COL_RESIZE, ROW_RESIZE, N_RESIZE, E_RESIZE,
            S_RESIZE, W_RESIZE, NE_RESIZE, NW_RESIZE, SE_RESIZE,
            SW_RESIZE, EW_RESIZE, NS_RESIZE, NESW_RESIZE, NWSE_RESIZE,
            ZOOM_IN, ZOOM_OUT
        }
    }
}
