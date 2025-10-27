# Code Generator Output Examples

This document shows example output from the new CSS-to-Compose code generator with ContentScope support.

## Example 1: Flexbox Container (ROW_SCOPE)

### Input CSS:
```css
.flex-container {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
}
```

### Generated Compose Code:
```kotlin
@Composable
fun FlexContainer(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp, Arrangement.SpaceBetween),
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}
```

### Key Features:
- **ContentScope**: `RowScope` - Children have access to `Modifier.weight()`, `Modifier.alignByBaseline()`
- **Gap handling**: Automatic `Arrangement.spacedBy(16.dp)`
- **Children support**: Accepts any number of children via `content` parameter

### Usage Example:
```kotlin
FlexContainer {
    Box(modifier = Modifier.weight(1f)) { Text("Item 1") }
    Box(modifier = Modifier.weight(2f)) { Text("Item 2") }  // 2x wider
    Box(modifier = Modifier.weight(1f)) { Text("Item 3") }
}
```

---

## Example 2: Positioned Box (BOX_SCOPE)

### Input CSS:
```css
.positioned {
    position: absolute;
    top: 20px;
    left: 20px;
}
```

### Generated Compose Code:
```kotlin
@Composable
fun PositionedBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = modifier
                .align(Alignment.TopStart)
                .offset(x = 20.dp, y = 20.dp)
        ) {
            content()
        }
    }
}
```

### Key Features:
- **ContentScope**: `BoxScope` - Children have access to `Modifier.align()`
- **Two-layer structure**: Outer box creates positioning context, inner box positions element
- **Absolute positioning**: Uses `align()` + `offset()` combination

### Usage Example:
```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    PositionedBox {
        Text("Absolutely positioned!")
    }

    // Other children can also use BoxScope.align()
    Box(modifier = Modifier.align(Alignment.BottomEnd)) {
        Text("Bottom right")
    }
}
```

---

## Example 3: Scrollable Column (COLUMN_SCOPE)

### Input CSS:
```css
.scrollable {
    display: flex;
    flex-direction: column;
    overflow-y: scroll;
}
```

### Generated Compose Code:
```kotlin
@Composable
fun ScrollableContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        content()
    }
}
```

### Key Features:
- **ContentScope**: `ColumnScope` - Children have access to `Modifier.weight()`, `Modifier.alignByBaseline()`
- **Automatic scrolling**: Only scrolls when content exceeds height
- **Vertical layout**: Column arranges children vertically

### Usage Example:
```kotlin
ScrollableContainer(modifier = Modifier.height(300.dp)) {
    repeat(50) { i ->
        Text("Item $i", modifier = Modifier.padding(8.dp))
    }
    // Automatically scrollable when content > 300.dp
}
```

---

## Example 4: Complex - Position + Flex + Scroll

### Input CSS:
```css
.complex {
    position: absolute;
    top: 50px;
    left: 50px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    overflow-y: scroll;
}
```

### Generated Compose Code:
```kotlin
@Composable
fun ComplexContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .align(Alignment.TopStart)
                .offset(x = 50.dp, y = 50.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}
```

### Key Features:
- **Three concerns handled**: Positioning + Flexbox + Scroll
- **Correct scope**: `ColumnScope` (innermost wrapper determines scope)
- **Proper hierarchy**: Box (position context) → Column (scroll + flex)

### Usage Example:
```kotlin
ComplexContainer(modifier = Modifier.size(400.dp, 600.dp)) {
    repeat(100) { i ->
        Text(
            "Centered item $i",
            modifier = Modifier.weight(1f)  // ColumnScope capability
        )
    }
}
```

---

## Example 5: FlowRow with Wrapping

### Input CSS:
```css
.tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}
```

### Generated Compose Code:
```kotlin
@Composable
fun Tags(
    modifier: Modifier = Modifier,
    content: @Composable FlowRowScope.() -> Unit = {}
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}
```

### Key Features:
- **ContentScope**: `FlowRowScope` - Children automatically wrap to next line
- **Gap between items**: Automatic spacing
- **Dynamic layout**: Adapts to available width

### Usage Example:
```kotlin
Tags {
    listOf("Kotlin", "Compose", "Android", "UI", "Declarative", "Modern").forEach { tag ->
        Chip(text = tag)  // Auto-wraps when row is full
    }
}
```

---

## ContentScope Cheat Sheet

| CSS Pattern | Wrapper | ContentScope | Children Can Use |
|-------------|---------|--------------|------------------|
| `display: flex; flex-direction: row` | `Row` | `ROW_SCOPE` | `weight()`, `alignByBaseline()` |
| `display: flex; flex-direction: column` | `Column` | `COLUMN_SCOPE` | `weight()`, `alignByBaseline()` |
| `display: flex; flex-wrap: wrap` (row) | `FlowRow` | `FLOW_ROW_SCOPE` | Auto-wrapping |
| `display: flex; flex-wrap: wrap` (column) | `FlowColumn` | `FLOW_COLUMN_SCOPE` | Auto-wrapping |
| `position: absolute/fixed` | `Box` | `BOX_SCOPE` | `align()` |
| `overflow: scroll` (vertical) | `Column` + scroll | `COLUMN_SCOPE` | `weight()` + scrolling |
| `overflow: scroll` (horizontal) | `Row` + scroll | `ROW_SCOPE` | `weight()` + scrolling |

---

## Benefits of ContentScope System

### 1. Children Know Their Capabilities
```kotlin
FlexContainer {
    // Children know they're in RowScope
    Box(modifier = Modifier.weight(1f)) { ... }  // ✅ Works!
}

Box {
    // Children know they're in BoxScope
    Text(modifier = Modifier.align(Alignment.Center))  // ✅ Works!
}
```

### 2. Type Safety
```kotlin
FlexContainer {
    Box(modifier = Modifier.align(Alignment.Center))  // ❌ Compile error!
    // align() not available in RowScope
}
```

### 3. No Knowledge of Children Required
```kotlin
// Generator doesn't need to know:
// - If children exist
// - How many children
// - What type of children

FlexContainer {
    // Can be empty
}

FlexContainer {
    Text("One child")
}

FlexContainer {
    repeat(100) { Text("Many children") }
}
```

### 4. CSS Behavior Preserved
```kotlin
// CSS: gap: 16px applies between children
FlexContainer {
    Box { ... }  // 16dp gap
    Box { ... }  // 16dp gap
    Box { ... }  // No gap after last
}

// CSS: position: absolute creates positioning context
PositionedBox {
    // Children can position themselves absolutely within this context
    Box(modifier = Modifier.align(Alignment.BottomEnd)) { ... }
}
```

---

## Implementation Status

✅ **Completed:**
- PropertyAnalyzer - Detects CSS patterns
- ComponentStructureBuilder - Builds component structure with ContentScope
- CodeGenerator - Generates code with proper scoped content parameters

🚧 **Next Steps:**
- Integrate with SimpleComposeBuilder
- Generate real components from CSS input
- Add modifier chain support
- Test with complex nested examples

---

**Generated:** 2025-10-15
**Architecture:** PropertyAnalyzer → ComponentStructureBuilder → CodeGenerator
