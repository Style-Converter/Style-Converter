package app.parsing.css.properties.shorthand

/**
 * Expands grid-template shorthand.
 * This is complex - for now we'll handle simple cases.
 * Full syntax: <grid-template-rows> / <grid-template-columns>
 * Or: none
 */
object GridTemplateExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        val trimmed = value.trim()

        if (trimmed.lowercase() == "none") {
            return mapOf(
                "grid-template-rows" to "none",
                "grid-template-columns" to "none",
                "grid-template-areas" to "none"
            )
        }

        // Simple case: rows / columns
        if (trimmed.contains("/")) {
            val parts = trimmed.split("/").map { it.trim() }
            return mapOf(
                "grid-template-rows" to (parts.getOrNull(0) ?: "none"),
                "grid-template-columns" to (parts.getOrNull(1) ?: "none"),
                "grid-template-areas" to "none"
            )
        }

        // Single value - unclear which it applies to, default to columns
        return mapOf(
            "grid-template-rows" to "none",
            "grid-template-columns" to trimmed,
            "grid-template-areas" to "none"
        )
    }
}

/**
 * Expands grid shorthand (very complex - simplified version).
 * Syntax: <grid-template> || <grid-auto-flow> [ <grid-auto-rows> [ / <grid-auto-columns> ] ]?
 */
object GridExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        // For simplicity, delegate to grid-template if it looks like template syntax
        if (value.contains("/") || value.lowercase() == "none") {
            val template = GridTemplateExpander.expand(value)
            return template + mapOf(
                "grid-auto-flow" to "row",
                "grid-auto-rows" to "auto",
                "grid-auto-columns" to "auto"
            )
        }

        // Otherwise keep as-is (complex syntax)
        return mapOf(
            "grid-template-rows" to "none",
            "grid-template-columns" to "none",
            "grid-template-areas" to "none",
            "grid-auto-flow" to value,
            "grid-auto-rows" to "auto",
            "grid-auto-columns" to "auto"
        )
    }
}

/**
 * Expands grid-row shorthand.
 * Syntax: <start> / <end>
 */
object GridRowExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        val trimmed = value.trim()

        if (trimmed.contains("/")) {
            val parts = trimmed.split("/").map { it.trim() }
            return mapOf(
                "grid-row-start" to (parts.getOrNull(0) ?: "auto"),
                "grid-row-end" to (parts.getOrNull(1) ?: "auto")
            )
        }

        return mapOf(
            "grid-row-start" to trimmed,
            "grid-row-end" to "auto"
        )
    }
}

/**
 * Expands grid-column shorthand.
 * Syntax: <start> / <end>
 */
object GridColumnExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        val trimmed = value.trim()

        if (trimmed.contains("/")) {
            val parts = trimmed.split("/").map { it.trim() }
            return mapOf(
                "grid-column-start" to (parts.getOrNull(0) ?: "auto"),
                "grid-column-end" to (parts.getOrNull(1) ?: "auto")
            )
        }

        return mapOf(
            "grid-column-start" to trimmed,
            "grid-column-end" to "auto"
        )
    }
}

/**
 * Expands grid-area shorthand.
 * Syntax: <row-start> / <column-start> / <row-end> / <column-end>
 * Or: <area-name>
 */
object GridAreaExpander : ShorthandExpander {
    override fun expand(value: String): Map<String, String> {
        val trimmed = value.trim()

        if (trimmed.contains("/")) {
            val parts = trimmed.split("/").map { it.trim() }
            return mapOf(
                "grid-row-start" to (parts.getOrNull(0) ?: "auto"),
                "grid-column-start" to (parts.getOrNull(1) ?: "auto"),
                "grid-row-end" to (parts.getOrNull(2) ?: "auto"),
                "grid-column-end" to (parts.getOrNull(3) ?: "auto")
            )
        }

        // Single identifier - grid area name
        return mapOf(
            "grid-row-start" to trimmed,
            "grid-column-start" to trimmed,
            "grid-row-end" to trimmed,
            "grid-column-end" to trimmed
        )
    }
}
