# Cross-Platform Style Properties Reference - Part 9

## Platform-Specific Properties - Jetpack Compose

|Property|CSS Equivalent/Notes|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|Semantics|`aria-*` attributes for accessibility|`Modifier.semantics { ... }`|`.accessibility*()` family|✅ Accessibility annotations|
|Pointer Input|Custom event handlers|`Modifier.pointerInput { ... }`|`.gesture()` with custom gestures|✅ Custom gesture handling|
|Indication|`:active` pseudo-class, `-webkit-tap-highlight-color`|`Modifier.indication(...)`|`.buttonStyle()` or custom|✅ Touch ripple/feedback|
|Graphics Layer|Hardware acceleration hints, `will-change`|`Modifier.graphicsLayer { ... }`|`.drawingGroup()`|✅ Hardware-accelerated rendering|
|Draw Behind|`::before` with custom drawing|`Modifier.drawBehind { ... }`|`.background(Canvas { })`|✅ Custom background drawing|
|Draw With Content|Layered drawing, masks|`Modifier.drawWithContent { ... }`|`.overlay()` or `.background()` with Canvas|✅ Custom drawing with content|
|Focusable|`:focus` pseudo-class|`Modifier.focusable()`|`.focusable()`|✅ Keyboard focus|
|Animated Visibility|CSS transitions with `display: none`|`AnimatedVisibility { ... }`|Conditional with `.transition()`|✅ Animated show/hide|
|Combined Clickable|`:hover`, `:active`, long-press handlers|`Modifier.combinedClickable { ... }`|`.onTapGesture() + .onLongPressGesture()`|✅ Multiple click actions|
|Swipeable|Touch gestures, `touch-action`|`Modifier.swipeable(...)`|`.gesture(DragGesture())` with custom logic|✅ Swipe gestures|
|Draggable|Drag-and-drop, `cursor: move`|`Modifier.draggable(...)`|`.gesture(DragGesture())`|✅ Drag functionality|
|Scrollable|`overflow: scroll` with custom behavior|`Modifier.scrollable(...)`|Custom scroll handling|✅ Custom scroll behavior|
|Nested Scroll|Nested scrolling containers|`Modifier.nestedScroll(...)`|Automatic nested scrolling|✅ Nested scroll coordination|
|Weight|`flex-grow` in flexbox|`Modifier.weight(1f)`|`.frame(maxWidth: .infinity)`|✅ Flexible sizing in layouts|
|Anchored Draggable|No CSS equivalent|`Modifier.anchoredDraggable(...)`|`.gesture(DragGesture())` with snap points|✅ Drag with snap positions|
|Selectable Group|ARIA radio group|`Modifier.selectableGroup()`|Custom accessibility grouping|✅ Accessibility grouping|
|Tri-State Toggleable|Checkbox indeterminate|`Modifier.triStateToggleable(...)`|Custom toggle with three states|✅ Three-state toggle (On/Off/Indeterminate)|
|Focus Properties|CSS focus management|`Modifier.focusProperties { ... }`|`.focusScope()` and focus modifiers|✅ Custom focus behavior|
|Focus Requester|JavaScript focus()|`Modifier.focusRequester(focusRequester)`|`@FocusState` and `.focused()`|✅ Programmatic focus control|
|On Focus Changed|`:focus` detection|`Modifier.onFocusChanged { ... }`|`.onFocusChange { ... }`|✅ Focus state observation|
|Clip To Bounds|`overflow: hidden`|`Modifier.clipToBounds()`|`.clipped()`|✅ Clip content to bounds|
|Draw With Cache|No CSS equivalent|`Modifier.drawWithCache { ... }`|No direct equivalent|✅ Cached drawing (Compose only)|
|Paint|Background with custom painter|`Modifier.paint(painter)`|No direct equivalent|✅ Custom painter (Compose only)|
|Safe Drawing Padding|CSS safe-area-inset|`Modifier.safeDrawingPadding()`|`.safeAreaInset { ... }`|✅ Safe area padding|
|Inner Shadow|`box-shadow: inset`|`Modifier.innerShadow(...)`|No direct equivalent|✅ Inner shadow (Compose Material3)|
|Drop Shadow|`filter: drop-shadow()`|`Modifier.dropShadow(...)`|`.shadow()`|✅ Drop shadow effects|
|Animate Bounds|No CSS equivalent|`Modifier.animateBounds()`|`.matchedGeometryEffect()`|✅ Animate layout bounds|
|Animate Item|CSS transitions|`Modifier.animateItem()`|`.transition()`|✅ List item animations|
|Hoverable|`:hover` pseudo-class|`Modifier.hoverable()` (desktop only)|`.onHover { ... }` (macOS/iPad)|⚠️ Desktop/tablet only|
|Magnifier|No CSS equivalent|`Modifier.magnifier()`|`.magnificationGesture()` or custom|✅ Magnifying glass effect (Compose)|
|Basic Marquee|`animation: marquee` (custom)|`Modifier.basicMarquee()`|Custom scroll animation|✅ Scrolling text effect (Compose)|
|Layout ID|CSS Grid area names|`Modifier.layoutId("id")`|Custom layout with ID|✅ Named layout positioning|
|Parent Data Modifier|No CSS equivalent|`Modifier.parentDataModifier { ... }`|Custom layout data|✅ Pass data to parent layout|
|On Placed|No CSS equivalent|`Modifier.onPlaced { ... }`|`.onGeometryChange { ... }`|✅ Layout position callback|
|On Sized Changed|ResizeObserver API|`Modifier.onSizeChanged { ... }`|`.onGeometryChange { ... }`|✅ Size change callback|
|On Global Positioned|No CSS equivalent|`Modifier.onGloballyPositioned { ... }`|`.onGeometryChange { ... }`|✅ Global position callback|
|With Content Color|Color inheritance|`Modifier.withContentColor(color)`|`.foregroundStyle()` or environment|✅ Content color theming|
|Minimum Interactive Component Size|Touch target sizing|`Modifier.minimumInteractiveComponentSize()`|`.contentShape()` with min size|✅ Accessibility touch targets|

---

## Platform-Specific Properties - SwiftUI

|Property|CSS Equivalent/Notes|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|Matched Geometry Effect|Shared element transitions (no CSS equivalent)|`SharedTransitionScope` (experimental)|`.matchedGeometryEffect(...)`|✅ Shared element transitions|
|Navigation Title|Page title, `<title>` tag|`Scaffold` with `topBar`|`.navigationTitle("Title")`|✅ Navigation bar title|
|Toolbar|Custom header elements|`Scaffold` with `topBar` actions|`.toolbar { ... }`|✅ Toolbar items|
|Sheet|Modal dialogs, `position: fixed` overlays|`ModalBottomSheet` or Dialog|`.sheet(isPresented:) { ... }`|✅ Modal presentation|
|Alert|`window.alert()`, modal dialogs|`AlertDialog`|`.alert(isPresented:) { ... }`|✅ Alert dialog|
|Confirmation Dialog|Action sheets, context menus|`ModalBottomSheet` with options|`.confirmationDialog(...)`|✅ Action sheet|
|Searchable|Search input fields|Custom search TextField in Scaffold|`.searchable(text:)`|✅ Search interface|
|Refreshable|Pull-to-refresh patterns|`SwipeRefresh` (accompanist library)|`.refreshable { ... }`|✅ Pull to refresh|
|Context Menu|`:hover` menus, right-click menus|`Modifier.combinedClickable` + DropdownMenu|`.contextMenu { ... }`|✅ Long-press menu|
|Task|Async operations on mount|`LaunchedEffect`|`.task { ... }`|✅ Async work lifecycle|
|On Change|Watching for value changes|`LaunchedEffect` with snapshot|`.onChange(of:) { ... }`|✅ Value change observer|
|On Appear|Component lifecycle, `DOMContentLoaded`|`DisposableEffect` or `LaunchedEffect`|`.onAppear { ... }`|✅ Lifecycle callbacks|
|On Disappear|Component unmount|`DisposableEffect` cleanup|`.onDisappear { ... }`|✅ Cleanup callbacks|
|Badge|Notification badges (no CSS equivalent)|Custom composable overlay|`.badge("3")`|✅ Badge on tab/list items|
|Help|`title` attribute for tooltips|Custom tooltip composable|`.help("Tooltip text")`|✅ Tooltip (macOS/iPadOS)|
|Text Selection|`user-select` property|`SelectionContainer { Text() }`|`.textSelection(.enabled)`|✅ Text selection control|
|Keyboard Shortcut|No CSS equivalent (JavaScript)|Custom key event handling|`.keyboardShortcut("s", modifiers: .command)`|✅ Keyboard shortcuts|
|Focus Section|Focus management (no CSS equivalent)|Focus management APIs|`.focusSection()`|✅ Focus navigation|
|Default Focus|`autofocus` attribute|Focus management APIs|`.defaultFocus(...)`|✅ Initial focus|

---

## Accessibility

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|N/A - ARIA role|`role="button"`|`Modifier.semantics { role = Role.Button }`|`.accessibilityAddTraits(.isButton)`|✅ Role/trait annotation|
|N/A - ARIA label|`aria-label="Close"`|`Modifier.semantics { contentDescription = "Close" }`|`.accessibilityLabel("Close")`|✅ Accessibility labels|
|N/A - ARIA hidden|`aria-hidden="true"`|`Modifier.semantics { invisibleToUser() }`|`.accessibilityHidden(true)`|✅ Hide from accessibility|
|N/A - ARIA live|`aria-live="polite"`|`Modifier.semantics { liveRegion = LiveRegionMode.Polite }`|`.accessibilityElement(children: .combine)`|✅ Live region support|
|N/A - focus visible|`:focus-visible`|`Modifier.focusable()` with indication|`.focusable()` with SwiftUI focus system|✅ Keyboard focus handling|
|N/A - ARIA describedby|`aria-describedby="desc1"`|`Modifier.semantics { contentDescription = "..." }`|`.accessibilityHint("...")`|✅ Additional descriptions|
|N/A - ARIA valuemin/max|`aria-valuemin="0" aria-valuemax="100"`|`Modifier.semantics { progressBarRangeInfo = ... }`|`.accessibilityValue("...")`|✅ Value ranges for controls|

---

## List Properties

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|list-style|`list-style: disc inside`|❌ N/A - No list styling|❌ N/A - No list styling|🔧 Use custom composables for bullet/numbered lists|
|list-style-type|`list-style-type: decimal`|🔧 Custom prefix in Text|🔧 Custom prefix in Text|🔧 Manual numbering/bullets|
|list-style-position|`list-style-position: inside`|🔧 Layout with Row/Column|🔧 Layout with HStack/VStack|🔧 Control via layout structure|
|list-style-image|`list-style-image: url('bullet.png')`|🔧 Use Image in Row|🔧 Use Image in HStack|🔧 Custom bullet with images|
|marker|`marker: url(#marker)`|❌ SVG-specific|❌ SVG-specific|❌ SVG marker reference|
|marker-start|`marker-start: url(#arrow)`|❌ SVG-specific|❌ SVG-specific|❌ SVG line marker|
|marker-mid|`marker-mid: url(#dot)`|❌ SVG-specific|❌ SVG-specific|❌ SVG line marker|
|marker-end|`marker-end: url(#arrow)`|❌ SVG-specific|❌ SVG-specific|❌ SVG line marker|
|marker-side|`marker-side: match-self`|❌ Experimental CSS|❌ Experimental CSS|❌ Experimental list marker positioning|

---

## Table Properties

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|table-layout|`table-layout: fixed`|❌ N/A - No table layout|❌ N/A - No table layout|🔧 Use LazyVerticalGrid or custom layout|
|caption-side|`caption-side: top`|🔧 Position Text above/below grid|🔧 Position Text above/below Grid|🔧 Manual positioning|
|empty-cells|`empty-cells: hide`|🔧 Conditional rendering|🔧 Conditional rendering|🔧 Hide empty cells programmatically|

---

## Multi-Column Layout

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|columns|`columns: 3 200px`|🔧 Use LazyVerticalGrid with columns|`LazyVGrid(columns: [GridItem(), ...])`|🔧 Grid-based multi-column|
|column-count|`column-count: 3`|`LazyVerticalGrid(columns = GridCells.Fixed(3))`|`LazyVGrid(columns: Array(repeating: GridItem(), count: 3))`|✅ Multi-column grid|
|column-width|`column-width: 200px`|`LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 200.dp))`|`LazyVGrid(columns: [GridItem(.adaptive(minimum: 200))])`|✅ Adaptive column width|
|column-fill|`column-fill: balance`|❌ No automatic balancing|❌ No automatic balancing|🔧 Manual content distribution|
|column-span|`column-span: all`|❌ No native column spanning|🔧 Custom Grid item spanning|🔧 Limited support|
|column-rule|`column-rule: 1px solid black`|🔧 Custom dividers between columns|`Divider()` between columns|🔧 Manual divider placement|
|column-rule-width|`column-rule-width: 2px`|🔧 Custom divider width|`Divider().frame(width: 2)`|🔧 Custom divider styling|
|column-rule-style|`column-rule-style: dashed`|🔧 Custom divider style|🔧 Custom divider with Canvas|🔧 Limited divider styles|
|column-rule-color|`column-rule-color: red`|`Divider(color = Color.Red)`|`Divider().background(Color.red)`|✅ Divider color control|

---

## SVG-Specific Properties

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|fill|`fill: red` (SVG)|🔧 Use Canvas with Path and fill|`Path().fill(Color.red)`|✅ Path/shape fill color|
|fill-opacity|`fill-opacity: 0.5`|🔧 Use alpha in fill color|`Path().fill(Color.red.opacity(0.5))`|✅ Fill transparency|
|fill-rule|`fill-rule: evenodd`|`Path.FillType.EvenOdd`|`.fill(style: FillStyle(eoFill: true))`|✅ Fill rule for complex paths|
|stroke|`stroke: blue` (SVG)|🔧 Use Canvas with Path and stroke|`Path().stroke(Color.blue)`|✅ Path stroke color|
|stroke-width|`stroke-width: 2`|🔧 Stroke width in Canvas|`Path().stroke(lineWidth: 2)`|✅ Stroke width control|
|stroke-opacity|`stroke-opacity: 0.7`|🔧 Use alpha in stroke color|`Path().stroke(Color.blue.opacity(0.7))`|✅ Stroke transparency|
|stroke-linecap|`stroke-linecap: round`|`StrokeCap.Round`|`StrokeStyle(lineCap: .round)`|✅ Line cap style|
|stroke-linejoin|`stroke-linejoin: bevel`|`StrokeJoin.Bevel`|`StrokeStyle(lineJoin: .bevel)`|✅ Line join style|
|stroke-miterlimit|`stroke-miterlimit: 4`|`strokeMiterLimit`|`StrokeStyle(miterLimit: 4)`|✅ Miter limit control|
|stroke-dasharray|`stroke-dasharray: 5 10`|`PathEffect.dashPathEffect(floatArrayOf(5f, 10f))`|`StrokeStyle(dash: [5, 10])`|✅ Dashed stroke pattern|
|stroke-dashoffset|`stroke-dashoffset: 5`|`PathEffect with phase`|`StrokeStyle(dashPhase: 5)`|✅ Dash pattern offset|
|stop-color|`stop-color: red` (SVG gradient)|Gradient stop color|`Gradient.Stop(color: .red, location: 0.5)`|✅ Gradient stop color|
|stop-opacity|`stop-opacity: 0.5`|Gradient stop with alpha|`.red.opacity(0.5)` in gradient stop|✅ Gradient stop opacity|
|flood-color|`flood-color: yellow` (SVG filter)|❌ N/A - No SVG filters|❌ N/A - No SVG filters|❌ SVG filter property|
|flood-opacity|`flood-opacity: 0.5`|❌ N/A - No SVG filters|❌ N/A - No SVG filters|❌ SVG filter property|
|lighting-color|`lighting-color: white`|❌ N/A - No SVG filters|❌ N/A - No SVG filters|❌ SVG filter property|
|vector-effect|`vector-effect: non-scaling-stroke`|❌ No equivalent|❌ No equivalent|❌ SVG-specific rendering|
|shape-rendering|`shape-rendering: crispEdges`|❌ No explicit control|❌ No explicit control|❌ SVG rendering hint|
|buffered-rendering|`buffered-rendering: auto`|❌ SVG-specific|❌ SVG-specific|❌ SVG rendering optimization|
|enable-background|`enable-background: new`|❌ SVG-specific|❌ SVG-specific|❌ SVG filter property|
|kerning|`kerning: auto` (SVG text)|🔧 Default text kerning|✅ `.kerning()` modifier|⚠️ SVG vs general text kerning|
|glyph-orientation-horizontal|`glyph-orientation-horizontal: 0deg`|❌ SVG-specific|❌ SVG-specific|❌ SVG text orientation|
|glyph-orientation-vertical|`glyph-orientation-vertical: auto`|❌ SVG-specific|❌ SVG-specific|❌ SVG text orientation|
|alignment-baseline|`alignment-baseline: middle`|❌ SVG-specific|❌ SVG-specific|❌ SVG text baseline|
|baseline-shift|`baseline-shift: super`|🔧 Use `.baselineOffset()`|🔧 Use `.baselineOffset()`|🔧 Text baseline adjustment|
|dominant-baseline|`dominant-baseline: central`|❌ SVG-specific|❌ SVG-specific|❌ SVG text baseline|
|baseline-source|`baseline-source: first`|❌ Experimental CSS|❌ Experimental CSS|❌ Experimental baseline property|
|dominant-baseline-adjust|`dominant-baseline-adjust: central`|❌ Experimental CSS|❌ Experimental CSS|❌ Experimental CSS property|

---

## Print & Page Media

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|page|`page: chapter1`|❌ N/A - No print layout|❌ N/A - No print layout|❌ CSS paged media|
|page-break-before|`page-break-before: always`|❌ N/A - No print layout|❌ N/A - No print layout|❌ CSS paged media|
|page-break-after|`page-break-after: avoid`|❌ N/A - No print layout|❌ N/A - No print layout|❌ CSS paged media|
|page-break-inside|`page-break-inside: avoid`|❌ N/A - No print layout|❌ N/A - No print layout|❌ CSS paged media|
|break-before|`break-before: page`|❌ N/A - No fragmentation|❌ N/A - No fragmentation|❌ CSS fragmentation|
|break-after|`break-after: column`|❌ N/A - No fragmentation|❌ N/A - No fragmentation|❌ CSS fragmentation|
|break-inside|`break-inside: avoid`|❌ N/A - No fragmentation|❌ N/A - No fragmentation|❌ CSS fragmentation|
|orphans|`orphans: 3`|❌ N/A - No orphan control|❌ N/A - No orphan control|❌ CSS paged media|
|widows|`widows: 2`|❌ N/A - No widow control|❌ N/A - No widow control|❌ CSS paged media|
|bleed|`bleed: 10mm`|❌ N/A - No print layout|❌ N/A - No print layout|❌ CSS paged media|
|marks|`marks: crop cross`|❌ N/A - No print marks|❌ N/A - No print marks|❌ CSS paged media|
|size|`size: A4 portrait`|❌ N/A - No page sizing|❌ N/A - No page sizing|❌ CSS paged media|

---

## Miscellaneous & Experimental

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|all|`all: initial`|❌ No equivalent|❌ No equivalent|❌ CSS reset property|
|direction|`direction: rtl`|`LocalLayoutDirection.current = LayoutDirection.Rtl`|`.environment(\.layoutDirection, .rightToLeft)`|✅ RTL layout support|
|unicode-bidi|`unicode-bidi: embed`|❌ Automatic bidirectional text|❌ Automatic bidirectional text|❌ CSS-specific bidi control|
|writing-mode|`writing-mode: vertical-rl`|🔧 Use rotated layout|🔧 Rotate text/views|🔧 Limited vertical text support|
|float|`float: left`|❌ No float layout|❌ No float layout|🔧 Use Box with alignment instead|
|float-defer|`float-defer: last`|❌ Experimental CSS|❌ Experimental CSS|❌ Experimental CSS property|
|float-offset|`float-offset: 10px`|❌ Experimental CSS|❌ Experimental CSS|❌ Experimental CSS property|
|float-reference|`float-reference: page`|❌ Experimental CSS|❌ Experimental CSS|❌ Experimental CSS property|
|clear|`clear: both`|❌ No float layout|❌ No float layout|❌ CSS float clearing|
|resize|`resize: both`|❌ No user resize|❌ No user resize|❌ CSS resizable elements|
|appearance|`appearance: none`|❌ No native control styling reset|❌ No native control styling reset|⚠️ System controls styled by platform|
|appearance-variant|`appearance-variant: no-limit-label`|❌ Experimental CSS|❌ Experimental CSS|❌ Experimental CSS property|
|field-sizing|`field-sizing: content`|❌ Experimental CSS|❌ Experimental CSS|❌ Experimental CSS property|
|input-security|`input-security: auto`|🔧 TextField visualTransformation|`.textContentType(.password)`|🔧 Password input security|
|interpolate-size|`interpolate-size: allow-keywords`|❌ Experimental CSS|❌ Experimental CSS|❌ Experimental CSS property|
|reading-flow|`reading-flow: flex-visual`|❌ Experimental CSS|❌ Experimental CSS|❌ Experimental CSS property|
|overlay|`overlay: auto`|❌ Experimental CSS|❌ Experimental CSS|❌ Experimental CSS property|

---

## Shape & Clip Properties

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|shape-outside|`shape-outside: circle(50%)`|❌ No float-based wrapping|❌ No float-based wrapping|❌ CSS shape-based text wrapping|
|shape-margin|`shape-margin: 10px`|❌ No shape-based wrapping|❌ No shape-based wrapping|❌ CSS shape wrapping margin|
|shape-padding|`shape-padding: 10px`|❌ Experimental CSS|❌ Experimental CSS|❌ Experimental CSS property|
|shape-image-threshold|`shape-image-threshold: 0.5`|❌ No shape-based wrapping|❌ No shape-based wrapping|❌ CSS image-based wrapping|
|clip|`clip: rect(0px, 100px, 100px, 0px)`|🔧 Use `Modifier.clip()` with custom shape|`.clipShape()` with custom path|⚠️ Deprecated CSS - use clip-path|

---

## Image & Object Properties

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|object-fit|`object-fit: cover`|`contentScale = ContentScale.Crop` in Image|`.aspectRatio(contentMode: .fill)` on Image|✅ Image scaling modes|
|object-position|`object-position: center`|`alignment` parameter in Image|`alignment` parameter in Image|✅ Image positioning|
|image-rendering|`image-rendering: pixelated`|❌ No control over rendering|❌ No control over rendering|❌ CSS image scaling quality|
|image-rendering-quality|`image-rendering-quality: high`|❌ Experimental CSS|❌ Experimental CSS|❌ Experimental CSS property|
|image-resolution|`image-resolution: 300dpi`|❌ No DPI control|❌ No DPI control|❌ CSS paged media|
|image-orientation|`image-orientation: from-image`|🔧 Manual rotation|🔧 Manual rotation|🔧 Image EXIF orientation|

---

## Content & Generated Content

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|content|`content: "→"`|🔧 Use Text composable|🔧 Use Text view|🔧 Generated content via composables/views|
|quotes|`quotes: '"' '"'`|🔧 Manual quote characters|🔧 Manual quote characters|🔧 Custom quote styling|
|counter-increment|`counter-increment: section`|🔧 Manual counter state|🔧 Manual counter state|🔧 CSS counter system|
|counter-reset|`counter-reset: section 0`|🔧 Manual counter state|🔧 Manual counter state|🔧 CSS counter system|
|counter-set|`counter-set: section 5`|🔧 Manual counter state|🔧 Manual counter state|🔧 CSS counter system|

---

## Ruby & CJK Typography

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|ruby-align|`ruby-align: center`|❌ No ruby annotation support|❌ No ruby annotation support|❌ CSS ruby annotation|
|ruby-merge|`ruby-merge: auto`|❌ No ruby annotation support|❌ No ruby annotation support|❌ CSS ruby annotation|
|ruby-position|`ruby-position: over`|❌ No ruby annotation support|❌ No ruby annotation support|❌ CSS ruby annotation|

---

## Speech & Audio

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|azimuth|`azimuth: left-side`|❌ No CSS speech|❌ No CSS speech|❌ CSS2 aural stylesheet|
|cue|`cue: url('beep.wav')`|❌ No CSS speech|❌ No CSS speech|❌ CSS2 aural stylesheet|
|cue-before|`cue-before: url('start.wav')`|❌ No CSS speech|❌ No CSS speech|❌ CSS2 aural stylesheet|
|cue-after|`cue-after: url('end.wav')`|❌ No CSS speech|❌ No CSS speech|❌ CSS2 aural stylesheet|
|elevation|`elevation: above`|❌ No CSS speech|❌ No CSS speech|❌ CSS2 aural stylesheet|
|pause|`pause: 100ms`|❌ No CSS speech|❌ No CSS speech|❌ CSS2 aural stylesheet|
|pause-before|`pause-before: 50ms`|❌ No CSS speech|❌ No CSS speech|❌ CSS2 aural stylesheet|
|pause-after|`pause-after: 50ms`|❌ No CSS speech|❌ No CSS speech|❌ CSS2 aural stylesheet|
|pitch|`pitch: high`|❌ No CSS speech|❌ No CSS speech|❌ CSS2 aural stylesheet|
|pitch-range|`pitch-range: 50`|❌ No CSS speech|❌ No CSS speech|❌ CSS2 aural stylesheet|
|rest|`rest: 200ms`|❌ No CSS speech|❌ No CSS speech|❌ CSS speech module|
|rest-before|`rest-before: 100ms`|❌ No CSS speech|❌ No CSS speech|❌ CSS speech module|
|rest-after|`rest-after: 100ms`|❌ No CSS speech|❌ No CSS speech|❌ CSS speech module|
|richness|`richness: 80`|❌ No CSS speech|❌ No CSS speech|❌ CSS2 aural stylesheet|
|speak|`speak: normal`|❌ No CSS speech|❌ No CSS speech|❌ CSS2 aural stylesheet|
|speak-as|`speak-as: spell-out`|❌ No CSS speech|❌ No CSS speech|❌ CSS speech module|
|speech-rate|`speech-rate: fast`|❌ No CSS speech|❌ No CSS speech|❌ CSS2 aural stylesheet|
|stress|`stress: 50`|❌ No CSS speech|❌ No CSS speech|❌ CSS2 aural stylesheet|
|voice-balance|`voice-balance: left`|❌ No CSS speech|❌ No CSS speech|❌ CSS speech module|
|voice-duration|`voice-duration: 2s`|❌ No CSS speech|❌ No CSS speech|❌ CSS speech module|
|voice-family|`voice-family: male`|❌ No CSS speech|❌ No CSS speech|❌ CSS2 aural stylesheet|
|voice-pitch|`voice-pitch: medium`|❌ No CSS speech|❌ No CSS speech|❌ CSS speech module|
|voice-range|`voice-range: medium`|❌ No CSS speech|❌ No CSS speech|❌ CSS speech module|
|voice-rate|`voice-rate: normal`|❌ No CSS speech|❌ No CSS speech|❌ CSS speech module|
|voice-stress|`voice-stress: moderate`|❌ No CSS speech|❌ No CSS speech|❌ CSS speech module|
|voice-volume|`voice-volume: medium`|❌ No CSS speech|❌ No CSS speech|❌ CSS speech module|
|volume|`volume: loud`|❌ No CSS speech|❌ No CSS speech|❌ CSS2 aural stylesheet|

---

## Vendor Prefixes & Regions

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|webkit|`-webkit-*` properties|❌ Vendor-specific|❌ Vendor-specific|❌ WebKit vendor prefix|
|moz|`-moz-*` properties|❌ Vendor-specific|❌ Vendor-specific|❌ Mozilla vendor prefix|
|ms|`-ms-*` properties|❌ Vendor-specific|❌ Vendor-specific|❌ Microsoft vendor prefix|
|o|`-o-*` properties|❌ Vendor-specific|❌ Vendor-specific|❌ Opera vendor prefix|
|container|`container: sidebar / inline-size`|❌ No container queries|❌ No container queries|❌ CSS container queries|
|flow-from|`flow-from: article-flow`|❌ No CSS regions|❌ No CSS regions|❌ CSS regions (deprecated)|
|flow-into|`flow-into: article-flow`|❌ No CSS regions|❌ No CSS regions|❌ CSS regions (deprecated)|
|region-fragment|`region-fragment: break`|❌ No CSS regions|❌ No CSS regions|❌ CSS regions (deprecated)|
|continue|`continue: auto`|❌ Experimental CSS|❌ Experimental CSS|❌ Experimental fragmentation|
|copy-into|`copy-into: article`|❌ Experimental CSS|❌ Experimental CSS|❌ Experimental CSS property|
|running|`running: header`|❌ CSS paged media|❌ CSS paged media|❌ CSS generated content for paged media|
|string-set|`string-set: chapter content()`|❌ CSS paged media|❌ CSS paged media|❌ CSS generated content for paged media|
|bookmark-label|`bookmark-label: content()`|❌ CSS paged media|❌ CSS paged media|❌ CSS generated content for paged media|
|bookmark-level|`bookmark-level: 1`|❌ CSS paged media|❌ CSS paged media|❌ CSS generated content for paged media|
|bookmark-state|`bookmark-state: open`|❌ CSS paged media|❌ CSS paged media|❌ CSS generated content for paged media|
|bookmark-target|`bookmark-target: attr(href)`|❌ CSS paged media|❌ CSS paged media|❌ CSS generated content for paged media|
|footnote-display|`footnote-display: block`|❌ CSS paged media|❌ CSS paged media|❌ CSS generated content for paged media|
|footnote-policy|`footnote-policy: auto`|❌ CSS paged media|❌ CSS paged media|❌ CSS generated content for paged media|
|leader|`leader: dotted`|❌ CSS generated content|❌ CSS generated content|❌ CSS generated content for paged media|
|line-grid|`line-grid: create`|❌ CSS line grid|❌ CSS line grid|❌ CSS line grid module|
|line-snap|`line-snap: baseline`|❌ CSS line grid|❌ CSS line grid|❌ CSS line grid module|
|line-clamp|`line-clamp: 3`|`maxLines = 3` in Text|`.lineLimit(3)` on Text|✅ Line clamping support|
|max-lines|`max-lines: 3`|`maxLines = 3` in Text|`.lineLimit(3)` on Text|✅ Same as line-clamp|
|initial-letter|`initial-letter: 3`|❌ No drop caps|❌ No drop caps|🔧 CSS drop caps - custom implementation needed|
|initial-letter-align|`initial-letter-align: hanging`|❌ No drop caps|❌ No drop caps|🔧 CSS drop caps alignment|
|hyphenate-limit-chars|`hyphenate-limit-chars: 6 3 3`|❌ Limited hyphenation control|❌ Limited hyphenation control|❌ CSS hyphenation limits|
|hyphenate-limit-last|`hyphenate-limit-last: always`|❌ Limited hyphenation control|❌ Limited hyphenation control|❌ CSS hyphenation limits|
|hyphenate-limit-lines|`hyphenate-limit-lines: 2`|❌ Limited hyphenation control|❌ Limited hyphenation control|❌ CSS hyphenation limits|
|hyphenate-limit-zone|`hyphenate-limit-zone: 8%`|❌ Limited hyphenation control|❌ Limited hyphenation control|❌ CSS hyphenation limits|
|hyphenate-character|`hyphenate-character: "-"`|❌ No custom hyphen character|❌ No custom hyphen character|❌ CSS hyphenation character|
|nav-up|`nav-up: #button1`|❌ No spatial navigation|❌ No spatial navigation|❌ CSS spatial navigation|
|nav-down|`nav-down: #button2`|❌ No spatial navigation|❌ No spatial navigation|❌ CSS spatial navigation|
|nav-left|`nav-left: #button3`|❌ No spatial navigation|❌ No spatial navigation|❌ CSS spatial navigation|
|nav-right|`nav-right: #button4`|❌ No spatial navigation|❌ No spatial navigation|❌ CSS spatial navigation|
|wrap-flow|`wrap-flow: both`|❌ CSS exclusions|❌ CSS exclusions|❌ CSS exclusions module|
|wrap-through|`wrap-through: wrap`|❌ CSS exclusions|❌ CSS exclusions|❌ CSS exclusions module|
|wrap-before|`wrap-before: auto`|❌ Experimental CSS|❌ Experimental CSS|❌ Experimental CSS property|
|wrap-after|`wrap-after: avoid`|❌ Experimental CSS|❌ Experimental CSS|❌ Experimental CSS property|
|wrap-inside|`wrap-inside: auto`|❌ Experimental CSS|❌ Experimental CSS|❌ Experimental CSS property|
|math-style|`math-style: compact`|❌ CSS math|❌ CSS math|❌ CSS math module (MathML)|
|math-shift|`math-shift: normal`|❌ CSS math|❌ CSS math|❌ CSS math module|
|math-depth|`math-depth: 0`|❌ CSS math|❌ CSS math|❌ CSS math module|
|presentation-level|`presentation-level: 0`|❌ Experimental CSS|❌ Experimental CSS|❌ Experimental CSS property|
|line-height-step|`line-height-step: 20px`|❌ Experimental CSS|❌ Experimental CSS|❌ Experimental CSS property|
|vertical-align|`vertical-align: middle`|`Alignment.CenterVertically` in Row|`alignment: .center` in HStack|✅ Vertical alignment in containers|
|vertical-align-last|`vertical-align-last: bottom`|❌ Experimental CSS|❌ Experimental CSS|❌ Experimental CSS property|
|word-space-transform|`word-space-transform: auto`|❌ Experimental CSS|❌ Experimental CSS|❌ Experimental CSS property|
|paint-order|`paint-order: stroke fill`|❌ SVG-specific|❌ SVG-specific|❌ SVG paint ordering|