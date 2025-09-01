package app.parsing.css.styles

import app.BaseIR
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import app.parsing.css.styles.background.background
import app.parsing.css.styles.opacity.opacity
import app.parsing.css.styles.border.border
import app.parsing.css.styles.display.display
import app.parsing.css.styles.width.width
import app.parsing.css.styles.margin.marginTop
import app.parsing.css.styles.margin.marginRight
import app.parsing.css.styles.margin.marginBottom
import app.parsing.css.styles.margin.marginLeft
import app.parsing.css.styles.margin.marginShorthand
import app.parsing.css.styles.padding.paddingTop
import app.parsing.css.styles.padding.paddingRight
import app.parsing.css.styles.padding.paddingBottom
import app.parsing.css.styles.padding.paddingLeft
import app.parsing.css.styles.padding.paddingShorthand
import app.parsing.css.styles.marginblock.marginBlockStart
import app.parsing.css.styles.marginblock.marginBlockEnd
import app.parsing.css.styles.marginblock.marginBlockShorthand
import app.parsing.css.styles.margininline.marginInlineStart
import app.parsing.css.styles.margininline.marginInlineEnd
import app.parsing.css.styles.margininline.marginInlineShorthand
import app.parsing.css.styles.paddingblock.paddingBlockStart
import app.parsing.css.styles.paddingblock.paddingBlockEnd
import app.parsing.css.styles.paddingblock.paddingBlockShorthand
import app.parsing.css.styles.paddinginline.paddingInlineStart
import app.parsing.css.styles.paddinginline.paddingInlineEnd
import app.parsing.css.styles.paddinginline.paddingInlineShorthand
import app.parsing.css.styles.borderwidth.borderTopWidth
import app.parsing.css.styles.borderwidth.borderRightWidth
import app.parsing.css.styles.borderwidth.borderBottomWidth
import app.parsing.css.styles.borderwidth.borderLeftWidth
import app.parsing.css.styles.borderwidth.borderWidthShorthand
import app.parsing.css.styles.borderstyle.borderTopStyle
import app.parsing.css.styles.borderstyle.borderRightStyle
import app.parsing.css.styles.borderstyle.borderBottomStyle
import app.parsing.css.styles.borderstyle.borderLeftStyle
import app.parsing.css.styles.borderstyle.borderStyleShorthand
import app.parsing.css.styles.bordercolor.borderTopColor
import app.parsing.css.styles.bordercolor.borderRightColor
import app.parsing.css.styles.bordercolor.borderBottomColor
import app.parsing.css.styles.bordercolor.borderLeftColor
import app.parsing.css.styles.bordercolor.borderColorShorthand
import app.parsing.css.styles.borderradius.borderTopLeftRadius
import app.parsing.css.styles.borderradius.borderTopRightRadius
import app.parsing.css.styles.borderradius.borderBottomRightRadius
import app.parsing.css.styles.borderradius.borderBottomLeftRadius
import app.parsing.css.styles.borderradius.borderRadiusShorthand
import app.parsing.css.styles.background.backgroundPosition
import app.parsing.css.styles.background.backgroundRepeat
import app.parsing.css.styles.background.backgroundSize
import app.parsing.css.styles.font.fontSize
import app.parsing.css.styles.font.fontWeight
import app.parsing.css.styles.font.fontFamily
import app.parsing.css.styles.font.lineHeight
import app.parsing.css.styles.textalign.textAlign
import app.parsing.css.styles.letterspacing.letterSpacing

typealias CssHandler = (prop: String, value: JsonElement, acc: BaseIR) -> BaseIR

fun parseBaseStyles(styles: JsonObject): BaseIR {
	var acc = BaseIR()
	for ((key, value) in styles) {
		val handler = HANDLERS[key]
		if (handler != null) {
			acc = handler(key, value, acc)
		} else {
			// Fallback: store as string in BaseIR.other so nothing is lost
			val current = acc.other?.toMutableMap() ?: mutableMapOf()
			current[key] = value.toString()
			acc = acc.copy(other = current)
		}
	}
	return acc
}

private val HANDLERS: Map<String, CssHandler> = mapOf(
	"background-color" to { p, v, acc -> background.applyBackgroundColor(p, v, acc) },
	"opacity" to { p, v, acc -> opacity.applyOpacity(p, v, acc) },
	"border-top" to { _, v, acc -> border.applyBorderSide("top", v, acc) },
	"border-right" to { _, v, acc -> border.applyBorderSide("right", v, acc) },
	"border-bottom" to { _, v, acc -> border.applyBorderSide("bottom", v, acc) },
	"border-left" to { _, v, acc -> border.applyBorderSide("left", v, acc) },
	"border-top-left-radius" to { _, v, acc -> border.applyRadiusCorner("topLeft", v, acc) },
	"border-top-right-radius" to { _, v, acc -> border.applyRadiusCorner("topRight", v, acc) },
	"border-bottom-right-radius" to { _, v, acc -> border.applyRadiusCorner("bottomRight", v, acc) },
	"border-bottom-left-radius" to { _, v, acc -> border.applyRadiusCorner("bottomLeft", v, acc) },
	// additional handlers
	"display" to { p, v, acc -> display.applyDisplay(p, v, acc) },
	"width" to { p, v, acc -> width.applyWidth(p, v, acc) },
	"margin-top" to { p, v, acc -> marginTop.applyMarginTop(p, v, acc) }
    ,"margin-right" to { p, v, acc -> marginRight.applyMarginRight(p, v, acc) }
    ,"margin-bottom" to { p, v, acc -> marginBottom.applyMarginBottom(p, v, acc) }
    ,"margin-left" to { p, v, acc -> marginLeft.applyMarginLeft(p, v, acc) }
    ,"margin" to { p, v, acc -> marginShorthand.applyMargin(p, v, acc) }
    ,"padding-top" to { p, v, acc -> paddingTop.applyPaddingTop(p, v, acc) }
    ,"padding-right" to { p, v, acc -> paddingRight.applyPaddingRight(p, v, acc) }
    ,"padding-bottom" to { p, v, acc -> paddingBottom.applyPaddingBottom(p, v, acc) }
    ,"padding-left" to { p, v, acc -> paddingLeft.applyPaddingLeft(p, v, acc) }
    ,"padding" to { p, v, acc -> paddingShorthand.applyPadding(p, v, acc) }
    // Logical properties (mapped to physical for now)
    ,"margin-block-start" to { p, v, acc -> marginBlockStart.applyMarginBlockStart(p, v, acc) }
    ,"margin-block-end" to { p, v, acc -> marginBlockEnd.applyMarginBlockEnd(p, v, acc) }
    ,"margin-block" to { p, v, acc -> marginBlockShorthand.applyMarginBlock(p, v, acc) }
    ,"margin-inline-start" to { p, v, acc -> marginInlineStart.applyMarginInlineStart(p, v, acc) }
    ,"margin-inline-end" to { p, v, acc -> marginInlineEnd.applyMarginInlineEnd(p, v, acc) }
    ,"margin-inline" to { p, v, acc -> marginInlineShorthand.applyMarginInline(p, v, acc) }
    ,"padding-block-start" to { p, v, acc -> paddingBlockStart.applyPaddingBlockStart(p, v, acc) }
    ,"padding-block-end" to { p, v, acc -> paddingBlockEnd.applyPaddingBlockEnd(p, v, acc) }
    ,"padding-block" to { p, v, acc -> paddingBlockShorthand.applyPaddingBlock(p, v, acc) }
    ,"padding-inline-start" to { p, v, acc -> paddingInlineStart.applyPaddingInlineStart(p, v, acc) }
    ,"padding-inline-end" to { p, v, acc -> paddingInlineEnd.applyPaddingInlineEnd(p, v, acc) }
    ,"padding-inline" to { p, v, acc -> paddingInlineShorthand.applyPaddingInline(p, v, acc) }
    // Border width
    ,"border-top-width" to { p, v, acc -> borderTopWidth.applyBorderTopWidth(p, v, acc) }
    ,"border-right-width" to { p, v, acc -> borderRightWidth.applyBorderRightWidth(p, v, acc) }
    ,"border-bottom-width" to { p, v, acc -> borderBottomWidth.applyBorderBottomWidth(p, v, acc) }
    ,"border-left-width" to { p, v, acc -> borderLeftWidth.applyBorderLeftWidth(p, v, acc) }
    ,"border-width" to { p, v, acc -> borderWidthShorthand.applyBorderWidth(p, v, acc) }
    // Border style
    ,"border-top-style" to { p, v, acc -> borderTopStyle.applyBorderTopStyle(p, v, acc) }
    ,"border-right-style" to { p, v, acc -> borderRightStyle.applyBorderRightStyle(p, v, acc) }
    ,"border-bottom-style" to { p, v, acc -> borderBottomStyle.applyBorderBottomStyle(p, v, acc) }
    ,"border-left-style" to { p, v, acc -> borderLeftStyle.applyBorderLeftStyle(p, v, acc) }
    ,"border-style" to { p, v, acc -> borderStyleShorthand.applyBorderStyle(p, v, acc) }
    // Border color
    ,"border-top-color" to { p, v, acc -> borderTopColor.applyBorderTopColor(p, v, acc) }
    ,"border-right-color" to { p, v, acc -> borderRightColor.applyBorderRightColor(p, v, acc) }
    ,"border-bottom-color" to { p, v, acc -> borderBottomColor.applyBorderBottomColor(p, v, acc) }
    ,"border-left-color" to { p, v, acc -> borderLeftColor.applyBorderLeftColor(p, v, acc) }
    ,"border-color" to { p, v, acc -> borderColorShorthand.applyBorderColor(p, v, acc) }
    // Border radius
    ,"border-top-left-radius" to { p, v, acc -> borderTopLeftRadius.applyBorderTopLeftRadius(p, v, acc) }
    ,"border-top-right-radius" to { p, v, acc -> borderTopRightRadius.applyBorderTopRightRadius(p, v, acc) }
    ,"border-bottom-right-radius" to { p, v, acc -> borderBottomRightRadius.applyBorderBottomRightRadius(p, v, acc) }
    ,"border-bottom-left-radius" to { p, v, acc -> borderBottomLeftRadius.applyBorderBottomLeftRadius(p, v, acc) }
    ,"border-radius" to { p, v, acc -> borderRadiusShorthand.applyBorderRadius(p, v, acc) }
    // Background extras
    ,"background-position" to { p, v, acc -> backgroundPosition.applyBackgroundPosition(p, v, acc) }
    ,"background-repeat" to { p, v, acc -> backgroundRepeat.applyBackgroundRepeat(p, v, acc) }
    ,"background-size" to { p, v, acc -> backgroundSize.applyBackgroundSize(p, v, acc) }
    // Typography
    ,"font-size" to { p, v, acc -> fontSize.applyFontSize(p, v, acc) }
    ,"font-weight" to { p, v, acc -> fontWeight.applyFontWeight(p, v, acc) }
    ,"font-family" to { p, v, acc -> fontFamily.applyFontFamily(p, v, acc) }
    ,"line-height" to { p, v, acc -> lineHeight.applyLineHeight(p, v, acc) }
    ,"text-align" to { p, v, acc -> textAlign.applyTextAlign(p, v, acc) }
    ,"letter-spacing" to { p, v, acc -> letterSpacing.applyLetterSpacing(p, v, acc) }
)


