package app.irmodels.properties.background

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class BackgroundImageProperty(
    val images: List<BackgroundImage>
) : IRProperty {
    override val propertyName = "background-image"

    @Serializable
    sealed interface BackgroundImage {
        @Serializable
        data class None(val unit: Unit = Unit) : BackgroundImage

        @Serializable
        data class Url(val url: IRUrl) : BackgroundImage

        @Serializable
        data class LinearGradient(
            val angle: IRAngle?,
            val colorStops: List<ColorStop>
        ) : BackgroundImage

        @Serializable
        data class RadialGradient(
            val shape: GradientShape?,
            val size: GradientSize?,
            val position: Position?,
            val colorStops: List<ColorStop>
        ) : BackgroundImage

        @Serializable
        data class ConicGradient(
            val angle: IRAngle?,
            val position: Position?,
            val colorStops: List<ColorStop>
        ) : BackgroundImage
    }

    @Serializable
    data class ColorStop(
        val color: IRColor,
        val position: IRPercentage?
    )

    @Serializable
    data class Position(val x: IRPercentage, val y: IRPercentage)

    enum class GradientShape { CIRCLE, ELLIPSE }
    enum class GradientSize {
        CLOSEST_SIDE, CLOSEST_CORNER,
        FARTHEST_SIDE, FARTHEST_CORNER
    }
}
