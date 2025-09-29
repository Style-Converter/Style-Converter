package app.parsing.css.mediaQueries

import app.IRMedia
import app.IRProperty
import app.parsing.css.CssMedia
import app.parsing.css.properties.PropertiesParser

fun parseMedia(media: List<CssMedia>?): List<IRMedia> {
	if (media == null) return emptyList()
    return media.map { m ->
        IRMedia(query = m.query, properties = PropertiesParser.parse(m.properties))
    }
}


