// ClipPathExtractor.ts — IR -> ClipPathConfig.
// Serialises every shape/function to its CSS form.  Length fields use
// `extractLength` so percentage + px + calc() all work.

import { extractLength, toCssLength } from '../../core/types/LengthValue';          // shared length parser
import { foldLast, type IRPropertyLike } from '../_shared';                          // cascade helper
import type { ClipPathConfig } from './ClipPathConfig';
import { CLIP_PATH_PROPERTY_TYPE } from './ClipPathConfig';

// Helper: convert an IR length-ish value to a CSS string, defaulting to '0'.
function len(raw: unknown): string {
  if (raw === undefined || raw === null) return '0';                                 // missing axis
  if (raw === 'auto') return 'auto';                                                 // rect(auto ...)
  const v = extractLength(raw);                                                      // shared parser
  return v.kind === 'unknown' ? '0' : toCssLength(v);                                // fallback to 0 per spec
}

// Helper: single shape function to CSS.  Handles the variants seen in fixtures.
function shapeToCss(o: Record<string, unknown>): string | undefined {
  switch (o.type) {                                                                  // discriminator
    case 'inset': {
      const t = len(o.t), r = len(o.r), b = len(o.b), l = len(o.l);                  // TRBL lengths
      const round = o.round !== undefined ? ` round ${len(o.round)}` : '';           // optional round <r>
      return `inset(${t} ${r} ${b} ${l}${round})`;
    }
    case 'circle': {
      const r = o.r !== undefined ? len(o.r) : (typeof o.px === 'number' ? `${o.px}px` : '');
      const pos = o.pos as Record<string, unknown> | undefined;
      const at = pos ? ` at ${len(pos.x)} ${len(pos.y)}` : '';                       // optional 'at x y'
      return `circle(${r}${at})`.replace('()', '(closest-side)').replace('( ', '(');
    }
    case 'ellipse': {
      const rx = len(o.rx), ry = len(o.ry);
      const pos = o.pos as Record<string, unknown> | undefined;
      const at = pos ? ` at ${len(pos.x)} ${len(pos.y)}` : '';
      return `ellipse(${rx} ${ry}${at})`;
    }
    case 'polygon': {
      if (!Array.isArray(o.points)) return undefined;                                // require points array
      const pts = o.points.map(p => {                                                // serialise each point
        const pt = p as Record<string, unknown>;
        return `${pt.x ?? 0}% ${pt.y ?? 0}%`;                                        // fixtures use 0-100 numbers
      });
      return `polygon(${pts.join(', ')})`;
    }
    case 'rect': {                                                                   // deprecated-shape-fn form
      return `rect(${len(o.t)} ${len(o.r)} ${len(o.b)} ${len(o.l)})`;
    }
    case 'xywh': {                                                                   // CSS Shapes 2 xywh()
      const tail = o.round !== undefined ? ` round ${len(o.round)}` : '';
      return `xywh(${len(o.x)} ${len(o.y)} ${len(o.w)} ${len(o.h)}${tail})`;
    }
    case 'path': {
      return typeof o.d === 'string' ? `path("${o.d}")` : undefined;                  // pass SVG d verbatim
    }
    default: return undefined;                                                         // unknown shape
  }
}

// Entry-point per-property parser.  Handles the `{geometry-box, shape?}` wrapper
// that the Kotlin parser emits for combined box+shape values.
function parseOne(data: unknown): string | undefined {
  if (data === null || data === undefined) return undefined;                         // absent
  if (typeof data === 'string') {                                                    // 'none' | '#id' | url ref
    return data;
  }
  if (typeof data !== 'object') return undefined;                                    // unknown primitive
  const o = data as Record<string, unknown>;
  // Combined form: {"geometry-box":"border-box"[, shape]}
  if (typeof o['geometry-box'] === 'string') {
    const box = o['geometry-box'] as string;
    if (o.shape && typeof o.shape === 'object') {
      const s = shapeToCss(o.shape as Record<string, unknown>);
      return s ? `${s} ${box}` : box;                                                 // CSS: <shape> <box>
    }
    return box;                                                                       // box keyword alone
  }
  // Bare shape: delegate.
  return shapeToCss(o);
}

export function extractClipPath(properties: IRPropertyLike[]): ClipPathConfig {
  return { value: foldLast(properties, CLIP_PATH_PROPERTY_TYPE, parseOne) };
}
