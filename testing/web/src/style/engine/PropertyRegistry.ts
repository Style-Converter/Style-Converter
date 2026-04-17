/**
 * Maps IRProperty.type → a typed piece of the resulting CSSStyles.
 *
 * Phase 0 scaffold: all properties still flow through the legacy
 * `StyleBuilder.ts` monolith; this registry exists so future phases
 * can migrate properties one at a time without forking the dispatch
 * path.
 *
 * See `CLAUDE.md` → *Per-property contract* for the migration contract.
 */

/**
 * Property-type names that have been migrated out of `StyleBuilder.ts`
 * and into dedicated `{Property}Extractor.ts` files under
 * `engine/{category}/`. Empty in Phase 0; filled by later phases.
 */
export const migratedProperties = new Set<string>([
  // Spacing — Phase 2 migration. See engine/spacing/*.
  'PaddingTop', 'PaddingRight', 'PaddingBottom', 'PaddingLeft',
  'PaddingBlockStart', 'PaddingBlockEnd', 'PaddingInlineStart', 'PaddingInlineEnd',
  'MarginTop', 'MarginRight', 'MarginBottom', 'MarginLeft',
  'MarginBlockStart', 'MarginBlockEnd', 'MarginInlineStart', 'MarginInlineEnd',
  'Gap', 'RowGap', 'ColumnGap',
  'MarginTrim',
  // Sizing — Phase 3 migration. See engine/sizing/*.
  'Width', 'Height', 'MinWidth', 'MaxWidth', 'MinHeight', 'MaxHeight',
  'AspectRatio',
  'BlockSize', 'InlineSize',
  'MinBlockSize', 'MaxBlockSize', 'MinInlineSize', 'MaxInlineSize',
  // Colors + background — Phase 4 migration. See engine/color/*, engine/background/*,
  // engine/effects/blend/*, engine/performance/*.
  'BackgroundColor', 'Color', 'Opacity', 'AccentColor', 'CaretColor',
  'BackgroundImage', 'BackgroundSize',
  'BackgroundPosition', 'BackgroundPositionX', 'BackgroundPositionY',
  'BackgroundRepeat', 'BackgroundClip', 'BackgroundOrigin', 'BackgroundAttachment',
  'MixBlendMode', 'BackgroundBlendMode', 'Isolation',
  // Borders — Phase 5 migration. See engine/borders/* and engine/effects/shadow/BoxShadow*.
  // Sides (24): width/style/color for 4 physical + 4 logical edges.
  'BorderTopWidth', 'BorderRightWidth', 'BorderBottomWidth', 'BorderLeftWidth',
  'BorderBlockStartWidth', 'BorderBlockEndWidth', 'BorderInlineStartWidth', 'BorderInlineEndWidth',
  'BorderTopStyle', 'BorderRightStyle', 'BorderBottomStyle', 'BorderLeftStyle',
  'BorderBlockStartStyle', 'BorderBlockEndStyle', 'BorderInlineStartStyle', 'BorderInlineEndStyle',
  'BorderTopColor', 'BorderRightColor', 'BorderBottomColor', 'BorderLeftColor',
  'BorderBlockStartColor', 'BorderBlockEndColor', 'BorderInlineStartColor', 'BorderInlineEndColor',
  // Radius (8): 4 physical corners + 4 logical corners.
  'BorderTopLeftRadius', 'BorderTopRightRadius', 'BorderBottomRightRadius', 'BorderBottomLeftRadius',
  'BorderStartStartRadius', 'BorderStartEndRadius', 'BorderEndStartRadius', 'BorderEndEndRadius',
  // Image (5): Source / Slice / Width / Outset / Repeat.
  'BorderImageSource', 'BorderImageSlice', 'BorderImageWidth', 'BorderImageOutset', 'BorderImageRepeat',
  // Outline (4): Width / Style / Color / Offset.
  'OutlineWidth', 'OutlineStyle', 'OutlineColor', 'OutlineOffset',
  // Shadow (1): BoxShadow mirrored under engine/effects/shadow/.
  'BoxShadow',
  // Misc (3): BoxDecorationBreak / CornerShape / BorderBoundary.
  'BoxDecorationBreak', 'CornerShape', 'BorderBoundary',
]);

/**
 * Returns true when the given IR property type is still served by the
 * legacy `StyleBuilder`. Used by the renderer during transition.
 */
export function isLegacyProperty(propertyType: string): boolean {
  // Negate membership: anything not yet migrated stays on the legacy path.
  return !migratedProperties.has(propertyType);
}

/** Count of migrated properties — exposed for the coverage report. */
export const migratedCount: number = migratedProperties.size;
