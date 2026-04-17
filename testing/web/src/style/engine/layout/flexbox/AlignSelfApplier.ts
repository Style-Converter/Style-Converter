// AlignSelfApplier.ts — emits a CSS declaration for the `alignSelf` property.
// Standard property — maps 1:1 to `CSSProperties['alignSelf']`.  Spec: CSS Box Alignment 3 — https://developer.mozilla.org/docs/Web/CSS/align-self.

import type { CSSProperties } from 'react';
import type { AlignSelfConfig } from './AlignSelfConfig';

export type AlignSelfStyles = Pick<CSSProperties, 'alignSelf'>;

export function applyAlignSelf(config: AlignSelfConfig): AlignSelfStyles {
  if (config.value === undefined) return {};                         // unset
  return { alignSelf: config.value } as AlignSelfStyles;                    // typed single-key
}
