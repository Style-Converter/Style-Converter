package app.irmodels.properties.typography

import app.irmodels.IRProperty
import kotlinx.serialization.Serializable

/**
 * Represents the CSS `text-justify` property.
 *
 * ## CSS Property
 * **Syntax**: `text-justify: auto | inter-character | inter-word | none`
 *
 * ## Description
 * Controls the justification method used when `text-align: justify` is applied.
 * Specifies how justified text should be aligned and spaced.
 *
 * ## Examples
 * ```kotlin
 * TextJustifyProperty(justify = TextJustify.Auto)
 * TextJustifyProperty(justify = TextJustify.InterWord)
 * TextJustifyProperty(justify = TextJustify.InterCharacter)
 * ```
 *
 * ## Platform Support
 * - **CSS**: Full support
 * - **Compose**: No control over justification method
 * - **SwiftUI**: No control over justification method
 *
 * @property justify The text justification method
 * @see [MDN text-justify](https://developer.mozilla.org/en-US/docs/Web/CSS/text-justify)
 */
@Serializable
data class TextJustifyProperty(
    val justify: TextJustify
) : IRProperty {
    override val propertyName = "text-justify"
}

/**
 * Represents text-justify values.
 */
@Serializable
enum class TextJustify {
    /**
     * Browser chooses the best justification method based on language and content.
     */
    AUTO,

    /**
     * Justification is achieved by increasing spacing between characters.
     * Good for languages like Thai or Japanese.
     */
    INTER_CHARACTER,

    /**
     * Justification is achieved by increasing spacing between words.
     * Good for languages that separate words using spaces, like English.
     */
    INTER_WORD,

    /**
     * Justification is disabled.
     */
    NONE
}
