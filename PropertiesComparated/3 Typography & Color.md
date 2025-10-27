# Cross-Platform Style Properties Reference - Part 3

## Typography

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|font|`font: bold 16px Arial`|🔧 Combine multiple TextStyle properties|🔧 Combine multiple font modifiers|🔧 Shorthand - set family, size, weight, style together|
|font-family|`font-family: 'Arial'`|`fontFamily = FontFamily(Font(R.font.arial))`|`.font(.custom('Arial', size: 16))`|✅ All support custom fonts|
|font-size|`font-size: 16px`|`fontSize = 16.sp`|`.font(.system(size: 16))`|✅ Direct equivalent in all|
|font-weight|`font-weight: bold`|`fontWeight = FontWeight.Bold`|`.fontWeight(.bold)`|✅ Direct equivalent in all|
|font-style|`font-style: italic`|`fontStyle = FontStyle.Italic`|`.italic()`|✅ Direct equivalent in all|
|font-display|`font-display: swap`|❌ N/A - Not applicable to mobile|❌ N/A - Not applicable to mobile|❌ CSS web font loading property|
|font-feature-settings|`font-feature-settings: "liga" 1`|`fontFeatureSettings = "liga"`|🔧 Use AttributedString with font features|✅ OpenType feature access|
|font-kerning|`font-kerning: normal`|🔧 Default behavior|✅ `.kerning()` modifier|⚠️ SwiftUI has explicit control|
|font-language-override|`font-language-override: "ENG"`|❌ No direct control|❌ No direct control|❌ CSS-specific OpenType feature|
|font-max-size|`font-max-size: 24px`|❌ No equivalent|❌ No equivalent|❌ Experimental CSS property|
|font-min-size|`font-min-size: 12px`|❌ No equivalent|❌ No equivalent|❌ Experimental CSS property|
|font-named-instance|`font-named-instance: "Bold Italic"`|🔧 Use variable font instance|🔧 Use variable font instance|🔧 Variable font support|
|font-palette|`font-palette: --custom`|❌ No support|❌ No support|❌ CSS color font palettes|
|font-size-adjust|`font-size-adjust: 0.5`|❌ No equivalent|❌ No equivalent|❌ CSS-specific font sizing|
|font-stretch|`font-stretch: expanded`|`FontVariation.Settings(FontVariation.width(150f))`|`.fontWidth(.expanded)` (iOS 16+)|✅ Variable font support|
|font-variant|`font-variant: small-caps`|`fontFeatureSettings = "smcp"`|`.lowercaseSmallCaps()` or `.uppercaseSmallCaps()`|⚠️ Limited OpenType feature access|
|font-variant-caps|`font-variant-caps: small-caps`|`fontFeatureSettings = "smcp"`|`.lowercaseSmallCaps()` or `.uppercaseSmallCaps()`|✅ SwiftUI has better support|
|font-variant-numeric|`font-variant-numeric: tabular-nums`|`fontFeatureSettings = "tnum"`|`.monospacedDigit()`|✅ SwiftUI has convenience modifiers|
|font-variant-alternates|`font-variant-alternates: swash(fancy)`|🔧 Use `fontFeatureSettings`|🔧 Use AttributedString with features|🔧 OpenType alternates access|
|font-variant-east-asian|`font-variant-east-asian: ruby`|🔧 Use `fontFeatureSettings` with CJK features|🔧 Use AttributedString|🔧 CJK typography features|
|font-variant-emoji|`font-variant-emoji: emoji`|❌ No explicit control|❌ No explicit control|❌ CSS-specific emoji rendering|
|font-variant-ligatures|`font-variant-ligatures: common-ligatures`|🔧 Use `fontFeatureSettings = "liga"`|🔧 Use AttributedString with ligatures|🔧 Ligature control|
|font-variant-position|`font-variant-position: super`|🔧 Use baseline shift or superscript|🔧 Use `.baselineOffset()`|🔧 Subscript/superscript positioning|
|font-variation-settings|`font-variation-settings: 'wght' 700`|`FontVariation.Settings(FontVariation.weight(700f))`|🔧 Use variable font settings|✅ Variable font axis control|
|font-optical-sizing|`font-optical-sizing: auto`|🔧 Use variable fonts with optical size axis|✅ Automatic in system fonts|⚠️ Limited explicit control|
|font-synthesis|`font-synthesis: none`|❌ No explicit control|❌ No explicit control|❌ CSS-specific|
|font-synthesis-position|`font-synthesis-position: none`|❌ No explicit control|❌ No explicit control|❌ CSS-specific synthesis control|
|font-synthesis-small-caps|`font-synthesis-small-caps: none`|❌ No explicit control|❌ No explicit control|❌ CSS-specific synthesis control|
|font-synthesis-style|`font-synthesis-style: none`|❌ No explicit control|❌ No explicit control|❌ CSS-specific synthesis control|
|font-synthesis-weight|`font-synthesis-weight: none`|❌ No explicit control|❌ No explicit control|❌ CSS-specific synthesis control|
|text-align|`text-align: center`|`textAlign = TextAlign.Center`|`.multilineTextAlignment(.center)`|✅ Direct equivalent in all|
|text-align-all|`text-align-all: justify`|❌ No equivalent|❌ No equivalent|❌ CSS-specific (forces alignment)|
|text-align-last|`text-align-last: right`|❌ No control over last line|❌ No control over last line|❌ CSS-specific|
|text-anchor|`text-anchor: middle`|❌ SVG-specific|❌ SVG-specific|❌ SVG text positioning|
|text-autospace|`text-autospace: ideograph-numeric`|❌ No equivalent|❌ No equivalent|❌ CSS-specific CJK spacing|
|text-box-edge|`text-box-edge: cap alphabetic`|❌ No equivalent|❌ No equivalent|❌ Experimental CSS property|
|text-box-trim|`text-box-trim: both`|❌ No equivalent|❌ No equivalent|❌ Experimental CSS property|
|text-combine-upright|`text-combine-upright: all`|❌ No support|❌ No support|❌ CSS vertical text property|
|text-decoration|`text-decoration: underline`|`textDecoration = TextDecoration.Underline`|`.underline()`|✅ Direct equivalent in all|
|text-decoration-line|`text-decoration-line: underline`|`textDecoration = TextDecoration.Underline`|`.underline()`|✅ Direct equivalent in all|
|text-decoration-color|`text-decoration-color: red`|`TextDecoration.Underline` (limited control)|🔧 Custom AttributedString or overlay|⚠️ Limited control in Compose|
|text-decoration-style|`text-decoration-style: wavy`|❌ Only solid underlines|🔧 Custom drawing with Canvas|⚠️ Very limited support|
|text-decoration-skip|`text-decoration-skip: ink`|❌ No control|❌ No control|❌ CSS-specific decoration rendering|
|text-decoration-skip-ink|`text-decoration-skip-ink: auto`|❌ No control|❌ No control|❌ CSS-specific ink skipping|
|text-decoration-thickness|`text-decoration-thickness: 2px`|❌ No control|🔧 Custom AttributedString|⚠️ Very limited support|
|text-emphasis-color|`text-emphasis-color: red`|❌ No support|❌ No support|❌ CSS-specific (CJK emphasis marks)|
|text-emphasis-position|`text-emphasis-position: over right`|❌ No support|❌ No support|❌ CSS-specific (CJK emphasis marks)|
|text-emphasis-style|`text-emphasis-style: filled circle`|❌ No support|❌ No support|❌ CSS-specific (CJK emphasis marks)|
|text-group-align|`text-group-align: start`|❌ No equivalent|❌ No equivalent|❌ Experimental CSS property|
|text-rendering|`text-rendering: optimizeLegibility`|❌ No explicit control|❌ No explicit control|❌ CSS rendering hint|
|text-size-adjust|`text-size-adjust: 100%`|❌ N/A - Not applicable|❌ N/A - Not applicable|❌ CSS mobile browser zoom control|
|text-space-collapse|`text-space-collapse: collapse`|❌ No explicit control|❌ No explicit control|❌ Experimental CSS property|
|text-space-trim|`text-space-trim: trim-inner`|❌ No equivalent|❌ No equivalent|❌ Experimental CSS property|
|text-spacing-trim|`text-spacing-trim: space-all`|❌ No equivalent|❌ No equivalent|❌ Experimental CSS property|
|text-wrap|`text-wrap: balance`|🔧 Default wrapping behavior|🔧 Default wrapping behavior|⚠️ CSS has more wrap modes|
|text-wrap-mode|`text-wrap-mode: wrap`|🔧 Use `softWrap`|🔧 Control wrapping|⚠️ Basic wrap control|
|text-wrap-style|`text-wrap-style: balance`|❌ No balance mode|❌ No balance mode|❌ CSS-specific text balancing|
|text-transform|`text-transform: uppercase`|🔧 `text.uppercase()` on the string|🔧 `.textCase(.uppercase)`|🔧 Handled differently - string manipulation vs style property|
|text-indent|`text-indent: 20px`|🔧 Use leading padding or custom text style|🔧 Use leading padding or custom layout|🔧 No direct equivalent|
|text-overflow|`text-overflow: ellipsis`|`overflow = TextOverflow.Ellipsis`|`.lineLimit(1).truncationMode(.tail)`|✅ Equivalent with different approach|
|text-underline-offset|`text-underline-offset: 5px`|❌ No control|🔧 Custom AttributedString|⚠️ Very limited support|
|text-underline-position|`text-underline-position: under`|❌ No control|❌ No control|❌ CSS-specific|
|text-justify|`text-justify: inter-word`|❌ No control over justification method|❌ No control over justification method|❌ System-controlled|
|text-emphasis|`text-emphasis: filled circle`|❌ No support|❌ No support|❌ CSS-specific (CJK typography)|
|text-orientation|`text-orientation: upright`|`Modifier.graphicsLayer { rotationZ = 90f }`|`.rotationEffect(.degrees(90))`|🔧 Use rotation for vertical text|
|line-height|`line-height: 1.5`|`lineHeight = 1.5.em`|`.lineSpacing(value)`|⚠️ Different units - SwiftUI uses spacing between lines, not total height|
|line-break|`line-break: strict`|`lineBreak = LineBreak.Paragraph`|❌ Limited control|⚠️ Some control in Compose|
|letter-spacing|`letter-spacing: 2px`|`letterSpacing = 2.sp`|`.kerning(2)`|✅ Direct equivalent (SwiftUI uses kerning term)|
|word-spacing|`word-spacing: 5px`|❌ No native support|❌ No native support|🔧 Would require custom text layout|
|white-space|`white-space: nowrap`|`softWrap = false`|`.lineLimit(1)`|✅ Equivalent with different property names|
|word-wrap / word-break|`word-break: break-all`|🔧 Default behavior, limited control|🔧 Default behavior, limited control|⚠️ Less granular control in mobile frameworks|
|hyphens|`hyphens: auto`|`hyphenation = true` (Android 14+)|❌ No direct control|⚠️ Very limited support|
|hanging-punctuation|`hanging-punctuation: first`|❌ No support|❌ No support|❌ CSS-specific typography feature|
|quotes|`quotes: '"' '"' "'" "'"`|❌ No CSS-style quote control|❌ No CSS-style quote control|❌ CSS-specific|
|tab-size|`tab-size: 4`|❌ No control|❌ No control|❌ CSS-specific|
|caret-color|`caret-color: red`|`TextField(cursorBrush = SolidColor(Color.Red))`|`.tint(.red)` affects cursor|✅ Cursor color control|

---

## Color

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|color (text color)|`color: red`|`color = Color.Red`|`.foregroundColor(.red)`|✅ Direct equivalent in all|
|RGB / RGBA|`color: rgba(255, 0, 0, 0.5)`|`Color(0xFF, 0x00, 0x00, 0x80)`|`Color(red: 1, green: 0, blue: 0, opacity: 0.5)`|✅ All support with different syntax|
|HSL / HSLA|`color: hsla(120, 100%, 50%, 0.5)`|🔧 Use `Color.hsv()` - HSV not HSL|🔧 Use `Color(hue:saturation:brightness:opacity:)` - HSB not HSL|⚠️ HSV/HSB instead of HSL in mobile|
|Hex colors|`color: #FF0000`|`Color(0xFFFF0000)`|🔧 `Color(hex: 0xFF0000)` with extension|✅ Compose native, SwiftUI needs extension|
|currentColor|`border-color: currentColor`|🔧 Pass `LocalContentColor.current`|🔧 Use `.foregroundColor()` inheritance|🔧 Manual color inheritance|
|color-mix()|`color: color-mix(in srgb, red 50%, blue)`|🔧 `Color.Red.copy(alpha = 0.5f).compositeOver(Color.Blue)`|🔧 Custom color blending function|🔧 Requires manual implementation|
|color-scheme|`color-scheme: light dark`|`isSystemInDarkTheme()`|`@Environment(\.colorScheme)`|✅ Dark mode detection|
|accent-color|`accent-color: blue`|`Modifier.indication(...)` or theme colors|`.tint(.blue)` (iOS 15+)|✅ Accent color control|
|color-adjust|`color-adjust: economy`|❌ No equivalent|❌ No equivalent|❌ CSS printing hint|
|color-interpolation|`color-interpolation: linearRGB`|❌ SVG-specific|❌ SVG-specific|❌ SVG color interpolation|
|color-interpolation-filters|`color-interpolation-filters: sRGB`|❌ SVG-specific|❌ SVG-specific|❌ SVG filter color space|
|color-rendering|`color-rendering: optimizeQuality`|❌ SVG-specific|❌ SVG-specific|❌ SVG rendering hint|
|forced-color-adjust|`forced-color-adjust: none`|❌ No equivalent|❌ No equivalent|❌ CSS high-contrast mode control|
|print-color-adjust|`print-color-adjust: exact`|❌ No equivalent|❌ No equivalent|❌ CSS printing color control|