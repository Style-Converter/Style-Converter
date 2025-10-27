# Cross-Platform Style Properties Reference - Part 4

## Transforms

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|transform|`transform: translate(10px, 20px) scale(1.5) rotate(45deg)`|Multiple modifiers chained|Multiple modifiers chained|✅ Chain multiple transforms|
|transform: translate|`transform: translate(10px, 20px)`|`Modifier.offset(x = 10.dp, y = 20.dp)`|`.offset(x: 10, y: 20)`|✅ Direct equivalent in all|
|translate (property)|`translate: 10px 20px`|`Modifier.offset(x = 10.dp, y = 20.dp)`|`.offset(x: 10, y: 20)`|✅ Direct equivalent|
|transform: translateX|`transform: translateX(10px)`|`Modifier.offset(x = 10.dp)`|`.offset(x: 10, y: 0)`|✅ Direct equivalent|
|transform: translateY|`transform: translateY(20px)`|`Modifier.offset(y = 20.dp)`|`.offset(x: 0, y: 20)`|✅ Direct equivalent|
|transform: translateZ|`transform: translateZ(100px)`|`Modifier.graphicsLayer { translationZ = 100.dp.toPx() }`|🔧 Use `.zIndex()` for layering|⚠️ Different 3D model|
|transform: scale|`transform: scale(1.5)`|`Modifier.scale(1.5f)`|`.scaleEffect(1.5)`|✅ Direct equivalent in all|
|scale (property)|`scale: 1.5`|`Modifier.scale(1.5f)`|`.scaleEffect(1.5)`|✅ Direct equivalent|
|transform: scaleX|`transform: scaleX(1.5)`|`Modifier.graphicsLayer { scaleX = 1.5f }`|`.scaleEffect(x: 1.5, y: 1)`|✅ Separate axis scaling|
|transform: scaleY|`transform: scaleY(0.5)`|`Modifier.graphicsLayer { scaleY = 0.5f }`|`.scaleEffect(x: 1, y: 0.5)`|✅ Separate axis scaling|
|transform: rotate|`transform: rotate(45deg)`|`Modifier.rotate(45f)`|`.rotationEffect(.degrees(45))`|✅ Direct equivalent in all|
|rotate (property)|`rotate: 45deg`|`Modifier.rotate(45f)`|`.rotationEffect(.degrees(45))`|✅ Direct equivalent|
|transform: rotateX|`transform: rotateX(45deg)`|`Modifier.graphicsLayer { rotationX = 45f }`|`.rotation3DEffect(.degrees(45), axis: (x: 1, y: 0, z: 0))`|✅ 3D rotation support|
|transform: rotateY|`transform: rotateY(45deg)`|`Modifier.graphicsLayer { rotationY = 45f }`|`.rotation3DEffect(.degrees(45), axis: (x: 0, y: 1, z: 0))`|✅ 3D rotation support|
|transform: rotateZ|`transform: rotateZ(45deg)`|`Modifier.graphicsLayer { rotationZ = 45f }`|`.rotationEffect(.degrees(45))`|✅ Standard rotation|
|transform: skew|`transform: skewX(10deg)`|🔧 Use `Modifier.graphicsLayer { rotationX/Y/Z }`|🔧 Use `.rotation3DEffect` for perspective skew|🔧 No direct 2D skew - use 3D transforms|
|transform: skewX|`transform: skewX(10deg)`|🔧 Use `Modifier.graphicsLayer`|🔧 Use `.rotation3DEffect`|🔧 No direct 2D skew|
|transform: skewY|`transform: skewY(10deg)`|🔧 Use `Modifier.graphicsLayer`|🔧 Use `.rotation3DEffect`|🔧 No direct 2D skew|
|transform-origin|`transform-origin: top left`|`Modifier.scale(transformOrigin = TransformOrigin(0f, 0f))`|`.scaleEffect(anchor: .topLeading)`|✅ Supported via transform parameters|
|transform: matrix|`transform: matrix(1, 0, 0, 1, 0, 0)`|`Modifier.graphicsLayer { transformOrigin/camera distance }`|🔧 Use `.transformEffect()` with CGAffineTransform|🔧 Advanced - requires platform-specific matrix operations|
|transform: perspective|`transform: perspective(1000px)`|`Modifier.graphicsLayer { cameraDistance = 8f }`|🔧 Use `.rotation3DEffect` with anchor|🔧 Different units and approach|
|transform-style|`transform-style: preserve-3d`|🔧 Use nested graphicsLayer transforms|🔧 Nested views with 3D transforms|🔧 Limited 3D hierarchy support|
|perspective|`perspective: 1000px`|`Modifier.graphicsLayer { cameraDistance = 8f }`|🔧 Use `.rotation3DEffect` anchor and perspective|🔧 3D perspective control available|
|perspective-origin|`perspective-origin: 50% 50%`|`Modifier.graphicsLayer { transformOrigin = TransformOrigin(0.5f, 0.5f) }`|🔧 Use anchor in `.rotation3DEffect`|🔧 Control 3D perspective origin|
|backface-visibility|`backface-visibility: hidden`|🔧 Use `Modifier.graphicsLayer { cameraDistance = ... }`|`.rotation3DEffect` with perspective|🔧 3D transform controls available|
|transform-box|`transform-box: border-box`|❌ No equivalent|❌ No equivalent|❌ CSS-specific transform reference box|

---

## Motion Path / Offset

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|offset|`offset: path('M 0 0 L 100 100') 50%`|❌ No native motion path|❌ No native motion path|🔧 Use animation with custom path|
|offset-anchor|`offset-anchor: center`|❌ No equivalent|❌ No equivalent|🔧 CSS motion path anchor|
|offset-distance|`offset-distance: 50%`|🔧 Animate along custom path|🔧 Use `.offset` with calculated positions|🔧 Manual path interpolation|
|offset-path|`offset-path: path('M 0 0 L 100 100')`|🔧 Use `Animatable` with custom path|🔧 Use `Path` with animation|🔧 Custom path animation|
|offset-position|`offset-position: 50% 50%`|❌ No equivalent|❌ No equivalent|❌ CSS-specific offset positioning|
|offset-rotate|`offset-rotate: auto`|🔧 Calculate rotation from path tangent|🔧 Calculate rotation during animation|🔧 Automatic rotation along path|