// ZoomApplier.ts — emits { zoom }.  MDN: zoom.
import type { CSSProperties } from 'react';
import type { ZoomConfig } from './ZoomConfig';
export function applyZoom(c: ZoomConfig): CSSProperties {
  return c.value === undefined ? {} : { zoom: c.value } as CSSProperties;
}
