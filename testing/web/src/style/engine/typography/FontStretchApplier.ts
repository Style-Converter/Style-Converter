// FontStretchApplier.ts — emits CSS declarations from a FontStretchConfig.
// Web is the privileged platform for typography: native CSS `fontStretch`
// handles every variant we parse.  This file only formats and returns.

import type { CSSProperties } from 'react';
import type { FontStretchConfig } from './FontStretchConfig';

// Output type narrowed to the exactly one CSS key this applier owns.
export type FontStretchStyles = Pick<CSSProperties, 'fontStretch'>;

export function applyFontStretch(config: FontStretchConfig): FontStretchStyles {
  if (config.value === undefined) return {};                        // unset -> empty
  return { fontStretch: config.value } as FontStretchStyles;                 // single-key output
}
