# Cross-Platform Style Properties Reference - Part 8

## Blend Modes

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|mix-blend-mode: normal|`mix-blend-mode: normal`|`Modifier.graphicsLayer { blendMode = BlendMode.SrcOver }`|`.blendMode(.normal)`|✅ Normal blending|
|mix-blend-mode: multiply|`mix-blend-mode: multiply`|`Modifier.graphicsLayer { blendMode = BlendMode.Multiply }`|`.blendMode(.multiply)`|✅ Direct equivalent|
|mix-blend-mode: screen|`mix-blend-mode: screen`|`Modifier.graphicsLayer { blendMode = BlendMode.Screen }`|`.blendMode(.screen)`|✅ Direct equivalent|
|mix-blend-mode: overlay|`mix-blend-mode: overlay`|`Modifier.graphicsLayer { blendMode = BlendMode.Overlay }`|`.blendMode(.overlay)`|✅ Direct equivalent|
|mix-blend-mode: darken|`mix-blend-mode: darken`|`Modifier.graphicsLayer { blendMode = BlendMode.Darken }`|`.blendMode(.darken)`|✅ Direct equivalent|
|mix-blend-mode: lighten|`mix-blend-mode: lighten`|`Modifier.graphicsLayer { blendMode = BlendMode.Lighten }`|`.blendMode(.lighten)`|✅ Direct equivalent|
|mix-blend-mode: color-dodge|`mix-blend-mode: color-dodge`|`Modifier.graphicsLayer { blendMode = BlendMode.ColorDodge }`|`.blendMode(.colorDodge)`|✅ Direct equivalent|
|mix-blend-mode: color-burn|`mix-blend-mode: color-burn`|`Modifier.graphicsLayer { blendMode = BlendMode.ColorBurn }`|`.blendMode(.colorBurn)`|✅ Direct equivalent|
|mix-blend-mode: hard-light|`mix-blend-mode: hard-light`|`Modifier.graphicsLayer { blendMode = BlendMode.Hardlight }`|`.blendMode(.hardLight)`|✅ Direct equivalent|
|mix-blend-mode: soft-light|`mix-blend-mode: soft-light`|`Modifier.graphicsLayer { blendMode = BlendMode.Softlight }`|`.blendMode(.softLight)`|✅ Direct equivalent|
|mix-blend-mode: difference|`mix-blend-mode: difference`|`Modifier.graphicsLayer { blendMode = BlendMode.Difference }`|`.blendMode(.difference)`|✅ Direct equivalent|
|mix-blend-mode: exclusion|`mix-blend-mode: exclusion`|`Modifier.graphicsLayer { blendMode = BlendMode.Exclusion }`|`.blendMode(.exclusion)`|✅ Direct equivalent|
|mix-blend-mode: hue|`mix-blend-mode: hue`|`Modifier.graphicsLayer { blendMode = BlendMode.Hue }`|`.blendMode(.hue)`|✅ Direct equivalent|
|mix-blend-mode: saturation|`mix-blend-mode: saturation`|`Modifier.graphicsLayer { blendMode = BlendMode.Saturation }`|`.blendMode(.saturation)`|✅ Direct equivalent|
|mix-blend-mode: color|`mix-blend-mode: color`|`Modifier.graphicsLayer { blendMode = BlendMode.Color }`|`.blendMode(.color)`|✅ Direct equivalent|
|mix-blend-mode: luminosity|`mix-blend-mode: luminosity`|`Modifier.graphicsLayer { blendMode = BlendMode.Luminosity }`|`.blendMode(.luminosity)`|✅ Direct equivalent|
|mix-blend-mode: plus-darker|`mix-blend-mode: plus-darker`|`Modifier.graphicsLayer { blendMode = BlendMode.Plus }`|`.blendMode(.plusDarker)`|✅ Plus/darker blending|
|mix-blend-mode: plus-lighter|`mix-blend-mode: plus-lighter`|`Modifier.graphicsLayer { blendMode = BlendMode.Plus }`|`.blendMode(.plusLighter)`|✅ Plus/lighter blending|
|background-blend-mode|`background-blend-mode: multiply`|🔧 Use `Modifier.drawBehind` with blend modes|`.blendMode(.multiply)` on background|✅ Blend modes on backgrounds|
|isolation|`isolation: isolate`|🔧 Use separate composable layers|🔧 Use separate view layers|🔧 Create stacking context for blend modes|
