package app.irmodels.properties.effects

import app.irmodels.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*

object ClipPathSerializer : KSerializer<ClipPathProperty.ClipPath> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ClipPath")

    override fun serialize(encoder: Encoder, value: ClipPathProperty.ClipPath) {
        require(encoder is JsonEncoder)
        encoder.encodeJsonElement(when (value) {
            is ClipPathProperty.ClipPath.None -> JsonPrimitive("none")
            is ClipPathProperty.ClipPath.Url -> encoder.json.encodeToJsonElement(IRUrl.serializer(), value.url)
            is ClipPathProperty.ClipPath.BasicShape -> encoder.json.encodeToJsonElement(ClipPathShapeSerializer, value.shape)
            is ClipPathProperty.ClipPath.GeometryBox -> buildJsonObject { put("geometry-box", value.box) }
            is ClipPathProperty.ClipPath.GeometryBoxShape -> buildJsonObject {
                put("geometry-box", value.box)
                put("shape", encoder.json.encodeToJsonElement(ClipPathShapeSerializer, value.shape))
            }
        })
    }

    override fun deserialize(decoder: Decoder): ClipPathProperty.ClipPath {
        require(decoder is JsonDecoder)
        val element = decoder.decodeJsonElement()
        return when {
            element is JsonPrimitive && element.content == "none" -> ClipPathProperty.ClipPath.None()
            element is JsonObject && element.containsKey("url") ->
                ClipPathProperty.ClipPath.Url(decoder.json.decodeFromJsonElement(IRUrl.serializer(), element))
            element is JsonObject && element.containsKey("geometry-box") && element.containsKey("shape") ->
                ClipPathProperty.ClipPath.GeometryBoxShape(
                    element["geometry-box"]!!.jsonPrimitive.content,
                    decoder.json.decodeFromJsonElement(ClipPathShapeSerializer, element["shape"]!!)
                )
            element is JsonObject && element.containsKey("geometry-box") ->
                ClipPathProperty.ClipPath.GeometryBox(element["geometry-box"]!!.jsonPrimitive.content)
            element is JsonObject ->
                ClipPathProperty.ClipPath.BasicShape(decoder.json.decodeFromJsonElement(ClipPathShapeSerializer, element))
            else -> ClipPathProperty.ClipPath.None()
        }
    }
}

object ClipPathShapeSerializer : KSerializer<ClipPathProperty.Shape> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ClipPathShape")

    override fun serialize(encoder: Encoder, value: ClipPathProperty.Shape) {
        require(encoder is JsonEncoder)
        val json = encoder.json
        encoder.encodeJsonElement(when (value) {
            is ClipPathProperty.Shape.Inset -> buildJsonObject {
                put("type", "inset")
                put("t", json.encodeToJsonElement(IRLength.serializer(), value.top))
                put("r", json.encodeToJsonElement(IRLength.serializer(), value.right))
                put("b", json.encodeToJsonElement(IRLength.serializer(), value.bottom))
                put("l", json.encodeToJsonElement(IRLength.serializer(), value.left))
                value.round?.let { put("round", json.encodeToJsonElement(IRLength.serializer(), it)) }
            }
            is ClipPathProperty.Shape.Circle -> buildJsonObject {
                put("type", "circle")
                value.radius?.let { put("r", json.encodeToJsonElement(IRLength.serializer(), it)) }
                value.position?.let { put("pos", json.encodeToJsonElement(ClipPathProperty.Position.serializer(), it)) }
            }
            is ClipPathProperty.Shape.Ellipse -> buildJsonObject {
                put("type", "ellipse")
                value.radiusX?.let { put("rx", json.encodeToJsonElement(IRLength.serializer(), it)) }
                value.radiusY?.let { put("ry", json.encodeToJsonElement(IRLength.serializer(), it)) }
                value.position?.let { put("pos", json.encodeToJsonElement(ClipPathProperty.Position.serializer(), it)) }
            }
            is ClipPathProperty.Shape.Polygon -> buildJsonObject {
                put("type", "polygon")
                put("points", json.encodeToJsonElement(ListSerializer(ClipPathProperty.Point.serializer()), value.points))
            }
            is ClipPathProperty.Shape.Path -> buildJsonObject {
                put("type", "path")
                put("d", value.d)
            }
            is ClipPathProperty.Shape.Rect -> buildJsonObject {
                put("type", "rect")
                value.top?.let { put("t", json.encodeToJsonElement(IRLength.serializer(), it)) } ?: put("t", "auto")
                value.right?.let { put("r", json.encodeToJsonElement(IRLength.serializer(), it)) } ?: put("r", "auto")
                value.bottom?.let { put("b", json.encodeToJsonElement(IRLength.serializer(), it)) } ?: put("b", "auto")
                value.left?.let { put("l", json.encodeToJsonElement(IRLength.serializer(), it)) } ?: put("l", "auto")
                value.round?.let { put("round", json.encodeToJsonElement(IRLength.serializer(), it)) }
            }
            is ClipPathProperty.Shape.Xywh -> buildJsonObject {
                put("type", "xywh")
                put("x", json.encodeToJsonElement(IRLength.serializer(), value.x))
                put("y", json.encodeToJsonElement(IRLength.serializer(), value.y))
                put("w", json.encodeToJsonElement(IRLength.serializer(), value.width))
                put("h", json.encodeToJsonElement(IRLength.serializer(), value.height))
                value.round?.let { put("round", json.encodeToJsonElement(IRLength.serializer(), it)) }
            }
        })
    }

    override fun deserialize(decoder: Decoder): ClipPathProperty.Shape {
        require(decoder is JsonDecoder)
        val obj = decoder.decodeJsonElement().jsonObject
        val json = decoder.json
        return when (obj["type"]?.jsonPrimitive?.content) {
            "inset" -> ClipPathProperty.Shape.Inset(
                json.decodeFromJsonElement(IRLength.serializer(), obj["t"]!!),
                json.decodeFromJsonElement(IRLength.serializer(), obj["r"]!!),
                json.decodeFromJsonElement(IRLength.serializer(), obj["b"]!!),
                json.decodeFromJsonElement(IRLength.serializer(), obj["l"]!!),
                obj["round"]?.let { json.decodeFromJsonElement(IRLength.serializer(), it) }
            )
            "circle" -> ClipPathProperty.Shape.Circle(
                obj["r"]?.let { json.decodeFromJsonElement(IRLength.serializer(), it) },
                obj["pos"]?.let { json.decodeFromJsonElement(ClipPathProperty.Position.serializer(), it) }
            )
            "ellipse" -> ClipPathProperty.Shape.Ellipse(
                obj["rx"]?.let { json.decodeFromJsonElement(IRLength.serializer(), it) },
                obj["ry"]?.let { json.decodeFromJsonElement(IRLength.serializer(), it) },
                obj["pos"]?.let { json.decodeFromJsonElement(ClipPathProperty.Position.serializer(), it) }
            )
            "polygon" -> ClipPathProperty.Shape.Polygon(
                json.decodeFromJsonElement(ListSerializer(ClipPathProperty.Point.serializer()), obj["points"]!!)
            )
            "path" -> ClipPathProperty.Shape.Path(obj["d"]?.jsonPrimitive?.content ?: "")
            "rect" -> ClipPathProperty.Shape.Rect(
                obj["t"]?.let { if (it is JsonPrimitive && it.content == "auto") null else json.decodeFromJsonElement(IRLength.serializer(), it) },
                obj["r"]?.let { if (it is JsonPrimitive && it.content == "auto") null else json.decodeFromJsonElement(IRLength.serializer(), it) },
                obj["b"]?.let { if (it is JsonPrimitive && it.content == "auto") null else json.decodeFromJsonElement(IRLength.serializer(), it) },
                obj["l"]?.let { if (it is JsonPrimitive && it.content == "auto") null else json.decodeFromJsonElement(IRLength.serializer(), it) },
                obj["round"]?.let { json.decodeFromJsonElement(IRLength.serializer(), it) }
            )
            "xywh" -> ClipPathProperty.Shape.Xywh(
                json.decodeFromJsonElement(IRLength.serializer(), obj["x"]!!),
                json.decodeFromJsonElement(IRLength.serializer(), obj["y"]!!),
                json.decodeFromJsonElement(IRLength.serializer(), obj["w"]!!),
                json.decodeFromJsonElement(IRLength.serializer(), obj["h"]!!),
                obj["round"]?.let { json.decodeFromJsonElement(IRLength.serializer(), it) }
            )
            else -> ClipPathProperty.Shape.Circle(null, null)
        }
    }
}
