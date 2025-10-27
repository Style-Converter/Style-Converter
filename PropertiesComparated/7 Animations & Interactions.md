# Cross-Platform Style Properties Reference - Part 7

## Animation & Transition

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|transition|`transition: all 0.3s ease`|🔧 `animate*AsState()` for individual properties|`.animation(.easeInOut(duration: 0.3))`|⚠️ Different paradigm - Compose animates per-property, SwiftUI per-view|
|transition-property|`transition-property: opacity, transform`|🔧 Use specific `animate*AsState` for each property|⚠️ Animates all animatable properties by default|⚠️ More granular control in Compose|
|transition-duration|`transition-duration: 0.5s`|`animationSpec = tween(durationMillis = 500)`|`.animation(.easeInOut(duration: 0.5))`|✅ Duration control in all|
|transition-timing-function|`transition-timing-function: cubic-bezier(...)`|`animationSpec = tween(easing = FastOutSlowInEasing)`|`.animation(.timingCurve())`|✅ Custom easing curves supported|
|transition-delay|`transition-delay: 0.2s`|`animationSpec = tween(delayMillis = 200)`|`.animation(.easeInOut.delay(0.2))`|✅ Delay supported in all|
|animation|`animation: fadeIn 1s ease-in-out`|🔧 Use updateTransition with animate* calls|🔧 Use withAnimation|🔧 More programmatic approach|
|animation-name|`animation-name: fadeIn`|🔧 Define animation in updateTransition|🔧 Use named animation functions|🔧 Different paradigm|
|animation-duration|`animation-duration: 1s`|`animationSpec = tween(durationMillis = 1000)`|`.animation(.easeInOut(duration: 1))`|✅ Duration control|
|animation-timing-function|`animation-timing-function: ease-in-out`|`animationSpec = tween(easing = FastOutSlowInEasing)`|`.animation(.easeInOut)`|✅ Timing function support|
|animation-delay|`animation-delay: 0.5s`|`animationSpec = tween(delayMillis = 500)`|`.animation(.easeInOut.delay(0.5))`|✅ Delay support|
|animation-iteration-count|`animation-iteration-count: infinite`|`infiniteRepeatable()` animation spec|`.animation(.easeInOut.repeatForever())`|✅ Infinite animation support|
|animation-direction|`animation-direction: reverse`|`repeatMode = RepeatMode.Reverse`|`.animation().repeatForever(autoreverses: true)`|✅ Reverse animation support|
|animation-fill-mode|`animation-fill-mode: forwards`|🔧 Set final value after animation|🔧 Use `withAnimation` with value retention|🔧 Manage state explicitly|
|animation-play-state|`animation-play-state: paused`|🔧 Use animatable state control|🔧 Conditional animation application|🔧 Programmatic control|
|animation-composition|`animation-composition: add`|❌ No equivalent|❌ No equivalent|❌ Experimental CSS property for combining animations|
|animation-range|`animation-range: entry 0% exit 100%`|❌ No equivalent|❌ No equivalent|❌ CSS scroll-driven animations|
|animation-range-start|`animation-range-start: entry 0%`|❌ No equivalent|❌ No equivalent|❌ CSS scroll-driven animations|
|animation-range-end|`animation-range-end: exit 100%`|❌ No equivalent|❌ No equivalent|❌ CSS scroll-driven animations|
|animation-timeline|`animation-timeline: scroll()`|❌ No scroll-driven animations|❌ No scroll-driven animations|❌ CSS scroll-driven animations|
|@keyframes|`@keyframes slide { 0% {...} 100% {...} }`|🔧 `updateTransition` with multiple animate* calls|🔧 `withAnimation` with sequential changes|🔧 Programmatic keyframe equivalent|
|transition-behavior|`transition-behavior: allow-discrete`|❌ No equivalent|❌ No equivalent|❌ Experimental CSS property|
|view-timeline|`view-timeline: --reveal block`|❌ No equivalent|❌ No equivalent|❌ CSS scroll-driven animations|
|view-timeline-axis|`view-timeline-axis: block`|❌ No equivalent|❌ No equivalent|❌ CSS scroll-driven animations|
|view-timeline-inset|`view-timeline-inset: 10px`|❌ No equivalent|❌ No equivalent|❌ CSS scroll-driven animations|
|view-timeline-name|`view-timeline-name: --reveal`|❌ No equivalent|❌ No equivalent|❌ CSS scroll-driven animations|
|view-transition-name|`view-transition-name: slide`|❌ No equivalent|❌ No equivalent|❌ CSS view transitions API|

---

## Interactions & States

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|:hover|`:hover { background: red }`|🔧 Use `Modifier.hoverable()` (desktop only) or clickable indication|🔧 `.onHover { }` for macOS/iPadOS|⚠️ Mobile doesn't have hover - use pressed state|
|:active|`:active { background: blue }`|🔧 Use interactionSource in clickable to track pressed state|🔧 Use @FocusState or button pressed appearance|🔧 Track interaction states programmatically|
|:focus|`:focus { outline: 2px solid blue }`|🔧 Use FocusRequester and onFocusChanged|🔧 Use @FocusState|🔧 Manual focus state management|
|:focus-visible|`:focus-visible { outline: 2px solid blue }`|🔧 Use FocusRequester and onFocusChanged|🔧 Use @FocusState with focused(_:equals:)|🔧 Manual focus state management|
|:disabled|`:disabled { opacity: 0.5 }`|🔧 Pass `enabled = false` to clickable/Button|`.disabled(true)` modifier|✅ Native disabled state support|
|:checked|`:checked { background: blue }`|Checkbox/Switch state|Toggle state|✅ Component state handling|
|::selection|`::selection { background: yellow }`|❌ No customization of selection appearance|❌ No customization of selection appearance|❌ System-controlled selection styling|
|::placeholder|`::placeholder { color: gray }`|TextField placeholder parameter styling|`.placeholder` modifier or prompt in TextField|✅ Can style placeholder text|
|touch-action|`touch-action: none`|🔧 Use pointerInput with custom gesture handling|🔧 Use `.gesture()` with custom gestures|🔧 Gesture handling controlled programmatically|
|touch-action: pan-x|`touch-action: pan-x`|`Modifier.pointerInput { detectHorizontalDragGestures {} }`|`.gesture(DragGesture())` with horizontal constraint|🔧 Custom gesture handling|
|touch-action: pan-y|`touch-action: pan-y`|`Modifier.pointerInput { detectVerticalDragGestures {} }`|`.gesture(DragGesture())` with vertical constraint|🔧 Custom gesture handling|
|touch-action: pinch-zoom|`touch-action: pinch-zoom`|`Modifier.pointerInput { detectTransformGestures {} }`|`.gesture(MagnificationGesture())`|🔧 Custom gesture handling|
|touch-action-delay|`touch-action-delay: none`|❌ No equivalent|❌ No equivalent|❌ Experimental CSS property|
|user-select|`user-select: none`|🔧 Text selection controlled by TextField/SelectionContainer|`.textSelection(.disabled)` on Text (iOS 15+)|⚠️ Limited to text selection contexts|
|user-select: text|`user-select: text`|`SelectionContainer { Text() }`|`.textSelection(.enabled)` (iOS 15+)|✅ Text selection control|
|pointer-events|`pointer-events: none`|`Modifier.clickable(enabled = false)` or no interaction modifiers|`.allowsHitTesting(false)`|✅ Control touch/click interactivity|
|cursor|`cursor: pointer`|`Modifier.pointerInput { }` or clickable|🔧 `.onHover` for macOS/iPad|⚠️ Mobile doesn't have cursor - use touch feedback instead|