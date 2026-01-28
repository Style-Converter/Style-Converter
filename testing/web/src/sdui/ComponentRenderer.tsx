/**
 * SDUI Component Renderer
 *
 * Renders IR components as HTML/CSS at runtime.
 * This is the web equivalent of the Android ComponentRenderer.
 */

import React, { useMemo } from 'react';
import type { IRComponent, IRProperty } from '../style/core/ir/IRModels';
import { buildStyles, type CSSStyles } from '../style/core/renderer/StyleBuilder';

interface ComponentRendererProps {
  component: IRComponent;
  depth?: number;
}

/**
 * Detect the display/layout type from properties.
 */
type DisplayType = 'block' | 'flex-row' | 'flex-column' | 'grid' | 'inline' | 'none';

function detectDisplayType(properties: IRProperty[]): DisplayType {
  let displayType: DisplayType = 'block';
  let flexDirection = 'row';

  for (const prop of properties) {
    if (prop.type === 'Display') {
      const keyword = typeof prop.data === 'string'
        ? prop.data
        : (prop.data as Record<string, unknown>)?.keyword || (prop.data as Record<string, unknown>)?.type;

      if (typeof keyword === 'string') {
        switch (keyword.toLowerCase().replace(/_/g, '-')) {
          case 'flex':
          case 'inline-flex':
            displayType = 'flex-row';
            break;
          case 'grid':
            displayType = 'grid';
            break;
          case 'inline':
          case 'inline-block':
            displayType = 'inline';
            break;
          case 'none':
            displayType = 'none';
            break;
        }
      }
    }

    if (prop.type === 'FlexDirection') {
      const direction = typeof prop.data === 'string'
        ? prop.data
        : (prop.data as Record<string, unknown>)?.keyword;

      if (typeof direction === 'string' && direction.toLowerCase().includes('column')) {
        flexDirection = 'column';
      }
    }
  }

  // Update flex direction
  if (displayType === 'flex-row' && flexDirection === 'column') {
    displayType = 'flex-column';
  }

  return displayType;
}

/**
 * Render a single IR component.
 */
export function ComponentRenderer({ component, depth = 0 }: ComponentRendererProps) {
  const displayType = useMemo(() => detectDisplayType(component.properties), [component.properties]);

  // Don't render if display: none
  if (displayType === 'none') {
    return null;
  }

  // Build styles from properties
  const styles = useMemo(() => buildStyles(component.properties), [component.properties]);

  // Add minimum sizing for empty components
  const containerStyles: CSSStyles = {
    ...styles,
    // Ensure minimum dimensions for visibility
    minWidth: styles.width || styles.minWidth || '50px',
    minHeight: styles.height || styles.minHeight || '30px',
  };

  // Render children or placeholder
  const content = component.children && component.children.length > 0 ? (
    <>
      {component.children.map((child, index) => (
        <ComponentRenderer key={child.id || index} component={child} depth={depth + 1} />
      ))}
    </>
  ) : (
    <PlaceholderContent name={component.name} />
  );

  return (
    <div
      data-component-id={component.id}
      data-component-name={component.name}
      style={containerStyles as React.CSSProperties}
    >
      {content}
    </div>
  );
}

/**
 * Placeholder content for components without children.
 */
interface PlaceholderContentProps {
  name: string;
}

function PlaceholderContent({ name }: PlaceholderContentProps) {
  return (
    <span
      style={{
        display: 'block',
        padding: '4px',
        fontSize: '11px',
        color: 'inherit',
        textAlign: 'center',
        opacity: 0.7,
        wordBreak: 'break-word',
      }}
    >
      {name.replace(/_/g, ' ')}
    </span>
  );
}

/**
 * Render a list of components.
 */
interface ComponentListProps {
  components: IRComponent[];
}

export function ComponentList({ components }: ComponentListProps) {
  return (
    <>
      {components.map((component, index) => (
        <ComponentRenderer key={component.id || index} component={component} />
      ))}
    </>
  );
}

export default ComponentRenderer;
