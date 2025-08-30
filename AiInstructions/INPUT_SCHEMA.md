## Input JSON Schema (Draft)

This schema captures components with style maps, optional selectors, and media queries.

### Top-Level Shape
```json
{
  "components": {
    "ComponentName": {
      "styles": { "property": "value" },
      "selectors": [
        {
          "when": ":hover", 
          "styles": { "property": "value" }
        }
      ],
      "media": [
        {
          "query": "(min-width: 768px)",
          "styles": { "property": "value" },
          "selectors": [ { "when": ":active", "styles": { } } ]
        }
      ]
    }
  }
}
```

### Notes
- `styles`: accepts shorthands (e.g., `margin`, `padding`, `border`, `box-shadow`, `font`)
- `selectors.when`: supports pseudo-classes/pseudo-elements (subset initially)
- `media.query`: standard CSS media conditions (subset initially)
- Future: `extends` for composition, `variants`/`states` for semantic states

### Minimal Example
```json
{
  "components": {
    "PrimaryButton": {
      "styles": {
        "background-color": "#6200EE",
        "color": "white",
        "padding": "12px 16px",
        "border-radius": "8px",
        "box-shadow": "0 2px 8px rgba(0,0,0,0.2)"
      },
      "selectors": [
        {
          "when": ":hover",
          "styles": { "box-shadow": "0 4px 10px rgba(0,0,0,0.25)" }
        }
      ],
      "media": [
        {
          "query": "(min-width: 768px)",
          "styles": { "padding": "14px 20px" }
        }
      ]
    }
  }
}
```

### Parsing Rules (MVP)
- Expand shorthands to longhands during normalization
- Media queries limited to width/height breakpoints initially
- Pseudo-classes supported initially: `:hover`, `:active`, `:disabled`, `:focus`
- Unsupported constructs must be surfaced in diagnostics


