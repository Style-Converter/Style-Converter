/**
 * Style Builder - Converts IR properties to CSS styles.
 *
 * This is the main entry point for converting IR properties to inline CSS.
 */

import type { IRProperty } from '../ir/IRModels';
import {
  extractColor,
  extractOpacity,
  extractLength,
  extractKeyword,
  extractFloat,
  extractDegrees,
  extractMs,
  extractInt,
} from '../types/ValueExtractors';

export interface CSSStyles {
  [key: string]: string | number | undefined;
}

/**
 * Convert a list of IR properties to CSS styles.
 */
export function buildStyles(properties: IRProperty[]): CSSStyles {
  const styles: CSSStyles = {};

  for (const prop of properties) {
    applyProperty(styles, prop);
  }

  return styles;
}

/**
 * Apply a single IR property to the styles object.
 */
function applyProperty(styles: CSSStyles, prop: IRProperty): void {
  const { type, data } = prop;

  switch (type) {
    // ==================== Colors ====================
    case 'BackgroundColor': {
      const color = extractColor(data);
      if (color) styles.backgroundColor = color;
      break;
    }
    case 'Color': {
      const color = extractColor(data);
      if (color) styles.color = color;
      break;
    }
    case 'Opacity': {
      const opacity = extractOpacity(data);
      if (opacity !== null) styles.opacity = opacity;
      break;
    }

    // ==================== Sizing ====================
    case 'Width': {
      const width = extractLength(data);
      if (width) styles.width = width;
      break;
    }
    case 'Height': {
      const height = extractLength(data);
      if (height) styles.height = height;
      break;
    }
    case 'MinWidth': {
      const minWidth = extractLength(data);
      if (minWidth) styles.minWidth = minWidth;
      break;
    }
    case 'MaxWidth': {
      const maxWidth = extractLength(data);
      if (maxWidth) styles.maxWidth = maxWidth;
      break;
    }
    case 'MinHeight': {
      const minHeight = extractLength(data);
      if (minHeight) styles.minHeight = minHeight;
      break;
    }
    case 'MaxHeight': {
      const maxHeight = extractLength(data);
      if (maxHeight) styles.maxHeight = maxHeight;
      break;
    }

    // ==================== Spacing ====================
    case 'PaddingTop': {
      const pt = extractLength(data);
      if (pt) styles.paddingTop = pt;
      break;
    }
    case 'PaddingRight': {
      const pr = extractLength(data);
      if (pr) styles.paddingRight = pr;
      break;
    }
    case 'PaddingBottom': {
      const pb = extractLength(data);
      if (pb) styles.paddingBottom = pb;
      break;
    }
    case 'PaddingLeft': {
      const pl = extractLength(data);
      if (pl) styles.paddingLeft = pl;
      break;
    }
    case 'MarginTop': {
      const mt = extractLength(data);
      if (mt) styles.marginTop = mt;
      break;
    }
    case 'MarginRight': {
      const mr = extractLength(data);
      if (mr) styles.marginRight = mr;
      break;
    }
    case 'MarginBottom': {
      const mb = extractLength(data);
      if (mb) styles.marginBottom = mb;
      break;
    }
    case 'MarginLeft': {
      const ml = extractLength(data);
      if (ml) styles.marginLeft = ml;
      break;
    }

    // ==================== Display & Layout ====================
    case 'Display': {
      const display = extractKeyword(data);
      if (display) styles.display = display.toLowerCase().replace('_', '-');
      break;
    }
    case 'FlexDirection': {
      const fd = extractKeyword(data);
      if (fd) styles.flexDirection = fd.toLowerCase().replace('_', '-');
      break;
    }
    case 'FlexWrap': {
      const fw = extractKeyword(data);
      if (fw) styles.flexWrap = fw.toLowerCase().replace('_', '-');
      break;
    }
    case 'JustifyContent': {
      const jc = extractKeyword(data);
      if (jc) styles.justifyContent = jc.toLowerCase().replace('_', '-');
      break;
    }
    case 'AlignItems': {
      const ai = extractKeyword(data);
      if (ai) styles.alignItems = ai.toLowerCase().replace('_', '-');
      break;
    }
    case 'AlignContent': {
      const ac = extractKeyword(data);
      if (ac) styles.alignContent = ac.toLowerCase().replace('_', '-');
      break;
    }
    case 'AlignSelf': {
      const as = extractKeyword(data);
      if (as) styles.alignSelf = as.toLowerCase().replace('_', '-');
      break;
    }
    case 'FlexGrow': {
      const fg = extractFloat(data);
      if (fg !== null) styles.flexGrow = fg;
      break;
    }
    case 'FlexShrink': {
      const fs = extractFloat(data);
      if (fs !== null) styles.flexShrink = fs;
      break;
    }
    case 'FlexBasis': {
      const fb = extractLength(data);
      if (fb) styles.flexBasis = fb;
      break;
    }
    case 'Order': {
      const order = extractInt(data);
      if (order !== null) styles.order = order;
      break;
    }

    // ==================== Gap ====================
    case 'Gap': {
      const gap = extractLength(data);
      if (gap) styles.gap = gap;
      break;
    }
    case 'RowGap': {
      const rg = extractLength(data);
      if (rg) styles.rowGap = rg;
      break;
    }
    case 'ColumnGap': {
      const cg = extractLength(data);
      if (cg) styles.columnGap = cg;
      break;
    }

    // ==================== Grid ====================
    case 'GridTemplateColumns': {
      styles.gridTemplateColumns = extractGridTemplate(data);
      break;
    }
    case 'GridTemplateRows': {
      styles.gridTemplateRows = extractGridTemplate(data);
      break;
    }
    case 'GridColumn': {
      const gc = extractKeyword(data) || extractGridSpan(data);
      if (gc) styles.gridColumn = gc;
      break;
    }
    case 'GridRow': {
      const gr = extractKeyword(data) || extractGridSpan(data);
      if (gr) styles.gridRow = gr;
      break;
    }
    case 'GridArea': {
      const ga = extractKeyword(data);
      if (ga) styles.gridArea = ga;
      break;
    }

    // ==================== Position ====================
    case 'Position': {
      const pos = extractKeyword(data);
      if (pos) styles.position = pos.toLowerCase();
      break;
    }
    case 'Top': {
      const top = extractLength(data);
      if (top) styles.top = top;
      break;
    }
    case 'Right': {
      const right = extractLength(data);
      if (right) styles.right = right;
      break;
    }
    case 'Bottom': {
      const bottom = extractLength(data);
      if (bottom) styles.bottom = bottom;
      break;
    }
    case 'Left': {
      const left = extractLength(data);
      if (left) styles.left = left;
      break;
    }
    case 'ZIndex': {
      const z = extractInt(data);
      if (z !== null) styles.zIndex = z;
      break;
    }

    // ==================== Borders ====================
    case 'BorderTopWidth':
    case 'BorderRightWidth':
    case 'BorderBottomWidth':
    case 'BorderLeftWidth': {
      const side = type.replace('Border', 'border').replace('Width', 'Width');
      const width = extractBorderWidth(data);
      if (width) styles[side] = width;
      break;
    }
    case 'BorderTopStyle':
    case 'BorderRightStyle':
    case 'BorderBottomStyle':
    case 'BorderLeftStyle': {
      const side = type.replace('Border', 'border').replace('Style', 'Style');
      const style = extractKeyword(data);
      if (style) styles[side] = style.toLowerCase();
      break;
    }
    case 'BorderTopColor':
    case 'BorderRightColor':
    case 'BorderBottomColor':
    case 'BorderLeftColor': {
      const side = type.replace('Border', 'border').replace('Color', 'Color');
      const color = extractColor(data);
      if (color) styles[side] = color;
      break;
    }
    case 'BorderRadius':
    case 'BorderTopLeftRadius':
    case 'BorderTopRightRadius':
    case 'BorderBottomLeftRadius':
    case 'BorderBottomRightRadius': {
      const cssProp = type.charAt(0).toLowerCase() + type.slice(1);
      const radius = extractLength(data);
      if (radius) styles[cssProp] = radius;
      break;
    }

    // ==================== Typography ====================
    case 'FontSize': {
      const fs = extractFontSize(data);
      if (fs) styles.fontSize = fs;
      break;
    }
    case 'FontWeight': {
      const fw = extractFontWeight(data);
      if (fw) styles.fontWeight = fw;
      break;
    }
    case 'FontFamily': {
      const ff = extractFontFamily(data);
      if (ff) styles.fontFamily = ff;
      break;
    }
    case 'FontStyle': {
      const fst = extractKeyword(data);
      if (fst) styles.fontStyle = fst.toLowerCase();
      break;
    }
    case 'LineHeight': {
      const lh = extractLineHeight(data);
      if (lh) styles.lineHeight = lh;
      break;
    }
    case 'LetterSpacing': {
      const ls = extractLength(data);
      if (ls) styles.letterSpacing = ls;
      break;
    }
    case 'TextAlign': {
      const ta = extractKeyword(data);
      if (ta) styles.textAlign = ta.toLowerCase();
      break;
    }
    case 'TextDecoration': {
      const td = extractKeyword(data);
      if (td) styles.textDecoration = td.toLowerCase().replace('_', ' ');
      break;
    }
    case 'TextTransform': {
      const tt = extractKeyword(data);
      if (tt) styles.textTransform = tt.toLowerCase();
      break;
    }
    case 'WhiteSpace': {
      const ws = extractKeyword(data);
      if (ws) styles.whiteSpace = ws.toLowerCase().replace('_', '-');
      break;
    }
    case 'WordBreak': {
      const wb = extractKeyword(data);
      if (wb) styles.wordBreak = wb.toLowerCase().replace('_', '-');
      break;
    }
    case 'OverflowWrap': {
      const ow = extractKeyword(data);
      if (ow) styles.overflowWrap = ow.toLowerCase().replace('_', '-');
      break;
    }

    // ==================== Overflow ====================
    case 'Overflow': {
      const ov = extractKeyword(data);
      if (ov) styles.overflow = ov.toLowerCase();
      break;
    }
    case 'OverflowX': {
      const ox = extractKeyword(data);
      if (ox) styles.overflowX = ox.toLowerCase();
      break;
    }
    case 'OverflowY': {
      const oy = extractKeyword(data);
      if (oy) styles.overflowY = oy.toLowerCase();
      break;
    }

    // ==================== Transforms ====================
    case 'Transform': {
      const transform = extractTransform(data);
      if (transform) styles.transform = transform;
      break;
    }
    case 'TransformOrigin': {
      const origin = extractTransformOrigin(data);
      if (origin) styles.transformOrigin = origin;
      break;
    }

    // ==================== Filters ====================
    case 'Filter': {
      const filter = extractFilter(data);
      if (filter) styles.filter = filter;
      break;
    }
    case 'BackdropFilter': {
      const bf = extractFilter(data);
      if (bf) styles.backdropFilter = bf;
      break;
    }

    // ==================== Transitions & Animations ====================
    case 'TransitionProperty': {
      const tp = extractTransitionProperty(data);
      if (tp) styles.transitionProperty = tp;
      break;
    }
    case 'TransitionDuration': {
      const td = extractTransitionDuration(data);
      if (td) styles.transitionDuration = td;
      break;
    }
    case 'TransitionTimingFunction': {
      const ttf = extractTimingFunction(data);
      if (ttf) styles.transitionTimingFunction = ttf;
      break;
    }
    case 'TransitionDelay': {
      const tde = extractTransitionDuration(data);
      if (tde) styles.transitionDelay = tde;
      break;
    }

    // ==================== Box Shadow ====================
    case 'BoxShadow': {
      const shadow = extractBoxShadow(data);
      if (shadow) styles.boxShadow = shadow;
      break;
    }

    // ==================== Text Shadow ====================
    case 'TextShadow': {
      const ts = extractTextShadow(data);
      if (ts) styles.textShadow = ts;
      break;
    }

    // ==================== Background Image ====================
    case 'BackgroundImage': {
      const bi = extractBackgroundImage(data);
      if (bi) styles.backgroundImage = bi;
      break;
    }
    case 'BackgroundSize': {
      const bs = extractBackgroundSize(data);
      if (bs) styles.backgroundSize = bs;
      break;
    }
    case 'BackgroundPosition': {
      const bp = extractBackgroundPosition(data);
      if (bp) styles.backgroundPosition = bp;
      break;
    }
    case 'BackgroundRepeat': {
      const br = extractKeyword(data);
      if (br) styles.backgroundRepeat = br.toLowerCase().replace('_', '-');
      break;
    }

    // ==================== Clip Path ====================
    case 'ClipPath': {
      const cp = extractClipPath(data);
      if (cp) styles.clipPath = cp;
      break;
    }

    // ==================== Cursor ====================
    case 'Cursor': {
      const cursor = extractKeyword(data);
      if (cursor) styles.cursor = cursor.toLowerCase().replace('_', '-');
      break;
    }

    // ==================== Visibility ====================
    case 'Visibility': {
      const vis = extractKeyword(data);
      if (vis) styles.visibility = vis.toLowerCase();
      break;
    }

    // ==================== Outline ====================
    case 'OutlineWidth': {
      const ow = extractLength(data);
      if (ow) styles.outlineWidth = ow;
      break;
    }
    case 'OutlineStyle': {
      const os = extractKeyword(data);
      if (os) styles.outlineStyle = os.toLowerCase();
      break;
    }
    case 'OutlineColor': {
      const oc = extractColor(data);
      if (oc) styles.outlineColor = oc;
      break;
    }
    case 'OutlineOffset': {
      const oo = extractLength(data);
      if (oo) styles.outlineOffset = oo;
      break;
    }

    // ==================== Aspect Ratio ====================
    case 'AspectRatio': {
      const ar = extractAspectRatio(data);
      if (ar) styles.aspectRatio = ar;
      break;
    }

    // ==================== Object Fit ====================
    case 'ObjectFit': {
      const of = extractKeyword(data);
      if (of) styles.objectFit = of.toLowerCase().replace('_', '-');
      break;
    }
    case 'ObjectPosition': {
      const op = extractBackgroundPosition(data);
      if (op) styles.objectPosition = op;
      break;
    }

    // ==================== Pointer Events ====================
    case 'PointerEvents': {
      const pe = extractKeyword(data);
      if (pe) styles.pointerEvents = pe.toLowerCase();
      break;
    }

    // ==================== User Select ====================
    case 'UserSelect': {
      const us = extractKeyword(data);
      if (us) styles.userSelect = us.toLowerCase();
      break;
    }

    // ==================== Mix Blend Mode ====================
    case 'MixBlendMode': {
      const mbm = extractKeyword(data);
      if (mbm) styles.mixBlendMode = mbm.toLowerCase().replace('_', '-');
      break;
    }

    // Default: try to apply as-is for unknown properties
    default:
      // Convert PascalCase to kebab-case
      const cssProperty = type.replace(/([A-Z])/g, '-$1').toLowerCase().replace(/^-/, '');
      const value = extractKeyword(data) || extractLength(data);
      if (value) {
        styles[cssProperty] = typeof value === 'string' ? value.toLowerCase().replace('_', '-') : value;
      }
  }
}

// ==================== Helper Functions ====================

function extractBorderWidth(data: unknown): string | null {
  if (typeof data === 'object' && data !== null) {
    const obj = data as Record<string, unknown>;

    // Check for normalized px value
    if (typeof obj.px === 'number') return `${obj.px}px`;

    // Check for keyword
    const keyword = extractKeyword(data);
    if (keyword) {
      switch (keyword.toLowerCase()) {
        case 'thin':
          return '1px';
        case 'medium':
          return '3px';
        case 'thick':
          return '5px';
        default:
          return keyword;
      }
    }
  }

  const length = extractLength(data);
  return length;
}

function extractFontSize(data: unknown): string | null {
  if (typeof data === 'object' && data !== null) {
    const obj = data as Record<string, unknown>;

    // Check for normalized px value
    if (typeof obj.px === 'number') return `${obj.px}px`;
  }

  const keyword = extractKeyword(data);
  if (keyword) return keyword.toLowerCase().replace('_', '-');

  return extractLength(data);
}

function extractFontWeight(data: unknown): string | number | null {
  if (typeof data === 'number') return data;

  if (typeof data === 'object' && data !== null) {
    const obj = data as Record<string, unknown>;

    if (typeof obj.weight === 'number') return obj.weight;
  }

  const keyword = extractKeyword(data);
  if (keyword) {
    switch (keyword.toLowerCase()) {
      case 'normal':
        return 400;
      case 'bold':
        return 700;
      default:
        return keyword.toLowerCase();
    }
  }

  return null;
}

function extractFontFamily(data: unknown): string | null {
  if (typeof data === 'string') return data;

  if (Array.isArray(data)) {
    return data.map((f) => (typeof f === 'string' ? f : '')).filter(Boolean).join(', ');
  }

  if (typeof data === 'object' && data !== null) {
    const obj = data as Record<string, unknown>;
    if (Array.isArray(obj.families)) {
      return obj.families.join(', ');
    }
    if (typeof obj.family === 'string') return obj.family;
  }

  return extractKeyword(data);
}

function extractLineHeight(data: unknown): string | number | null {
  if (typeof data === 'number') return data;

  if (typeof data === 'object' && data !== null) {
    const obj = data as Record<string, unknown>;

    if (typeof obj.value === 'number') {
      if (obj.unit === 'px') return `${obj.value}px`;
      return obj.value; // multiplier
    }
  }

  const keyword = extractKeyword(data);
  if (keyword === 'normal') return 'normal';

  return extractLength(data);
}

function extractGridTemplate(data: unknown): string {
  if (Array.isArray(data)) {
    return data
      .map((track) => {
        if (typeof track === 'object' && track !== null) {
          const t = track as Record<string, unknown>;
          if (typeof t.fr === 'number') return `${t.fr}fr`;
          if (typeof t.px === 'number') return `${t.px}px`;
          if (typeof t.minmax === 'object') {
            const mm = t.minmax as Record<string, unknown>;
            return `minmax(${extractLength(mm.min) || '0'}, ${extractLength(mm.max) || '1fr'})`;
          }
        }
        return extractLength(track) || 'auto';
      })
      .join(' ');
  }

  return extractKeyword(data) || 'none';
}

function extractGridSpan(data: unknown): string | null {
  if (typeof data === 'object' && data !== null) {
    const obj = data as Record<string, unknown>;
    if (typeof obj.start === 'number' && typeof obj.end === 'number') {
      return `${obj.start} / ${obj.end}`;
    }
    if (typeof obj.span === 'number') {
      return `span ${obj.span}`;
    }
  }
  return null;
}

function extractTransform(data: unknown): string | null {
  if (typeof data === 'string') return data;

  if (typeof data === 'object' && data !== null) {
    const obj = data as Record<string, unknown>;

    // Check for expression
    if (obj.type === 'expression' && typeof obj.expr === 'string') {
      return obj.expr;
    }

    // Check for functions list
    if (Array.isArray(obj.list)) {
      return obj.list
        .map((fn) => {
          const f = fn as Record<string, unknown>;
          switch (f.fn) {
            case 'translate': {
              const x = extractLength(f.x) || '0';
              const y = extractLength(f.y) || '0';
              return `translate(${x}, ${y})`;
            }
            case 'translateX':
              return `translateX(${extractLength(f.x) || '0'})`;
            case 'translateY':
              return `translateY(${extractLength(f.y) || '0'})`;
            case 'rotate':
              return `rotate(${extractDegrees(f.angle) || 0}deg)`;
            case 'scale': {
              const sx = f.x ?? 1;
              const sy = f.y ?? sx;
              return `scale(${sx}, ${sy})`;
            }
            case 'scaleX':
              return `scaleX(${f.x ?? 1})`;
            case 'scaleY':
              return `scaleY(${f.y ?? 1})`;
            case 'skew':
              return `skew(${extractDegrees(f.x) || 0}deg, ${extractDegrees(f.y) || 0}deg)`;
            case 'skewX':
              return `skewX(${extractDegrees(f.x) || 0}deg)`;
            case 'skewY':
              return `skewY(${extractDegrees(f.y) || 0}deg)`;
            case 'matrix':
              return `matrix(${(f.values as number[])?.join(', ') || '1,0,0,1,0,0'})`;
            default:
              return null;
          }
        })
        .filter(Boolean)
        .join(' ');
    }
  }

  return null;
}

function extractTransformOrigin(data: unknown): string | null {
  if (typeof data === 'string') return data;

  if (typeof data === 'object' && data !== null) {
    const obj = data as Record<string, unknown>;
    const x = extractLength(obj.x) || 'center';
    const y = extractLength(obj.y) || 'center';
    return `${x} ${y}`;
  }

  return null;
}

function extractFilter(data: unknown): string | null {
  if (typeof data === 'string') return data;

  if (Array.isArray(data)) {
    return data
      .map((fn) => {
        const f = fn as Record<string, unknown>;
        switch (f.fn) {
          case 'blur':
            return `blur(${extractLength(f.r) || '0'})`;
          case 'brightness':
            return `brightness(${f.v ?? 100}%)`;
          case 'contrast':
            return `contrast(${f.v ?? 100}%)`;
          case 'grayscale':
            return `grayscale(${f.v ?? 0}%)`;
          case 'saturate':
            return `saturate(${f.v ?? 100}%)`;
          case 'sepia':
            return `sepia(${f.v ?? 0}%)`;
          case 'invert':
            return `invert(${f.v ?? 0}%)`;
          case 'hue-rotate':
            return `hue-rotate(${extractDegrees(f.angle) || 0}deg)`;
          case 'opacity':
            return `opacity(${f.v ?? 100}%)`;
          case 'drop-shadow': {
            const x = extractLength(f.x) || '0';
            const y = extractLength(f.y) || '0';
            const blur = extractLength(f.blur) || '0';
            const color = extractColor(f.color) || 'black';
            return `drop-shadow(${x} ${y} ${blur} ${color})`;
          }
          default:
            return null;
        }
      })
      .filter(Boolean)
      .join(' ');
  }

  return null;
}

function extractTransitionProperty(data: unknown): string | null {
  if (typeof data === 'string') return data;
  if (Array.isArray(data)) return data.join(', ');
  return 'all';
}

function extractTransitionDuration(data: unknown): string | null {
  if (Array.isArray(data)) {
    return data.map((t) => `${extractMs(t) || 0}ms`).join(', ');
  }
  const ms = extractMs(data);
  return ms !== null ? `${ms}ms` : null;
}

function extractTimingFunction(data: unknown): string | null {
  if (typeof data === 'string') return data;

  if (Array.isArray(data)) {
    return data
      .map((tf) => extractSingleTimingFunction(tf))
      .filter(Boolean)
      .join(', ');
  }

  return extractSingleTimingFunction(data);
}

function extractSingleTimingFunction(data: unknown): string | null {
  if (typeof data === 'string') return data;

  if (typeof data === 'object' && data !== null) {
    const obj = data as Record<string, unknown>;

    if (Array.isArray(obj.cb)) {
      const [x1, y1, x2, y2] = obj.cb;
      return `cubic-bezier(${x1}, ${y1}, ${x2}, ${y2})`;
    }

    if (typeof obj.steps === 'number') {
      const jump = (obj.jumpTerm as string) || 'end';
      return `steps(${obj.steps}, ${jump})`;
    }

    if (obj.type === 'linear') return 'linear';

    if (typeof obj.original === 'string') return obj.original;
  }

  return null;
}

function extractBoxShadow(data: unknown): string | null {
  if (typeof data === 'string') return data;

  if (Array.isArray(data)) {
    return data
      .map((shadow) => {
        const s = shadow as Record<string, unknown>;
        const inset = s.inset ? 'inset ' : '';
        const x = extractLength(s.x) || '0';
        const y = extractLength(s.y) || '0';
        const blur = extractLength(s.blur) || '0';
        const spread = extractLength(s.spread) || '0';
        const color = extractColor(s.c) || extractColor(s.color) || 'rgba(0,0,0,0.25)';
        return `${inset}${x} ${y} ${blur} ${spread} ${color}`;
      })
      .join(', ');
  }

  return null;
}

function extractTextShadow(data: unknown): string | null {
  if (typeof data === 'string') return data;

  if (Array.isArray(data)) {
    return data
      .map((shadow) => {
        const s = shadow as Record<string, unknown>;
        const x = extractLength(s.x) || '0';
        const y = extractLength(s.y) || '0';
        const blur = extractLength(s.blur) || '0';
        const color = extractColor(s.c) || extractColor(s.color) || 'rgba(0,0,0,0.25)';
        return `${x} ${y} ${blur} ${color}`;
      })
      .join(', ');
  }

  return null;
}

function extractBackgroundImage(data: unknown): string | null {
  if (typeof data === 'string') return data;

  if (Array.isArray(data)) {
    return data
      .map((img) => {
        const i = img as Record<string, unknown>;
        const type = i.type as string;

        if (type === 'none') return 'none';
        if (type === 'url') return `url(${i.url})`;

        if (type?.includes('gradient')) {
          return extractGradient(i);
        }

        return null;
      })
      .filter(Boolean)
      .join(', ');
  }

  return null;
}

function extractGradient(data: Record<string, unknown>): string {
  const type = data.type as string;
  const stops = extractGradientStops(data.stops as unknown[]);

  if (type === 'linear-gradient' || type === 'repeating-linear-gradient') {
    const angle = extractDegrees(data.angle) ?? 180;
    const prefix = type === 'repeating-linear-gradient' ? 'repeating-' : '';
    return `${prefix}linear-gradient(${angle}deg, ${stops})`;
  }

  if (type === 'radial-gradient' || type === 'repeating-radial-gradient') {
    const pos = data.position as Record<string, unknown> | undefined;
    const x = pos?.x ?? 50;
    const y = pos?.y ?? 50;
    const prefix = type === 'repeating-radial-gradient' ? 'repeating-' : '';
    return `${prefix}radial-gradient(circle at ${x}% ${y}%, ${stops})`;
  }

  if (type === 'conic-gradient' || type === 'repeating-conic-gradient') {
    const fromAngle = extractDegrees(data.fromAngle) ?? 0;
    const pos = data.position as Record<string, unknown> | undefined;
    const x = pos?.x ?? 50;
    const y = pos?.y ?? 50;
    const prefix = type === 'repeating-conic-gradient' ? 'repeating-' : '';
    return `${prefix}conic-gradient(from ${fromAngle}deg at ${x}% ${y}%, ${stops})`;
  }

  return 'none';
}

function extractGradientStops(stops: unknown[] | undefined): string {
  if (!stops) return 'transparent, transparent';

  // Filter out corrupted stops (e.g. "from" parsed as a color from conic-gradient)
  const cssColorKeywords = new Set([
    'transparent', 'currentcolor', 'inherit',
    'red', 'blue', 'green', 'yellow', 'orange', 'purple', 'pink', 'white', 'black',
    'cyan', 'magenta', 'gray', 'grey', 'coral', 'navy', 'teal', 'lime', 'aqua',
    'maroon', 'olive', 'silver', 'fuchsia', 'crimson', 'salmon', 'tomato',
    'gold', 'khaki', 'plum', 'orchid', 'violet', 'indigo', 'sienna', 'peru',
    'tan', 'wheat', 'beige', 'linen', 'ivory', 'snow', 'azure', 'honeydew',
  ]);

  const isValidColor = (c: string) =>
    c.startsWith('#') || c.startsWith('rgb') || c.startsWith('hsl') ||
    cssColorKeywords.has(c.toLowerCase());

  return stops
    .map((stop) => {
      const s = stop as Record<string, unknown>;
      const color = extractColor(s.color);
      if (!color || !isValidColor(color)) return null;
      const position = s.position;
      return (position !== null && position !== undefined && typeof position === 'number')
        ? `${color} ${position}%`
        : color;
    })
    .filter(Boolean)
    .join(', ') || 'transparent, transparent';
}

function extractBackgroundSize(data: unknown): string | null {
  const keyword = extractKeyword(data);
  if (keyword) return keyword.toLowerCase();

  if (typeof data === 'object' && data !== null) {
    const obj = data as Record<string, unknown>;
    const width = extractLength(obj.width) || 'auto';
    const height = extractLength(obj.height) || 'auto';
    return `${width} ${height}`;
  }

  return null;
}

function extractBackgroundPosition(data: unknown): string | null {
  const keyword = extractKeyword(data);
  if (keyword) return keyword.toLowerCase();

  if (typeof data === 'object' && data !== null) {
    const obj = data as Record<string, unknown>;
    const x = obj.x ?? 50;
    const y = obj.y ?? 50;
    return `${x}% ${y}%`;
  }

  return null;
}

function extractClipPath(data: unknown): string | null {
  if (typeof data === 'string') return data;

  if (typeof data === 'object' && data !== null) {
    const obj = data as Record<string, unknown>;

    if (obj.type === 'none') return 'none';

    if (obj.type === 'circle') {
      const r = extractLength(obj.radius) || '50%';
      const pos = obj.position as Record<string, unknown> | undefined;
      const at = pos ? `at ${pos.x ?? 50}% ${pos.y ?? 50}%` : '';
      return `circle(${r} ${at})`.trim();
    }

    if (obj.type === 'ellipse') {
      const rx = extractLength(obj.radiusX) || '50%';
      const ry = extractLength(obj.radiusY) || '50%';
      return `ellipse(${rx} ${ry})`;
    }

    if (obj.type === 'inset') {
      const top = extractLength(obj.top) || '0';
      const right = extractLength(obj.right) || '0';
      const bottom = extractLength(obj.bottom) || '0';
      const left = extractLength(obj.left) || '0';
      return `inset(${top} ${right} ${bottom} ${left})`;
    }

    if (obj.type === 'polygon' && Array.isArray(obj.points)) {
      const points = obj.points
        .map((p) => {
          const pt = p as Record<string, unknown>;
          // Support both {x, y} objects and [x, y] arrays
          const x = pt.x ?? (pt as unknown as number[])[0] ?? 0;
          const y = pt.y ?? (pt as unknown as number[])[1] ?? 0;
          return `${x}% ${y}%`;
        })
        .join(', ');
      return `polygon(${points})`;
    }

    if (obj.type === 'path' && typeof obj.d === 'string') {
      return `path("${obj.d}")`;
    }
  }

  return null;
}

function extractAspectRatio(data: unknown): string | null {
  if (typeof data === 'number') return String(data);
  if (typeof data === 'string') return data;

  if (typeof data === 'object' && data !== null) {
    const obj = data as Record<string, unknown>;
    if (typeof obj.width === 'number' && typeof obj.height === 'number') {
      return `${obj.width} / ${obj.height}`;
    }
    if (typeof obj.ratio === 'number') {
      return String(obj.ratio);
    }
  }

  return null;
}
