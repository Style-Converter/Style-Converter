## Input JSON Schema (Implemented)

This schema captures components with CSS-like properties, selectors, and media queries.

### Top-Level Shape (Actual Implementation)
```json
{
  "components": {
    "ComponentName": {
      "properties": { "property-name": "value" },
      "selectors": [
        {
          "when": ":hover",
          "properties": { "property-name": "value" }
        }
      ],
      "media": [
        {
          "query": "(min-width: 768px)",
          "properties": { "property-name": "value" }
        }
      ]
    }
  }
}
```

### Key Differences from Original Design
- Uses `"properties"` instead of `"styles"` (matches actual implementation in `CssParsing.kt`)
- Selector field name is `"when"` (parsed as `selector` in `CssSelector` model)
- No nested selectors in media queries (simplified for MVP)

### Parsing Implementation Details

#### Component Structure
- **components** (required): Map of component name → component definition
  - **ComponentName** (string key): Name of the style component
    - **properties** (optional): Map of CSS property → value
    - **selectors** (optional): Array of selector definitions
    - **media** (optional): Array of media query definitions

#### Selector Structure
```typescript
{
  "when": string,        // Pseudo-class/element selector
  "properties": { }      // CSS properties for this selector
}
```

**Supported selectors**: `:hover`, `:active`, `:focus`, `:disabled`

#### Media Query Structure
```typescript
{
  "query": string,       // Media query condition
  "properties": { }      // CSS properties for this media query
}
```

**Supported queries**: `(min-width: ...)`, `(max-width: ...)`, and other standard CSS media features

#### Property Values
- **String values**: All property values are strings (parsed by PropertyParser)
- **Numeric values**: Automatically converted to strings in JSON
- **Boolean values**: Automatically converted to strings
- **Shorthand properties**: Accepted (expansion planned but not yet implemented)
  - Example: `"margin": "10px 20px"`, `"padding": "16px"`
  - Currently stored as-is in IR; shorthand expansion coming in future update

### Full Working Example
```json
{
  "components": {
    "PrimaryButton": {
      "properties": {
        "background-color": "#6200EE",
        "opacity": 1,
        "border-top": "2px solid #000000",
        "border-right": "1px solid #FF0000",
        "border-bottom": "0px none #000000",
        "border-left": "3px solid #00FF00",
        "border-top-left-radius": "8px",
        "border-top-right-radius": "4px",
        "border-bottom-right-radius": "2px",
        "border-bottom-left-radius": "0px",
        "color": "#FFFFFF",
        "padding": "10px 16px",
        "font-weight": "600"
      },
      "selectors": [
        { "when": ":hover", "properties": { "opacity": 0.9 } },
        { "when": ":active", "properties": { "opacity": 0.8, "transform": "scale(0.98)" } }
      ],
      "media": [
        {
          "query": "(min-width: 768px)",
          "properties": { "border-left": "4px solid #00FF00" }
        }
      ]
    },
    "Card": {
      "properties": {
        "background-color": "#FFFFFF",
        "border": "1px solid #E0E0E0",
        "border-radius": "8px",
        "padding": "16px",
        "box-shadow": "0px 2px 4px rgba(0,0,0,0.1)"
      },
      "selectors": [
        { "when": ":hover", "properties": { "box-shadow": "0px 4px 8px rgba(0,0,0,0.15)" } }
      ]
    }
  }
}
```

### Property Value Parsing

The `GenericPropertyParser` detects and parses these value types:

#### Colors
- **Hex**: `#RGB`, `#RRGGBB`, `#RRGGBBAA`
- **RGB/RGBA**: `rgb(255, 0, 0)`, `rgba(255, 0, 0, 0.5)`
- **HSL/HSLA**: `hsl(180, 50%, 50%)`, `hsla(180, 50%, 50%, 0.8)`
- **Named colors**: `red`, `blue`, `transparent`, etc.

#### Lengths
- **Absolute**: `10px`, `5pt`, `2cm`, `3mm`, `1in`
- **Relative**: `2em`, `1.5rem`, `50%`, `10vh`, `20vw`
- **Zero**: `0` (unitless)

#### Keywords
- **Layout**: `flex`, `block`, `inline`, `none`, `auto`
- **Alignment**: `center`, `start`, `end`, `stretch`
- **Font**: `bold`, `italic`, `normal`
- **Border**: `solid`, `dashed`, `dotted`, `none`
- **Other**: `inherit`, `initial`, `unset`

#### Functions
- **Calculations**: `calc(100% - 20px)`
- **Variables**: `var(--primary-color)`
- **Transforms**: `scale(0.98)`, `rotate(45deg)`
- **Colors**: `rgb(...)`, `hsl(...)`, `color-mix(...)`
- **Gradients**: `linear-gradient(...)`, `radial-gradient(...)`

#### Shadows
- **Box Shadow**: `0px 2px 4px rgba(0,0,0,0.1)`
- **Text Shadow**: `1px 1px 2px black`
- **Multiple**: `0 2px 4px rgba(0,0,0,0.1), 0 4px 8px rgba(0,0,0,0.2)`
- **Inset**: `inset 0 1px 3px rgba(0,0,0,0.3)`

#### URLs
- **URL function**: `url('image.png')`, `url("path/to/file.jpg")`

### Parsing Process
1. **JSON Input** → `JsonObject` (kotlinx.serialization)
2. **JsonInputToCssComponents** → `CssComponents` model
3. **PropertiesParser.parse** → For each property:
   - Look up parser via `PropertyParserRegistry.find(propertyName)`
   - Currently uses `GenericPropertyParser` for all properties
   - Parser detects value types using primitive parsers
   - Returns `IRProperty` with classified values
4. **Selector/Media Parsing** → `IRSelector`, `IRMedia`
5. **Final Output** → `IRDocument` with all components

### Validation and Error Handling (Future)
- Schema validation (currently permissive)
- Unknown property warnings
- Invalid value detection
- Lossy conversion warnings
- Diagnostics output

### Future Enhancements
- Optional `variants` for semantic states (success, error, warning)
- Optional `extends` for component composition
- Top-level `globals` for CSS variables/design tokens
- Nested selectors in media queries
- Animation and transition definitions


