/**
 * Core IR (Intermediate Representation) types for SDUI.
 *
 * These types mirror the Kotlin IR models from the main project,
 * providing a unified format for CSS property data that can be
 * rendered on any platform.
 */

/**
 * Root document containing all components.
 */
export interface IRDocument {
  components: IRComponent[];
}

/**
 * A single UI component with its styles.
 */
export interface IRComponent {
  /** Unique identifier for SDUI (e.g., "button-001") */
  id: string;
  /** Component type/class name (e.g., "Button", "Card") */
  name: string;
  /** List of CSS properties as IR */
  properties: IRProperty[];
  /** State-based styles (hover, focus, etc.) */
  selectors: IRSelector[];
  /** Responsive breakpoint styles */
  media: IRMedia[];
  /** Nested child components for containers */
  children: IRComponent[] | null;
}

/**
 * A CSS property in IR format.
 *
 * Uses generic JsonElement (any) for data to handle all 446+ property types flexibly.
 * Specific property handling is done in StyleApplier.
 */
export interface IRProperty {
  type: string;
  data: unknown;
}

/**
 * Pseudo-class selector styles (e.g., :hover, :focus).
 */
export interface IRSelector {
  condition: string;
  properties: IRProperty[];
}

/**
 * Media query styles (e.g., min-width: 768px).
 */
export interface IRMedia {
  query: string;
  properties: IRProperty[];
}
