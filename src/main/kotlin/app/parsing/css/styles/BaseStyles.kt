package app.parsing.css.styles

import app.BaseIR
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import app.parsing.css.styles.background.background
import app.parsing.css.styles.opacity.opacity
import app.parsing.css.styles.border.border

typealias CssHandler = (prop: String, value: JsonElement, acc: BaseIR) -> BaseIR

fun parseBaseStyles(styles: JsonObject): BaseIR {
	var acc = BaseIR()
	for ((key, value) in styles) {
		val handler = HANDLERS[key]
		if (handler != null) {
			acc = handler(key, value, acc)
		}
	}
	return acc
}

private val HANDLERS: Map<String, CssHandler> = mapOf(
	"background-color" to { p, v, acc -> background.applyBackgroundColor(p, v, acc) },
	"opacity" to { p, v, acc -> opacity.applyOpacity(p, v, acc) },
	"border-top" to { _, v, acc -> border.applyBorderSide("top", v, acc) },
	"border-right" to { _, v, acc -> border.applyBorderSide("right", v, acc) },
	"border-bottom" to { _, v, acc -> border.applyBorderSide("bottom", v, acc) },
	"border-left" to { _, v, acc -> border.applyBorderSide("left", v, acc) },
	"border-top-left-radius" to { _, v, acc -> border.applyRadiusCorner("topLeft", v, acc) },
	"border-top-right-radius" to { _, v, acc -> border.applyRadiusCorner("topRight", v, acc) },
	"border-bottom-right-radius" to { _, v, acc -> border.applyRadiusCorner("bottomRight", v, acc) },
	"border-bottom-left-radius" to { _, v, acc -> border.applyRadiusCorner("bottomLeft", v, acc) }
)


