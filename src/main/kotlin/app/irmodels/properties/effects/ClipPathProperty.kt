package app.irmodels.properties.effects

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class ClipPathProperty(
    val value: ClipPath
) : IRProperty {
    override val propertyName = "clip-path"

    @Serializable(with = ClipPathSerializer::class)
    sealed interface ClipPath {
        @Serializable data class None(val unit: Unit = Unit) : ClipPath
        @Serializable data class Url(val url: IRUrl) : ClipPath
        @Serializable data class BasicShape(val shape: Shape) : ClipPath
        @Serializable data class GeometryBox(val box: String) : ClipPath // margin-box, border-box, padding-box, content-box, fill-box, stroke-box, view-box
        @Serializable data class GeometryBoxShape(val box: String, val shape: Shape) : ClipPath
    }

    @Serializable(with = ClipPathShapeSerializer::class)
    sealed interface Shape {
        @Serializable data class Inset(val top: IRLength, val right: IRLength, val bottom: IRLength, val left: IRLength, val round: IRLength?) : Shape
        @Serializable data class Circle(val radius: IRLength?, val position: Position?) : Shape
        @Serializable data class Ellipse(val radiusX: IRLength?, val radiusY: IRLength?, val position: Position?) : Shape
        @Serializable data class Polygon(val points: List<Point>) : Shape
        @Serializable data class Path(val d: String) : Shape
        @Serializable data class Rect(val top: IRLength?, val right: IRLength?, val bottom: IRLength?, val left: IRLength?, val round: IRLength?) : Shape // null means 'auto'
        @Serializable data class Xywh(val x: IRLength, val y: IRLength, val width: IRLength, val height: IRLength, val round: IRLength?) : Shape
    }

    @Serializable data class Position(val x: IRLength, val y: IRLength)
    @Serializable data class Point(val x: IRPercentage, val y: IRPercentage)
}
