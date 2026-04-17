#!/usr/bin/env node
//
// compare-screenshots.mjs
//
// Cross-platform component screenshot comparison.
//
// Reads per-component PNGs from:
//   testing/iOS/screenshots/
//   testing/Android/screenshots/
//   testing/web/screenshots/
//
// For each component (matched by filename like `012_Color_HSL.png`) it:
//
//   1. Normalizes all three images to the same dimensions (padding rather
//      than stretching, so borders / shadows / layouts stay aligned).
//   2. Computes a pairwise **pixelmatch** diff — percentage of pixels
//      whose RGB differs by more than an anti-aliasing-aware threshold.
//   3. Computes a pairwise **SSIM** (structural similarity) score — a
//      perceptual metric that tolerates font rendering / subpixel AA but
//      still catches "the shadow is missing" / "the shape is wrong".
//   4. Optionally diffs against a committed baseline in testing/baseline/
//      and fails hard if any component regresses beyond the threshold.
//
// Outputs:
//   - testing/report/                  (HTML report + diff PNGs + manifest.json)
//   - testing/report/index.html        (open in any browser)
//   - testing/report/manifest.json     (structured data for CI / tooling)
//
// Exit codes:
//   0 — success or no baseline
//   1 — at least one component regressed beyond thresholds
//   2 — script / IO error
//
// Usage:
//     node compare-screenshots.mjs [options]
//
// Options:
//     --baseline                Compare against testing/baseline/ and fail
//                               on regressions. Without this flag only the
//                               cross-platform report is generated.
//     --update-baseline         Copy the current captures into testing/baseline/.
//                               Use after intentional visual changes.
//     --ssim-threshold <n>      Minimum SSIM for a component to pass against
//                               the baseline (default 0.95).
//     --pixel-threshold <n>     Max % of pixels allowed to differ (default 2).
//

import { readdirSync, existsSync, mkdirSync, rmSync, copyFileSync, readFileSync, writeFileSync } from 'node:fs';
import { resolve, dirname, basename, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { PNG } from 'pngjs';
import sharp from 'sharp';
import pixelmatchDefault from 'pixelmatch';
import { ssim as computeSsim } from 'ssim.js';

const pixelmatch = pixelmatchDefault.default ?? pixelmatchDefault;

const __dirname = dirname(fileURLToPath(import.meta.url));

// ── Paths ────────────────────────────────────────────────────────────────────
const PLATFORMS = ['iOS', 'Android', 'web'];

const paths = {
  iOS:     resolve(__dirname, 'iOS/screenshots'),
  Android: resolve(__dirname, 'Android/screenshots'),
  web:     resolve(__dirname, 'web/screenshots'),
  baseline: resolve(__dirname, 'baseline'),
  report:   resolve(__dirname, 'report'),
};

// ── Args ─────────────────────────────────────────────────────────────────────
const args = process.argv.slice(2);
const useBaseline    = args.includes('--baseline');
const updateBaseline = args.includes('--update-baseline');
const ssimThreshold  = Number(getArg('--ssim-threshold') ?? 0.95);
const pixelThreshold = Number(getArg('--pixel-threshold') ?? 2);
// Optional label (e.g. the input IR filename) so the report headline says
// which test case it was generated from. Populated by test-all.sh.
const inputLabel     = getArg('--input') ?? process.env.TEST_INPUT ?? '';

function getArg(name) {
  const i = args.indexOf(name);
  return i >= 0 ? args[i + 1] : undefined;
}

// ── Baseline update shortcut ─────────────────────────────────────────────────
if (updateBaseline) {
  await syncBaseline();
  process.exit(0);
}

// ── Main ─────────────────────────────────────────────────────────────────────
// `await main()` is placed at the bottom of the file so all module-level
// `const` declarations (including BASE_CSS below) have been initialized
// before the async work starts reaching into them. ESM hoists bindings but
// not initializers; accessing a const before its declaration throws
// "Cannot access X before initialization" (TDZ).

async function main() {
  console.log('→ collecting captures…');
  const captures = collectCaptures();
  const names = sortedUnion(captures);
  console.log(`  ${names.length} unique component(s) across platforms`);

  rmSync(paths.report, { recursive: true, force: true });
  mkdirSync(paths.report, { recursive: true });
  mkdirSync(join(paths.report, 'diffs'), { recursive: true });
  mkdirSync(join(paths.report, 'images'), { recursive: true });

  // Copy originals into report/images/ so the HTML can reference them without
  // relying on relative paths that break when the report is moved.
  for (const p of PLATFORMS) {
    const dst = join(paths.report, 'images', p);
    mkdirSync(dst, { recursive: true });
    for (const name of Object.keys(captures[p])) {
      copyFileSync(captures[p][name], join(dst, name));
    }
  }
  if (useBaseline && existsSync(paths.baseline)) {
    const dst = join(paths.report, 'images', 'baseline');
    mkdirSync(dst, { recursive: true });
    for (const f of readdirSync(paths.baseline)) {
      if (f.endsWith('.png')) copyFileSync(join(paths.baseline, f), join(dst, f));
    }
  }

  // Per-component analysis.
  const rows = [];
  let regressionCount = 0;

  for (const name of names) {
    process.stdout.write(`  ${name}… `);
    const row = await analyzeComponent(name, captures);
    rows.push(row);

    if (useBaseline && row.baseline && row.baseline.regressed) {
      regressionCount += 1;
      process.stdout.write('REGRESSION\n');
    } else {
      process.stdout.write(summarize(row) + '\n');
    }
  }

  // Write HTML + manifest.
  const manifest = {
    generatedAt: new Date().toISOString(),
    inputLabel,
    thresholds: { ssim: ssimThreshold, pixel: pixelThreshold },
    rows,
  };
  writeFileSync(
    join(paths.report, 'manifest.json'),
    JSON.stringify(manifest, null, 2)
  );
  writeFileSync(
    join(paths.report, 'index.html'),
    renderHTML(rows, {
      useBaseline,
      ssimThreshold,
      pixelThreshold,
      regressionCount,
      inputLabel,
    })
  );

  console.log(`\n✓ report: ${join(paths.report, 'index.html')}`);
  if (useBaseline) {
    // Count how many baseline comparisons actually ran. A row only counts
    // if its .baseline.platforms contained at least one pair of {current,
    // baseline} images. Without this check, running `--baseline` with
    // zero captures would print "✓ no regressions" (because the regressed
    // counter stayed at 0) and falsely green-light CI.
    let checked = 0;
    for (const r of rows) {
      if (!r.baseline) continue;
      for (const info of Object.values(r.baseline.platforms)) {
        if (info && info.ssim !== undefined) checked += 1;
      }
    }

    if (checked === 0) {
      console.error('✗ baseline mode requested but 0 comparisons ran');
      console.error('  (no current captures match any baseline image — is the baseline empty,');
      console.error('   or did every capture step fail?)');
      process.exit(2);
    }

    if (regressionCount > 0) {
      console.error(`✗ ${regressionCount} component(s) regressed beyond thresholds (${checked} platform-comparisons ran)`);
      process.exit(1);
    } else {
      console.log(`✓ no regressions vs baseline (${checked} platform-comparisons ran)`);
    }
  }
}

// ─────────────────────────────────────────────────────────────────────────────

function collectCaptures() {
  const captures = { iOS: {}, Android: {}, web: {} };
  for (const p of PLATFORMS) {
    if (!existsSync(paths[p])) {
      console.warn(`  ⚠ ${p}: ${paths[p]} not found`);
      continue;
    }
    for (const f of readdirSync(paths[p])) {
      if (!f.endsWith('.png')) continue;
      // Skip ancillary images (e.g. iOS simulator.png)
      if (/^simulator|^logcat/.test(f)) continue;
      captures[p][f] = join(paths[p], f);
    }
  }
  return captures;
}

function sortedUnion(captures) {
  const all = new Set();
  for (const p of PLATFORMS) Object.keys(captures[p]).forEach((k) => all.add(k));
  return [...all].sort();
}

async function analyzeComponent(name, captures) {
  // Load + align every platform's PNG for this component. A single
  // corrupt / non-PNG file in any screenshots directory used to crash
  // the whole comparison run — now we isolate the failure per-platform
  // and surface it as `platforms[p].error` so the report can show which
  // file is broken without losing the rest of the comparison.
  const loaded = {};
  const loadErrors = {};
  for (const p of PLATFORMS) {
    const path = captures[p][name];
    if (!path) { loaded[p] = null; continue; }
    try {
      loaded[p] = await loadPng(path);
    } catch (e) {
      loaded[p] = null;
      loadErrors[p] = e.message ?? String(e);
      console.warn(`  ⚠ ${p}/${name}: ${loadErrors[p]}`);
    }
  }

  // Compute shared canvas size: the max width (platforms should all be 390
  // by contract — if they're not, we flag it) and max height so everyone
  // gets padded identically.
  const nonNull = PLATFORMS.map((p) => loaded[p]).filter(Boolean);
  if (nonNull.length === 0) {
    // Every platform failed to decode (or files missing). Return a
    // well-shaped row so `renderRow` can still produce a sensible entry
    // per-platform error cell, rather than crashing on undefined
    // lookups in `r.platforms[p]`.
    const platforms = {};
    for (const p of PLATFORMS) {
      if (loadErrors[p]) platforms[p] = { present: false, error: loadErrors[p] };
      else platforms[p] = { present: false };
    }
    const pairs = {
      'iOS-Android': null, 'iOS-web': null, 'Android-web': null,
    };
    return { name, platforms, pairs, baseline: null, errors: loadErrors };
  }
  const canvasW = Math.max(...nonNull.map((i) => i.width));
  const canvasH = Math.max(...nonNull.map((i) => i.height));

  const normalized = {};
  for (const p of PLATFORMS) {
    normalized[p] = loaded[p]
      ? await padToCanvas(loaded[p], canvasW, canvasH)
      : null;
  }

  // Pairwise comparisons for the HTML report.
  const pairs = {};
  const pairSpecs = [
    ['iOS', 'Android'],
    ['iOS', 'web'],
    ['Android', 'web'],
  ];

  for (const [a, b] of pairSpecs) {
    const key = `${a}-${b}`;
    if (!normalized[a] || !normalized[b]) {
      pairs[key] = null;
      continue;
    }
    pairs[key] = await diffPair(
      normalized[a],
      normalized[b],
      canvasW,
      canvasH,
      `${name.replace(/\.png$/, '')}__${key}.png`
    );
  }

  // Per-platform dimension metadata. `error` populated when the PNG failed
  // to decode; the HTML report renders it as a red cell instead of "missing".
  const platforms = {};
  for (const p of PLATFORMS) {
    if (loaded[p]) {
      platforms[p] = {
        present: true,
        width: loaded[p].width,
        height: loaded[p].height,
        image: `images/${p}/${name}`,
      };
    } else if (loadErrors[p]) {
      platforms[p] = { present: false, error: loadErrors[p] };
    } else {
      platforms[p] = { present: false };
    }
  }

  // Baseline comparison — we compare each platform's current capture to its
  // baseline counterpart (same platform, same component name).
  let baseline = null;
  if (useBaseline && existsSync(paths.baseline)) {
    baseline = await compareBaseline(name, normalized, canvasW, canvasH);
  }

  return { name, canvasW, canvasH, platforms, pairs, baseline };
}

async function loadPng(path) {
  const buf = readFileSync(path);
  return PNG.sync.read(buf);
}

/**
 * Pad `img` (width × height) onto a (W × H) canvas filled with the standard
 * capture background (#1A1A2E) — no stretching, no resampling. Keeps the
 * component pixel-aligned even when one platform produced a taller image
 * than another.
 */
async function padToCanvas(img, W, H) {
  if (img.width === W && img.height === H) return img;

  const padded = await sharp(PNG.sync.write(img))
    .extend({
      top: 0,
      bottom: Math.max(0, H - img.height),
      left: 0,
      right: Math.max(0, W - img.width),
      background: { r: 0x1A, g: 0x1A, b: 0x2E, alpha: 1 },
    })
    .png()
    .toBuffer();

  return PNG.sync.read(padded);
}

/**
 * Pixel-diff via pixelmatch + SSIM via ssim.js. Returns diff metrics and
 * writes a visual diff PNG to the report folder.
 */
async function diffPair(a, b, W, H, diffFilename) {
  const diff = new PNG({ width: W, height: H });
  // `threshold` controls per-pixel color-delta tolerance. 0.25 is generous
  // enough that cross-platform font AA doesn't flag entire glyph edges as
  // mismatches, but still catches real changes (colors shifting, borders
  // appearing / disappearing, shadows moving).
  // `includeAA: true` skips AA pixels entirely — usually desirable for this
  // kind of cross-renderer comparison.
  const mismatched = pixelmatch(a.data, b.data, diff.data, W, H, {
    threshold: 0.25,
    includeAA: true,
    diffColor: [255, 80, 80],
    alpha: 0.15,
  });

  const diffPath = join(paths.report, 'diffs', diffFilename);
  writeFileSync(diffPath, PNG.sync.write(diff));

  const pixelPct = (mismatched / (W * H)) * 100;

  // SSIM — rescale to a small grid if the image is huge to keep this fast.
  const ssimScore = await safeSsim(a, b);

  return {
    pixelMismatchedCount: mismatched,
    pixelMismatchedPct: +pixelPct.toFixed(3),
    ssim: ssimScore,
    diffImage: `diffs/${diffFilename}`,
  };
}

async function safeSsim(a, b) {
  // ssim.js expects ImageData-like objects (data: Uint8ClampedArray, width, height)
  try {
    const aImg = { data: new Uint8ClampedArray(a.data), width: a.width, height: a.height };
    const bImg = { data: new Uint8ClampedArray(b.data), width: b.width, height: b.height };
    const r = computeSsim(aImg, bImg, { ssim: 'fast' });
    return +r.mssim.toFixed(4);
  } catch (e) {
    return null;
  }
}

async function compareBaseline(name, normalized, canvasW, canvasH) {
  const result = { platforms: {}, regressed: false };

  for (const p of PLATFORMS) {
    const baselinePath = join(paths.baseline, `${p}__${name}`);
    if (!existsSync(baselinePath) || !normalized[p]) {
      result.platforms[p] = { present: false };
      continue;
    }
    const baseImg = await padToCanvas(await loadPng(baselinePath), canvasW, canvasH);
    const pair = await diffPair(
      baseImg,
      normalized[p],
      canvasW,
      canvasH,
      `${name.replace(/\.png$/, '')}__baseline-${p}.png`
    );
    const regressed =
      pair.pixelMismatchedPct > pixelThreshold ||
      (pair.ssim !== null && pair.ssim < ssimThreshold);
    if (regressed) result.regressed = true;
    result.platforms[p] = { ...pair, regressed };
  }

  return result;
}

async function syncBaseline() {
  console.log('→ updating baseline from current captures…');

  // Guard against clobbering a good baseline with nothing. If the current
  // capture run produced zero PNGs (e.g. test-all.sh was run with all
  // platforms skipped, or every platform failed), bail out rather than
  // silently wiping testing/baseline/ and pretending we updated it.
  let totalAvailable = 0;
  for (const p of PLATFORMS) {
    if (!existsSync(paths[p])) continue;
    totalAvailable += readdirSync(paths[p])
      .filter((f) => f.endsWith('.png') && !/^simulator|^logcat/.test(f))
      .length;
  }
  if (totalAvailable === 0) {
    console.error('✗ refusing to update baseline: no current captures found.');
    console.error('  expected PNGs under testing/{iOS,Android,web}/screenshots/');
    console.error('  (did a capture step fail, or was everything skipped?)');
    process.exit(2);
  }

  rmSync(paths.baseline, { recursive: true, force: true });
  mkdirSync(paths.baseline, { recursive: true });
  let count = 0;
  for (const p of PLATFORMS) {
    if (!existsSync(paths[p])) continue;
    for (const f of readdirSync(paths[p])) {
      if (!f.endsWith('.png')) continue;
      if (/^simulator|^logcat/.test(f)) continue;
      copyFileSync(join(paths[p], f), join(paths.baseline, `${p}__${f}`));
      count += 1;
    }
  }
  console.log(`✓ wrote ${count} baseline image(s) to ${paths.baseline}`);
}

function summarize(row) {
  const bits = [];
  for (const [k, v] of Object.entries(row.pairs)) {
    if (!v) continue;
    bits.push(`${k} ${v.ssim?.toFixed(2) ?? '—'}`);
  }
  return bits.join(' · ');
}

// ─────────────────────────────────────────────────────────────────────────────
// HTML report
// ─────────────────────────────────────────────────────────────────────────────

function renderHTML(rows, opts) {
  // "Identical" threshold: SSIM >= 0.97 across every pair. SSIM is
  // perceptually-grounded and handles font AA naturally. Raw pixel diffs
  // are dominated by subpixel AA differences and aren't a useful summary
  // metric, but they're still shown per-pair for drill-down.
  const isIdentical = (r) =>
    Object.values(r.pairs)
      .filter(Boolean)
      .every((p) => p.ssim !== null && p.ssim >= 0.97);

  const totals = {
    components: rows.length,
    regressions: opts.useBaseline
      ? rows.filter((r) => r.baseline?.regressed).length
      : 0,
    identical: rows.filter(isIdentical).length,
  };

  const rowsHtml = rows.map((r) => renderRow(r, opts)).join('\n');

  // Stash a minimum-SSIM + searchable name on each row's DOM node so the
  // client-side sort + filter can work without re-parsing metrics.
  const labelHtml = opts.inputLabel
    ? ` <span class="input-label">(${escape(opts.inputLabel)})</span>`
    : '';

  return `<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Style Converter — cross-platform capture report</title>
<style>
${BASE_CSS}
</style>
</head>
<body>
<header class="top">
  <h1>Cross-platform capture report${labelHtml}</h1>
  <p class="meta">
    ${totals.components} components · ${totals.identical} identical (SSIM &ge; 0.97)
    ${opts.useBaseline ? ` · <span class="${totals.regressions > 0 ? 'bad' : 'ok'}">${totals.regressions} regressions</span>` : ''}
    · SSIM threshold ${opts.ssimThreshold} · pixel threshold ${opts.pixelThreshold}%
  </p>
  <p class="meta">
    Generated ${new Date().toLocaleString()}
  </p>
  <div class="controls">
    <input type="search" id="search" placeholder="Filter components by name…" autocomplete="off">
    <label><input type="checkbox" id="onlyDiff"> diffs only</label>
    <label for="sort">sort:</label>
    <select id="sort">
      <option value="index">by index (default)</option>
      <option value="worst">worst SSIM first</option>
      <option value="best">best SSIM first</option>
      <option value="name">by name</option>
    </select>
    <span class="visible-count" id="visibleCount"></span>
  </div>
</header>

<main id="rows">
${rowsHtml}
</main>

<script>
(() => {
  const rowsEl = document.getElementById('rows');
  const rows = Array.from(rowsEl.querySelectorAll('.row'));
  const search = document.getElementById('search');
  const onlyDiff = document.getElementById('onlyDiff');
  const sortSel = document.getElementById('sort');
  const countEl = document.getElementById('visibleCount');

  function minSsim(r) {
    // Data attribute written server-side; parseFloat never returns NaN for
    // valid values, and falls back to 1 so rows missing the attr stay at
    // the "best" end of sorts.
    const v = parseFloat(r.getAttribute('data-min-ssim'));
    return Number.isFinite(v) ? v : 1;
  }

  function apply() {
    const q = (search.value || '').toLowerCase().trim();
    const diffsOnly = onlyDiff.checked;
    const sortBy = sortSel.value;

    // Sort (detach + reattach children — O(n) vs O(n²) for reorder loops).
    const ordered = [...rows];
    if (sortBy === 'worst') ordered.sort((a, b) => minSsim(a) - minSsim(b));
    else if (sortBy === 'best') ordered.sort((a, b) => minSsim(b) - minSsim(a));
    else if (sortBy === 'name') ordered.sort((a, b) =>
      a.dataset.name.localeCompare(b.dataset.name));
    else ordered.sort((a, b) =>
      Number(a.dataset.index) - Number(b.dataset.index));

    const frag = document.createDocumentFragment();
    let visible = 0;
    for (const r of ordered) {
      const matches = !q || r.dataset.name.toLowerCase().includes(q);
      const diffOk = !diffsOnly || r.classList.contains('has-diff') || r.classList.contains('regressed');
      if (matches && diffOk) {
        r.style.display = '';
        visible++;
      } else {
        r.style.display = 'none';
      }
      frag.appendChild(r);
    }
    rowsEl.appendChild(frag);
    countEl.textContent = visible === rows.length
      ? \`\${rows.length} components\`
      : \`\${visible} / \${rows.length} components\`;
  }

  search.addEventListener('input', apply);
  onlyDiff.addEventListener('change', apply);
  sortSel.addEventListener('change', apply);
  apply();

  // Keyboard: '/' focuses search — standard convention.
  document.addEventListener('keydown', (e) => {
    if (e.key === '/' && document.activeElement !== search) {
      e.preventDefault();
      search.focus();
      search.select();
    }
  });
})();
</script>
</body>
</html>`;
}

function renderRow(r, opts) {
  const dimsOk = PLATFORMS
    .map((p) => r.platforms[p])
    .filter((pp) => pp.present)
    .every((pp, _, arr) => pp.width === arr[0].width && pp.height === arr[0].height);

  // A row is marked "has-diff" when any pair drops below the perceptual
  // threshold — in practice this means a structural change (missing shadow,
  // wrong shape, mis-applied color), not just font AA differences.
  const hasDiff = Object.values(r.pairs).some(
    (p) => p && p.ssim !== null && p.ssim < 0.90
  );

  // Lowest SSIM across every pair. Used by the client-side "sort by worst"
  // option. 1.0 means perfect parity; 0.0 means "nothing in common".
  const ssims = Object.values(r.pairs)
    .filter((p) => p && p.ssim !== null)
    .map((p) => p.ssim);
  const minSsim = ssims.length ? Math.min(...ssims).toFixed(4) : '1';

  // Index from the filename prefix (NNN_Foo.png) — client uses it to
  // restore the default "index" sort order after filtering.
  const indexMatch = r.name.match(/^(\d+)_/);
  const index = indexMatch ? Number(indexMatch[1]) : 0;

  const cls = [hasDiff ? 'has-diff' : '', r.baseline?.regressed ? 'regressed' : ''].filter(Boolean).join(' ');

  return `<section class="row ${cls}"
      data-has-diff="${hasDiff}"
      data-min-ssim="${minSsim}"
      data-index="${index}"
      data-name="${escape(r.name)}">
    <h2>
      ${escape(r.name)}
      <span class="min-ssim" title="lowest SSIM across pairs">SSIM ${Number(minSsim).toFixed(3)}</span>
      ${dimsOk ? '' : '<span class="badge warn">size mismatch</span>'}
      ${r.baseline?.regressed ? '<span class="badge bad">regressed</span>' : ''}
    </h2>

    <div class="platforms">
      ${PLATFORMS.map((p) => renderPlatformCell(p, r)).join('')}
    </div>

    <div class="pairs">
      ${renderPair('iOS ↔ Android', r.pairs['iOS-Android'])}
      ${renderPair('iOS ↔ Web',     r.pairs['iOS-web'])}
      ${renderPair('Android ↔ Web', r.pairs['Android-web'])}
    </div>

    ${opts.useBaseline ? renderBaseline(r.baseline) : ''}
  </section>`;
}

function renderPlatformCell(p, r) {
  const pp = r.platforms[p];
  if (pp.error) {
    return `<div class="platform errored">
      <div class="plabel">${p} <span class="bad">decode error</span></div>
      <div class="pimg muted err">${escape(pp.error)}</div>
    </div>`;
  }
  if (!pp.present) {
    return `<div class="platform missing"><div class="plabel">${p}</div><div class="pimg muted">missing</div></div>`;
  }
  // loading="lazy" keeps the browser from decoding 600+ images on report
  // open — cheap win for large reports.
  return `<div class="platform">
    <div class="plabel">${p} <span class="dim">${pp.width}×${pp.height}</span></div>
    <img src="${pp.image}" alt="${p} capture" loading="lazy">
  </div>`;
}

function renderPair(title, pair) {
  if (!pair) return `<div class="pair missing"><div class="ptitle">${title}</div><div class="pimg muted">n/a</div></div>`;
  // Severity tiers keyed on SSIM since it's a perceptual metric:
  //   bad  (< 0.85) — clearly different, likely a rendering bug
  //   warn (< 0.97) — noticeable but may be font/AA noise
  //   ok   — perceptually identical
  const sev = pair.ssim === null ? 'warn'
           : pair.ssim < 0.85 ? 'bad'
           : pair.ssim < 0.97 ? 'warn'
           : 'ok';
  return `<div class="pair ${sev}">
    <div class="ptitle">${title}</div>
    <div class="metrics">
      <span>SSIM <b>${pair.ssim?.toFixed(4) ?? '—'}</b></span>
      <span>Δpx <b>${pair.pixelMismatchedPct.toFixed(2)}%</b></span>
    </div>
    <img src="${pair.diffImage}" alt="${title} diff" loading="lazy">
  </div>`;
}

function renderBaseline(baseline) {
  if (!baseline) return '';
  const cells = PLATFORMS.map((p) => {
    const b = baseline.platforms[p];
    if (!b?.present) return `<div class="pair missing"><div class="ptitle">${p} vs baseline</div><div class="pimg muted">n/a</div></div>`;
    const sev = b.regressed ? 'bad' : b.pixelMismatchedPct > 0.5 ? 'warn' : 'ok';
    return `<div class="pair ${sev}">
      <div class="ptitle">${p} vs baseline ${b.regressed ? '⚠️' : ''}</div>
      <div class="metrics">
        <span>SSIM <b>${b.ssim?.toFixed(4) ?? '—'}</b></span>
        <span>Δpx <b>${b.pixelMismatchedPct.toFixed(2)}%</b></span>
      </div>
      <img src="${b.diffImage}" alt="${p} baseline diff" loading="lazy">
    </div>`;
  }).join('');
  return `<details class="baseline" ${baseline.regressed ? 'open' : ''}>
    <summary>Baseline comparison${baseline.regressed ? ' — REGRESSIONS' : ''}</summary>
    <div class="pairs">${cells}</div>
  </details>`;
}

function escape(s) {
  return s.replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

const BASE_CSS = `
* { box-sizing: border-box; }
body {
  margin: 0;
  font-family: -apple-system, system-ui, sans-serif;
  background: #111;
  color: #eee;
  font-size: 14px;
}
.top {
  position: sticky;
  top: 0;
  z-index: 10;
  padding: 16px 24px;
  background: #1a1a2e;
  border-bottom: 1px solid #333;
}
.top h1 { margin: 0 0 4px 0; font-size: 18px; }
.top h1 .input-label { font-weight: 400; color: #888; font-size: 13px; font-family: ui-monospace, monospace; }
.top .meta { margin: 2px 0; color: #999; font-size: 13px; }

.controls {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}
.controls input[type="search"] {
  flex: 1;
  min-width: 200px;
  max-width: 360px;
  padding: 6px 10px;
  background: rgba(255,255,255,0.05);
  border: 1px solid rgba(255,255,255,0.15);
  border-radius: 4px;
  color: #fff;
  font-size: 13px;
  outline: none;
}
.controls input[type="search"]:focus { border-color: #68a; }
.controls label { color: #aaa; font-size: 13px; cursor: pointer; user-select: none; }
.controls select {
  background: rgba(255,255,255,0.05);
  border: 1px solid rgba(255,255,255,0.15);
  border-radius: 4px;
  color: #fff;
  padding: 5px 8px;
  font-size: 13px;
}
.visible-count { color: #888; font-size: 12px; margin-left: auto; }

main { padding: 16px 24px; }

/* Narrow viewports: stack platform / pair grids instead of horizontal scroll */
@media (max-width: 900px) {
  .platforms, .pairs { grid-template-columns: 1fr !important; }
  main { padding: 12px; }
  .top { padding: 12px; }
  .controls input[type="search"] { max-width: none; }
}

.row {
  margin-bottom: 24px;
  padding: 16px;
  background: #181824;
  border: 1px solid #2a2a3a;
  border-radius: 8px;
}
.row.has-diff { border-color: #663; }
.row.regressed { border-color: #a33; background: #2a1818; }
.row { overflow: hidden; } /* prevent inner overflow bubbling to body */

.row h2 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-family: ui-monospace, monospace;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}
.badge {
  font-family: -apple-system, system-ui, sans-serif;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 3px;
  font-weight: 500;
}
.badge.warn { background: #664; color: #fe9; }
.badge.bad  { background: #622; color: #fcc; }

.min-ssim {
  font-family: -apple-system, system-ui, sans-serif;
  font-size: 11px;
  color: #888;
  font-weight: 400;
  margin-left: auto;
}

.platforms { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-bottom: 12px; }
.platform { background: #0e0e18; padding: 8px; border-radius: 4px; }
.platform .plabel { font-size: 12px; color: #ccc; margin-bottom: 6px; font-family: ui-monospace, monospace; }
.platform .plabel .dim { color: #666; margin-left: 4px; }
.platform img { display: block; width: 100%; max-width: 100%; height: auto; image-rendering: pixelated; }
.platform.missing .pimg.muted { color: #555; text-align: center; padding: 32px 0; font-style: italic; }
.platform.errored { border: 1px solid #a44; }
.platform.errored .pimg.err { color: #f99; padding: 16px; font-family: ui-monospace, monospace; font-size: 11px; word-break: break-word; }

.pairs { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.pair {
  background: #0e0e18;
  padding: 8px;
  border-radius: 4px;
  border-left: 3px solid transparent;
}
.pair.ok   { border-left-color: #4a4; }
.pair.warn { border-left-color: #aa4; }
.pair.bad  { border-left-color: #a44; }
.pair .ptitle { font-size: 12px; color: #ccc; margin-bottom: 6px; font-family: ui-monospace, monospace; }
.pair .metrics { font-size: 11px; color: #aaa; margin-bottom: 6px; display: flex; gap: 12px; }
.pair .metrics b { color: #fff; font-weight: 500; }
.pair img { display: block; width: 100%; max-width: 100%; height: auto; image-rendering: pixelated; }
.pair.missing .pimg.muted { color: #555; text-align: center; padding: 32px 0; font-style: italic; }

.baseline {
  margin-top: 12px;
  padding: 8px 12px;
  background: #12121c;
  border-radius: 4px;
}
.baseline summary { cursor: pointer; color: #aaa; font-size: 12px; }
.baseline[open] summary { margin-bottom: 8px; }

.ok  { color: #6c6; }
.bad { color: #f99; }

/* (Client-side JS handles diff-only filtering via row.style.display now —
   the old CSS-based .filter-diffs toggle is no longer used.) */
`;

// Entry point — placed at EOF so every module-level const above has already
// been initialized before we start rendering HTML.
await main();
