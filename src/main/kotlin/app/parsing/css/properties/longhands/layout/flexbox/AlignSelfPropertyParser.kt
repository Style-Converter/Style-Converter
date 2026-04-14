package app.parsing.css.properties.longhands.layout.flexbox

import app.irmodels.IRProperty
import app.irmodels.properties.layout.flexbox.AlignSelfProperty
import app.parsing.css.properties.longhands.PropertyParser

object AlignSelfPropertyParser : PropertyParser {
    override fun parse(value: String): IRProperty? {
        val trimmed = value.trim().lowercase()
        val alignSelf = when (trimmed) {
            "auto" -> AlignSelfProperty.AlignSelf.AUTO
            "flex-start" -> AlignSelfProperty.AlignSelf.FLEX_START
            "flex-end" -> AlignSelfProperty.AlignSelf.FLEX_END
            "center" -> AlignSelfProperty.AlignSelf.CENTER
            "baseline" -> AlignSelfProperty.AlignSelf.BASELINE
            "stretch" -> AlignSelfProperty.AlignSelf.STRETCH
            "start" -> AlignSelfProperty.AlignSelf.START
            "end" -> AlignSelfProperty.AlignSelf.END
            "self-start" -> AlignSelfProperty.AlignSelf.SELF_START
            "self-end" -> AlignSelfProperty.AlignSelf.SELF_END
            else -> return null
        }
        return AlignSelfProperty(alignSelf)
    }
}
