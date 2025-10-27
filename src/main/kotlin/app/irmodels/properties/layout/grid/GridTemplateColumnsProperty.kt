package app.irmodels.properties.layout.grid

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class GridTemplateColumnsProperty(
    val value: GridTemplate
) : IRProperty {
    override val propertyName = "grid-template-columns"
}

@Serializable
sealed interface GridTemplate {
    @Serializable
    data class None(val unit: Unit = Unit) : GridTemplate

    @Serializable
    data class TrackList(val tracks: List<TrackSize>) : GridTemplate

    @Serializable
    data class Auto(val unit: Unit = Unit) : GridTemplate
}

@Serializable
sealed interface TrackSize {
    @Serializable
    data class LengthValue(val length: IRLength) : TrackSize

    @Serializable
    data class PercentageValue(val percentage: IRPercentage) : TrackSize

    @Serializable
    data class Flex(val value: IRNumber) : TrackSize

    @Serializable
    data class MinContent(val unit: Unit = Unit) : TrackSize

    @Serializable
    data class MaxContent(val unit: Unit = Unit) : TrackSize

    @Serializable
    data class Auto(val unit: Unit = Unit) : TrackSize

    @Serializable
    data class FitContent(val size: IRLength) : TrackSize

    @Serializable
    data class MinMax(val min: TrackSize, val max: TrackSize) : TrackSize

    @Serializable
    data class Repeat(val count: RepeatCount, val tracks: List<TrackSize>) : TrackSize
}

@Serializable
sealed interface RepeatCount {
    @Serializable
    data class Number(val value: Int) : RepeatCount

    @Serializable
    data class AutoFill(val unit: Unit = Unit) : RepeatCount

    @Serializable
    data class AutoFit(val unit: Unit = Unit) : RepeatCount
}
