package app.parsing.css.styles.opacity

import app.BaseIR
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive

internal object opacity {
	fun applyOpacity(prop: String, value: JsonElement, acc: BaseIR): BaseIR {
		val v = value.jsonPrimitive.doubleOrNull ?: return acc
		return acc.copy(opacity = v)
	}
}


