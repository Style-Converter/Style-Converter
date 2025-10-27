# Cross-Platform Style Properties Reference - Part 2

## Borders

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|border|`border: 1px solid red`|`Modifier.border(1.dp, Color.Red)`|`.border(Color.red, width: 1)`|✅ Direct equivalent in all|
|border-width|`border-width: 2px`|`Modifier.border(2.dp, Color.Red)`|`.border(Color.red, width: 2)`|✅ Direct equivalent in all|
|border-top-width|`border-top-width: 2px`|❌ No native support|❌ No native support|🔧 Need custom drawing or overlay|
|border-right-width|`border-right-width: 2px`|❌ No native support|❌ No native support|🔧 Need custom drawing or overlay|
|border-bottom-width|`border-bottom-width: 2px`|❌ No native support|❌ No native support|🔧 Need custom drawing or overlay|
|border-left-width|`border-left-width: 2px`|❌ No native support|❌ No native support|🔧 Need custom drawing or overlay|
|border-color|`border-color: red`|`Modifier.border(1.dp, Color.Red)`|`.border(Color.red, width: 1)`|✅ Direct equivalent in all|
|border-top-color|`border-top-color: red`|❌ No native support|❌ No native support|🔧 Use Box with custom drawBehind/Canvas or overlay|
|border-right-color|`border-right-color: blue`|❌ No native support|❌ No native support|🔧 Use Box with custom drawBehind/Canvas or overlay|
|border-bottom-color|`border-bottom-color: green`|❌ No native support|❌ No native support|🔧 Use Box with custom drawBehind/Canvas or overlay|
|border-left-color|`border-left-color: yellow`|❌ No native support|❌ No native support|🔧 Use Box with custom drawBehind/Canvas or overlay|
|border-style|`border-style: dashed`|`Modifier.border(1.dp, Color.Red, shape = DashedShape)`|`.overlay(RoundedRectangle().strokeBorder(style: StrokeStyle(dash: [5])))`|🔧 Requires custom shape (Compose) or stroke style (SwiftUI)|
|border-top-style|`border-top-style: dashed`|❌ No native support|❌ No native support|🔧 Custom drawing with dash pattern|
|border-right-style|`border-right-style: dotted`|❌ No native support|❌ No native support|🔧 Custom drawing with dash pattern|
|border-bottom-style|`border-bottom-style: solid`|❌ No native support|❌ No native support|🔧 Custom drawing|
|border-left-style|`border-left-style: double`|❌ No native support|❌ No native support|🔧 Custom drawing|
|border-top|`border-top: 1px solid red`|❌ No native support|❌ No native support|🔧 Custom drawing combining width, style, color|
|border-right|`border-right: 1px solid blue`|❌ No native support|❌ No native support|🔧 Custom drawing combining width, style, color|
|border-bottom|`border-bottom: 2px dashed green`|❌ No native support|❌ No native support|🔧 Custom drawing combining width, style, color|
|border-left|`border-left: 1px solid yellow`|❌ No native support|❌ No native support|🔧 Custom drawing combining width, style, color|
|border-radius|`border-radius: 8px`|`Modifier.clip(RoundedCornerShape(8.dp))` or `border` with shape|`.cornerRadius(8)` or `.clipShape(RoundedRectangle(cornerRadius: 8))`|✅ Equivalent through clipping/shape systems|
|border-top-left-radius|`border-top-left-radius: 8px`|`Modifier.clip(RoundedCornerShape(topStart = 8.dp))`|`.clipShape(RoundedRectangle(cornerRadius: 8))`|✅ Individual corner control|
|border-top-right-radius|`border-top-right-radius: 8px`|`Modifier.clip(RoundedCornerShape(topEnd = 8.dp))`|`.clipShape(RoundedRectangle(cornerRadius: 8))`|✅ Individual corner control|
|border-bottom-left-radius|`border-bottom-left-radius: 8px`|`Modifier.clip(RoundedCornerShape(bottomStart = 8.dp))`|`.clipShape(RoundedRectangle(cornerRadius: 8))`|✅ Individual corner control|
|border-bottom-right-radius|`border-bottom-right-radius: 8px`|`Modifier.clip(RoundedCornerShape(bottomEnd = 8.dp))`|`.clipShape(RoundedRectangle(cornerRadius: 8))`|✅ Individual corner control|
|border-start-start-radius|`border-start-start-radius: 8px`|`Modifier.clip(RoundedCornerShape(topStart = 8.dp))`|`.clipShape(RoundedRectangle(cornerRadius: 8))`|✅ Logical corner (top-left in LTR, top-right in RTL)|
|border-start-end-radius|`border-start-end-radius: 8px`|`Modifier.clip(RoundedCornerShape(topEnd = 8.dp))`|`.clipShape(RoundedRectangle(cornerRadius: 8))`|✅ Logical corner (top-right in LTR, top-left in RTL)|
|border-end-start-radius|`border-end-start-radius: 8px`|`Modifier.clip(RoundedCornerShape(bottomStart = 8.dp))`|`.clipShape(RoundedRectangle(cornerRadius: 8))`|✅ Logical corner (bottom-left in LTR, bottom-right in RTL)|
|border-end-end-radius|`border-end-end-radius: 8px`|`Modifier.clip(RoundedCornerShape(bottomEnd = 8.dp))`|`.clipShape(RoundedRectangle(cornerRadius: 8))`|✅ Logical corner (bottom-right in LTR, bottom-left in RTL)|
|border-image|`border-image: url('border.png') 30 round`|🔧 Custom drawBehind with image slicing|🔧 Custom Canvas with image drawing|🔧 No native support|
|border-image-source|`border-image-source: url('border.png')`|🔧 Part of custom border-image implementation|🔧 Part of custom implementation|🔧 No native support|
|border-image-slice|`border-image-slice: 30`|🔧 Part of custom border-image implementation|🔧 Part of custom implementation|🔧 No native support|
|border-image-width|`border-image-width: 10px`|🔧 Part of custom border-image implementation|🔧 Part of custom implementation|🔧 No native support|
|border-image-outset|`border-image-outset: 5px`|🔧 Part of custom border-image implementation|🔧 Part of custom implementation|🔧 No native support|
|border-image-repeat|`border-image-repeat: round`|🔧 Part of custom implementation|🔧 Part of custom implementation|🔧 No native support|
|border-block|`border-block: 1px solid red`|🔧 Combine top and bottom borders|🔧 Combine top and bottom borders|🔧 Logical shorthand for block borders|
|border-block-width|`border-block-width: 2px`|🔧 Custom drawing for top/bottom|🔧 Custom drawing for top/bottom|🔧 Logical width for block borders|
|border-block-style|`border-block-style: dashed`|🔧 Custom drawing for top/bottom|🔧 Custom drawing for top/bottom|🔧 Logical style for block borders|
|border-block-color|`border-block-color: red`|🔧 Custom drawing for top/bottom|🔧 Custom drawing for top/bottom|🔧 Logical color for block borders|
|border-block-start|`border-block-start: 1px solid red`|❌ No native support|❌ No native support|🔧 Logical block-start border (top in LTR)|
|border-block-start-width|`border-block-start-width: 2px`|❌ No native support|❌ No native support|🔧 Logical block-start width|
|border-block-start-style|`border-block-start-style: dashed`|❌ No native support|❌ No native support|🔧 Logical block-start style|
|border-block-start-color|`border-block-start-color: red`|❌ No native support|❌ No native support|🔧 Logical block-start color|
|border-block-end|`border-block-end: 1px solid blue`|❌ No native support|❌ No native support|🔧 Logical block-end border (bottom in LTR)|
|border-block-end-width|`border-block-end-width: 2px`|❌ No native support|❌ No native support|🔧 Logical block-end width|
|border-block-end-style|`border-block-end-style: solid`|❌ No native support|❌ No native support|🔧 Logical block-end style|
|border-block-end-color|`border-block-end-color: blue`|❌ No native support|❌ No native support|🔧 Logical block-end color|
|border-inline|`border-inline: 1px solid green`|🔧 Combine start and end borders|🔧 Combine start and end borders|🔧 Logical shorthand for inline borders|
|border-inline-width|`border-inline-width: 2px`|🔧 Custom drawing for start/end|🔧 Custom drawing for start/end|🔧 Logical width for inline borders|
|border-inline-style|`border-inline-style: dashed`|🔧 Custom drawing for start/end|🔧 Custom drawing for start/end|🔧 Logical style for inline borders|
|border-inline-color|`border-inline-color: green`|🔧 Custom drawing for start/end|🔧 Custom drawing for start/end|🔧 Logical color for inline borders|
|border-inline-start|`border-inline-start: 1px solid green`|❌ No native support|❌ No native support|🔧 Logical inline-start border (left in LTR, right in RTL)|
|border-inline-start-width|`border-inline-start-width: 2px`|❌ No native support|❌ No native support|🔧 Logical inline-start width|
|border-inline-start-style|`border-inline-start-style: dashed`|❌ No native support|❌ No native support|🔧 Logical inline-start style|
|border-inline-start-color|`border-inline-start-color: green`|❌ No native support|❌ No native support|🔧 Logical inline-start color|
|border-inline-end|`border-inline-end: 1px solid yellow`|❌ No native support|❌ No native support|🔧 Logical inline-end border (right in LTR, left in RTL)|
|border-inline-end-width|`border-inline-end-width: 2px`|❌ No native support|❌ No native support|🔧 Logical inline-end width|
|border-inline-end-style|`border-inline-end-style: solid`|❌ No native support|❌ No native support|🔧 Logical inline-end style|
|border-inline-end-color|`border-inline-end-color: yellow`|❌ No native support|❌ No native support|🔧 Logical inline-end color|
|border-collapse|`border-collapse: collapse`|❌ N/A - No table layout|❌ N/A - No table layout|❌ CSS table-specific property|
|border-spacing|`border-spacing: 5px`|⚠️ Use gap in LazyVerticalGrid/LazyHorizontalGrid|⚠️ Use spacing in LazyVGrid/LazyHGrid|⚠️ CSS table-specific, use grid spacing instead|
|border-boundary|`border-boundary: element`|❌ No equivalent|❌ No equivalent|❌ Experimental CSS property|
|box-decoration-break|`box-decoration-break: clone`|❌ No equivalent|❌ No equivalent|❌ CSS-specific for inline element rendering|
|outline|`outline: 2px solid blue`|❌ No direct equivalent|❌ No direct equivalent|🔧 Use overlay with offset or custom drawing|
|outline-width|`outline-width: 2px`|❌ No direct equivalent|❌ No direct equivalent|🔧 Use overlay with offset|
|outline-color|`outline-color: blue`|❌ No direct equivalent|❌ No direct equivalent|🔧 Use overlay with offset|
|outline-style|`outline-style: dashed`|🔧 Use custom shape with dash pattern|🔧 Use StrokeStyle(dash:) in overlay|🔧 Similar to border-style workarounds|
|outline-offset|`outline-offset: 5px`|🔧 Use multiple border() modifiers with padding|🔧 Use .overlay() with offset rectangles|🔧 Requires custom implementation|

## Shadows & Effects

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|box-shadow|`box-shadow: 2px 4px 8px rgba(0,0,0,0.3)`|`Modifier.shadow(elevation = 4.dp, shape = shape, clip = false)`|`.shadow(color: .black.opacity(0.3), radius: 4, x: 2, y: 4)`|⚠️ Compose uses elevation (Material Design), SwiftUI closer to CSS|
|box-shadow (multiple)|`box-shadow: 0 2px 4px red, 0 4px 8px blue`|❌ No native support for multiple shadows|🔧 Stack multiple views with different shadows|🔧 Limited support - need workarounds|
|text-shadow|`text-shadow: 2px 2px 4px black`|`Modifier.shadow()` on Text or TextStyle with shadow|`.shadow(color: .black, radius: 2, x: 2, y: 2)` on Text|✅ All support text shadows|
|opacity|`opacity: 0.5`|`Modifier.alpha(0.5f)`|`.opacity(0.5)`|✅ Direct equivalent in all|
|filter: blur|`filter: blur(5px)`|`Modifier.blur(5.dp)`|`.blur(radius: 5)`|✅ Direct equivalent in all|
|filter: brightness|`filter: brightness(1.2)`|🔧 Use ColorFilter.colorMatrix()|`.brightness(0.2)` (additive, not multiplicative)|🔧 SwiftUI has direct modifier with different semantics|
|filter: contrast|`filter: contrast(1.5)`|🔧 Use ColorFilter.colorMatrix()|`.contrast(0.5)` (additive, not multiplicative)|🔧 SwiftUI has direct modifier with different semantics|
|filter: saturate|`filter: saturate(200%)`|`ColorFilter.colorMatrix()` with saturation|`.saturation(2.0)`|✅ SwiftUI has direct modifier|
|filter: grayscale|`filter: grayscale(100%)`|`ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })`|`.grayscale(1.0)` or `.colorMultiply(.gray)`|✅ SwiftUI simpler, Compose more flexible|
|filter: sepia|`filter: sepia(100%)`|🔧 Custom ColorMatrix|🔧 Custom ColorMatrix or `.colorMultiply()`|🔧 Requires manual color matrix|
|filter: hue-rotate|`filter: hue-rotate(90deg)`|`ColorFilter.colorMatrix()` with hue rotation|`.hueRotation(.degrees(90))`|✅ SwiftUI has direct modifier|
|filter: invert|`filter: invert(100%)`|`ColorFilter.colorMatrix()` with inversion|🔧 Custom color inversion|🔧 Requires custom implementation|
|filter: drop-shadow|`filter: drop-shadow(2px 4px 8px black)`|`Modifier.shadow()` or `graphicsLayer { shadowElevation }`|`.shadow(color:radius:x:y:)`|✅ Similar to box-shadow|
|backdrop-filter|`backdrop-filter: blur(10px)`|❌ No native support|🔧 Use `.background(.ultraThinMaterial)` for blur effect|⚠️ Limited support - SwiftUI has Material blur backgrounds|
|backdrop-filter: blur|`backdrop-filter: blur(10px)`|❌ No native support|`.background(.ultraThinMaterial)` or `.thinMaterial`|⚠️ SwiftUI has Material blur backgrounds|
|backdrop-filter: brightness|`backdrop-filter: brightness(1.5)`|❌ No native support|❌ No native support (Material only)|❌ Very limited support|
|clip-path|`clip-path: circle(50%)`|`Modifier.clip(CircleShape)`|`.clipShape(Circle())`|✅ Through shape clipping systems|
|clip-path: polygon|`clip-path: polygon(0 0, 100% 0, 50% 100%)`|`Modifier.clip(GenericShape { path... })`|`.clipShape(Path { path in ... })`|✅ Custom path clipping|
|clip-path: ellipse|`clip-path: ellipse(50% 50%)`|`Modifier.clip(CircleShape)` or custom|`.clipShape(Ellipse())`|✅ Ellipse clipping|
|clip-path: inset|`clip-path: inset(10px)`|`Modifier.padding(10.dp).clip(RectangleShape)`|`.clipShape(Rectangle()).padding(10)`|✅ Inset clipping|
|mask|`mask: url(#mask)`|🔧 Use `Modifier.drawWithContent` with custom masking|🔧 Use `.mask()` modifier|🔧 SwiftUI has direct support, Compose requires custom drawing|
|mask-image|`mask-image: url('mask.png')`|`Modifier.drawWithContent { drawWithLayer(BlendMode.DstIn) }`|`.mask(Image('mask'))`|✅ SwiftUI has direct support|
|mask-mode|`mask-mode: alpha`|🔧 Use appropriate blend mode|🔧 Control via mask type|🔧 Platform-specific handling|
|mask-size|`mask-size: 50%`|🔧 Scale mask content in drawWithContent|`.mask(Image().resizable().aspectRatio())`|🔧 Control via mask content|
|mask-position|`mask-position: center`|🔧 Offset mask in drawWithContent|`.mask(content.offset())`|🔧 Position mask content|
|mask-composite|`mask-composite: add`|🔧 Use multiple drawWithContent layers|🔧 Stack multiple `.mask()` modifiers|🔧 Limited composite operations|
|mask-border|`mask-border: url('mask.png') 30`|🔧 Custom implementation combining mask and border|🔧 Custom implementation|🔧 No native support|
|mask-border-mode|`mask-border-mode: luminance`|🔧 Part of custom mask-border implementation|🔧 Part of custom implementation|🔧 No native support|
|mask-border-outset|`mask-border-outset: 10px`|🔧 Part of custom mask-border implementation|🔧 Part of custom implementation|🔧 No native support|
|mask-border-repeat|`mask-border-repeat: round`|🔧 Part of custom mask-border implementation|🔧 Part of custom implementation|🔧 No native support|
|mask-border-slice|`mask-border-slice: 30`|🔧 Part of custom mask-border implementation|🔧 Part of custom implementation|🔧 No native support|
|mask-border-source|`mask-border-source: url('mask.png')`|🔧 Part of custom mask-border implementation|🔧 Part of custom implementation|🔧 No native support|
|mask-border-width|`mask-border-width: 10px`|🔧 Part of custom mask-border implementation|🔧 Part of custom implementation|🔧 No native support|
|mask-clip|`mask-clip: border-box`|🔧 Control clip area in drawWithContent|🔧 Control mask clipping area|🔧 Part of mask implementation|
|mask-origin|`mask-origin: border-box`|🔧 Control mask origin in drawWithContent|🔧 Control mask origin|🔧 Part of mask implementation|
|mask-position-x|`mask-position-x: center`|🔧 Control horizontal mask position|🔧 Use `.mask(content.offset())`|🔧 Part of mask-position|
|mask-position-y|`mask-position-y: center`|🔧 Control vertical mask position|🔧 Use `.mask(content.offset())`|🔧 Part of mask-position|
|mask-repeat|`mask-repeat: repeat`|🔧 Tile mask in drawWithContent|🔧 Tile mask content|🔧 Custom tiling implementation|
|mask-type|`mask-type: alpha`|🔧 Control mask channel in drawWithContent|🔧 Control mask type|🔧 Platform-specific handling|
|clip-path-geometry-box|`clip-path-geometry-box: border-box`|🔧 Part of clip-path implementation|🔧 Part of clipShape|🔧 Defines reference box for clip-path|
|clip-rule|`clip-rule: evenodd`|🔧 Use Path.FillType.EvenOdd|`.fill(style: FillStyle(eoFill: true))`|✅ Control clip fill rule|
