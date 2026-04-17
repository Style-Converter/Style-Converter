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

const browser = await puppeteer.launch({ headless: true });
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
  await page.evaluate(() =>
    new Promise((r) => requestAnimationFrame(() => requestAnimationFrame(r)))
  );

  // ── Capture ────────────────────────────────────────────────────────────────
  // We drive the capture off the document directly rather than via $$() so
  // detached-handle races don't bite us on large lists.
  const manifest = await page.$$eval('[data-capture-canvas]', (els) =>
    els.map((el) => ({
      index: Number(el.getAttribute('data-capture-index')),
      id:    el.getAttribute('data-capture-id') ?? '',
      name:  el.getAttribute('data-capture-name') ?? 'unknown',
    }))
  );

  console.log(`  capturing ${manifest.length} canvases → ${outDir}`);
  let captured = 0;
  for (const entry of manifest) {
    const safe = entry.name.replace(/[^A-Za-z0-9._-]/g, '_');
    const filename = `${String(entry.index).padStart(3, '0')}_${safe}.png`;

    const handle = await page.$(
      `[data-capture-canvas][data-capture-index="${entry.index}"]`
    );
    if (!handle) {
      console.warn(`  ⚠ canvas ${entry.index} (${entry.name}) not found`);
      continue;
    }

    // Scroll into view so element-level screenshots see the final layout.
    await handle.evaluate((el) =>
      el.scrollIntoView({ block: 'center', behavior: 'instant' })
    );
    await handle.screenshot({ path: resolve(outDir, filename), omitBackground: false });
    await handle.dispose();
    captured += 1;
  }

  console.log(`✓ captured ${captured} / ${manifest.length} canvases`);
} finally {
  await browser.close();
}
