#!/usr/bin/env node
//
// capture-screenshots.mjs
//
// Drives the web app in `?mode=capture` (a chromeless, paginated-free,
// single-page flat list of CaptureCanvas elements) and writes one PNG per
// component to testing/web/screenshots/.
//
// Output contract (matches iOS `CaptureCanvas.swift` + Android `CaptureCanvas`):
//   - width         : exactly 390 px
//   - height        : natural (no clamping)
//   - background    : solid #1A1A2E
//   - padding       : 16 px
//   - no gallery chrome of any kind
//
// Assumes the dev server is already running. test-all.sh starts it.
//
// Usage:
//     node capture-screenshots.mjs [--url http://localhost:3000] [--out screenshots]
//

import puppeteer from 'puppeteer';
import { mkdirSync, rmSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));

// ── Args ─────────────────────────────────────────────────────────────────────
const args = process.argv.slice(2);
const baseUrl = getArg('--url') ?? 'http://localhost:3000';
const outDir  = resolve(__dirname, getArg('--out') ?? 'screenshots');

function getArg(name) {
  const i = args.indexOf(name);
  return i >= 0 ? args[i + 1] : undefined;
}

// ── Setup ────────────────────────────────────────────────────────────────────
rmSync(outDir, { recursive: true, force: true });
mkdirSync(outDir, { recursive: true });

// `headless: 'new'` uses the modern Chrome/Chromium headless rendering
// path. The legacy `headless: true` (aka 'shell') suppresses paint / rAF
// callbacks when no display is attached — that's what was causing
// `Runtime.callFunctionOn timed out` during per-element screenshots once
// the page grew beyond ~50 canvases. `'new'` keeps paint running and
// lets elementHandle.screenshot do its internal bounding-box evaluation
// reliably.
//
// `protocolTimeout` bumped from the 30s default so a slow per-element
// screenshot loop (109 captures × ~300ms each) doesn't overflow the
// single-call limit on a cold CI machine.
const browser = await puppeteer.launch({
  headless: 'new',
  protocolTimeout: 180_000,
  args: [
    '--disable-background-timer-throttling',
    '--disable-renderer-backgrounding',
    '--disable-backgrounding-occluded-windows',
  ],
});
try {
  const page = await browser.newPage();

  // 390 px viewport at 1x scale. iOS captures at scale=1.0 and Android at
  // 160 dpi (1dp == 1px), so we match that exactly.
  await page.setViewport({ width: 390, height: 844, deviceScaleFactor: 1 });

  // Page-error = an actual uncaught exception. Always report these.
  page.on('pageerror', (err) => console.error('[pageerror]', err.message));

  // Console errors are noisier — React dev-mode warnings show up here as
  // "error" level. Filter out the pre-existing property-naming warnings
  // that come from `ComponentRenderer.tsx` setting kebab-case style props
  // (an unrelated web-renderer issue that we don't own in this script).
  // Anything else is logged so real problems surface.
  const ignoredRE = /Unsupported style property|Failed to load resource:.*404/;
  page.on('console', (msg) => {
    if (msg.type() !== 'error') return;
    const text = msg.text();
    if (ignoredRE.test(text)) return;
    console.error('[console error]', text);
  });

  const captureUrl = `${baseUrl.replace(/\/$/, '')}/?mode=capture`;
  console.log(`→ loading ${captureUrl}`);
  await page.goto(captureUrl, { waitUntil: 'domcontentloaded', timeout: 60_000 });

  // Wait for the CaptureGallery sentinel (emitted after the IR is loaded and
  // every canvas has been rendered). `data-capture-ready` holds the expected
  // canvas count, so we can compare against the live canvas count below.
  await page.waitForSelector('[data-capture-ready]', { timeout: 30_000 });

  const [expected, actual] = await page.evaluate(() => {
    const sentinel = document.querySelector('[data-capture-ready]');
    const expected = Number(sentinel?.getAttribute('data-capture-ready') ?? '0');
    const actual   = document.querySelectorAll('[data-capture-canvas]').length;
    return [expected, actual];
  });
  console.log(`  gallery reports ${actual} / ${expected} canvases rendered`);

  if (actual !== expected) {
    throw new Error(`Canvas count mismatch (${actual} / ${expected})`);
  }

  // Let any lingering async work (images, fonts) settle before we screenshot.
  await page.evaluate(() =>
    document.fonts?.ready ?? Promise.resolve()
  );
  // Brief post-render settle. We can't use requestAnimationFrame here —
  // headless Chrome throttles (and in some versions outright suppresses)
  // RAF callbacks when the page isn't actually being painted to a display,
  // which makes an `await page.evaluate(() => new Promise(r => rAF(r)))`
  // hang until the protocol timeout fires. setTimeout stays on the JS
  // task queue and is not throttled the same way.
  await page.evaluate(() =>
    new Promise((r) => setTimeout(r, 50))
  );

  // ── Capture ────────────────────────────────────────────────────────────────
  //
  // We fetch the manifest **and every bounding rect** in a single
  // `$$eval` up-front, then resize the viewport to the full page height,
  // take ONE full-page screenshot, and crop N PNGs from it with sharp.
  //
  // Why this instead of elementHandle.screenshot()?
  //   Puppeteer's per-element screenshot path internally calls
  //   ElementHandle.evaluate(...) to resolve the clip rect, and each
  //   such call has to round-trip through CDP's Runtime.callFunctionOn.
  //   When the page grows past ~50 canvases, that per-element evaluate
  //   starts timing out (`Runtime.callFunctionOn timed out`) on headless
  //   Chrome — probably paint-gated layout queries getting throttled.
  //   One page screenshot + N CPU-side sharp crops avoids every single
  //   one of those round-trips.
  const manifest = await page.$$eval('[data-capture-canvas]', (els) =>
    els.map((el) => {
      const r = el.getBoundingClientRect();
      return {
        index: Number(el.getAttribute('data-capture-index')),
        id:    el.getAttribute('data-capture-id') ?? '',
        name:  el.getAttribute('data-capture-name') ?? 'unknown',
        x:     Math.round(r.left),
        y:     Math.round(r.top + window.scrollY),  // include current scroll offset
        width: Math.round(r.width),
        height: Math.round(r.height),
      };
    })
  );

  // Capture gallery is a flat vertical list starting at (0,0); the page
  // height is the sum of all canvas heights (~20 000 px for 109 canvases).
  //
  // Tried `page.screenshot({ fullPage: true })` — times out on large
  // pages because Puppeteer internally stitches frame-by-frame.
  //
  // Fix: resize the viewport to exactly the document height, then take
  // a single regular (non-full-page) screenshot. Chrome renders a tall
  // viewport in one go without the stitching overhead.
  const pageHeight = await page.evaluate(() => Math.max(
    document.documentElement.scrollHeight,
    document.body?.scrollHeight ?? 0,
  ));
  await page.setViewport({ width: 390, height: pageHeight, deviceScaleFactor: 1 });
  // Give layout one tick to settle into the new viewport.
  await page.evaluate(() => new Promise((r) => setTimeout(r, 50)));

  console.log(`  capturing ${manifest.length} canvases via ${390}×${pageHeight} screenshot → ${outDir}`);
  const fullPng = await page.screenshot({ type: 'png' });

  // Crop with sharp — CPU-only, no more round-trips to the browser.
  const sharpMod = await import('sharp');
  const sharp = sharpMod.default;
  let captured = 0;
  for (const entry of manifest) {
    const safe = entry.name.replace(/[^A-Za-z0-9._-]/g, '_');
    const filename = `${String(entry.index).padStart(3, '0')}_${safe}.png`;

    // Defensive clamp: rects with zero dims (hidden elements) are
    // skipped so sharp doesn't throw; 0-dim canvases legitimately
    // shouldn't exist in capture mode but we log if they do.
    if (entry.width <= 0 || entry.height <= 0) {
      console.warn(`  ⚠ canvas ${entry.index} (${entry.name}) has zero dimensions — skipping`);
      continue;
    }

    await sharp(fullPng)
      .extract({
        left:   entry.x,
        top:    entry.y,
        width:  entry.width,
        height: entry.height,
      })
      .toFile(resolve(outDir, filename));
    captured += 1;
  }

  console.log(`✓ captured ${captured} / ${manifest.length} canvases`);
} finally {
  await browser.close();
}
