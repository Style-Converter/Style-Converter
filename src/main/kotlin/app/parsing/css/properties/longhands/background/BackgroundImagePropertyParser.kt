package app.parsing.css.properties.longhands.background

import app.irmodels.*
import app.irmodels.properties.background.BackgroundImageProperty
import app.parsing.css.properties.longhands.PropertyParser
import app.parsing.css.properties.primitiveParsers.AngleParser
import app.parsing.css.properties.primitiveParsers.ColorParser
import app.parsing.css.properties.primitiveParsers.PercentageParser
import app.parsing.css.properties.primitiveParsers.UrlParser
import app.parsing.css.properties.primitiveParsers.ExpressionDetector
import app.parsing.css.properties.primitiveParsers.GlobalKeywords
import app.parsing.css.properties.primitiveParsers.TokenizationUtils

/**
 * Parser for the `background-image` property.
 *
 * Supports:
 * - none: No background image
 * - url(): Image from URL or data URI
 * - linear-gradient(): Linear gradient
 * - radial-gradient(): Radial gradient
 * - conic-gradient(): Conic gradient
 * - repeating-linear-gradient(): Repeating linear gradient
 * - repeating-radial-gradient(): Repeating repeating radial gradient
 * - repeating-conic-gradient(): Repeating conic gradient
 *
 * Multiple images can be specified as comma-separated values.
 */
object BackgroundImagePropertyParser : PropertyParser {
    override fun parse(value: String): IRProperty? {
        val trimmed = value.trim()
        val lowered = trimmed.lowercase()

        // Handle global keywords
        if (GlobalKeywords.isGlobalKeyword(lowered)) {
            return BackgroundImageProperty(listOf(BackgroundImageProperty.BackgroundImage.Keyword(lowered)))
        }

        // Check for var() or other complex expressions - use Raw
        if (ExpressionDetector.containsExpression(lowered)) {
            return BackgroundImageProperty(listOf(BackgroundImageProperty.BackgroundImage.Raw(trimmed)))
        }

        // Split by comma for multiple background images
        val imageStrings = TokenizationUtils.splitByComma(lowered)
        if (imageStrings.isEmpty()) {
            return BackgroundImageProperty(listOf(BackgroundImageProperty.BackgroundImage.Raw(trimmed)))
        }

        val images = imageStrings.map { parseImage(it.trim()) ?: BackgroundImageProperty.BackgroundImage.Raw(it.trim()) }

        return BackgroundImageProperty(images)
    }

    private fun parseImage(value: String): BackgroundImageProperty.BackgroundImage? {
        return when {
            value == "none" -> BackgroundImageProperty.BackgroundImage.None()
            value.startsWith("url(") -> parseUrl(value)
            value.startsWith("linear-gradient(") -> parseLinearGradient(value, repeating = false)
            value.startsWith("repeating-linear-gradient(") -> parseLinearGradient(value, repeating = true)
            value.startsWith("radial-gradient(") -> parseRadialGradient(value, repeating = false)
            value.startsWith("repeating-radial-gradient(") -> parseRadialGradient(value, repeating = true)
            value.startsWith("conic-gradient(") -> parseConicGradient(value, repeating = false)
            value.startsWith("repeating-conic-gradient(") -> parseConicGradient(value, repeating = true)
            else -> null
        }
    }

    private fun parseUrl(value: String): BackgroundImageProperty.BackgroundImage? {
        val url = UrlParser.parse(value) ?: return null
        return BackgroundImageProperty.BackgroundImage.Url(url)
    }

    private fun parseLinearGradient(value: String, repeating: Boolean): BackgroundImageProperty.BackgroundImage? {
        val funcName = if (repeating) "repeating-linear-gradient" else "linear-gradient"
        val content = TokenizationUtils.extractFunctionContent(value, funcName) ?: return null

        // Parse angle and color stops
        val parts = TokenizationUtils.splitByComma(content)
        if (parts.isEmpty()) return null

        var angle: IRAngle? = null
        var colorStopStart = 0

        // Check if first part is an angle or direction
        val firstPart = parts[0].trim()

        // Try parsing as angle first
        AngleParser.parse(firstPart)?.let {
            angle = it
            colorStopStart = 1
        } ?: run {
            // Try parsing as direction keyword (to right, to bottom, to top left, etc.)
            if (firstPart.startsWith("to ")) {
                angle = parseDirectionToAngle(firstPart)
                if (angle != null) {
                    colorStopStart = 1
                }
            }
        }

        // Parse color stops
        val colorStops = parts.drop(colorStopStart).mapNotNull { parseColorStop(it.trim()) }
        if (colorStops.isEmpty()) return null

        return BackgroundImageProperty.BackgroundImage.LinearGradient(angle, colorStops, repeating)
    }

    /**
     * Convert "to right", "to bottom", etc. to angle values.
     */
    private fun parseDirectionToAngle(direction: String): IRAngle? {
        return when (direction.lowercase()) {
            "to top" -> IRAngle.fromDegrees(0.0)
            "to top right", "to right top" -> IRAngle.fromDegrees(45.0)
            "to right" -> IRAngle.fromDegrees(90.0)
            "to bottom right", "to right bottom" -> IRAngle.fromDegrees(135.0)
            "to bottom" -> IRAngle.fromDegrees(180.0)
            "to bottom left", "to left bottom" -> IRAngle.fromDegrees(225.0)
            "to left" -> IRAngle.fromDegrees(270.0)
            "to top left", "to left top" -> IRAngle.fromDegrees(315.0)
            else -> null
        }
    }

    private fun parseRadialGradient(value: String, repeating: Boolean): BackgroundImageProperty.BackgroundImage? {
        val funcName = if (repeating) "repeating-radial-gradient" else "radial-gradient"
        val content = TokenizationUtils.extractFunctionContent(value, funcName) ?: return null

        // Simplified parsing: just extract color stops
        val parts = TokenizationUtils.splitByComma(content)
        val colorStops = parts.mapNotNull { parseColorStop(it.trim()) }
        if (colorStops.isEmpty()) return null

        return BackgroundImageProperty.BackgroundImage.RadialGradient(null, null, null, colorStops, repeating)
    }

    private fun parseConicGradient(value: String, repeating: Boolean): BackgroundImageProperty.BackgroundImage? {
        val funcName = if (repeating) "repeating-conic-gradient" else "conic-gradient"
        val content = TokenizationUtils.extractFunctionContent(value, funcName) ?: return null

        val parts = TokenizationUtils.splitByComma(content)
        if (parts.isEmpty()) return null

        // Handle "from <angle>" and/or "at <position>" prefix before color stops
        var fromAngle: IRAngle? = null
        var colorStopStart = 0
        val firstPart = parts[0].trim()

        if (firstPart.startsWith("from ")) {
            val afterFrom = firstPart.removePrefix("from ").trim()
            val atIndex = afterFrom.indexOf(" at ")
            val anglePart = if (atIndex >= 0) afterFrom.substring(0, atIndex).trim() else afterFrom
            fromAngle = AngleParser.parse(anglePart)
            colorStopStart = 1
        } else if (firstPart.startsWith("at ")) {
            colorStopStart = 1
        }

        val colorStops = parts.drop(colorStopStart).mapNotNull { parseColorStop(it.trim()) }
        if (colorStops.isEmpty()) return null

        return BackgroundImageProperty.BackgroundImage.ConicGradient(fromAngle, null, colorStops, repeating)
    }

    private fun parseColorStop(value: String): BackgroundImageProperty.ColorStop? {
        // Split by space to separate color and position
        val parts = value.split("""\s+""".toRegex())
        if (parts.isEmpty()) return null

        val color = ColorParser.parse(parts[0]) ?: return null
        val position = if (parts.size > 1) PercentageParser.parse(parts[1]) else null

        return BackgroundImageProperty.ColorStop(color, position)
    }

}
