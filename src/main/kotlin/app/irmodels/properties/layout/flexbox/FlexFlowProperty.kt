package app.irmodels.properties.layout.flexbox

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `flex-flow` property.
 *
 * ## CSS Property
 * **Syntax**: `flex-flow: <flex-direction> || <flex-wrap>`
 *
 * ## Description
 * Shorthand property for setting flex-direction and flex-wrap properties.
 * Defines how flex items are placed in the flex container and whether they wrap.
 *
 * ## Examples
 * ```kotlin
 * // Row direction with wrapping
 * FlexFlowProperty(
 *     direction = FlexDirection.ROW,
 *     wrap = FlexWrap.WRAP
 * )
 *
 * // Column direction without wrapping
 * FlexFlowProperty(
 *     direction = FlexDirection.COLUMN,
 *     wrap = FlexWrap.NOWRAP
 * )
 * ```
 *
 * ## Platform Support
 * - **CSS**: Full support
 * - **Compose**: Use FlowRow/FlowColumn for wrapping
 * - **SwiftUI**: Use HStack/VStack, custom layout for wrapping
 *
 * @property direction The flex direction value
 * @property wrap The flex wrap value (defaults to NOWRAP if not specified)
 * @see [MDN flex-flow](https://developer.mozilla.org/en-US/docs/Web/CSS/flex-flow)
 */
@Serializable
data class FlexFlowProperty(
    val direction: FlexDirection,
    val wrap: FlexWrap = FlexWrap.NOWRAP
) : IRProperty {
    override val propertyName = "flex-flow"
}

/**
 * Represents flex-direction values.
 */
@Serializable
enum class FlexDirection {
    /**
     * Flex items are laid out in a row (left to right in LTR).
     */
    ROW,

    /**
     * Flex items are laid out in a row (right to left in LTR).
     */
    ROW_REVERSE,

    /**
     * Flex items are laid out in a column (top to bottom).
     */
    COLUMN,

    /**
     * Flex items are laid out in a column (bottom to top).
     */
    COLUMN_REVERSE
}

/**
 * Represents flex-wrap values.
 */
@Serializable
enum class FlexWrap {
    /**
     * Flex items are laid out in a single line (default).
     * May cause overflow.
     */
    NOWRAP,

    /**
     * Flex items wrap onto multiple lines from top to bottom.
     */
    WRAP,

    /**
     * Flex items wrap onto multiple lines from bottom to top.
     */
    WRAP_REVERSE
}
