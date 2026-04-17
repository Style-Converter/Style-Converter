/**
 * Main App Component
 *
 * Entry point for the SDUI web testing application.
 */

import React, { useState, useEffect, useCallback, useMemo } from 'react';
import type { IRDocument } from '../style/core/ir/IRModels';
import { ComponentGallery } from './ComponentGallery';
import { CaptureGallery } from './CaptureGallery';
import { useHotReload } from '../style/debug/hotreload/HotReloadManager';

const IR_ASSET_PATH = '/ir-components.json';

/**
 * Returns true when the URL contains `?mode=capture` — the chromeless
 * render used by the Puppeteer capture script. Parsed once per mount,
 * and doesn't change during a session.
 */
function isCaptureMode(): boolean {
  if (typeof window === 'undefined') return false;
  return new URLSearchParams(window.location.search).get('mode') === 'capture';
}

export function App() {
  const [document, setDocument] = useState<IRDocument | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [captureMode] = useState(isCaptureMode);

  // Toggle the body class once, before the first render, so the chromeless
  // capture styles apply to #root and body without a flash of the gallery
  // frame. The class is set/unset synchronously from this effect.
  useEffect(() => {
    if (typeof window === 'undefined') return;
    if (captureMode) {
      window.document.body.classList.add('capture-mode');
      return () => {
        window.document.body.classList.remove('capture-mode');
      };
    }
  }, [captureMode]);

  // Load document from assets
  const loadDocument = useCallback(async () => {
    try {
      const response = await fetch(IR_ASSET_PATH);
      if (!response.ok) {
        throw new Error(`Failed to load: ${response.status}`);
      }
      const data = await response.json() as IRDocument;
      setDocument(data);
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load IR document');
      console.error('Load error:', err);
    }
  }, []);

  // Initial load
  useEffect(() => {
    loadDocument();
  }, [loadDocument]);

  // Hot reload support.
  //
  // Two things we handle here to keep the capture-mode console quiet:
  //
  //   1. Disable polling entirely in capture mode — there's no gallery to
  //      update and Puppeteer doesn't need the IR re-fetched every 2s.
  //   2. Memoize both the callback and the options object. `useHotReload`'s
  //      internal useCallback depends on the `onDocumentUpdate` callback
  //      identity; passing an inline lambda creates a new function ref on
  //      every render, which triggers the internal effect, which calls
  //      setState, which re-renders, which → infinite loop and React's
  //      "Maximum update depth exceeded" warning.
  const onDocumentUpdate = useCallback((doc: IRDocument) => {
    setDocument(doc);
  }, []);
  const hotReloadOpts = useMemo(
    () => ({ enabled: !captureMode }),
    [captureMode]
  );
  useHotReload(onDocumentUpdate, hotReloadOpts);

  // Capture mode bypasses the gallery entirely: no chrome, no search, no
  // pagination — a flat vertical list of chromeless canvases, ready for
  // Puppeteer to element-screenshot one by one.
  if (captureMode) {
    if (error) {
      return <pre style={styles.errorPlain}>{error}</pre>;
    }
    if (!document) {
      return <div data-capture-loading="true" style={{ display: 'none' }} />;
    }
    return <CaptureGallery document={document} />;
  }

  return (
    <div style={styles.app}>
      <header style={styles.header}>
        <div style={styles.headerTopRow}>
          <div style={styles.headerLeft}>
            <h1 style={styles.title}>Style Converter</h1>
            <span style={styles.subtitle}>Web Testing</span>
          </div>
        </div>
        <input
          type="text"
          placeholder="Search components..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          style={styles.searchInput}
        />
      </header>

      <main style={styles.main}>
        {error ? (
          <div style={styles.error}>
            <h3>Error Loading Document</h3>
            <p>{error}</p>
            <button onClick={loadDocument} style={styles.retryButton}>
              Retry
            </button>
          </div>
        ) : (
          <ComponentGallery document={document} searchQuery={searchQuery} />
        )}
      </main>
    </div>
  );
}

/**
 * Styles
 */
const styles: Record<string, React.CSSProperties> = {
  app: {
    height: '100%',
    display: 'flex',
    flexDirection: 'column',
  },
  header: {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
    padding: '12px',
    background: 'rgba(0,0,0,0.3)',
    borderBottom: '1px solid rgba(255,255,255,0.1)',
  },
  headerTopRow: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  headerLeft: {
    display: 'flex',
    alignItems: 'baseline',
    gap: '12px',
  },
  title: {
    margin: 0,
    fontSize: '20px',
    fontWeight: 600,
    color: '#fff',
  },
  subtitle: {
    color: '#888',
    fontSize: '14px',
  },
  searchInput: {
    width: '100%',
    padding: '8px 12px',
    background: 'rgba(255,255,255,0.05)',
    border: '1px solid rgba(255,255,255,0.1)',
    borderRadius: '6px',
    color: '#fff',
    fontSize: '14px',
    outline: 'none',
  },
  main: {
    flex: 1,
    overflow: 'auto',
  },
  error: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    padding: '48px',
    color: '#f87171',
    textAlign: 'center',
  },
  errorPlain: {
    color: '#f87171',
    padding: '16px',
    margin: 0,
    fontFamily: 'monospace',
  },
  retryButton: {
    marginTop: '16px',
    padding: '8px 24px',
    background: 'rgba(239, 68, 68, 0.2)',
    border: '1px solid rgba(239, 68, 68, 0.3)',
    borderRadius: '6px',
    color: '#f87171',
    fontSize: '14px',
    cursor: 'pointer',
  },
};

export default App;
