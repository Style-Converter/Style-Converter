# Cross-Platform Style Properties Reference - Part 12

## Best Practices

### For SDUI Schema Design

1. **Use semantic properties** - Not just CSS property names
2. **Platform-agnostic defaults** - Choose defaults that work everywhere
3. **Explicit over implicit** - Don't rely on platform defaults
4. **Validate early** - Catch invalid property combinations at parse time
5. **Version your schema** - Allow for evolution and backwards compatibility

### For Code Generation

1. **Generate readable code** - Developers should understand generated code
2. **Add comments** - Explain complex workarounds
3. **Optimize for performance** - Use platform-specific optimizations
4. **Handle errors gracefully** - Fallbacks for unsupported properties
5. **Support hot reload** - For rapid development iteration

### For Property Mapping

1. **Document all mappings** - Keep this reference updated
2. **Test on real devices** - Not just simulators/emulators
3. **Consider RTL support** - Use start/end instead of left/right
4. **Respect platform conventions** - Don't fight the platform
5. **Measure performance impact** - Complex workarounds can be slow

### For Teams Using SDUI

1. **Establish design system** - Limited, well-supported property set
2. **Create component library** - Pre-built, tested components
3. **Provide design tools** - Help designers understand constraints
4. **Train developers** - On SDUI patterns and limitations
5. **Monitor runtime performance** - SDUI can impact app performance

---

## Summary Statistics

### By Support Level

|Support Level|Count|Percentage|
|---|---|---|
|✅ Direct Support|~95|~35%|
|🔧 Workaround Available|~120|~45%|
|⚠️ Limited/Different|~35|~13%|
|❌ Not Supported|~20|~7%|

**Total Properties Documented:** ~270

---

## Platform-Specific Notes

### Jetpack Compose Limitations

1. **No individual border colors/styles** - Always requires custom drawing with `drawBehind`
2. **No CSS Grid** - Decompose to Row/Column or use LazyVerticalGrid/LazyHorizontalGrid
3. **Z-index by declaration order** - Last child is on top (no explicit z-index property)
4. **Elevation instead of box-shadow** - Material Design approach with `shadow()` modifier
5. **No native margin** - Use parent spacing with `Arrangement.spacedBy()`
6. **Limited backdrop filters** - No native blur-behind support
7. **FlowRow experimental** - Flex-wrap functionality is in experimental API
8. **ColorMatrix for filters** - More complex filter API compared to SwiftUI
9. **No position: sticky** - Requires custom scroll behavior with LazyColumn
10. **Hex colors native** - But need to use 0xAARRGGBB format (alpha first)

### SwiftUI Limitations

1. **Limited grid spanning** - Grid (iOS 16+) has basic column/row span support
2. **No native hex colors** - Need extension: `Color(hex: 0xFF0000)`
3. **line-height vs lineSpacing** - Different calculation (spacing between lines, not total height)
4. **Version-gated features** - Many features require iOS 15+, iOS 16+, or iOS 17+
5. **No native margin** - Use parent spacing
6. **Backdrop blur limited** - Material backgrounds only (ultraThinMaterial, etc.)
7. **No FlowLayout** - Need custom layout or manual wrapping for flex-wrap
8. **Filter semantics differ** - brightness/contrast are additive, not multiplicative like CSS
9. **No position: sticky** - Requires custom scroll behavior
10. **Grid iOS 16+ only** - Older versions need LazyVGrid or custom solutions

### CSS Advantages

1. **Full border control per side** - Individual colors, styles, and widths
2. **CSS Grid with template areas** - Named grid areas and complex layouts
3. **position: sticky** - Built-in sticky positioning
4. **Multiple shadows** - Can stack multiple box-shadow values
5. **Pseudo-elements** - ::before, ::after for decorative content
6. **float/clear layout** - Legacy but powerful layout options
7. **Rich media queries** - Extensive capability detection
8. **Print stylesheets** - Dedicated print layout control
9. **Shape wrapping** - Text wrapping around shapes with shape-outside
10. **Extensive filter support** - More filter types and better stacking

---

## Frequently Asked Questions

### Q: Can I achieve 100% visual parity across all platforms?

**A:** No, but you can get very close. Aim for 90-95% parity. Some properties (like position: sticky, multiple shadows, individual border sides) require workarounds or have no mobile equivalent.

### Q: Should I support all CSS properties in my SDUI parser?

**A:** No. Focus on the ~35% with direct support and the ~45% with reasonable workarounds. Document limitations for the rest.

### Q: How do I handle responsive design in SDUI?

**A:** Use breakpoints in your schema that map to:
- **Compose:** `BoxWithConstraints` or `WindowSizeClass`
- **SwiftUI:** `GeometryReader` or `@Environment(\.horizontalSizeClass)`

### Q: What about dark mode support?

**A:** All three platforms support dark mode:
- **CSS:** `@media (prefers-color-scheme: dark)`
- **Compose:** `isSystemInDarkTheme()`
- **SwiftUI:** `@Environment(\.colorScheme)`

### Q: How do I handle platform-specific features?

**A:** Include optional platform-specific properties in your schema:
```json
{
  "common": { /* cross-platform properties */ },
  "compose": { /* Compose-specific */ },
  "swiftui": { /* SwiftUI-specific */ }
}
```

---

## Conclusion

This comprehensive reference provides a foundation for building robust SDUI parsers that support CSS, Jetpack Compose, and SwiftUI. Key takeaways:

- **~35% direct support** - Many properties map cleanly across platforms
- **~45% workarounds available** - Most features can be achieved with custom code
- **~13% limited/different** - Some properties have platform-specific semantics
- **~7% not supported** - A small set of CSS-specific features have no mobile equivalent

### Success Factors

1. **Set realistic expectations** - Not all CSS properties can be perfectly replicated
2. **Design for constraints** - Build your design system around well-supported properties
3. **Test extensively** - Visual parity requires rigorous testing
4. **Iterate continuously** - Learn from real-world usage and improve
5. **Document everything** - Help future maintainers understand decisions

### Future Directions

1. **CSS Grid improvements** - As mobile frameworks evolve
2. **Better filter support** - More native filter operations
3. **Enhanced animations** - Richer animation primitives
4. **Performance optimizations** - Faster code generation and execution
5. **AI-assisted conversion** - Automated CSS to SDUI transformation

### Common Pitfalls to Avoid

1. **Over-relying on workarounds** - Use native solutions when available
2. **Ignoring platform conventions** - Respect Material Design and Human Interface Guidelines
3. **Neglecting accessibility** - Ensure ARIA mappings are correct
4. **Poor error handling** - Always have fallbacks for unsupported properties
5. **Inadequate testing** - Test on real devices, not just emulators

---

## Quick Reference Card

### Most Important Mappings

**Layout:**
- `display: flex` → `Row{}`/`Column{}` → `HStack{}`/`VStack{}`
- `position: absolute` → `Box` with alignment → `ZStack` with alignment
- CSS Grid → LazyVerticalGrid → LazyVGrid

**Spacing:**
- `padding` → `Modifier.padding()` → `.padding()`
- `margin` → Parent spacing → Parent spacing
- `gap` → `Arrangement.spacedBy()` → `spacing:` parameter

**Sizing:**
- `width`/`height` → `Modifier.width()`/`.height()` → `.frame(width:height:)`
- `flex-grow` → `Modifier.weight()` → `.frame(maxWidth: .infinity)`

**Styling:**
- `background-color` → `Modifier.background()` → `.background()`
- `border` → `Modifier.border()` → `.border()`
- `border-radius` → `Modifier.clip()` → `.cornerRadius()`

**Typography:**
- `font-size` → `fontSize = 16.sp` → `.font(.system(size: 16))`
- `text-align` → `textAlign = TextAlign.Center` → `.multilineTextAlignment()`
- `color` → `color = Color.Red` → `.foregroundColor()`

**Effects:**
- `opacity` → `Modifier.alpha()` → `.opacity()`
- `box-shadow` → `Modifier.shadow()` → `.shadow()`
- `filter: blur` → `Modifier.blur()` → `.blur()`

**Transforms:**
- `transform: translate` → `Modifier.offset()` → `.offset()`
- `transform: scale` → `Modifier.scale()` → `.scaleEffect()`
- `transform: rotate` → `Modifier.rotate()` → `.rotationEffect()`

---

## Resources for Further Learning

**Jetpack Compose:**
- [Official Documentation](https://developer.android.com/jetpack/compose)
- [Modifier Documentation](https://developer.android.com/jetpack/compose/modifiers)
- [Layout Documentation](https://developer.android.com/jetpack/compose/layouts)

**SwiftUI:**
- [Official Documentation](https://developer.apple.com/documentation/swiftui)
- [Layout Protocol](https://developer.apple.com/documentation/swiftui/layout)
- [View Modifiers](https://developer.apple.com/documentation/swiftui/view-modifiers)

**CSS:**
- [MDN Web Docs](https://developer.mozilla.org/en-US/docs/Web/CSS)
- [CSS Specifications](https://www.w3.org/Style/CSS/)
- [Can I Use](https://caniuse.com/) - for browser support

---

**Good luck with your SDUI parser! 🚀**

---

**Document Version:** 1.0  
**Last Updated:** 2024  
**Total Properties:** ~270  
**Total Files:** 12