/**
 * Main App Component
 *
 * Entry point for the SDUI web testing application.
 */

import React, { useState, useEffect, useCallback } from 'react';
import type { IRDocument } from '../style/core/ir/IRModels';
import { ComponentGallery } from './ComponentGallery';
import { HotReloadStatus, useHotReload } from '../style/debug/hotreload/HotReloadManager';

const IR_ASSET_PATH = '/ir-components.json';

export function App() {
  const [document, setDocument] = useState<IRDocument | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [error, setError] = useState<string | null>(null);

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

  // Hot reload support
  const { isWatching, reloadCount } = useHotReload((doc) => {
    setDocument(doc);
  });

  return (
    <div style={styles.app}>
      <header style={styles.header}>
        <div style={styles.headerLeft}>
          <h1 style={styles.title}>Style Converter</h1>
          <span style={styles.subtitle}>Web Testing</span>
        </div>

        <div style={styles.headerCenter}>
          <input
            type="text"
            placeholder="Search components..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            style={styles.searchInput}
          />
        </div>

        <div style={styles.headerRight}>
          <HotReloadStatus isWatching={isWatching} reloadCount={reloadCount} />
          <button onClick={loadDocument} style={styles.reloadButton}>
            Reload
          </button>
        </div>
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
    minHeight: '100vh',
    display: 'flex',
    flexDirection: 'column',
  },
  header: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: '12px 24px',
    background: 'rgba(0,0,0,0.3)',
    borderBottom: '1px solid rgba(255,255,255,0.1)',
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
  headerCenter: {
    flex: 1,
    maxWidth: '400px',
    margin: '0 24px',
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
  headerRight: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
  },
  reloadButton: {
    padding: '8px 16px',
    background: 'rgba(59, 130, 246, 0.2)',
    border: '1px solid rgba(59, 130, 246, 0.3)',
    borderRadius: '6px',
    color: '#60a5fa',
    fontSize: '14px',
    cursor: 'pointer',
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
