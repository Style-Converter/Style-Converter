package app.irmodels.properties.transforms

import app.irmodels.*
import kotlinx.serialization.Serializable

@Serializable
data class TransformProperty(
    val functions: List<TransformFunction>
) : IRProperty {
    override val propertyName = "transform"
}

@Serializable
sealed interface TransformFunction {
    @Serializable
    data class Translate(val x: IRLength, val y: IRLength) : TransformFunction

    @Serializable
    data class TranslateX(val x: IRLength) : TransformFunction

    @Serializable
    data class TranslateY(val y: IRLength) : TransformFunction

    @Serializable
    data class TranslateZ(val z: IRLength) : TransformFunction

    @Serializable
    data class Translate3d(val x: IRLength, val y: IRLength, val z: IRLength) : TransformFunction

    @Serializable
    data class Scale(val x: IRNumber, val y: IRNumber) : TransformFunction

    @Serializable
    data class ScaleX(val x: IRNumber) : TransformFunction

    @Serializable
    data class ScaleY(val y: IRNumber) : TransformFunction

    @Serializable
    data class ScaleZ(val z: IRNumber) : TransformFunction

    @Serializable
    data class Scale3d(val x: IRNumber, val y: IRNumber, val z: IRNumber) : TransformFunction

    @Serializable
    data class Rotate(val angle: IRAngle) : TransformFunction

    @Serializable
    data class RotateX(val angle: IRAngle) : TransformFunction

    @Serializable
    data class RotateY(val angle: IRAngle) : TransformFunction

    @Serializable
    data class RotateZ(val angle: IRAngle) : TransformFunction

    @Serializable
    data class Rotate3d(val x: IRNumber, val y: IRNumber, val z: IRNumber, val angle: IRAngle) : TransformFunction

    @Serializable
    data class Skew(val x: IRAngle, val y: IRAngle) : TransformFunction

    @Serializable
    data class SkewX(val angle: IRAngle) : TransformFunction

    @Serializable
    data class SkewY(val angle: IRAngle) : TransformFunction

    @Serializable
    data class Perspective(val length: IRLength) : TransformFunction

    @Serializable
    data class Matrix(
        val a: IRNumber, val b: IRNumber, val c: IRNumber,
        val d: IRNumber, val e: IRNumber, val f: IRNumber
    ) : TransformFunction

    @Serializable
    data class Matrix3d(
        val a1: IRNumber, val b1: IRNumber, val c1: IRNumber, val d1: IRNumber,
        val a2: IRNumber, val b2: IRNumber, val c2: IRNumber, val d2: IRNumber,
        val a3: IRNumber, val b3: IRNumber, val c3: IRNumber, val d3: IRNumber,
        val a4: IRNumber, val b4: IRNumber, val c4: IRNumber, val d4: IRNumber
    ) : TransformFunction
}
