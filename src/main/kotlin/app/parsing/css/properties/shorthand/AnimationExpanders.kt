package app.parsing.css.properties.shorthand

import app.parsing.css.properties.tokenizers.WhitespaceTokenizer

/**
 * Expands transition shorthand.
 * Full syntax: <property> <duration> <timing-function> <delay>
 * Supports comma-separated multiple transitions.
 */
object TransitionExpander : MultiValueShorthandExpander() {
    private val timingFunctions = setOf(
        "ease", "ease-in", "ease-out", "ease-in-out", "linear",
        "step-start", "step-end"
    )

    override fun expandSingle(tokens: List<String>): Map<String, String> {
        var property = "all"
        var duration = "0s"
        var timingFunction = "ease"
        var delay = "0s"
        var durationSet = false

        for (token in tokens) {
            when {
                // Timing function keyword
                token.lowercase() in timingFunctions -> timingFunction = token
                token.startsWith("cubic-bezier(") -> timingFunction = token
                token.startsWith("steps(") -> timingFunction = token

                // Time values (duration or delay)
                token.matches(Regex("^[0-9.]+m?s$")) -> {
                    if (!durationSet) {
                        duration = token
                        durationSet = true
                    } else {
                        delay = token
                    }
                }

                // Property name
                else -> property = token
            }
        }

        return mapOf(
            "transition-property" to property,
            "transition-duration" to duration,
            "transition-timing-function" to timingFunction,
            "transition-delay" to delay
        )
    }

    override fun expandMultiple(items: List<String>): Map<String, String> {
        // Multiple transitions - extract first token from each as property name
        return mapOf(
            "transition-property" to items.map { WhitespaceTokenizer.tokenize(it)[0] }.joinToString(", "),
            "transition-duration" to "0s",
            "transition-timing-function" to "ease",
            "transition-delay" to "0s"
        )
    }
}

/**
 * Expands animation shorthand.
 * Syntax: <name> <duration> <timing-function> <delay> <iteration-count> <direction> <fill-mode> <play-state>
 * Supports comma-separated multiple animations.
 */
object AnimationExpander : MultiValueShorthandExpander() {
    private val timingFunctions = setOf(
        "ease", "ease-in", "ease-out", "ease-in-out", "linear",
        "step-start", "step-end"
    )
    private val directions = setOf("normal", "reverse", "alternate", "alternate-reverse")
    private val fillModes = setOf("none", "forwards", "backwards", "both")
    private val playStates = setOf("running", "paused")

    override fun expandSingle(tokens: List<String>): Map<String, String> {
        var name = "none"
        var duration = "0s"
        var timingFunction = "ease"
        var delay = "0s"
        var iterationCount = "1"
        var direction = "normal"
        var fillMode = "none"
        var playState = "running"
        var durationSet = false

        for (token in tokens) {
            when {
                // Timing function
                token.lowercase() in timingFunctions -> timingFunction = token
                token.startsWith("cubic-bezier(") -> timingFunction = token
                token.startsWith("steps(") -> timingFunction = token

                // Direction
                token.lowercase() in directions -> direction = token

                // Fill mode
                token.lowercase() in fillModes -> fillMode = token

                // Play state
                token.lowercase() in playStates -> playState = token

                // Time values
                token.matches(Regex("^[0-9.]+m?s$")) -> {
                    if (!durationSet) {
                        duration = token
                        durationSet = true
                    } else {
                        delay = token
                    }
                }

                // Iteration count
                token.matches(Regex("^[0-9.]+$")) || token.lowercase() == "infinite" -> {
                    iterationCount = token
                }

                // Animation name (anything else)
                else -> name = token
            }
        }

        return mapOf(
            "animation-name" to name,
            "animation-duration" to duration,
            "animation-timing-function" to timingFunction,
            "animation-delay" to delay,
            "animation-iteration-count" to iterationCount,
            "animation-direction" to direction,
            "animation-fill-mode" to fillMode,
            "animation-play-state" to playState
        )
    }

    override fun expandMultiple(items: List<String>): Map<String, String> {
        // Multiple animations - extract first token from each as animation name
        return mapOf(
            "animation-name" to items.map { WhitespaceTokenizer.tokenize(it)[0] }.joinToString(", "),
            "animation-duration" to "0s",
            "animation-timing-function" to "ease",
            "animation-delay" to "0s",
            "animation-iteration-count" to "1",
            "animation-direction" to "normal",
            "animation-fill-mode" to "none",
            "animation-play-state" to "running"
        )
    }
}
