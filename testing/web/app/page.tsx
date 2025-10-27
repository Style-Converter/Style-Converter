'use client';

import { useState, useEffect } from 'react';

interface CSSComponent {
  name: string;
  properties: Record<string, string>;
  selectors?: Array<{
    selector: string;
    properties: Record<string, string>;
  }>;
  media?: Array<{
    query: string;
    properties: Record<string, string>;
  }>;
}

interface CSSData {
  components: Record<string, {
    properties?: Record<string, string>;
    selectors?: Array<{
      selector: string;
      properties: Record<string, string>;
    }>;
    media?: Array<{
      query: string;
      properties: Record<string, string>;
    }>;
  }>;
}

export default function Home() {
  const [components, setComponents] = useState<CSSComponent[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [dynamicCSS, setDynamicCSS] = useState<string>('');

  useEffect(() => {
    // Load CSS components from JSON file
    fetch('/test-data.json')
      .then(res => res.json())
      .then((data: CSSData) => {
        // Helper function to convert CSS property names to React camelCase
        const toCamelCase = (str: string): string => {
          return str.replace(/-([a-z])/g, (g) => g[1].toUpperCase());
        };

        const convertProperties = (props: Record<string, string>): Record<string, string> => {
          const converted: Record<string, string> = {};
          Object.entries(props).forEach(([key, value]) => {
            converted[toCamelCase(key)] = value;
          });
          return converted;
        };

        // Generate CSS for selectors and media queries
        let cssRules = '';
        Object.entries(data.components).forEach(([name, config]) => {
          const className = `component-${name.replace(/[^a-z0-9]/gi, '-')}`;

          // Add selector styles (hover, active, etc.)
          if (config.selectors) {
            config.selectors.forEach((selectorConfig) => {
              const properties = Object.entries(selectorConfig.properties)
                .map(([key, value]) => `  ${key}: ${value};`)
                .join('\n');
              cssRules += `.${className}:${selectorConfig.selector} {\n${properties}\n}\n\n`;
            });
          }

          // Add media query styles
          if (config.media) {
            config.media.forEach((mediaConfig) => {
              const properties = Object.entries(mediaConfig.properties)
                .map(([key, value]) => `    ${key}: ${value};`)
                .join('\n');
              cssRules += `@media (${mediaConfig.query}) {\n  .${className} {\n${properties}\n  }\n}\n\n`;
            });
          }
        });

        setDynamicCSS(cssRules);

        const componentsList: CSSComponent[] = Object.entries(data.components).map(
          ([name, config]) => ({
            name,
            properties: config.properties ? convertProperties(config.properties) : {},
            selectors: config.selectors,
            media: config.media,
          })
        );
        setComponents(componentsList);
      })
      .catch(err => {
        setError('Failed to load test data: ' + err.message);
        console.error('Error loading test data:', err);
      });
  }, []);

  if (error) {
    return (
      <main className="min-h-screen p-8">
        <div className="max-w-4xl mx-auto">
          <h1 className="text-3xl font-bold mb-8 text-center">CSS Style Converter - Visual Test</h1>
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
            {error}
          </div>
          <p className="mt-4 text-sm text-gray-600">
            Please place your test-data.json file in the public/ directory.
          </p>
        </div>
      </main>
    );
  }

  if (components.length === 0) {
    return (
      <main className="min-h-screen p-8">
        <div className="max-w-4xl mx-auto">
          <h1 className="text-3xl font-bold mb-8 text-center">CSS Style Converter - Visual Test</h1>
          <p className="text-center text-gray-600">Loading components...</p>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-screen p-8 bg-gray-50">
      {/* Inject dynamic CSS for selectors and media queries */}
      <style dangerouslySetInnerHTML={{ __html: dynamicCSS }} />

      <div className="max-w-4xl mx-auto">
        <h1 className="text-4xl font-bold mb-12 text-center text-gray-900">
          CSS Style Converter - Visual Test
        </h1>

        <div className="space-y-12">
          {components.map((component, index) => {
            const className = `component-${component.name.replace(/[^a-z0-9]/gi, '-')}`;

            return (
              <div key={index} className="bg-white rounded-lg shadow-md p-6">
                {/* Component Title */}
                <h2 className="text-2xl font-semibold text-center mb-6 text-gray-800 border-b pb-3">
                  Component: {component.name}
                </h2>

                {/* Display raw CSS properties */}
                <div className="mb-6 p-4 bg-gray-100 rounded">
                  <h3 className="text-sm font-semibold mb-2 text-gray-700">Base Properties:</h3>
                  <pre className="text-xs text-gray-600 overflow-x-auto">
                    {JSON.stringify(component.properties, null, 2)}
                  </pre>
                </div>

                {/* Display selectors if any */}
                {component.selectors && component.selectors.length > 0 && (
                  <div className="mb-6 p-4 bg-blue-50 rounded">
                    <h3 className="text-sm font-semibold mb-2 text-blue-700">Selectors:</h3>
                    {component.selectors.map((sel, idx) => (
                      <div key={idx} className="mb-2">
                        <span className="text-xs font-medium text-blue-600">:{sel.selector}</span>
                        <pre className="text-xs text-gray-600 overflow-x-auto mt-1">
                          {JSON.stringify(sel.properties, null, 2)}
                        </pre>
                      </div>
                    ))}
                  </div>
                )}

                {/* Display media queries if any */}
                {component.media && component.media.length > 0 && (
                  <div className="mb-6 p-4 bg-green-50 rounded">
                    <h3 className="text-sm font-semibold mb-2 text-green-700">Media Queries:</h3>
                    {component.media.map((media, idx) => (
                      <div key={idx} className="mb-2">
                        <span className="text-xs font-medium text-green-600">@media ({media.query})</span>
                        <pre className="text-xs text-gray-600 overflow-x-auto mt-1">
                          {JSON.stringify(media.properties, null, 2)}
                        </pre>
                      </div>
                    ))}
                  </div>
                )}

                {/* Render the actual component with styles */}
                <div className="flex justify-center">
                  <div
                    className={`${className}`}
                    style={component.properties}
                  >
                    {/* Child elements for testing flex/grid layouts */}
                    <div className="bg-white/80 border border-gray-400 rounded px-3 py-2 text-xs">
                      Child 1
                    </div>
                    <div className="bg-white/80 border border-gray-400 rounded px-3 py-2 text-xs">
                      Child 2
                    </div>
                    <div className="bg-white/80 border border-gray-400 rounded px-3 py-2 text-xs">
                      Child 3
                    </div>
                  </div>
                </div>

                <p className="text-xs text-gray-500 text-center mt-4">
                  Hover over the element to see :hover styles • Resize window to see media queries
                </p>
              </div>
            );
          })}
        </div>

        <div className="mt-12 text-center text-sm text-gray-500">
          <p>Total components: {components.length}</p>
        </div>
      </div>
    </main>
  );
}
