// BackgroundPositionConfig.ts — typed record for the pair
// (BackgroundPositionX, BackgroundPositionY).  The IR never emits the combined
// `BackgroundPosition` longhand after shorthand expansion, so we recombine the
// X/Y tuple here and let the applier pick between CSS `backgroundPosition`
// (when both axes are set) and the per-axis longhands (when one is missing).

// Per-axis CSS fragment such as 'center', '20px', '30%'.  We keep it pre-
// rendered because every shape we see collapses nicely to a CSS token.
export type AxisValue = string;

// Config holder — either axis is optional so partial declarations round-trip.
export interface BackgroundPositionConfig {
  x?: AxisValue;                                                      // horizontal axis CSS token
  y?: AxisValue;                                                      // vertical axis CSS token
}

// IR property types recognised.
export const BACKGROUND_POSITION_X = 'BackgroundPositionX' as const;
export const BACKGROUND_POSITION_Y = 'BackgroundPositionY' as const;
export type BackgroundPositionPropertyType =
  | typeof BACKGROUND_POSITION_X
  | typeof BACKGROUND_POSITION_Y;
