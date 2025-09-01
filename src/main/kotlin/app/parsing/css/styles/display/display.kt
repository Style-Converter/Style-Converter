package app.parsing.css.styles.display

import app.BaseIR
import app.LayoutIR
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object display {
    fun applyDisplay(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
        val v = value.jsonPrimitive.content.trim()
        val layout = acc.layout ?: LayoutIR()
        return acc.copy(layout = layout.copy(display = v))
    }
}

