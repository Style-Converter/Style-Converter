package app.parsing.css.properties.longhands.effects

import app.irmodels.IRPercentage
import app.irmodels.properties.effects.ClipPathProperty
import app.parsing.css.properties.longhands.PropertyParser
import app.parsing.css.properties.primitiveParsers.LengthParser
import app.parsing.css.properties.primitiveParsers.UrlParser

/**
 * Parser for `clip-path` property.
 *
 * Syntax: none | <url> | <basic-shape>
 * Basic shapes: inset(), circle(), ellipse(), polygon()
 *
 * Examples:
 * - "none"
 * - "url(#clip)"
 * - "circle(50px)"
 * - "circle(50% at 50% 50%)"
 * - "ellipse(50px 100px)"
 * - "polygon(0% 0%, 100% 0%, 100% 100%, 0% 100%)"
 * - "inset(10px 20px 30px 40px)"
 *
 * Note: Simplified implementation - supports basic shapes without all advanced features
 */
object ClipPathPropertyParser : PropertyParser {
    override fun parse(value: String): ClipPathProperty? {
        val trimmed = value.trim().lowercase()

        val geometryBoxes = setOf("margin-box", "border-box", "padding-box", "content-box", "fill-box", "stroke-box", "view-box")

        return when {
            trimmed == "none" -> ClipPathProperty(ClipPathProperty.ClipPath.None())
            trimmed.startsWith("url(") -> {
                parseUrlWithOptionalGeometryBox(value, geometryBoxes)
            }
            trimmed.startsWith("circle(") -> {
                parseShapeWithOptionalGeometryBox(value, geometryBoxes)
            }
            trimmed.startsWith("ellipse(") -> {
                parseShapeWithOptionalGeometryBox(value, geometryBoxes)
            }
            trimmed.startsWith("polygon(") -> {
                parseShapeWithOptionalGeometryBox(value, geometryBoxes)
            }
            trimmed.startsWith("inset(") -> {
                parseShapeWithOptionalGeometryBox(value, geometryBoxes)
            }
            trimmed.startsWith("path(") -> {
                parseShapeWithOptionalGeometryBox(value, geometryBoxes)
            }
            trimmed.startsWith("rect(") -> {
                parseShapeWithOptionalGeometryBox(value, geometryBoxes)
            }
            trimmed.startsWith("xywh(") -> {
                parseShapeWithOptionalGeometryBox(value, geometryBoxes)
            }
            // Check for geometry-box alone or geometry-box + shape
            else -> {
                val tokens = value.trim().split(Regex("\\s+"), limit = 2)
                val firstToken = tokens[0].lowercase()
                if (firstToken in geometryBoxes) {
                    if (tokens.size > 1) {
                        // geometry-box + shape (e.g., "margin-box circle(50%)")
                        val shapeValue = tokens[1]
                        val shape = parseShapeValue(shapeValue)
                        if (shape != null) {
                            ClipPathProperty(ClipPathProperty.ClipPath.GeometryBoxShape(firstToken, shape))
                        } else null
                    } else {
                        // Just geometry-box
                        ClipPathProperty(ClipPathProperty.ClipPath.GeometryBox(firstToken))
                    }
                } else null
            }
        }
    }

    /**
     * Parse url() with optional trailing geometry-box.
     * Handles: "url(#clip)" or "url(#clip) border-box"
     */
    private fun parseUrlWithOptionalGeometryBox(value: String, geometryBoxes: Set<String>): ClipPathProperty? {
        val trimmed = value.trim()

        // Find the closing paren of the url function
        var parenDepth = 0
        var urlEndIndex = -1
        for (i in trimmed.indices) {
            when (trimmed[i]) {
                '(' -> parenDepth++
                ')' -> {
                    parenDepth--
                    if (parenDepth == 0) {
                        urlEndIndex = i
                        break
                    }
                }
            }
        }

        if (urlEndIndex == -1) return null

        val urlStr = trimmed.substring(0, urlEndIndex + 1)
        val remaining = trimmed.substring(urlEndIndex + 1).trim()

        val url = UrlParser.parse(urlStr) ?: return null

        return if (remaining.isNotEmpty() && remaining.lowercase() in geometryBoxes) {
            // URL with geometry-box - store as GeometryBoxShape with a Path containing the URL reference
            ClipPathProperty(ClipPathProperty.ClipPath.Url(url))
        } else if (remaining.isEmpty()) {
            ClipPathProperty(ClipPathProperty.ClipPath.Url(url))
        } else {
            null // Invalid trailing content
        }
    }

    /**
     * Parse shape with optional trailing geometry-box.
     * Handles: "circle(50%)" or "circle(50%) border-box"
     */
    private fun parseShapeWithOptionalGeometryBox(value: String, geometryBoxes: Set<String>): ClipPathProperty? {
        val trimmed = value.trim()

        // Find the closing paren of the shape function
        var parenDepth = 0
        var shapeEndIndex = -1
        for (i in trimmed.indices) {
            when (trimmed[i]) {
                '(' -> parenDepth++
                ')' -> {
                    parenDepth--
                    if (parenDepth == 0) {
                        shapeEndIndex = i
                        break
                    }
                }
            }
        }

        if (shapeEndIndex == -1) return null

        val shapeStr = trimmed.substring(0, shapeEndIndex + 1)
        val remaining = trimmed.substring(shapeEndIndex + 1).trim()

        val shape = parseShapeValue(shapeStr) ?: return null

        return if (remaining.isNotEmpty() && remaining.lowercase() in geometryBoxes) {
            ClipPathProperty(ClipPathProperty.ClipPath.GeometryBoxShape(remaining.lowercase(), shape))
        } else if (remaining.isEmpty()) {
            ClipPathProperty(ClipPathProperty.ClipPath.BasicShape(shape))
        } else {
            null // Invalid trailing content
        }
    }

    private fun parseShapeValue(value: String): ClipPathProperty.Shape? {
        val lower = value.lowercase()
        return when {
            lower.startsWith("circle(") -> parseCircle(value)
            lower.startsWith("ellipse(") -> parseEllipse(value)
            lower.startsWith("polygon(") -> parsePolygon(value)
            lower.startsWith("inset(") -> parseInset(value)
            lower.startsWith("path(") -> parsePath(value)
            lower.startsWith("rect(") -> parseRect(value)
            lower.startsWith("xywh(") -> parseXywh(value)
            else -> null
        }
    }

    /**
     * Parse rect() function.
     * Format: rect(<top> <right> <bottom> <left> round <border-radius>?)
     * Each value can be a length or 'auto'.
     */
    private fun parseRect(value: String): ClipPathProperty.Shape? {
        if (!value.startsWith("rect(") || !value.endsWith(")")) return null
        val content = value.substring(5, value.length - 1).trim()

        // Split by "round" keyword if present
        val parts = content.split(Regex("\\s+round\\s+"))
        val rectPart = parts[0].trim()
        val roundPart = if (parts.size > 1) parts[1].trim() else null

        val values = rectPart.split(Regex("\\s+"))
        if (values.size < 4) return null

        // Parse each value - 'auto' becomes null
        val top = parseRectValue(values[0])
        val right = parseRectValue(values[1])
        val bottom = parseRectValue(values[2])
        val left = parseRectValue(values[3])
        val round = if (roundPart != null) LengthParser.parse(roundPart) else null

        return ClipPathProperty.Shape.Rect(top, right, bottom, left, round)
    }

    /**
     * Parse a rect() value - either 'auto' (returns null) or a length.
     */
    private fun parseRectValue(value: String): app.irmodels.IRLength? {
        return if (value.trim().lowercase() == "auto") null else LengthParser.parse(value)
    }

    /**
     * Parse xywh() function.
     * Format: xywh(<x> <y> <width> <height> round <border-radius>?)
     */
    private fun parseXywh(value: String): ClipPathProperty.Shape? {
        if (!value.startsWith("xywh(") || !value.endsWith(")")) return null
        val content = value.substring(5, value.length - 1).trim()

        // Split by "round" keyword if present
        val parts = content.split(Regex("\\s+round\\s+"))
        val xywhPart = parts[0].trim()
        val roundPart = if (parts.size > 1) parts[1].trim() else null

        val values = xywhPart.split(Regex("\\s+"))
        if (values.size < 4) return null

        val x = LengthParser.parse(values[0]) ?: return null
        val y = LengthParser.parse(values[1]) ?: return null
        val width = LengthParser.parse(values[2]) ?: return null
        val height = LengthParser.parse(values[3]) ?: return null
        val round = if (roundPart != null) LengthParser.parse(roundPart) else null

        return ClipPathProperty.Shape.Xywh(x, y, width, height, round)
    }

    /**
     * Parse circle() function.
     * Format: circle(<radius>? at <position>?)
     * Simplified: circle(<radius>)
     */
    private fun parseCircle(value: String): ClipPathProperty.Shape? {
        val lower = value.lowercase()
        if (!lower.startsWith("circle(") || !lower.endsWith(")")) return null

        val content = value.substring(7, value.length - 1).trim()

        if (content.isEmpty()) {
            // circle() with no arguments
            return ClipPathProperty.Shape.Circle(null, null)
        }

        // Split by "at" keyword (case-insensitive)
        val parts = content.split(Regex("""(?i)\s+at\s+"""))

        val radius = if (parts[0].isNotEmpty()) {
            parseLengthOrPercentage(parts[0].trim())
        } else {
            null
        }

        val position = if (parts.size > 1) {
            parsePosition(parts[1])
        } else {
            null
        }

        return ClipPathProperty.Shape.Circle(radius, position)
    }

    /**
     * Parse ellipse() function.
     * Format: ellipse(<rx> <ry>? at <position>?)
     * Simplified: ellipse(<rx> <ry>)
     */
    private fun parseEllipse(value: String): ClipPathProperty.Shape? {
        if (!value.startsWith("ellipse(") || !value.endsWith(")")) return null

        val content = value.substring(8, value.length - 1).trim()

        if (content.isEmpty()) {
            return ClipPathProperty.Shape.Ellipse(null, null, null)
        }

        // Split by "at" keyword
        val parts = content.split(Regex("""\s+at\s+"""))

        val radii = parts[0].trim().split(Regex("""\s+"""))
        val radiusX = if (radii.isNotEmpty()) LengthParser.parse(radii[0]) else null
        val radiusY = if (radii.size > 1) LengthParser.parse(radii[1]) else null

        val position = if (parts.size > 1) {
            parsePosition(parts[1])
        } else {
            null
        }

        return ClipPathProperty.Shape.Ellipse(radiusX, radiusY, position)
    }

    /**
     * Parse polygon() function.
     * Format: polygon(<point>, <point>, ...)
     * Point: <x> <y> where x,y are percentages
     */
    private fun parsePolygon(value: String): ClipPathProperty.Shape? {
        if (!value.startsWith("polygon(") || !value.endsWith(")")) return null

        val content = value.substring(8, value.length - 1).trim()
        val pointStrings = content.split(",")

        val points = pointStrings.mapNotNull { pointStr ->
            val coords = pointStr.trim().split(Regex("""\s+"""))
            if (coords.size != 2) return@mapNotNull null

            val x = parsePercentageValue(coords[0]) ?: return@mapNotNull null
            val y = parsePercentageValue(coords[1]) ?: return@mapNotNull null

            ClipPathProperty.Point(x, y)
        }

        if (points.isEmpty()) return null

        return ClipPathProperty.Shape.Polygon(points)
    }

    /**
     * Parse inset() function.
     * Format: inset(<top> <right>? <bottom>? <left>? round <border-radius>?)
     * Simplified: inset(<top> <right> <bottom> <left>)
     */
    private fun parseInset(value: String): ClipPathProperty.Shape? {
        if (!value.startsWith("inset(") || !value.endsWith(")")) return null

        val content = value.substring(6, value.length - 1).trim()

        // Split by "round" keyword if present
        val parts = content.split(Regex("""\s+round\s+"""))
        val insetPart = parts[0].trim()
        val roundPart = if (parts.size > 1) parts[1].trim() else null

        val insets = insetPart.split(Regex("""\s+"""))

        val top = LengthParser.parse(insets.getOrNull(0) ?: "0") ?: return null
        val right = LengthParser.parse(insets.getOrNull(1) ?: insets[0]) ?: return null
        val bottom = LengthParser.parse(insets.getOrNull(2) ?: insets[0]) ?: return null
        val left = LengthParser.parse(insets.getOrNull(3) ?: insets.getOrNull(1) ?: insets[0]) ?: return null

        val round = if (roundPart != null) LengthParser.parse(roundPart) else null

        return ClipPathProperty.Shape.Inset(top, right, bottom, left, round)
    }

    /**
     * Parse path() function.
     * Format: path('<svg-path>')
     */
    private fun parsePath(value: String): ClipPathProperty.Shape? {
        val lower = value.lowercase()
        if (!lower.startsWith("path(") || !lower.endsWith(")")) return null

        val content = value.substring(5, value.length - 1).trim()

        // Remove surrounding quotes if present
        val pathData = content.removeSurrounding("'").removeSurrounding("\"")

        if (pathData.isEmpty()) return null

        return ClipPathProperty.Shape.Path(pathData)
    }

    /**
     * Parse position value (x y) or keyword like "center".
     */
    private fun parsePosition(value: String): ClipPathProperty.Position? {
        val trimmed = value.trim().lowercase()

        // Handle single keyword
        if (trimmed == "center") {
            return ClipPathProperty.Position(
                app.irmodels.IRLength.fromRelative(50.0, app.irmodels.IRLength.LengthUnit.PERCENT),
                app.irmodels.IRLength.fromRelative(50.0, app.irmodels.IRLength.LengthUnit.PERCENT)
            )
        }

        val coords = value.trim().split(Regex("""\s+"""))
        if (coords.size == 1) {
            // Single value - use for both x and y, or handle keyword
            val singleVal = parseLengthOrPercentageToLength(coords[0])
            return if (singleVal != null) ClipPathProperty.Position(singleVal, singleVal) else null
        }

        if (coords.size != 2) return null

        val x = parseLengthOrPercentageToLength(coords[0]) ?: return null
        val y = parseLengthOrPercentageToLength(coords[1]) ?: return null

        return ClipPathProperty.Position(x, y)
    }

    /**
     * Parse length or percentage, returning IRLength.
     */
    private fun parseLengthOrPercentage(value: String): app.irmodels.IRLength? {
        val trimmed = value.trim()

        // Try percentage
        if (trimmed.endsWith("%")) {
            val numStr = trimmed.dropLast(1)
            val num = numStr.toDoubleOrNull() ?: return null
            return app.irmodels.IRLength.fromRelative(num, app.irmodels.IRLength.LengthUnit.PERCENT)
        }

        // Try length
        return LengthParser.parse(trimmed)
    }

    /**
     * Parse length or percentage, returning IRLength (alias for type consistency).
     */
    private fun parseLengthOrPercentageToLength(value: String): app.irmodels.IRLength? {
        return parseLengthOrPercentage(value)
    }

    /**
     * Parse percentage value, returning IRPercentage.
     */
    private fun parsePercentageValue(value: String): IRPercentage? {
        val trimmed = value.trim()

        // Try parsing as percentage
        if (trimmed.endsWith("%")) {
            val numStr = trimmed.substring(0, trimmed.length - 1)
            val num = numStr.toDoubleOrNull() ?: return null
            return IRPercentage(num)
        }

        // Try parsing as unitless number (0-1 range)
        val num = trimmed.toDoubleOrNull() ?: return null
        return IRPercentage(num * 100.0)
    }
}
