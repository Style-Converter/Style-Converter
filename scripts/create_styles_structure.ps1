$ErrorActionPreference = 'Stop'

function Sanitize([string]$name) { return ($name -replace '[<>:"/\\|?*]','-') }

function Ensure-Directory([string]$path) {
	if (-not [string]::IsNullOrWhiteSpace($path)) {
		if (-not (Test-Path -LiteralPath $path)) {
			New-Item -ItemType Directory -Force -Path $path | Out-Null
		}
	}
}

function New-FolderTree([string]$base, $node) {
	if ($null -eq $node) { return }

	# String -> leaf folder
	if ($node -is [string]) {
		$leaf = Sanitize $node
		$path = Join-Path -Path $base -ChildPath $leaf
		Ensure-Directory -path $path
		return
	}

	# Hashtable / IDictionary
	if ($node -is [System.Collections.IDictionary]) {
		foreach ($key in $node.Keys) {
			$group = Sanitize ([string]$key)
			$groupPath = Join-Path -Path $base -ChildPath $group
			Ensure-Directory -path $groupPath
			$children = $node[$key]
			if ($children -is [System.Collections.IEnumerable] -and -not ($children -is [string])) {
				foreach ($child in $children) { New-FolderTree -base $groupPath -node $child }
			} else { New-FolderTree -base $groupPath -node $children }
		}
		return
	}

	# PSCustomObject
	if ($node -and $node.PSObject -and $node.PSObject.Properties) {
		foreach ($p in $node.PSObject.Properties) {
			$group = Sanitize ([string]$p.Name)
			$groupPath = Join-Path -Path $base -ChildPath $group
			Ensure-Directory -path $groupPath
			$children = $p.Value
			if ($children -is [System.Collections.IEnumerable] -and -not ($children -is [string])) {
				foreach ($child in $children) { New-FolderTree -base $groupPath -node $child }
			} else { New-FolderTree -base $groupPath -node $children }
		}
		return
	}

	# Arrays
	if ($node -is [System.Collections.IEnumerable]) {
		foreach ($item in $node) { New-FolderTree -base $base -node $item }
		return
	}
}

$stylesRoot = 'src/main/kotlin/app/parsing/css/styles'
Ensure-Directory -path $stylesRoot

$json = @'
{
  "Layout_&_Display": [
    "display",
    "contain",
    "contain-intrinsic-width",
    "contain-intrinsic-height",
    {
      "contain-intrinsic-size": [
        "contain-intrinsic-width",
        "contain-intrinsic-height"
      ]
    },
    "box-direction",
    "box-orient",
    "box-align",
    "box-pack",
    "box-flex",
    "box-flex-group",
    "box-lines",
    "box-ordinal-group",
    "isolation",
    "align-content",
    "align-items",
    "align-self",
    {
      "gap": [
        "row-gap",
        "column-gap"
      ]
    },
    "grid-auto-flow",
    "grid-auto-columns",
    "grid-auto-rows",
    {
      "grid": [
        "grid-template-rows",
        "grid-template-columns",
        "grid-template-areas",
        "grid-auto-rows",
        "grid-auto-columns",
        "grid-auto-flow"
      ]
    },
    {
      "grid-template": [
        "grid-template-rows",
        "grid-template-columns",
        "grid-template-areas"
      ]
    },
    {
      "grid-area": [
        "grid-row-start",
        "grid-column-start",
        "grid-row-end",
        "grid-column-end"
      ]
    },
    {
      "grid-column": [
        "grid-column-start",
        "grid-column-end"
      ]
    },
    {
      "grid-row": [
        "grid-row-start",
        "grid-row-end"
      ]
    },
    "grid-template-areas",
    "grid-template-columns",
    "grid-template-rows",
    "box-sizing",
    "block-size",
    "inline-size",
    "height",
    "width",
    "min-width",
    "min-height",
    "max-width",
    "max-height",
    "aspect-ratio",
    "display",
    "float",
    "clear",
    "break-after",
    "break-before",
    "break-inside",
    "caption-side",
    "columns",
    {
      "columns": [
        "column-width",
        "column-count"
      ]
    },
    "column-width",
    "column-count",
    "column-fill",
    "column-gap",
    "column-span",
    "box-decoration-break",
    "content-visibility",
    "container-name",
    "container-type",
    {
      "container": [
        "container-name",
        "container-type"
      ]
    },
    "writing-mode",
    "direction",
    "text-orientation",
    "vertical-align",
    "display",
    "visibility"
  ],

  "Positioning_&_Layering": [
    "position",
    "position-anchor",
    "position-area",
    {
      "position-try": [
        "position-try-order",
        "position-try-fallbacks"
      ]
    },
    "position-try-order",
    "position-try-fallbacks",
    "top",
    "right",
    "bottom",
    "left",
    {
      "inset": [
        "top",
        "right",
        "bottom",
        "left"
      ]
    },
    {
      "inset-inline": [
        "inset-inline-start",
        "inset-inline-end"
      ]
    },
    {
      "inset-block": [
        "inset-block-start",
        "inset-block-end"
      ]
    },
    "inset-inline-start",
    "inset-inline-end",
    "inset-block-start",
    "inset-block-end",
    "z-index",
    "order",
    "isolation",
    "contain",
    "position-visibility",
    "position-area",
    "position-anchor",
    "box-shadow",
    "backface-visibility",
    "will-change",
    "stacking-context: (managed by position/isolation/z-index/will-change)"
  ],

  "Box_Model_(Spacing_&_Sizing)": [
    "box-sizing",
    {
      "margin": [
        "margin-top",
        "margin-right",
        "margin-bottom",
        "margin-left"
      ]
    },
    "margin-top",
    "margin-right",
    "margin-bottom",
    "margin-left",
    {
      "margin-inline": [
        "margin-inline-start",
        "margin-inline-end"
      ]
    },
    "margin-inline-start",
    "margin-inline-end",
    {
      "margin-block": [
        "margin-block-start",
        "margin-block-end"
      ]
    },
    "margin-block-start",
    "margin-block-end",
    {
      "padding": [
        "padding-top",
        "padding-right",
        "padding-bottom",
        "padding-left"
      ]
    },
    "padding-top",
    "padding-right",
    "padding-bottom",
    "padding-left",
    {
      "padding-inline": [
        "padding-inline-start",
        "padding-inline-end"
      ]
    },
    "padding-inline-start",
    "padding-inline-end",
    {
      "padding-block": [
        "padding-block-start",
        "padding-block-end"
      ]
    },
    "padding-block-start",
    "padding-block-end",
    "width",
    "height",
    "min-width",
    "min-height",
    "max-width",
    "max-height",
    "block-size",
    "inline-size",
    "min-inline-size",
    "max-inline-size",
    "min-block-size",
    "max-block-size",
    "aspect-ratio",
    "contain-intrinsic-inline-size",
    "contain-intrinsic-block-size",
    {
      "contain-intrinsic-size": [
        "contain-intrinsic-width",
        "contain-intrinsic-height"
      ]
    }
  ],

  "Borders_&_Outlines": [
    {
      "border": [
        "border-width",
        "border-style",
        "border-color"
      ]
    },
    {
      "border-width": [
        "border-top-width",
        "border-right-width",
        "border-bottom-width",
        "border-left-width"
      ]
    },
    {
      "border-style": [
        "border-top-style",
        "border-right-style",
        "border-bottom-style",
        "border-left-style"
      ]
    },
    {
      "border-color": [
        "border-top-color",
        "border-right-color",
        "border-bottom-color",
        "border-left-color"
      ]
    },
    {
      "border-top": [
        "border-top-width",
        "border-top-style",
        "border-top-color"
      ]
    },
    {
      "border-right": [
        "border-right-width",
        "border-right-style",
        "border-right-color"
      ]
    },
    {
      "border-bottom": [
        "border-bottom-width",
        "border-bottom-style",
        "border-bottom-color"
      ]
    },
    {
      "border-left": [
        "border-left-width",
        "border-left-style",
        "border-left-color"
      ]
    },
    {
      "border-block": [
        "border-block-width",
        "border-block-style",
        "border-block-color"
      ]
    },
    {
      "border-block-start": [
        "border-block-start-width",
        "border-block-start-style",
        "border-block-start-color"
      ]
    },
    {
      "border-block-end": [
        "border-block-end-width",
        "border-block-end-style",
        "border-block-end-color"
      ]
    },
    {
      "border-inline": [
        "border-inline-width",
        "border-inline-style",
        "border-inline-color"
      ]
    },
    {
      "border-inline-start": [
        "border-inline-start-width",
        "border-inline-start-style",
        "border-inline-start-color"
      ]
    },
    {
      "border-inline-end": [
        "border-inline-end-width",
        "border-inline-end-style",
        "border-inline-end-color"
      ]
    },
    "border-image-source",
    "border-image-slice",
    "border-image-width",
    "border-image-outset",
    {
      "border-image": [
        "border-image-source",
        "border-image-slice",
        "border-image-width",
        "border-image-outset",
        "border-image-repeat"
      ]
    },
    "border-radius",
    {
      "border-radius": [
        "border-top-left-radius",
        "border-top-right-radius",
        "border-bottom-right-radius",
        "border-bottom-left-radius"
      ]
    },
    "outline-color",
    "outline-style",
    "outline-width",
    {
      "outline": [
        "outline-color",
        "outline-style",
        "outline-width"
      ]
    },
    "box-shadow",
    "border-collapse",
    "border-spacing",
    "border-image",
    "border-top-left-radius",
    "border-top-right-radius",
    "border-bottom-left-radius",
    "border-bottom-right-radius",
    "border-start-start-radius",
    "border-start-end-radius",
    "border-end-start-radius",
    "border-end-end-radius"
  ],

  "Backgrounds_&_Masks": [
    {
      "background": [
        "background-image",
        "background-position",
        "background-size",
        "background-repeat",
        "background-attachment",
        "background-origin",
        "background-clip",
        "background-color"
      ]
    },
    "background-image",
    "background-position",
    "background-position-x",
    "background-position-y",
    "background-size",
    "background-repeat",
    "background-attachment",
    "background-origin",
    "background-clip",
    "background-color",
    "background-blend-mode",
    "backdrop-filter",
    "mask-image",
    "mask-mode",
    "mask-position",
    "mask-size",
    "mask-repeat",
    "mask-origin",
    "mask-clip",
    {
      "mask": [
        "mask-image",
        "mask-mode",
        "mask-position",
        "mask-size",
        "mask-repeat",
        "mask-origin",
        "mask-clip",
        "mask-type"
      ]
    },
    "mask-type",
    "mask-border-source",
    "mask-border-slice",
    "mask-border-width",
    "mask-border-outset",
    "mask-border-repeat",
    {
      "mask-border": [
        "mask-border-source",
        "mask-border-slice",
        "mask-border-width",
        "mask-border-outset",
        "mask-border-repeat",
        "mask-border-mode"
      ]
    },
    "mask-border-mode",
    "-webkit-mask-box-image",
    "-webkit-mask-position-x",
    "-webkit-mask-position-y",
    "-webkit-mask-repeat-x",
    "-webkit-mask-repeat-y"
  ],

  "Typography_&_Text": [
    "font-family",
    "font-style",
    "font-weight",
    "font-size",
    "font-stretch",
    "font-variation-settings",
    "font-feature-settings",
    "font-kerning",
    "font-language-override",
    "font-optical-sizing",
    "font-palette",
    "font-smooth",
    "font-size-adjust",
    {
      "font": [
        "font-style",
        "font-variant",
        "font-weight",
        "font-stretch",
        "font-size",
        "line-height",
        "font-family"
      ]
    },
    "font-variant-alternates",
    {
      "font-variant": [
        "font-variant-ligatures",
        "font-variant-caps",
        "font-variant-numeric",
        "font-variant-east-asian",
        "font-variant-emoji",
        "font-variant-position"
      ]
    },
    "font-variation-settings",
    "font-synthesis",
    {
      "font-synthesis": [
        "font-synthesis-weight",
        "font-synthesis-style",
        "font-synthesis-small-caps"
      ]
    },
    "line-height",
    "line-height-step",
    "letter-spacing",
    "word-spacing",
    "text-align",
    "text-align-last",
    "text-indent",
    "text-justify",
    "text-transform",
    "text-decoration-color",
    "text-decoration-style",
    "text-decoration-line",
    "text-decoration-thickness",
    {
      "text-decoration": [
        "text-decoration-line",
        "text-decoration-style",
        "text-decoration-color",
        "text-decoration-thickness"
      ]
    },
    {
      "text-emphasis": [
        "text-emphasis-style",
        "text-emphasis-color"
      ]
    },
    "text-shadow",
    "text-overflow",
    "text-orientation",
    "text-wrap-mode",
    "text-wrap-style",
    {
      "text-wrap": [
        "text-wrap-mode",
        "text-wrap-style"
      ]
    },
    {
      "text-box": [
        "text-box-trim",
        "text-box-edge"
      ]
    },
    "text-box-trim",
    "text-box-edge",
    "white-space",
    "white-space-collapse",
    "hyphens",
    "hyphenate-character",
    "hyphenate-limit-chars",
    "initial-letter",
    "list-style-type",
    "list-style-image",
    "list-style-position",
    {
      "list-style": [
        "list-style-type",
        "list-style-position",
        "list-style-image"
      ]
    },
    "list-style",
    "marker",
    "marker-start",
    "marker-mid",
    "marker-end",
    "quotes",
    "tab-size",
    "widows",
    "orphans",
    "ruby-align",
    "ruby-position",
    "line-clamp",
    "text-size-adjust",
    "letter-spacing",
    "text-anchor",
    "alignment-baseline",
    "dominant-baseline"
  ],

  "SVG_&_Vector_Styling": [
    "fill",
    "fill-opacity",
    "fill-rule",
    "stroke",
    "stroke-width",
    "stroke-opacity",
    "stroke-linecap",
    "stroke-linejoin",
    "stroke-miterlimit",
    "stroke-dasharray",
    "stroke-dashoffset",
    "vector-effect",
    "shape-outside",
    "shape-margin",
    "shape-image-threshold",
    "shape-rendering",
    "clip-path",
    "clip-rule",
    "cx",
    "cy",
    "r",
    "rx",
    "ry",
    "paint-order",
    "lighting-color",
    "mask",
    "mask-image",
    "mask-position",
    "mask-size",
    "stop-color",
    "stop-opacity",
    "filter",
    "flood-color",
    "flood-opacity",
    "color-interpolation",
    "color-interpolation-filters"
  ],

  "Colors_&_Effects": [
    "color",
    "color-scheme",
    "accent-color",
    "background-blend-mode",
    "mix-blend-mode",
    "isolation",
    "opacity",
    "filter",
    "backdrop-filter",
    "box-shadow",
    "caret-color",
    "scrollbar-color",
    "scrollbar-width",
    "outline-color",
    "overlay",
    "print-color-adjust",
    "forced-color-adjust"
  ],

  "Transforms_&_3D": [
    "transform",
    "transform-origin",
    "transform-style",
    "transform-box",
    "translate",
    "rotate",
    "scale",
    "perspective",
    "perspective-origin",
    "preserve-3d",
    "backface-visibility",
    "translate",
    "will-change"
  ],

  "Transitions_&_Animations": [
    {
      "transition": [
        "transition-property",
        "transition-duration",
        "transition-timing-function",
        "transition-delay",
        "transition-behavior"
      ]
    },
    "transition-property",
    "transition-duration",
    "transition-timing-function",
    "transition-delay",
    "transition-behavior",
    {
      "animation": [
        "animation-name",
        "animation-duration",
        "animation-timing-function",
        "animation-delay",
        "animation-iteration-count",
        "animation-direction",
        "animation-fill-mode",
        "animation-play-state",
        "animation-timeline"
      ]
    },
    "animation-name",
    "animation-duration",
    "animation-timing-function",
    "animation-delay",
    "animation-iteration-count",
    "animation-direction",
    "animation-fill-mode",
    "animation-play-state",
    "animation-composition",
    "animation-timeline",
    {
      "animation-range": [
        "animation-range-start",
        "animation-range-end"
      ]
    },
    "animation-range-start",
    "animation-range-end",
    "animation-composition",
    {
      "scroll-timeline": [
        "scroll-timeline-name",
        "scroll-timeline-axis"
      ]
    },
    "scroll-timeline-name",
    "scroll-timeline-axis",
    {
      "view-timeline": [
        "view-timeline-name",
        "view-timeline-axis",
        "view-timeline-inset"
      ]
    },
    "view-timeline-name",
    "view-timeline-axis",
    "view-timeline-inset",
    "view-transition-name",
    "view-transition-class",
    "transition"
  ],

  "Scrolling_&_Interaction": [
    "overflow",
    {
      "overflow": [
        "overflow-x",
        "overflow-y"
      ]
    },
    "overflow-x",
    "overflow-y",
    {
      "overscroll-behavior": [
        "overscroll-behavior-x",
        "overscroll-behavior-y"
      ]
    },
    "overscroll-behavior-x",
    "overscroll-behavior-y",
    "overscroll-behavior-block",
    "overscroll-behavior-inline",
    "scroll-behavior",
    {
      "scroll-margin": [
        "scroll-margin-top",
        "scroll-margin-right",
        "scroll-margin-bottom",
        "scroll-margin-left"
      ]
    },
    "scroll-margin-top",
    "scroll-margin-right",
    "scroll-margin-bottom",
    "scroll-margin-left",
    {
      "scroll-margin-inline": [
        "scroll-margin-inline-start",
        "scroll-margin-inline-end"
      ]
    },
    {
      "scroll-margin-block": [
        "scroll-margin-block-start",
        "scroll-margin-block-end"
      ]
    },
    {
      "scroll-padding": [
        "scroll-padding-top",
        "scroll-padding-right",
        "scroll-padding-bottom",
        "scroll-padding-left"
      ]
    },
    "scroll-padding-top",
    "scroll-padding-right",
    "scroll-padding-bottom",
    "scroll-padding-left",
    {
      "scroll-padding-inline": [
        "scroll-padding-inline-start",
        "scroll-padding-inline-end"
      ]
    },
    {
      "scroll-padding-block": [
        "scroll-padding-block-start",
        "scroll-padding-block-end"
      ]
    },
    "scroll-snap-type",
    "scroll-snap-align",
    "scroll-snap-stop",
    "scroll-marker-group",
    "scrollbar-gutter",
    "scrollbar-color",
    "scrollbar-width",
    "touch-action",
    "pointer-events",
    "cursor",
    "user-select",
    "user-modify",
    "zoom",
    "resize",
    "overlay",
    "position-visibility"
  ],

  "Content_&_Generated_Content": [
    "content",
    "quotes",
    "counter-increment",
    "counter-reset",
    "counter-set",
    "marker",
    "marker-start",
    "marker-mid",
    "marker-end",
    "::before (pseudo-element)",
    "::after (pseudo-element)"
  ],

  "Tables_&_Lists": [
    "table-layout",
    "caption-side",
    "empty-cells",
    "border-collapse",
    "border-spacing",
    "list-style",
    "list-style-type",
    "list-style-image",
    "list-style-position",
    "columns",
    "column-gap",
    "column-rule",
    {
      "column-rule": [
        "column-rule-width",
        "column-rule-style",
        "column-rule-color"
      ]
    }
  ],

  "Accessibility_&_Semantic_Adjustments": [
    "appearance",
    "forced-color-adjust",
    "color-scheme",
    "caret-color",
    "speak-as",
    "print-color-adjust",
    "unicode-bidi",
    "direction",
    "user-select",
    "text-size-adjust",
    "accessibility: (ARIA properties are outside CSS list)"
  ],

  "Non-standard_/_Vendor-prefixed": [
    "-moz-float-edge",
    "-moz-force-broken-image-icon",
    "-moz-orient",
    "-moz-user-focus",
    "-moz-user-input",
    "-webkit-box-reflect",
    "-webkit-border-before",
    "-webkit-mask-box-image",
    "-webkit-mask-composite",
    "-webkit-mask-position-x",
    "-webkit-mask-position-y",
    "-webkit-mask-repeat-x",
    "-webkit-mask-repeat-y",
    "-webkit-tap-highlight-color",
    "-webkit-text-fill-color",
    "-webkit-text-security",
    {
      "-webkit-text-stroke": [
        "-webkit-text-stroke-width",
        "-webkit-text-stroke-color"
      ]
    },
    "-webkit-text-stroke-color",
    "-webkit-text-stroke-width",
    "-webkit-touch-callout"
  ]
}
'@

$data = $json | ConvertFrom-Json
New-FolderTree -base $stylesRoot -node $data
Write-Host 'DONE'

