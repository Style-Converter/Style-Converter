# Cross-Platform Style Properties Reference - Part 5

## Layout - Flexbox

|Property|CSS|Jetpack Compose|SwiftUI|Notes|
|---|---|---|---|---|
|display: flex|`display: flex`|`Row { }` or `Column { }`|`HStack { }` or `VStack { }`|✅ Equivalent through layout composables/views|
|flex-direction|`flex-direction: row`|Row (horizontal) or Column (vertical)|HStack or VStack|✅ Choose container type instead of property|
|flex-direction: row|`flex-direction: row`|`Row { }`|`HStack { }`|✅ Horizontal layout|
|flex-direction: column|`flex-direction: column`|`Column { }`|`VStack { }`|✅ Vertical layout|
|flex-direction: row-reverse|`flex-direction: row-reverse`|`Row(horizontalArrangement = Arrangement.End, reverseLayout = true)`|`HStack { }.environment(\.layoutDirection, .rightToLeft)`|🔧 Different approach|
|flex-direction: column-reverse|`flex-direction: column-reverse`|`Column(verticalArrangement = Arrangement.Bottom, reverseLayout = true)`|`VStack { }.rotationEffect(.degrees(180))`|🔧 Different approach|
|flex-flow|`flex-flow: row wrap`|🔧 Use `FlowRow` for row wrapping|🔧 Combine HStack direction with custom wrap|🔧 Shorthand for flex-direction + flex-wrap|
|justify-content|`justify-content: space-between`|`horizontalArrangement = Arrangement.SpaceBetween` (in Row)|`HStack(spacing: 0) { Spacer() between items }`|✅ Compose has direct equivalent, SwiftUI uses Spacer pattern|
|justify-content: flex-start|`justify-content: flex-start`|`horizontalArrangement = Arrangement.Start`|`HStack(alignment: .leading)`|✅ Start alignment|
|justify-content: center|`justify-content: center`|`horizontalArrangement = Arrangement.Center`|`HStack { Spacer(); content; Spacer() }`|✅ Center alignment|
|justify-content: flex-end|`justify-content: flex-end`|`horizontalArrangement = Arrangement.End`|`HStack(alignment: .trailing)`|✅ End alignment|
|justify-content: space-around|`justify-content: space-around`|`horizontalArrangement = Arrangement.SpaceAround`|🔧 Manual Spacer placement|✅ Compose direct, SwiftUI manual|
|justify-content: space-evenly|`justify-content: space-evenly`|`horizontalArrangement = Arrangement.SpaceEvenly`|🔧 Manual Spacer placement|✅ Compose direct, SwiftUI manual|
|align-items|`align-items: center`|`verticalAlignment = Alignment.CenterVertically` (in Row)|`HStack(alignment: .center)`|✅ Direct equivalent in all|
|align-items: flex-start|`align-items: flex-start`|`verticalAlignment = Alignment.Top`|`HStack(alignment: .top)`|✅ Top alignment|
|align-items: flex-end|`align-items: flex-end`|`verticalAlignment = Alignment.Bottom`|`HStack(alignment: .bottom)`|✅ Bottom alignment|
|align-items: stretch|`align-items: stretch`|`Modifier.fillMaxHeight()` on children|`.frame(maxHeight: .infinity)` on children|✅ Fill cross-axis|
|align-items: baseline|`align-items: baseline`|`verticalAlignment = Alignment.CenterVertically` (limited)|`HStack(alignment: .firstTextBaseline)`|⚠️ SwiftUI has text baseline, Compose limited|
|align-content|`align-content: space-between`|🔧 Use FlowRow/FlowColumn with arrangement|🔧 Custom layout|⚠️ Limited multi-line flex support|
|align-self|`align-self: flex-end`|`Modifier.align(Alignment.End)` on child|`.alignmentGuide(.trailing)` on child|✅ Applied to child instead of parent|
|flex-wrap|`flex-wrap: wrap`|`FlowRow { }` or `FlowColumn { }`|🔧 Custom layout or LazyVStack with manual wrapping|⚠️ Compose has FlowRow (experimental), SwiftUI needs custom solution|
|flex-wrap: nowrap|`flex-wrap: nowrap`|`Row { }` or `Column { }` (default)|`HStack { }` or `VStack { }` (default)|✅ Default behavior|
|flex-wrap: wrap-reverse|`flex-wrap: wrap-reverse`|🔧 FlowRow with reverse layout|🔧 Custom layout|🔧 Limited support|
|flex-grow|`flex-grow: 1`|`Modifier.weight(1f)`|`.frame(maxWidth: .infinity)`|✅ Equivalent concepts with different syntax|
|flex-shrink|`flex-shrink: 0`|⚠️ Use `Modifier.width()` for fixed size|⚠️ Use `.frame(width:)` for fixed size|⚠️ No direct equivalent - control via sizing instead|
|flex-basis|`flex-basis: 100px`|⚠️ Set initial size with `Modifier.width()`|⚠️ Set initial size with `.frame(width:)`|⚠️ No direct equivalent - use sizing modifiers|
|flex (shorthand)|`flex: 1 1 auto`|`Modifier.weight(1f)` (growth only)|`.frame(maxWidth: .infinity)`|⚠️ Only flex-grow equivalent|
|gap (flex gap)|`gap: 10px`|`Arrangement.spacedBy(10.dp)` in Row/Column|`HStack(spacing: 10)` or `VStack(spacing: 10)`|✅ Direct equivalent in all|
|order|`order: 2`|🔧 Reorder children manually in composition|🔧 Use conditional logic to reorder views|🔧 No declarative order property|
|place-content|`place-content: center`|`Arrangement.Center` + `Alignment.Center`|`.frame(maxWidth: .infinity, alignment: .center)`|✅ Combine justify/align|
|place-items|`place-items: center`|`Alignment.Center` in parent|`alignment: .center` parameter|✅ Combine justify/align for items|
|place-self|`place-self: center`|`Modifier.align(Alignment.Center)`|`.frame(maxWidth: .infinity, alignment: .center)`|✅ Self-alignment|