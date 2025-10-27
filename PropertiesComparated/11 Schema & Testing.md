# Cross-Platform Style Properties Reference - Part 11

## Canonical Schema Proposal

```json
{
  "version": "1.0",
  "properties": {
    "dimensions": {
      "width": {"value": 100, "unit": "dp|px|%", "min": 50, "max": 500},
      "height": {"value": 100, "unit": "dp|px|%", "min": 50, "max": 500},
      "aspectRatio": "16/9"
    },
    "spacing": {
      "padding": {"all": 10, "top": 15, "end": 10, "bottom": 15, "start": 10},
      "margin": {"all": 10, "auto": false},
      "gap": {"row": 10, "column": 15}
    },
    "border": {
      "uniform": {
        "width": 1,
        "color": "#000000",
        "style": "solid|dashed|dotted",
        "radius": {"all": 8, "topStart": 8, "topEnd": 8}
      },
      "individual": {
        "top": {"width": 2, "color": "#FF0000"},
        "end": {"width": 1, "color": "#0000FF"}
      }
    },
    "background": {
      "type": "color|gradient|image",
      "color": "#FFFFFF",
      "gradient": {
        "type": "linear|radial|conic",
        "colors": ["#FF0000", "#0000FF"],
        "angle": 90
      }
    },
    "typography": {
      "family": "Arial",
      "size": 16,
      "weight": "normal|bold|100-900",
      "align": "left|center|right",
      "decoration": "none|underline|line-through"
    },
    "layout": {
      "type": "flex|grid|absolute",
      "flex": {
        "direction": "row|column",
        "justify": "start|center|end|space-between",
        "align": "start|center|end|stretch",
        "gap": 10
      }
    },
    "transform": {
      "translate": {"x": 10, "y": 20},
      "scale": 1.5,
      "rotate": 45
    },
    "effects": {
      "opacity": 0.5,
      "shadow": {"x": 2, "y": 4, "blur": 8, "color": "rgba(0,0,0,0.3)"},
      "blur": 5
    },
    "animation": {
      "duration": 300,
      "timingFunction": "ease-in-out",
      "delay": 0
    },
    "accessibility": {
      "role": "button|link|heading",
      "label": "Close button",
      "hidden": false
    }
  }
}
```

---

## Testing Matrix

When implementing your SDUI parser, test these scenarios:

|Scenario|CSS|Compose|SwiftUI|Expected Behavior|
|---|---|---|---|---|
|Uniform border|✅ Direct|✅ Direct|✅ Direct|Identical rendering|
|Individual border colors|✅ Direct|🔧 Custom draw|🔧 Overlay|Visual match, code differs|
|Simple 3-column grid|✅ Grid|🔧 Row with weights|🔧 LazyVGrid|Layout matches|
|Grid with spanning|✅ Grid|❌ Manual compose|⚠️ iOS 16+ Grid|May differ|
|Grid template areas|✅ Grid|❌ Manual nest|❌ Manual nest|Requires decomposition|
|Margin spacing|✅ Margin|🔧 Parent spacing|🔧 Parent spacing|Layout matches|
|Position absolute|✅ Absolute|🔧 Box + offset|🔧 ZStack + offset|Position matches|
|Position sticky|✅ Sticky|❌ Custom scroll|❌ Custom scroll|Not supported mobile|
|Flexbox space-between|✅ Direct|✅ Arrangement|🔧 Spacer pattern|Layout matches|
|Flex wrap|✅ Direct|⚠️ FlowRow (exp)|🔧 Custom|May differ|
|Multiple shadows|✅ Direct|❌ Limited|🔧 Stacking|May differ visually|
|Backdrop blur|✅ Direct|❌ Not supported|✅ Material|SwiftUI only|
|Conic gradient|✅ Direct|🔧 Custom shader|✅ AngularGradient|SwiftUI simpler|
|Advanced filters|✅ Direct|🔧 ColorMatrix|✅ Modifiers|SwiftUI simpler|
|Dark mode|✅ Media query|✅ isSystemInDarkTheme|✅ @Environment|All detect correctly|
|Hover state|✅ :hover|⚠️ Desktop only|⚠️ macOS/iPad only|Mobile: use press|
|Text shadows|✅ Direct|✅ Direct|✅ Direct|All supported|
|3D transforms|✅ Direct|✅ graphicsLayer|✅ rotation3DEffect|All supported|
|Blend modes|✅ Direct|✅ graphicsLayer|✅ .blendMode|All supported|
|Masks|✅ Direct|🔧 drawWithContent|✅ .mask|SwiftUI simpler|
|Clip paths|✅ Direct|✅ .clip|✅ .clipShape|All supported|

---

## Migration Path

For existing CSS to SDUI conversion:

### Phase 1: Audit & Analyze
1. **Scan your CSS** - Use tools to extract all properties used
2. **Categorize by support level** - Use this reference table
3. **Identify dependencies** - Note which properties depend on others
4. **Document usage patterns** - Which combinations are most common
5. **Assess complexity** - Rate each property usage as simple, medium, or complex

### Phase 2: Plan & Design
1. **Design SDUI schema** - Based on the canonical schema proposal
2. **Plan adaptations** - For Tier 2 properties (workarounds needed)
3. **Document limitations** - For Tier 3 properties (not supported)
4. **Create mapping tables** - CSS property → SDUI property → Platform code
5. **Define fallback strategies** - What happens when property not supported

### Phase 3: Build Infrastructure
1. **Build parser** - CSS or JSON to SDUI schema
2. **Create code generators** - SDUI schema to platform-specific code
3. **Implement adapters** - For complex properties requiring custom code
4. **Add validation** - Ensure SDUI schema is valid and complete
5. **Create preview tools** - Visual preview of SDUI on each platform

### Phase 4: Test & Validate
1. **Unit tests** - Each property conversion individually
2. **Integration tests** - Property combinations and interactions
3. **Visual regression tests** - Ensure pixel-perfect (or acceptable) rendering
4. **Performance tests** - Ensure generated code is performant
5. **Edge case tests** - Test boundary conditions and unusual values

### Phase 5: Deploy & Monitor
1. **Gradual rollout** - Start with simple components
2. **Monitor rendering** - Track visual differences across platforms
3. **Gather feedback** - From developers using the system
4. **Iterate and improve** - Based on real-world usage
5. **Document learnings** - Build knowledge base of patterns and solutions