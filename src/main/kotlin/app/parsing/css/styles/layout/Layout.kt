package app.parsing.css.styles.layout

import app.BaseIR
import app.LayoutIR
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal object layout {
	fun applyDisplay(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
		val v = value.jsonPrimitive.content
		val cur = acc.layout ?: LayoutIR()
		return acc.copy(layout = cur.copy(display = v))
	}
}


