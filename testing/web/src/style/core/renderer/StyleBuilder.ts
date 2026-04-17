/**
 * Style Builder - Converts IR properties to CSS styles.
 *
 * This is the main entry point for converting IR properties to inline CSS.
 */

import type { IRProperty } from '../ir/IRModels';
import {
  extractColor,
  extractLength,
  extractKeyword,
  extractDegrees,
  extractMs,
} from '../types/ValueExtractors';
// Phase-2 engine wiring — spacing properties now flow through per-family
// extractors + appliers instead of the legacy switch below.
import { isLegacyProperty } from '../../engine/PropertyRegistry';
import { extractPadding } from '../../engine/spacing/PaddingExtractor';
import { applyPadding } from '../../engine/spacing/PaddingApplier';
import { extractMargin } from '../../engine/spacing/MarginExtractor';
import { applyMargin } from '../../engine/spacing/MarginApplier';
import { extractGap } from '../../engine/spacing/GapExtractor';
import { applyGap } from '../../engine/spacing/GapApplier';
import { extractMarginTrim } from '../../engine/spacing/MarginTrimExtractor';
import { applyMarginTrim } from '../../engine/spacing/MarginTrimApplier';
// Phase-3 sizing engine — width/height/min-*/max-*/block-size/inline-size/aspect-ratio.
import { extractSize } from '../../engine/sizing/SizeExtractor';
import { applySize } from '../../engine/sizing/SizeApplier';
// Phase-4 color + background engine — see engine/color/*, engine/background/*,
// engine/effects/blend/*, engine/performance/*.
import { extractBackgroundColor } from '../../engine/color/BackgroundColorExtractor';
import { applyBackgroundColor }   from '../../engine/color/BackgroundColorApplier';
import { extractTextColor }       from '../../engine/color/ColorExtractor';
import { applyTextColor }         from '../../engine/color/ColorApplier';
import { extractOpacity }         from '../../engine/color/OpacityExtractor';
import { applyOpacity }           from '../../engine/color/OpacityApplier';
import { extractAccentColor }     from '../../engine/color/AccentColorExtractor';
import { applyAccentColor }       from '../../engine/color/AccentColorApplier';
import { extractCaretColor }      from '../../engine/color/CaretColorExtractor';
import { applyCaretColor }        from '../../engine/color/CaretColorApplier';
import { extractBackgroundImage }      from '../../engine/background/BackgroundImageExtractor';
import { applyBackgroundImage }        from '../../engine/background/BackgroundImageApplier';
import { extractBackgroundSize }       from '../../engine/background/BackgroundSizeExtractor';
import { applyBackgroundSize }         from '../../engine/background/BackgroundSizeApplier';
import { extractBackgroundPosition }   from '../../engine/background/BackgroundPositionExtractor';
import { applyBackgroundPosition }     from '../../engine/background/BackgroundPositionApplier';
import { extractBackgroundRepeat }     from '../../engine/background/BackgroundRepeatExtractor';
import { applyBackgroundRepeat }       from '../../engine/background/BackgroundRepeatApplier';
import { extractBackgroundClip }       from '../../engine/background/BackgroundClipExtractor';
import { applyBackgroundClip }         from '../../engine/background/BackgroundClipApplier';
import { extractBackgroundOrigin }     from '../../engine/background/BackgroundOriginExtractor';
import { applyBackgroundOrigin }       from '../../engine/background/BackgroundOriginApplier';
import { extractBackgroundAttachment } from '../../engine/background/BackgroundAttachmentExtractor';
import { applyBackgroundAttachment }   from '../../engine/background/BackgroundAttachmentApplier';
import { extractBlendMode } from '../../engine/effects/blend/BlendModeExtractor';
import { applyBlendMode }   from '../../engine/effects/blend/BlendModeApplier';
import { extractIsolation } from '../../engine/performance/IsolationExtractor';
import { applyIsolation }   from '../../engine/performance/IsolationApplier';
// Phase-5 borders engine — see engine/borders/* and engine/effects/shadow/*.
// Each triplet folds the relevant IRProperty entries into a typed Config and
// the Applier emits the native CSS key/value.  47 properties total.
import { applyBordersPhase5 } from '../../engine/borders/_dispatch';
import { applyBoxShadow } from '../../engine/effects/shadow/BoxShadowApplier';
import { extractBoxShadow } from '../../engine/effects/shadow/BoxShadowExtractor';
// Phase-6 typography engine — 109 properties (CaretColor migrated in Phase 4).
import { applyTypographyPhase6 } from '../../engine/typography/_dispatch';
// Phase-7 layout engine — 55 properties (flexbox, grid, position, advanced
// anchor + motion-path, plus root flow/top-layer keywords).
import { applyLayoutPhase7 } from '../../engine/layout/_dispatch';

export interface CSSStyles {
  [key: string]: string | number | undefined;
}

/**
 * Convert a list of IR properties to CSS styles.
 */
export function buildStyles(properties: IRProperty[]): CSSStyles {
  const styles: CSSStyles = {};

  // Phase-2 engine path — spacing properties are bucketed once and
  // handled by dedicated Config/Applier triplets.  The legacy switch
  // below skips any migrated type via `isLegacyProperty`.
  Object.assign(styles, applyPadding(extractPadding(properties)));
  Object.assign(styles, applyMargin(extractMargin(properties)));
  Object.assign(styles, applyGap(extractGap(properties)));
  const marginTrim = extractMarginTrim(properties);
  if (marginTrim) Object.assign(styles, applyMarginTrim(marginTrim));
  // Phase-3 sizing — width/height/min-*/max-*/block-size/inline-size/aspect-ratio.
  Object.assign(styles, applySize(extractSize(properties)));

  // Phase-4 color + background engine.  Each extractor is a single-pass fold
  // over `properties`; appliers emit CSS keys that the browser renders
  // natively (static + dynamic colors, gradients, blend modes, isolation).
  Object.assign(styles, applyBackgroundColor(extractBackgroundColor(properties)));
  Object.assign(styles, applyTextColor(extractTextColor(properties)));
  Object.assign(styles, applyOpacity(extractOpacity(properties)));
  Object.assign(styles, applyAccentColor(extractAccentColor(properties)));
  Object.assign(styles, applyCaretColor(extractCaretColor(properties)));
  Object.assign(styles, applyBackgroundImage(extractBackgroundImage(properties)));
  Object.assign(styles, applyBackgroundSize(extractBackgroundSize(properties)));
  Object.assign(styles, applyBackgroundPosition(extractBackgroundPosition(properties)));
  Object.assign(styles, applyBackgroundRepeat(extractBackgroundRepeat(properties)));
  Object.assign(styles, applyBackgroundClip(extractBackgroundClip(properties)));
  Object.assign(styles, applyBackgroundOrigin(extractBackgroundOrigin(properties)));
  Object.assign(styles, applyBackgroundAttachment(extractBackgroundAttachment(properties)));
  Object.assign(styles, applyBlendMode(extractBlendMode(properties)));
  Object.assign(styles, applyIsolation(extractIsolation(properties)));

  // Phase-5 borders engine — 46 per-side/corner/image/outline/misc
  // properties plus BoxShadow routed separately.
  Object.assign(styles, applyBordersPhase5(properties));
  Object.assign(styles, applyBoxShadow(extractBoxShadow(properties)));

  // Phase-6 typography engine — single fold handles all 109 typography props.
  Object.assign(styles, applyTypographyPhase6(properties));

  // Phase-7 layout engine — flexbox + grid + position + advanced anchor/
  // motion-path + flow/top-layer keywords.  Replaces the legacy Display/
  // Flex*/Justify*/Align*/Grid*/Position/Top/Right/Bottom/Left/ZIndex switch
  // cases below.
  Object.assign(styles, applyLayoutPhase7(properties));

  for (const prop of properties) {
    // Skip properties already served by the engine path above.
    if (!isLegacyProperty(prop.type)) continue;
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
    // Colors/Opacity/AccentColor/CaretColor migrated in Phase 4 (engine/color/*).

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
    // Display / FlexDirection / FlexWrap / JustifyContent / AlignItems /
    // AlignContent / AlignSelf / FlexGrow / FlexShrink / FlexBasis / Order
    // migrated to engine/layout/flexbox/* in Phase 7 (applyLayoutPhase7).

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
    // GridTemplateColumns / GridTemplateRows / GridTemplateAreas,
    // GridAutoColumns / GridAutoRows / GridAutoFlow,
    // GridColumnStart / GridColumnEnd / GridRowStart / GridRowEnd,
    // JustifyItems / JustifySelf / AlignTracks / JustifyTracks / MasonryAutoFlow
    // migrated to engine/layout/grid/* in Phase 7.
    // The `GridColumn` / `GridRow` / `GridArea` shorthands are expanded by
    // the Kotlin parser before reaching us, so no legacy case is needed.

    // ==================== Position ====================
    // Position / Top / Right / Bottom / Left, Inset{Block,Inline}{Start,End},
    // and ZIndex migrated to engine/layout/position/* in Phase 7.

    // ==================== Borders ====================
    // Migrated to engine/borders/* in Phase 5 — all Border*Width/Style/Color,
    // every BorderRadius variant, BorderImage*, Outline*, BoxShadow,
    // BoxDecorationBreak, CornerShape, BorderBoundary flow through
    // applyBordersPhase5 above.  The legacy `BorderRadius` shorthand
    // (as opposed to the per-corner longhands) is expanded by the Kotlin
    // CSS parser before it reaches us, so no case here is needed.

    // ==================== Typography ====================
    // Migrated to engine/typography/* in Phase 6 — all font-*, line-*,
    // letter/word-spacing, text-*, white-space, word-break, overflow-wrap,
    // line-break, hyphens, direction, writing-mode, ruby-*, vertical-align,
    // text-shadow, text-overflow, line-clamp, quotes, hanging-punctuation,
    // initial-letter*, orphans, widows, text-rendering, kerning, line-grid,
    // line-snap, tab-size, etc. flow through `applyTypographyPhase6` above.

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
    // Migrated to engine/effects/shadow/BoxShadow* in Phase 5.

    // ==================== Text Shadow ====================
    // Migrated to engine/typography/TextShadow* in Phase 6.

    // ==================== Background Image ====================
    // BackgroundImage/Size/Position/Repeat/Clip/Origin/Attachment migrated
    // in Phase 4 (engine/background/*).

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
    // Migrated to engine/borders/outline/Outline* in Phase 5.

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
      const op = legacyExtractBackgroundPosition(data);
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
    // MixBlendMode migrated in Phase 4 (engine/effects/blend/*).

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

// extractBorderWidth removed — now handled by engine/borders/sides/_shared.ts

// Legacy typography extractors (FontSize/FontWeight/FontFamily/LineHeight)
// removed in Phase 6 — replaced by the per-property triplets under
// engine/typography/*.

// extractGridTemplate / extractGridSpan removed in Phase 7 — replaced by
// engine/layout/grid/_grid_shared.ts (trackSize / renderTrackList / gridLine).

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

// Legacy extractBoxShadow removed — now handled by engine/effects/shadow/BoxShadow*

// Legacy extractTextShadow removed in Phase 6 — see engine/typography/TextShadow*.

// Legacy background-position helper retained for `ObjectPosition` (not yet migrated).
function legacyExtractBackgroundPosition(data: unknown): string | null {
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
