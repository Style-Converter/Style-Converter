package app.parsing.css.properties.shorthand

/** Expands margin shorthand. Handles 1-4 value syntax (top right bottom left). */
val MarginExpander = FourValueExpander("margin-top", "margin-right", "margin-bottom", "margin-left")

/** Expands padding shorthand. Handles 1-4 value syntax (top right bottom left). */
val PaddingExpander = FourValueExpander("padding-top", "padding-right", "padding-bottom", "padding-left")
