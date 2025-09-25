package app.parsing

import app.*

/**
 * Parses a CSS size token into numeric value and unit string.
 * Examples: "2px" -> (2.0, "px"), "1.5em" -> (1.5, "em"), "0" -> (0.0, null)
 */
fun parseSizeToken(s: String): Pair<Double?, String?> {
    val t = s.trim()
    val re = Regex("^(-?\\d*\\.?\\d+)([a-zA-Z%]+)?$")
    val m = re.find(t)
    if (m != null) {
        val num = m.groupValues[1].toDoubleOrNull()
        val unit = m.groupValues.getOrNull(2)?.ifEmpty { null }
        return num to unit
    }
    // keyword widths
    return when (t.lowercase()) {
        "thin" -> 1.0 to "px"
        "medium" -> 3.0 to "px"
        "thick" -> 5.0 to "px"
        else -> null to null
    }
}

/**
 * Parses a color string into ColorRgba format, supporting hex and rgb/rgba formats
 * @param input Color string (e.g., "#ff0000", "rgb(255,0,0)", "rgba(255,0,0,0.5)")
 * @return Parsed ColorRgba object, or null if the format is invalid or input is null
 */
fun parseColor(input: String?): ColorRgba? {
    if (input == null) return null
    val s = input.trim()
    if (s.equals("transparent", ignoreCase = true)) return ColorRgba(0, 0, 0, 0.0)
    val named = mapOf(
        "black" to ColorRgba(0,0,0,1.0),
        "white" to ColorRgba(255,255,255,1.0),
        "red" to ColorRgba(255,0,0,1.0),
        "green" to ColorRgba(0,128,0,1.0),
        "blue" to ColorRgba(0,0,255,1.0),
        "purple" to ColorRgba(128,0,128,1.0),
        "orange" to ColorRgba(255,165,0,1.0),
        "teal" to ColorRgba(0,128,128,1.0),
        "gray" to ColorRgba(128,128,128,1.0),
        "grey" to ColorRgba(128,128,128,1.0),
        "yellow" to ColorRgba(255,255,0,1.0),
        "cyan" to ColorRgba(0,255,255,1.0),
        "aqua" to ColorRgba(0,255,255,1.0),
        "magenta" to ColorRgba(255,0,255,1.0),
        "lime" to ColorRgba(0,255,0,1.0),
        "hotpink" to ColorRgba(255,105,180,1.0)
    )
    named[s.lowercase()]?.let { return it }
    
    // Parse hex formats: #rgb, #rgba, #rrggbb, #rrggbbaa
    if (s.startsWith('#')) {
        val hex = s.substring(1)
        fun expand(c: Char): String = "$c$c"
        when (hex.length) {
            3 -> {
                val r = expand(hex[0]).toInt(16)
                val g = expand(hex[1]).toInt(16)
                val b = expand(hex[2]).toInt(16)
                return ColorRgba(r, g, b, 1.0)
            }
            4 -> {
                val r = expand(hex[0]).toInt(16)
                val g = expand(hex[1]).toInt(16)
                val b = expand(hex[2]).toInt(16)
                val a = expand(hex[3]).toInt(16) / 255.0
                return ColorRgba(r, g, b, a)
            }
            6 -> {
                val r = hex.substring(0, 2).toInt(16)
                val g = hex.substring(2, 4).toInt(16)
                val b = hex.substring(4, 6).toInt(16)
                return ColorRgba(r, g, b, 1.0)
            }
            8 -> {
                val r = hex.substring(0, 2).toInt(16)
                val g = hex.substring(2, 4).toInt(16)
                val b = hex.substring(4, 6).toInt(16)
                val a = hex.substring(6, 8).toInt(16) / 255.0
                return ColorRgba(r, g, b, a)
            }
        }
    }
    
    // Parse rgb/rgba format: rgb(255,0,0) or rgba(255,0,0,0.5)
    val rgb = Regex("^rgba?\\((\\d+),\\s*(\\d+),\\s*(\\d+)(?:,\\s*(\\d*\\.?\\d+))?\\)$").find(s)
    if (rgb != null) {
        val r = rgb.groupValues[1].toInt().coerceIn(0, 255)
        val g = rgb.groupValues[2].toInt().coerceIn(0, 255)
        val b = rgb.groupValues[3].toInt().coerceIn(0, 255)
        val a = rgb.groupValues.getOrNull(4)?.takeIf { it.isNotEmpty() }?.toDoubleOrNull()?.coerceIn(0.0, 1.0) ?: 1.0
        return ColorRgba(r, g, b, a)
    }

    // Parse hsl/hsla format: hsl(210, 50%, 40%) or hsla(210, 50%, 40%, .5)
    val hsl = Regex("^hsla?\\(([-+]?[\\d.]+),\\s*([-+]?[\\d.]+)%?,\\s*([-+]?[\\d.]+)%?(?:,\\s*(\\d*\\.?\\d+))?\\)$", RegexOption.IGNORE_CASE).find(s)
    if (hsl != null) {
        val h = hsl.groupValues[1].toDoubleOrNull() ?: return null
        val ss = hsl.groupValues[2].toDoubleOrNull()?.coerceIn(0.0, 100.0)?.div(100.0) ?: return null
        val l = hsl.groupValues[3].toDoubleOrNull()?.coerceIn(0.0, 100.0)?.div(100.0) ?: return null
        val a = hsl.groupValues.getOrNull(4)?.takeIf { it.isNotEmpty() }?.toDoubleOrNull()?.coerceIn(0.0, 1.0) ?: 1.0
        fun hue2rgb(p: Double, q: Double, tIn: Double): Double {
            var t = tIn
            if (t < 0) t += 1.0
            if (t > 1) t -= 1.0
            return when {
                t < 1.0/6.0 -> p + (q - p) * 6.0 * t
                t < 1.0/2.0 -> q
                t < 2.0/3.0 -> p + (q - p) * (2.0/3.0 - t) * 6.0
                else -> p
            }
        }
        val q = if (l < 0.5) l * (1 + ss) else l + ss - l * ss
        val p = 2 * l - q
        val hk = ((h % 360.0) + 360.0) % 360.0 / 360.0
        val r = (hue2rgb(p, q, hk + 1.0/3.0) * 255.0).toInt().coerceIn(0, 255)
        val g = (hue2rgb(p, q, hk) * 255.0).toInt().coerceIn(0, 255)
        val b = (hue2rgb(p, q, hk - 1.0/3.0) * 255.0).toInt().coerceIn(0, 255)
        return ColorRgba(r, g, b, a)
    }
    return null
}
