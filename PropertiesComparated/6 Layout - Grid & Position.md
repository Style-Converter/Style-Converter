# Cross-Platform Style Properties Reference - Part 6

## Layout - Grid

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|display: grid|`display: grid`|❌ No native CSS Grid equivalent|❌ No native CSS Grid equivalent|🔧 Use LazyVerticalGrid/LazyHGrid or nested Row/Column|
|grid-template-columns|`grid-template-columns: 1fr 2fr 1fr`|🔧 `LazyVerticalGrid(columns = GridCells.Fixed(3))`|🔧 `LazyVGrid(columns: [GridItem(.flexible()), ...])`|🔧 Simpler grid systems - no fr units or complex templates|
|grid-template-rows|`grid-template-rows: 100px auto 100px`|🔧 Use nested Column with specific heights|🔧 Use nested VStack with specific heights|🔧 No direct equivalent - manually compose|
|grid-template-areas|`grid-template-areas: 'header header' 'sidebar main'`|❌ No equivalent|❌ No equivalent|🔧 Manually compose layout structure with nested containers|
|grid-column|`grid-column: 1 / 3`|❌ No spanning support in LazyVerticalGrid|🔧 GridRow with columnSpan in iOS 16+ Grid|🔧 Very limited - SwiftUI has Grid (iOS 16+), Compose lacks native spanning|
|grid-row|`grid-row: 1 / 3`|❌ No spanning support in LazyVerticalGrid|🔧 GridRow with rowSpan in iOS 16+ Grid|🔧 Very limited support|
|grid-column-start|`grid-column-start: 2`|❌ No equivalent|🔧 iOS 16+ Grid|🔧 Limited support|
|grid-column-end|`grid-column-end: 4`|❌ No equivalent|🔧 iOS 16+ Grid|🔧 Limited support|
|grid-row-start|`grid-row-start: 1`|❌ No equivalent|🔧 iOS 16+ Grid|🔧 Limited support|
|grid-row-end|`grid-row-end: 3`|❌ No equivalent|🔧 iOS 16+ Grid|🔧 Limited support|
|grid-gap|`grid-gap: 10px`|`Arrangement.spacedBy(10.dp)` in LazyVerticalGrid|LazyVGrid spacing parameter|✅ Spacing supported in grid layouts|
|grid-column-gap|`grid-column-gap: 15px`|`horizontalArrangement = Arrangement.spacedBy(15.dp)`|LazyVGrid spacing parameter|✅ Column spacing|
|grid-row-gap|`grid-row-gap: 10px`|`verticalArrangement = Arrangement.spacedBy(10.dp)`|LazyVGrid spacing parameter|✅ Row spacing|
|grid-auto-flow|`grid-auto-flow: dense`|❌ No equivalent|❌ No equivalent|❌ Not supported - grids fill in order|
|grid-auto-columns|`grid-auto-columns: 100px`|🔧 Use `GridCells.Fixed()` or `Adaptive()`|🔧 Use `GridItem(.fixed(100))`|🔧 Different auto-sizing model|
|grid-auto-rows|`grid-auto-rows: 100px`|🔧 Set row heights manually|🔧 Set row heights manually in Grid|🔧 No auto-row sizing|
|justify-items (grid)|`justify-items: center`|❌ Use individual item alignment|`Grid { GridRow { }.gridCellColumns() }`|⚠️ Limited grid alignment control|
|align-items (grid)|`align-items: center`|❌ Use individual item alignment|Grid alignment parameters|⚠️ Limited control|
|justify-self|`justify-self: end`|`Modifier.align(Alignment.End)` in LazyGrid item|`.gridCellAnchor(.trailing)` in Grid|🔧 Item-level alignment|
|align-self (grid)|`align-self: end`|`Modifier.align(Alignment.Bottom)` in LazyGrid item|`.gridCellAnchor(.bottom)` in Grid|🔧 Item-level alignment|
|minmax()|`grid-template-columns: minmax(100px, 1fr)`|🔧 `GridCells.Adaptive(minSize = 100.dp)`|🔧 `GridItem(.flexible(minimum: 100))`|🔧 Simplified min/max sizing|
|repeat()|`grid-template-columns: repeat(3, 1fr)`|`GridCells.Fixed(3)` or Adaptive|Array of GridItem (count: 3)|✅ Repetition supported|
|fit-content() (grid)|`grid-template-columns: fit-content(200px)`|🔧 Manual calculation with constraints|🔧 `GridItem(.flexible())` with max|🔧 Approximate with flexible items|
|auto-fill|`grid-template-columns: repeat(auto-fill, 100px)`|`GridCells.Adaptive(minSize = 100.dp)`|🔧 Custom calculation in LazyVGrid|✅ Compose Adaptive is similar|
|auto-fit|`grid-template-columns: repeat(auto-fit, 100px)`|🔧 Similar to Adaptive but stretches|🔧 Custom grid with flexible items|🔧 No exact equivalent|
|grid|`grid: auto-flow / 1fr 2fr`|🔧 Combine GridCells with arrangement|🔧 Combine GridItem arrays|🔧 Shorthand for grid-template-rows/columns/areas + grid-auto-rows/columns/flow|
|grid-template|`grid-template: 'a a' 'b c' / 1fr 1fr`|🔧 Custom composition with named areas|🔧 Custom Grid layout|🔧 Shorthand for template-rows/columns/areas|
|grid-area|`grid-area: header`|❌ No named area support|🔧 Use Grid with custom positioning|🔧 Shorthand for row-start/column-start/row-end/column-end|
|grid-auto-track|`grid-auto-track: 1fr`|❌ No equivalent|❌ No equivalent|❌ Experimental CSS property|
|masonry-auto-flow|`masonry-auto-flow: next`|❌ No masonry layout|❌ No masonry layout|❌ Experimental CSS masonry|
|justify-tracks|`justify-tracks: space-between`|❌ No equivalent|❌ No equivalent|❌ Experimental CSS property|
|align-tracks|`align-tracks: start`|❌ No equivalent|❌ No equivalent|❌ Experimental CSS property|

---

## Layout - Position

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|position|`position: relative`|⚠️ All layouts are relative by default|⚠️ All views are relative by default|⚠️ No separate position property needed|
|position: relative|`position: relative`|⚠️ Default behavior - all layouts relative to parent|⚠️ Default behavior - all views relative to parent|⚠️ Default positioning model|
|position: absolute|`position: absolute; top: 10px; left: 10px`|`Box { Box(Modifier.offset(x=10.dp, y=10.dp).align(Alignment.TopStart)) }`|`ZStack(alignment: .topLeading) { Color.clear; View().offset(x:10, y:10) }`|🔧 Use Box/ZStack with alignment and offset|
|position: fixed|`position: fixed; top: 0`|🔧 Use Scaffold with topBar, or BoxWithConstraints at screen level|🔧 Use `.overlay(alignment:)` at root view level|🔧 No direct equivalent - position at root level|
|position: sticky|`position: sticky; top: 0`|❌ No native support|❌ No native support|🔧 Use LazyColumn with custom scroll behavior or Scaffold|
|position-anchor|`position-anchor: --myanchor`|❌ No equivalent|❌ No equivalent|❌ CSS anchor positioning (experimental)|
|position-area|`position-area: top left`|❌ No equivalent|❌ No equivalent|❌ CSS anchor positioning (experimental)|
|position-try|`position-try: flip-block`|❌ No equivalent|❌ No equivalent|❌ CSS anchor positioning fallback (experimental)|
|position-try-options|`position-try-options: flip-block, flip-inline`|❌ No equivalent|❌ No equivalent|❌ CSS anchor positioning (experimental)|
|position-try-order|`position-try-order: most-height`|❌ No equivalent|❌ No equivalent|❌ CSS anchor positioning (experimental)|
|position-visibility|`position-visibility: always`|❌ No equivalent|❌ No equivalent|❌ CSS anchor positioning (experimental)|
|position-fallback|`position-fallback: --myfallback`|❌ No equivalent|❌ No equivalent|❌ CSS anchor positioning (experimental)|
|top|`top: 10px`|🔧 Use `Modifier.offset()` or alignment in Box|🔧 Use `.offset()` or position in ZStack|🔧 Combined with Box/ZStack alignment|
|right|`right: 10px`|🔧 Use `Modifier.offset()` or alignment in Box|🔧 Use `.offset()` or position in ZStack|🔧 Combined with Box/ZStack alignment|
|bottom|`bottom: 10px`|🔧 Use `Modifier.offset()` or alignment in Box|🔧 Use `.offset()` or position in ZStack|🔧 Combined with Box/ZStack alignment|
|left|`left: 10px`|🔧 Use `Modifier.offset()` or alignment in Box|🔧 Use `.offset()` or position in ZStack|🔧 Combined with Box/ZStack alignment|
|z-index|`z-index: 10`|🔧 Order of children in Box determines z-order (last = top)|🔧 Use `.zIndex(10)` modifier|⚠️ Compose uses declaration order, SwiftUI has explicit zIndex|
|inset|`inset: 10px`|🔧 Combination of offset and alignment|🔧 Combination of offset and frame|🔧 Shorthand for top/right/bottom/left|
|inset-block|`inset-block: 10px`|🔧 Combination of vertical offset|🔧 Combination of vertical offset|🔧 Logical shorthand for top/bottom|
|inset-block-start|`inset-block-start: 10px`|🔧 Use `Modifier.offset(y = 10.dp)` from top|🔧 Use `.offset(y: 10)` from top|🔧 Logical block-start (top in LTR)|
|inset-block-end|`inset-block-end: 10px`|🔧 Use `Modifier.offset()` from bottom|🔧 Use `.offset()` from bottom|🔧 Logical block-end (bottom in LTR)|
|inset-inline|`inset-inline: 20px`|🔧 Combination of horizontal offset|🔧 Combination of horizontal offset|🔧 Logical shorthand for left/right|
|inset-inline-start|`inset-inline-start: 20px`|🔧 Use `Modifier.offset(x = 20.dp)` from start|🔧 Use `.offset(x: 20)` from leading|🔧 Logical inline-start (left in LTR, right in RTL)|
|inset-inline-end|`inset-inline-end: 20px`|🔧 Use `Modifier.offset()` from end|🔧 Use `.offset()` from trailing|🔧 Logical inline-end (right in LTR, left in RTL)|
|inset-area|`inset-area: top left`|❌ No equivalent|❌ No equivalent|❌ CSS anchor positioning (experimental)|
|anchor-name|`anchor-name: --myanchor`|❌ No equivalent|❌ No equivalent|❌ CSS anchor positioning (experimental)|
|anchor-scope|`anchor-scope: all`|❌ No equivalent|❌ No equivalent|❌ CSS anchor positioning (experimental)|

---

## Overflow & Scroll

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|overflow|`overflow: auto`|`Modifier.verticalScroll()` or `LazyColumn`|`ScrollView`|✅ Scrollable containers|
|overflow-x|`overflow-x: scroll`|`Modifier.horizontalScroll()`|`ScrollView(.horizontal)`|✅ Horizontal scrolling|
|overflow-y|`overflow-y: scroll`|`Modifier.verticalScroll()`|`ScrollView(.vertical)`|✅ Vertical scrolling|
|overflow-block|`overflow-block: auto`|`Modifier.verticalScroll()` (block direction)|`ScrollView(.vertical)`|✅ Logical block overflow|
|overflow-inline|`overflow-inline: auto`|`Modifier.horizontalScroll()` (inline direction)|`ScrollView(.horizontal)`|✅ Logical inline overflow|
|overflow-wrap|`overflow-wrap: break-word`|🔧 Default text wrapping behavior|🔧 Default text wrapping|⚠️ Text-specific wrapping control|
|overflow-anchor|`overflow-anchor: auto`|❌ No equivalent|❌ No equivalent|❌ CSS scroll anchoring|
|overflow-clip-margin|`overflow-clip-margin: 10px`|❌ No equivalent|❌ No equivalent|❌ CSS overflow clipping control|
|overscroll-behavior|`overscroll-behavior: contain`|❌ Limited control|🔧 `.scrollBounceBehavior()`|⚠️ Some bounce/overscroll control|
|overscroll-behavior-x|`overscroll-behavior-x: contain`|❌ Limited control|🔧 `.scrollBounceBehavior(.basedOnSize)`|⚠️ Platform-specific behavior|
|overscroll-behavior-y|`overscroll-behavior-y: none`|❌ Limited control|🔧 `.scrollBounceBehavior(.always)`|⚠️ Platform-specific behavior|
|overscroll-behavior-block|`overscroll-behavior-block: contain`|❌ Limited control|🔧 Control block-direction overscroll|⚠️ Logical overscroll control|
|overscroll-behavior-inline|`overscroll-behavior-inline: contain`|❌ Limited control|🔧 Control inline-direction overscroll|⚠️ Logical overscroll control|
|scroll-behavior|`scroll-behavior: smooth`|`Modifier.verticalScroll(animationSpec = ...)`|`.scrollTargetBehavior()` or animation|🔧 Animated scrolling|
|scroll-margin|`scroll-margin: 10px`|🔧 Add padding in scrollable container|🔧 Use `scrollTargetLayout()`|🔧 Scroll snap margins|
|scroll-margin-top|`scroll-margin-top: 10px`|🔧 Padding in scroll container|🔧 Custom scroll target|🔧 Individual scroll margins|
|scroll-margin-right|`scroll-margin-right: 10px`|🔧 Padding in scroll container|🔧 Custom scroll target|🔧 Individual scroll margins|
|scroll-margin-bottom|`scroll-margin-bottom: 10px`|🔧 Padding in scroll container|🔧 Custom scroll target|🔧 Individual scroll margins|
|scroll-margin-left|`scroll-margin-left: 10px`|🔧 Padding in scroll container|🔧 Custom scroll target|🔧 Individual scroll margins|
|scroll-margin-block|`scroll-margin-block: 10px`|🔧 Padding in scroll container|🔧 Custom scroll target|🔧 Logical scroll margins|
|scroll-margin-block-start|`scroll-margin-block-start: 10px`|🔧 Padding in scroll container|🔧 Custom scroll target|🔧 Logical scroll margin|
|scroll-margin-block-end|`scroll-margin-block-end: 10px`|🔧 Padding in scroll container|🔧 Custom scroll target|🔧 Logical scroll margin|
|scroll-margin-inline|`scroll-margin-inline: 20px`|🔧 Padding in scroll container|🔧 Custom scroll target|🔧 Logical scroll margins|
|scroll-margin-inline-start|`scroll-margin-inline-start: 20px`|🔧 Padding in scroll container|🔧 Custom scroll target|🔧 Logical scroll margin|
|scroll-margin-inline-end|`scroll-margin-inline-end: 20px`|🔧 Padding in scroll container|🔧 Custom scroll target|🔧 Logical scroll margin|
|scroll-padding|`scroll-padding: 10px`|🔧 Use `contentPadding` in LazyColumn|`.safeAreaPadding()` or padding|🔧 Padding for scroll containers|
|scroll-padding-top|`scroll-padding-top: 10px`|`contentPadding = PaddingValues(top = 10.dp)`|`.safeAreaInset(edge: .top)`|🔧 Individual scroll padding|
|scroll-padding-right|`scroll-padding-right: 10px`|`contentPadding = PaddingValues(end = 10.dp)`|`.safeAreaInset(edge: .trailing)`|🔧 Individual scroll padding|
|scroll-padding-bottom|`scroll-padding-bottom: 10px`|`contentPadding = PaddingValues(bottom = 10.dp)`|`.safeAreaInset(edge: .bottom)`|🔧 Individual scroll padding|
|scroll-padding-left|`scroll-padding-left: 10px`|`contentPadding = PaddingValues(start = 10.dp)`|`.safeAreaInset(edge: .leading)`|🔧 Individual scroll padding|
|scroll-padding-block|`scroll-padding-block: 10px`|`contentPadding = PaddingValues(vertical = 10.dp)`|`.safeAreaPadding(.vertical)`|🔧 Logical scroll padding|
|scroll-padding-block-start|`scroll-padding-block-start: 10px`|`contentPadding = PaddingValues(top = 10.dp)`|`.safeAreaInset(edge: .top)`|🔧 Logical scroll padding|
|scroll-padding-block-end|`scroll-padding-block-end: 10px`|`contentPadding = PaddingValues(bottom = 10.dp)`|`.safeAreaInset(edge: .bottom)`|🔧 Logical scroll padding|
|scroll-padding-inline|`scroll-padding-inline: 20px`|`contentPadding = PaddingValues(horizontal = 20.dp)`|`.safeAreaPadding(.horizontal)`|🔧 Logical scroll padding|
|scroll-padding-inline-start|`scroll-padding-inline-start: 20px`|`contentPadding = PaddingValues(start = 20.dp)`|`.safeAreaInset(edge: .leading)`|🔧 Logical scroll padding|
|scroll-padding-inline-end|`scroll-padding-inline-end: 20px`|`contentPadding = PaddingValues(end = 20.dp)`|`.safeAreaInset(edge: .trailing)`|🔧 Logical scroll padding|
|scroll-snap-align|`scroll-snap-align: center`|🔧 Use `LazyColumn` with custom snapping|`.scrollTargetBehavior(.paging)`|🔧 Snap-to-item scrolling|
|scroll-snap-margin|`scroll-snap-margin: 10px`|🔧 Custom snap behavior with margins|🔧 Custom scroll targets|🔧 Deprecated - use scroll-margin|
|scroll-snap-margin-top|`scroll-snap-margin-top: 10px`|🔧 Custom snap behavior|🔧 Custom scroll targets|🔧 Deprecated|
|scroll-snap-margin-right|`scroll-snap-margin-right: 10px`|🔧 Custom snap behavior|🔧 Custom scroll targets|🔧 Deprecated|
|scroll-snap-margin-bottom|`scroll-snap-margin-bottom: 10px`|🔧 Custom snap behavior|🔧 Custom scroll targets|🔧 Deprecated|
|scroll-snap-margin-left|`scroll-snap-margin-left: 10px`|🔧 Custom snap behavior|🔧 Custom scroll targets|🔧 Deprecated|
|scroll-snap-margin-block|`scroll-snap-margin-block: 10px`|🔧 Custom snap behavior|🔧 Custom scroll targets|🔧 Deprecated|
|scroll-snap-margin-block-start|`scroll-snap-margin-block-start: 10px`|🔧 Custom snap behavior|🔧 Custom scroll targets|🔧 Deprecated|
|scroll-snap-margin-block-end|`scroll-snap-margin-block-end: 10px`|🔧 Custom snap behavior|🔧 Custom scroll targets|🔧 Deprecated|
|scroll-snap-margin-inline|`scroll-snap-margin-inline: 20px`|🔧 Custom snap behavior|🔧 Custom scroll targets|🔧 Deprecated|
|scroll-snap-margin-inline-start|`scroll-snap-margin-inline-start: 20px`|🔧 Custom snap behavior|🔧 Custom scroll targets|🔧 Deprecated|
|scroll-snap-margin-inline-end|`scroll-snap-margin-inline-end: 20px`|🔧 Custom snap behavior|🔧 Custom scroll targets|🔧 Deprecated|
|scroll-snap-stop|`scroll-snap-stop: always`|❌ No equivalent|🔧 Custom scroll behavior|⚠️ Limited control|
|scroll-snap-type|`scroll-snap-type: x mandatory`|🔧 Use pager or custom snap behavior|`.scrollTargetBehavior(.paging)`|🔧 Snap scrolling mode|
|scroll-start|`scroll-start: top`|🔧 Set initial scroll state|🔧 Use `.defaultScrollAnchor()`|🔧 Initial scroll position|
|scroll-start-x|`scroll-start-x: 100px`|🔧 Set horizontal scroll state|🔧 Use `ScrollViewReader`|🔧 Initial horizontal position|
|scroll-start-y|`scroll-start-y: 100px`|🔧 Set vertical scroll state|🔧 Use `ScrollViewReader`|🔧 Initial vertical position|
|scroll-start-block|`scroll-start-block: start`|🔧 Set block-direction scroll|🔧 Use default scroll anchor|🔧 Logical scroll start|
|scroll-start-inline|`scroll-start-inline: end`|🔧 Set inline-direction scroll|🔧 Use default scroll anchor|🔧 Logical scroll start|
|scroll-start-target|`scroll-start-target: auto`|❌ No equivalent|❌ No equivalent|❌ Experimental CSS property|
|scroll-start-target-block|`scroll-start-target-block: auto`|❌ No equivalent|❌ No equivalent|❌ Experimental CSS property|
|scroll-start-target-inline|`scroll-start-target-inline: auto`|❌ No equivalent|❌ No equivalent|❌ Experimental CSS property|
|scroll-start-target-x|`scroll-start-target-x: auto`|❌ No equivalent|❌ No equivalent|❌ Experimental CSS property|
|scroll-start-target-y|`scroll-start-target-y: auto`|❌ No equivalent|❌ No equivalent|❌ Experimental CSS property|
|scroll-timeline|`scroll-timeline: --mytimeline`|❌ No equivalent|❌ No equivalent|❌ CSS scroll-linked animations|
|scroll-timeline-axis|`scroll-timeline-axis: block`|❌ No equivalent|❌ No equivalent|❌ CSS scroll-linked animations|
|scroll-timeline-name|`scroll-timeline-name: --mytimeline`|❌ No equivalent|❌ No equivalent|❌ CSS scroll-linked animations|
|scrollbar-color|`scrollbar-color: red blue`|❌ Limited customization|❌ System-controlled|❌ Very limited scrollbar styling|
|scrollbar-width|`scrollbar-width: thin`|❌ System-controlled|❌ System-controlled|❌ System-controlled scrollbar width|
|scrollbar-gutter|`scrollbar-gutter: stable`|❌ No equivalent|❌ No equivalent|❌ CSS scrollbar space reservation|