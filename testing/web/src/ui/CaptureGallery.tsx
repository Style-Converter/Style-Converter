/**
 * CaptureGallery — chromeless render of every component in the IR,
 * specifically for headless screenshot capture.
 *
 * Visited via `?mode=capture`. Each component renders inside a
 * <CaptureCanvas> element with:
 *   - exactly 390 px width
 *   - natural height (no clamping)
 *   - solid #1A1A2E background (no alpha compositing)
 *   - 16 px padding on all sides
 *   - no gallery chrome (no card header / footer / border / label)
 *   - a data-capture-canvas marker with index + name for Puppeteer
 *
 * Matches the iOS `CaptureCanvas.swift` and Android `CaptureCanvas`
 * composable exactly — so captures from all three platforms are directly
 * pixel-diffable.
 */

import React from 'react';
import type { IRComponent, IRDocument } from '../style/core/ir/IRModels';
import { ComponentRenderer } from '../sdui/ComponentRenderer';

interface CaptureGalleryProps {
  document: IRDocument;
}

/** Flatten nested component trees — matches the iOS / Android flat walk. */
function flatten(components: IRComponent[]): IRComponent[] {
  const out: IRComponent[] = [];
  const walk = (c: IRComponent) => {
    out.push(c);
    c.children?.forEach(walk);
  };
  components.forEach(walk);
  return out;
}

export function CaptureGallery({ document }: CaptureGalleryProps) {
  const flat = React.useMemo(() => flatten(document.components), [document]);

  return (
    <div style={containerStyle}>
      {flat.map((component, index) => (
        <CaptureCanvas key={component.id || index} component={component} index={index} />
      ))}
      {/* Sentinel so Puppeteer can tell when the full list has rendered. */}
      <div data-capture-ready={flat.length} style={{ height: 0, overflow: 'hidden' }} />
    </div>
  );
}

interface CaptureCanvasProps {
  component: IRComponent;
  index: number;
}

/** Single chromeless capture surface. */
export function CaptureCanvas({ component, index }: CaptureCanvasProps) {
  return (
    <div
      data-capture-canvas
      data-capture-index={index}
      data-capture-id={component.id}
      data-capture-name={component.name}
      style={canvasStyle}
    >
      <ComponentRenderer component={component} />
    </div>
  );
}

/** Flat vertical list, no gaps, same background as the canvases themselves
 *  so any sub-pixel bleed around element screenshots is invisible. */
const containerStyle: React.CSSProperties = {
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'flex-start',
  background: '#1A1A2E',
  margin: 0,
  padding: 0,
};

/** 390 × natural, #1A1A2E, 16 px padding — the shared capture contract. */
const canvasStyle: React.CSSProperties = {
  width: '390px',
  boxSizing: 'border-box',
  padding: '16px',
  background: '#1A1A2E',
};

export default CaptureGallery;
