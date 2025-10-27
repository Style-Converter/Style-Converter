package app.irmodels.properties.scrolling

import app.irmodels.IRProperty
import app.irmodels.IRLength
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `overflow-clip-margin` property.
 *
 * ## CSS Property
 * **Syntax**: `overflow-clip-margin: <length> | <visual-box>`
 *
 * ## Description
 * Determines how far outside its bounds an element with `overflow: clip` may paint
 * before being clipped. Only applies to elements with overflow: clip.
 *
 * ## Examples
 * ```kotlin
 * OverflowClipMarginProperty(value = OverflowClipMargin.Length(IRLength(10.0, LengthUnit.PX)))
 * OverflowClipMarginProperty(value = OverflowClipMargin.ContentBox)
 * OverflowClipMarginProperty(value = OverflowClipMargin.PaddingBox)
 * OverflowClipMarginProperty(value = OverflowClipMargin.BorderBox)
 * ```
 *
 * @property value The overflow clip margin value
 * @see [MDN overflow-clip-margin](https://developer.mozilla.org/en-US/docs/Web/CSS/overflow-clip-margin)
 */
@Serializable
data class OverflowClipMarginProperty(
    val value: OverflowClipMargin
) : IRProperty {
    override val propertyName = "overflow-clip-margin"
}

/**
 * Represents an overflow-clip-margin value.
 */
@Serializable
sealed interface OverflowClipMargin {
    @Serializable
    data class Length(val value: IRLength) : OverflowClipMargin

    @Serializable
    data object ContentBox : OverflowClipMargin

    @Serializable
    data object PaddingBox : OverflowClipMargin

    @Serializable
    data object BorderBox : OverflowClipMargin
}
