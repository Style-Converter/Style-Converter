// _dispatch.ts — Phase-9 scroll-timeline dispatch (3 properties).
// Kept separate from animations/_dispatch.ts because scroll-timeline lives
// under engine/scrolling/ per CLAUDE.md (mirror of irmodels/properties/scrolling/).
//
// All three appliers widen through Record<string,string> because csstype's
// view of scroll-driven animations is still intermittent.  See each applier
// for the MDN link.

import type { CSSProperties } from 'react';

import { extractScrollTimeline } from './ScrollTimelineExtractor';
import { applyScrollTimeline } from './ScrollTimelineApplier';
import { extractScrollTimelineName } from './ScrollTimelineNameExtractor';
import { applyScrollTimelineName } from './ScrollTimelineNameApplier';
import { extractScrollTimelineAxis } from './ScrollTimelineAxisExtractor';
import { applyScrollTimelineAxis } from './ScrollTimelineAxisApplier';

interface IRPropertyLike { type: string; data: unknown }

// Emit scroll-timeline longhands.  Order is irrelevant — three distinct keys.
export function applyScrollingPhase9(properties: IRPropertyLike[]): CSSProperties {
  const out: CSSProperties = {};
  Object.assign(out, applyScrollTimeline(extractScrollTimeline(properties)));
  Object.assign(out, applyScrollTimelineName(extractScrollTimelineName(properties)));
  Object.assign(out, applyScrollTimelineAxis(extractScrollTimelineAxis(properties)));
  return out;
}
