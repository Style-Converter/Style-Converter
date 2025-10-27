package app.irmodels.properties.background

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

@Serializable
data class BackgroundRepeatProperty(
    val repeats: List<BackgroundRepeat>
) : IRProperty {
    override val propertyName = "background-repeat"

    @Serializable
    sealed interface BackgroundRepeat {
        @Serializable
        data class TwoValue(val x: RepeatValue, val y: RepeatValue) : BackgroundRepeat

        @Serializable
        data class OneValue(val value: RepeatKeyword) : BackgroundRepeat

        enum class RepeatKeyword {
            REPEAT, SPACE, ROUND, NO_REPEAT
        }

        enum class RepeatValue {
            REPEAT, NO_REPEAT
        }
    }
}
