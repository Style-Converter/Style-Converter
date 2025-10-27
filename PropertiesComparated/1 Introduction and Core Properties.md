# Cross-Platform Style Properties Reference - Part 1

**Version:** 1.0  
**Last Updated:** 2024  
**Total Properties Documented:** ~270  
**Platforms Covered:** CSS, Jetpack Compose, SwiftUI

---

## Legend

- ✅ **Direct equivalent** - Property exists natively in all platforms
- 🔧 **Workaround available** - Can be achieved with custom code
- ⚠️ **Limited/Different** - Exists but with different semantics or limitations
- ❌ **Not supported** - No equivalent, difficult to implement

---

## Dimensions & Sizing

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|width|`width: 100px`|`Modifier.width(100.dp)`|`.frame(width: 100)`|✅ Direct equivalent in all|
|height|`height: 100px`|`Modifier.height(100.dp)`|`.frame(height: 100)`|✅ Direct equivalent in all|
|min-width|`min-width: 50px`|`Modifier.widthIn(min = 50.dp)`|`.frame(minWidth: 50)`|✅ Direct equivalent in all|
|max-width|`max-width: 200px`|`Modifier.widthIn(max = 200.dp)`|`.frame(maxWidth: 200)`|✅ Direct equivalent in all|
|min-height|`min-height: 50px`|`Modifier.heightIn(min = 50.dp)`|`.frame(minHeight: 50)`|✅ Direct equivalent in all|
|max-height|`max-height: 200px`|`Modifier.heightIn(max = 200.dp)`|`.frame(maxHeight: 200)`|✅ Direct equivalent in all|
|aspect-ratio|`aspect-ratio: 16/9`|`Modifier.aspectRatio(16f/9f)`|`.aspectRatio(16/9, contentMode: .fit)`|✅ Direct equivalent in all|
|box-sizing|`box-sizing: border-box`|❌ N/A - Always includes padding in size|❌ N/A - Always includes padding in size|⚠️ CSS-specific, mobile frameworks always use border-box equivalent|
|fit-content|`width: fit-content`|`Modifier.wrapContentWidth()`|`.fixedSize(horizontal: true, vertical: false)`|✅ Shrink to content size|
|min-content|`width: min-content`|`Modifier.wrapContentWidth(unbounded = false)`|`.fixedSize()`|⚠️ CSS has more precise control|
|max-content|`width: max-content`|`Modifier.wrapContentWidth(unbounded = true)`|`.fixedSize().frame(maxWidth: .infinity)`|⚠️ Different semantics|
|calc()|`width: calc(100% - 20px)`|🔧 `BoxWithConstraints { maxWidth - 20.dp }`|🔧 `GeometryReader { geometry.size.width - 20 }`|🔧 Requires programmatic calculation|
|clamp()|`width: clamp(100px, 50%, 500px)`|`Modifier.widthIn(min = 100.dp, max = 500.dp).fillMaxWidth(0.5f)`|`.frame(minWidth: 100, maxWidth: 500)`|🔧 Combine min/max with flexible sizing|
|contain-intrinsic-size|`contain-intrinsic-size: 300px 400px`|❌ No equivalent|❌ No equivalent|❌ CSS-specific for layout containment|
|block-size|`block-size: 200px`|`Modifier.height(200.dp)` (vertical writing) or `width` (horizontal)|`.frame(height: 200)` (vertical) or `width` (horizontal)|✅ Logical dimension - height in vertical writing mode|
|inline-size|`inline-size: 200px`|`Modifier.width(200.dp)` (vertical writing) or `height` (horizontal)|`.frame(width: 200)` (vertical) or `height` (horizontal)|✅ Logical dimension - width in vertical writing mode|
|min-block-size|`min-block-size: 100px`|`Modifier.heightIn(min = 100.dp)`|`.frame(minHeight: 100)`|✅ Logical min dimension|
|max-block-size|`max-block-size: 300px`|`Modifier.heightIn(max = 300.dp)`|`.frame(maxHeight: 300)`|✅ Logical max dimension|
|min-inline-size|`min-inline-size: 100px`|`Modifier.widthIn(min = 100.dp)`|`.frame(minWidth: 100)`|✅ Logical min dimension|
|max-inline-size|`max-inline-size: 300px`|`Modifier.widthIn(max = 300.dp)`|`.frame(maxWidth: 300)`|✅ Logical max dimension|

---

## Display & Visibility

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|display|`display: block`|Use Row/Column/Box composables|Use VStack/HStack/ZStack views|✅ Layout type chosen by container|
|visibility|`visibility: hidden`|`Modifier.alpha(0f)` or conditional rendering|`.opacity(0)` or conditional rendering|⚠️ Hidden but takes space vs removed from layout|
|content-visibility|`content-visibility: auto`|❌ No equivalent|❌ No equivalent|❌ CSS-specific rendering optimization|
|contain|`contain: layout`|❌ No equivalent|❌ No equivalent|❌ CSS-specific layout containment|
|container-type|`container-type: inline-size`|❌ No equivalent|❌ No equivalent|❌ CSS container queries - not applicable|
|container-name|`container-name: sidebar`|❌ No equivalent|❌ No equivalent|❌ CSS container queries - not applicable|
|contain-intrinsic-width|`contain-intrinsic-width: 300px`|❌ No equivalent|❌ No equivalent|❌ CSS-specific for layout containment|
|contain-intrinsic-height|`contain-intrinsic-height: 400px`|❌ No equivalent|❌ No equivalent|❌ CSS-specific for layout containment|
|contain-intrinsic-block-size|`contain-intrinsic-block-size: 400px`|❌ No equivalent|❌ No equivalent|❌ CSS-specific for layout containment|
|contain-intrinsic-inline-size|`contain-intrinsic-inline-size: 300px`|❌ No equivalent|❌ No equivalent|❌ CSS-specific for layout containment|
|isolation|`isolation: isolate`|🔧 Use separate composable layers|🔧 Use separate view layers|🔧 Create stacking context for blend modes|
|will-change|`will-change: transform`|🔧 Use `Modifier.graphicsLayer { }` for hardware acceleration|🔧 Use `.drawingGroup()` for layer backing|🔧 Performance hint - handled differently|

---

## Spacing - Padding

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|padding|`padding: 10px`|`Modifier.padding(10.dp)`|`.padding(10)`|✅ Direct equivalent in all|
|padding-top|`padding-top: 10px`|`Modifier.padding(top = 10.dp)`|`.padding(.top, 10)`|✅ Direct equivalent in all|
|padding-right|`padding-right: 10px`|`Modifier.padding(end = 10.dp)`|`.padding(.trailing, 10)`|✅ Direct equivalent (note: end/trailing for RTL support)|
|padding-bottom|`padding-bottom: 10px`|`Modifier.padding(bottom = 10.dp)`|`.padding(.bottom, 10)`|✅ Direct equivalent in all|
|padding-left|`padding-left: 10px`|`Modifier.padding(start = 10.dp)`|`.padding(.leading, 10)`|✅ Direct equivalent (note: start/leading for RTL support)|
|padding (h/v)|`padding: 10px 20px`|`Modifier.padding(horizontal = 20.dp, vertical = 10.dp)`|`.padding(.horizontal, 20).padding(.vertical, 10)`|✅ All support shorthand|
|padding-block|`padding-block: 10px`|`Modifier.padding(vertical = 10.dp)`|`.padding(.vertical, 10)`|✅ Logical vertical padding|
|padding-block-start|`padding-block-start: 10px`|`Modifier.padding(top = 10.dp)`|`.padding(.top, 10)`|✅ Logical block-start (top in LTR)|
|padding-block-end|`padding-block-end: 10px`|`Modifier.padding(bottom = 10.dp)`|`.padding(.bottom, 10)`|✅ Logical block-end (bottom in LTR)|
|padding-inline|`padding-inline: 20px`|`Modifier.padding(horizontal = 20.dp)`|`.padding(.horizontal, 20)`|✅ Logical horizontal padding|
|padding-inline-start|`padding-inline-start: 20px`|`Modifier.padding(start = 20.dp)`|`.padding(.leading, 20)`|✅ Logical inline-start (left in LTR, right in RTL)|
|padding-inline-end|`padding-inline-end: 20px`|`Modifier.padding(end = 20.dp)`|`.padding(.trailing, 20)`|✅ Logical inline-end (right in LTR, left in RTL)|

---

## Spacing - Margin

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|margin|`margin: 10px`|⚠️ Use Spacer or parent layout spacing|⚠️ Use Spacer() or parent spacing|⚠️ CSS-specific - Compose/SwiftUI use spacing in parent containers|
|margin-top|`margin-top: 10px`|⚠️ Use `Spacer(Modifier.height(10.dp))` before item|⚠️ Use `Spacer(minLength: 10)` before item|⚠️ CSS-specific - Compose/SwiftUI handle spacing differently|
|margin-right|`margin-right: 10px`|⚠️ Use `Spacer(Modifier.width(10.dp))` after item|⚠️ Use `Spacer(minLength: 10)` after item|⚠️ CSS-specific - Use parent spacing or Spacer|
|margin-bottom|`margin-bottom: 10px`|⚠️ Use `Spacer(Modifier.height(10.dp))` after item|⚠️ Use `Spacer(minLength: 10)` after item|⚠️ CSS-specific - Compose/SwiftUI handle spacing differently|
|margin-left|`margin-left: 10px`|⚠️ Use `Spacer(Modifier.width(10.dp))` before item|⚠️ Use `Spacer(minLength: 10)` before item|⚠️ CSS-specific - Use parent spacing or Spacer|
|margin-block|`margin-block: 10px`|⚠️ Use `Spacer(Modifier.height(10.dp))` before/after|⚠️ Use `Spacer(minLength: 10)` before/after|⚠️ Logical vertical margin - use Spacer|
|margin-block-start|`margin-block-start: 10px`|⚠️ Use `Spacer(Modifier.height(10.dp))` before item|⚠️ Use `Spacer(minLength: 10)` before item|⚠️ Logical block-start margin (top in LTR)|
|margin-block-end|`margin-block-end: 10px`|⚠️ Use `Spacer(Modifier.height(10.dp))` after item|⚠️ Use `Spacer(minLength: 10)` after item|⚠️ Logical block-end margin (bottom in LTR)|
|margin-inline|`margin-inline: 20px`|⚠️ Use `Spacer(Modifier.width(20.dp))` before/after|⚠️ Use `Spacer(minLength: 20)` before/after|⚠️ Logical horizontal margin - use Spacer|
|margin-inline-start|`margin-inline-start: 20px`|⚠️ Use `Spacer(Modifier.width(20.dp))` before item|⚠️ Use `Spacer(minLength: 20)` before item|⚠️ Logical inline-start margin (left in LTR, right in RTL)|
|margin-inline-end|`margin-inline-end: 20px`|⚠️ Use `Spacer(Modifier.width(20.dp))` after item|⚠️ Use `Spacer(minLength: 20)` after item|⚠️ Logical inline-end margin (right in LTR, left in RTL)|
|margin: auto (centering)|`margin: 0 auto`|`Modifier.fillMaxWidth().wrapContentWidth(Align.CenterHorizontally)`|`.frame(maxWidth: .infinity, alignment: .center)`|🔧 Requires different approach - use alignment instead|
|margin-trim|`margin-trim: block`|❌ No equivalent|❌ No equivalent|❌ CSS-specific for collapsing margins at container edges|

---

## Spacing - Gap Properties

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|gap|`gap: 10px`|`Arrangement.spacedBy(10.dp)` in Row/Column|`HStack(spacing: 10)` or `VStack(spacing: 10)`|✅ Direct equivalent in all|
|row-gap|`row-gap: 10px`|`verticalArrangement = Arrangement.spacedBy(10.dp)`|`VStack(spacing: 10)` or `LazyVGrid(spacing: 10)`|✅ Direct equivalent|
|column-gap|`column-gap: 15px`|`horizontalArrangement = Arrangement.spacedBy(15.dp)`|`HStack(spacing: 15)` or `LazyHGrid(spacing: 15)`|✅ Direct equivalent|
|gap (shorthand)|`gap: 10px 15px`|`Arrangement.spacedBy(10.dp)` + `spacedBy(15.dp)`|Separate spacing for V/HStack|✅ Supported with separate values|

---

## Background

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|background|`background: red url(img.png) center/cover`|🔧 Combine multiple modifiers|🔧 Combine multiple modifiers|🔧 Shorthand - combine color, image, position, size modifiers|
|background-color|`background-color: red`|`Modifier.background(Color.Red)`|`.background(Color.red)`|✅ Direct equivalent in all|
|background-image|`background-image: url('image.png')`|`Modifier.background(painter = painterResource(R.drawable.image))`|`.background(Image('image'))`|✅ Equivalent with different syntax|
|background-gradient (linear)|`background: linear-gradient(to right, red, blue)`|`Modifier.background(Brush.horizontalGradient(listOf(Color.Red, Color.Blue)))`|`.background(LinearGradient(colors: [.red, .blue], startPoint: .leading, endPoint: .trailing))`|✅ All support with different syntax|
|background-gradient (radial)|`background: radial-gradient(circle, red, blue)`|`Modifier.background(Brush.radialGradient(listOf(Color.Red, Color.Blue)))`|`.background(RadialGradient(colors: [.red, .blue], center: .center, startRadius: 0, endRadius: 100))`|✅ All support with different syntax|
|conic-gradient|`background: conic-gradient(from 90deg, red, blue)`|🔧 Custom `Brush` with `Shader` or Canvas|`.background(AngularGradient(colors: [.red, .blue], center: .center))`|✅ SwiftUI has AngularGradient, Compose needs custom|
|repeating-linear-gradient|`background: repeating-linear-gradient(red 0px, blue 10px)`|🔧 Custom Brush with tile mode|🔧 Custom Canvas drawing|🔧 Limited native support|
|repeating-radial-gradient|`background: repeating-radial-gradient(circle, red, blue)`|🔧 Custom Brush implementation|🔧 Custom Canvas drawing|🔧 No native support|
|background-size|`background-size: cover`|🔧 Use `contentScale = ContentScale.Crop` in Image/AsyncImage|🔧 Use `.aspectRatio(contentMode: .fill)` on Image|🔧 Handled by image component properties, not background modifier|
|background-position|`background-position: center`|🔧 Use alignment parameter in Box or contentAlignment in Image|🔧 Use alignment parameter in background modifier|🔧 Handled through alignment systems|
|background-position-x|`background-position-x: right`|🔧 Use alignment with horizontal component|🔧 Use alignment with horizontal component|🔧 Part of background-position in Compose/SwiftUI|
|background-position-y|`background-position-y: bottom`|🔧 Use alignment with vertical component|🔧 Use alignment with vertical component|🔧 Part of background-position in Compose/SwiftUI|
|background-repeat|`background-repeat: repeat`|🔧 Custom drawing with drawWithContent or tiled brush|🔧 Custom drawing with Canvas or GeometryReader|🔧 No direct equivalent - requires custom implementation|
|background-clip|`background-clip: text`|🔧 Use `Brush` in TextStyle|🔧 Use `.foregroundStyle()` with gradient|🔧 Different approach for text backgrounds|
|background-origin|`background-origin: border-box`|⚠️ Always padding-box by default|⚠️ Always padding-box behavior|⚠️ Less control than CSS|
|background-attachment|`background-attachment: fixed`|🔧 Use Box with parallax scroll modifier|🔧 Custom ScrollView with geometry|🔧 Requires custom scroll handling|
|background-blend-mode|`background-blend-mode: multiply`|🔧 Use `Modifier.drawBehind` with blend modes|`.blendMode(.multiply)` on background|✅ Blend modes supported|
|multiple backgrounds|`background: url(img1), url(img2)`|🔧 Stack multiple Box layers with backgrounds|🔧 Use `ZStack` with multiple backgrounds|🔧 Manual layer stacking|