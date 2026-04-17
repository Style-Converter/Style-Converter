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
export const migratedProperties = new Set<string>([]);

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
