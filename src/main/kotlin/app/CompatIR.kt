package app

/**
 * Temporary compatibility IR to keep existing parsers compiling while migrating to PropertyIR-only flow.
 */
data class BaseIR(
	var properties: MutableList<PropertyIR>? = null,
	var opacity: Double? = null,
	var typography: TypographyIR? = null,
	var spacing: SpacingIR? = null,
	var layout: LayoutIR? = null
)

data class TypographyIR(
	var fontSizePx: Double? = null
)

data class SpacingIR(
	var paddingTopPx: Double? = null,
	var paddingRightPx: Double? = null,
	var paddingBottomPx: Double? = null,
	var paddingLeftPx: Double? = null
)

data class LayoutIR(
	var display: String? = null
)


