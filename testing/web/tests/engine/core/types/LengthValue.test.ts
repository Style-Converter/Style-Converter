// Tests for extractLength — one test per documented IR shape (see examples/primitives/lengths-*.json).
import { describe, it, expect } from 'vitest';
import { extractLength, toCssLength } from '../../../../src/style/engine/core/types/LengthValue';

describe('extractLength', () => {
  // ---- absolute units (lengths-absolute.json) --------------------------------
  it('handles raw { px:N } shape with no wrapper', () => {
    expect(extractLength({ px: 20 })).toEqual({ kind: 'exact', px: 20 });
  });

  it('handles { type:"length", px:N } wrapper (Width case)', () => {
    expect(extractLength({ type: 'length', px: 200 })).toEqual({ kind: 'exact', px: 200 });
  });

  it('handles { type:"length", px:N, original:{v,u:"PT"} } — px wins over original', () => {
    expect(extractLength({ type: 'length', px: 160, original: { v: 120, u: 'PT' } }))
      .toEqual({ kind: 'exact', px: 160 });
  });

  // ---- font-relative (lengths-font-relative.json) ----------------------------
  it('handles font-relative em with no px (context-dependent)', () => {
    expect(extractLength({ type: 'length', original: { v: 2, u: 'EM' } }))
      .toEqual({ kind: 'relative', value: 2, unit: 'em' });
  });

  it('handles rem / rlh / cap / ch / ic / lh / ex units', () => {
    for (const u of ['REM','RLH','CAP','CH','IC','LH','EX']) {
      const out = extractLength({ type: 'length', original: { v: 3, u } });
      expect(out.kind).toBe('relative');
    }
  });

  // ---- viewport (lengths-viewport.json) --------------------------------------
  it('handles classic viewport units', () => {
    expect(extractLength({ type: 'length', original: { v: 50, u: 'VW' } }))
      .toEqual({ kind: 'relative', value: 50, unit: 'vw' });
  });

  it('handles small/large/dynamic viewport variants', () => {
    for (const u of ['SVW','LVH','DVMIN','SVMAX','LVI','DVB']) {
      const out = extractLength({ type: 'length', original: { v: 30, u } });
      expect(out.kind).toBe('relative');
    }
  });

  // ---- container-query (lengths-container.json) ------------------------------
  it('handles container-query units', () => {
    expect(extractLength({ type: 'length', original: { v: 50, u: 'CQW' } }))
      .toEqual({ kind: 'relative', value: 50, unit: 'cqw' });
    expect(extractLength({ type: 'length', original: { v: 30, u: 'CQMIN' } }).kind).toBe('relative');
  });

  // ---- intrinsic (lengths-intrinsic.json) ------------------------------------
  it('handles bare "auto" string', () => {
    expect(extractLength('auto')).toEqual({ kind: 'auto' });
  });

  it('handles bare "min-content" / "max-content" / "fit-content"', () => {
    expect(extractLength('min-content')).toEqual({ kind: 'intrinsic', intrinsicKind: 'min-content' });
    expect(extractLength('max-content')).toEqual({ kind: 'intrinsic', intrinsicKind: 'max-content' });
    expect(extractLength('fit-content')).toEqual({ kind: 'intrinsic', intrinsicKind: 'fit-content' });
  });

  // ---- percentage and fr (lengths-special.json) ------------------------------
  it('handles { type:"percentage", value:N }', () => {
    expect(extractLength({ type: 'percentage', value: 50 }))
      .toEqual({ kind: 'relative', value: 50, unit: 'percent' });
  });

  it('handles { fr:N } grid-fraction shape', () => {
    expect(extractLength({ fr: 2 })).toEqual({ kind: 'fraction', fr: 2 });
  });

  // ---- edge cases ------------------------------------------------------------
  it('returns unknown on null/undefined/empty', () => {
    expect(extractLength(null).kind).toBe('unknown');
    expect(extractLength(undefined).kind).toBe('unknown');
    expect(extractLength({}).kind).toBe('unknown');
  });

  it('returns unknown on unrecognised unit', () => {
    expect(extractLength({ original: { v: 5, u: 'NOPE' } }).kind).toBe('unknown');
  });

  it('treats bare number as percentage (Phase-2 margin/padding convention)', () => {
    // Margin_Percent_10 / Padding_Percent_10 fixtures emit bare JSON numbers for %.
    expect(extractLength(10)).toEqual({ kind: 'relative', value: 10, unit: 'percent' });
    expect(extractLength(0)).toEqual({ kind: 'relative', value: 0, unit: 'percent' });
  });

  it('handles calc expression shape ({type:"calc",expression:...})', () => {
    expect(extractLength({ type: 'calc', expression: '100% - 10px' }))
      .toEqual({ kind: 'calc', expression: '100% - 10px' });
  });

  it('handles {expr:"calc(...)"} shape (Phase-2 spacing survey)', () => {
    // Spacing fixtures emit {expr:"calc(10px + 5px)"}; outer calc() wrapper stripped.
    expect(extractLength({ expr: 'calc(10px + 5px)' }))
      .toEqual({ kind: 'calc', expression: '10px + 5px' });
  });

  it('handles {expr:"..."} without an outer calc() wrapper', () => {
    expect(extractLength({ expr: '100% - 2em' }))
      .toEqual({ kind: 'calc', expression: '100% - 2em' });
  });
});

describe('toCssLength', () => {
  it('formats every kind', () => {
    expect(toCssLength({ kind: 'exact', px: 20 })).toBe('20px');
    expect(toCssLength({ kind: 'relative', value: 50, unit: 'percent' })).toBe('50%');
    expect(toCssLength({ kind: 'relative', value: 2, unit: 'em' })).toBe('2em');
    expect(toCssLength({ kind: 'auto' })).toBe('auto');
    expect(toCssLength({ kind: 'intrinsic', intrinsicKind: 'min-content' })).toBe('min-content');
    expect(toCssLength({ kind: 'fraction', fr: 1 })).toBe('1fr');
    expect(toCssLength({ kind: 'calc', expression: '10px + 5%' })).toBe('calc(10px + 5%)');
    expect(toCssLength({ kind: 'unknown' })).toBe('auto');
  });
});
