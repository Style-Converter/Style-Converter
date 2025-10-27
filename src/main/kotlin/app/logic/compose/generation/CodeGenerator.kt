package app.logic.compose.generation

import app.logic.compose.analysis.SelectorType

/**
 * Generates final Kotlin/Compose code from a ComponentStructure
 */
class CodeGenerator {

    /**
     * Generates complete composable function code
     */
    fun generate(structure: ComponentStructure, existingModifiers: List<String> = emptyList()): String {
        val code = StringBuilder()

        // Generate function signature
        code.append(generateFunctionSignature(structure))
        code.append(" {\n")

        // Generate state setup
        structure.stateSetup.forEach { state ->
            code.append(generateStateSetup(state))
        }

        // Generate animation setup
        structure.animationSetup.forEach { anim ->
            code.append(generateAnimationSetup(anim))
        }

        // Add blank line after state/animation setup if they exist
        if (structure.stateSetup.isNotEmpty() || structure.animationSetup.isNotEmpty()) {
            code.append("\n")
        }

        // Generate wrappers and content
        code.append(generateBody(structure, existingModifiers))

        code.append("}\n")

        return code.toString()
    }

    /**
     * Generates the function signature with proper content scope
     */
    private fun generateFunctionSignature(structure: ComponentStructure): String {
        val params = mutableListOf<String>()

        // Always include modifier parameter
        params.add("modifier: Modifier = Modifier")

        // Add additional parameters
        structure.additionalParameters.forEach { param ->
            val paramStr = if (param.defaultValue != null) {
                "${param.name}: ${param.type} = ${param.defaultValue}"
            } else {
                "${param.name}: ${param.type}"
            }
            params.add(paramStr)
        }

        // Add content parameter if needed
        if (structure.needsContentParameter) {
            val contentType = getContentType(structure.contentScope)
            val defaultValue = if (structure.contentDefaultEmpty) " = {}" else ""
            params.add("content: $contentType$defaultValue")
        }

        // Add text parameter if it's a text component
        if (structure.textConfig != null) {
            params.add("text: String")
        }

        return """@Composable
fun ${structure.name}(
    ${params.joinToString(",\n    ")}
)"""
    }

    /**
     * Gets the appropriate content type based on scope
     */
    private fun getContentType(scope: ContentScope): String {
        return when (scope) {
            ContentScope.ROW_SCOPE -> "@Composable RowScope.() -> Unit"
            ContentScope.COLUMN_SCOPE -> "@Composable ColumnScope.() -> Unit"
            ContentScope.BOX_SCOPE -> "@Composable BoxScope.() -> Unit"
            ContentScope.FLOW_ROW_SCOPE -> "@Composable FlowRowScope.() -> Unit"
            ContentScope.FLOW_COLUMN_SCOPE -> "@Composable FlowColumnScope.() -> Unit"
            ContentScope.DEFAULT -> "@Composable () -> Unit"
        }
    }

    /**
     * Generates state variable setup
     */
    private fun generateStateSetup(state: StateSetup): String {
        return if (state.mutable) {
            "    var ${state.variableName} by remember { mutableStateOf(${state.initialValue}) }\n"
        } else {
            "    val ${state.variableName} = remember { ${state.initialValue} }\n"
        }
    }

    /**
     * Generates animation setup
     */
    private fun generateAnimationSetup(anim: AnimationSetup): String {
        val animFunction = when (anim.type) {
            "Float" -> "animateFloatAsState"
            "Color" -> "animateColorAsState"
            "Dp" -> "animateDpAsState"
            "Int" -> "animateIntAsState"
            else -> "animateFloatAsState"
        }

        return """    val ${anim.variableName} by $animFunction(
        targetValue = ${anim.targetValue},
        animationSpec = ${anim.animationSpec}
    )
"""
    }

    /**
     * Generates the body with wrappers and content
     */
    private fun generateBody(structure: ComponentStructure, existingModifiers: List<String>): String {
        val code = StringBuilder()
        val indent = "    "

        // If responsive modifiers exist, wrap everything in BoxWithConstraints
        if (structure.needsBoxWithConstraints && structure.responsiveModifiers.isNotEmpty()) {
            code.append("${indent}BoxWithConstraints {\n")
            code.append(generateResponsiveBody(structure, existingModifiers, "$indent    "))
            code.append("$indent}\n")
        } else {
            code.append(generateNonResponsiveBody(structure, existingModifiers, indent))
        }

        return code.toString()
    }

    /**
     * Generates body for responsive components (inside BoxWithConstraints)
     */
    private fun generateResponsiveBody(structure: ComponentStructure, existingModifiers: List<String>, indent: String): String {
        val code = StringBuilder()

        // Generate outer wrapper (if exists)
        if (structure.outerWrapper != null) {
            code.append(generateWrapperOpen(structure.outerWrapper, indent, isOuter = true))
        }

        // Generate inner wrapper or direct content
        if (structure.innerWrapper != null) {
            val allModifiers = buildModifierChain(structure, existingModifiers)
            code.append(generateWrapperOpen(
                structure.innerWrapper,
                if (structure.outerWrapper != null) "$indent    " else indent,
                isOuter = false,
                modifiers = allModifiers,
                structure = structure
            ))

            // Generate content inside inner wrapper
            code.append(generateContent(structure, if (structure.outerWrapper != null) "$indent        " else "$indent    "))

            code.append(generateWrapperClose(if (structure.outerWrapper != null) "$indent    " else indent))
        } else if (structure.outerWrapper != null) {
            // No inner wrapper, content goes directly in outer wrapper
            code.append(generateContent(structure, "$indent    "))
        } else {
            // No wrappers at all - just render content with modifiers on a Box
            val allModifiers = buildModifierChain(structure, existingModifiers)
            if (allModifiers.isNotEmpty()) {
                code.append("${indent}Box(\n")
                code.append("$indent    modifier = modifier\n")
                allModifiers.filterNot { it.trim().startsWith("/*") }.forEach { mod ->
                    code.append("$indent        .$mod\n")
                }
                code.append("$indent) {\n")
                code.append(generateContent(structure, "$indent    "))
                code.append("$indent}\n")
            } else {
                code.append(generateContent(structure, indent))
            }
        }

        // Close outer wrapper (if exists)
        if (structure.outerWrapper != null) {
            code.append(generateWrapperClose(indent))
        }

        return code.toString()
    }

    /**
     * Generates body for non-responsive components
     */
    private fun generateNonResponsiveBody(structure: ComponentStructure, existingModifiers: List<String>, indent: String): String {
        val code = StringBuilder()

        // Generate outer wrapper (if exists)
        if (structure.outerWrapper != null) {
            code.append(generateWrapperOpen(structure.outerWrapper, indent, isOuter = true))
        }

        // Generate inner wrapper or direct content
        if (structure.innerWrapper != null) {
            val allModifiers = buildModifierChain(structure, existingModifiers)
            code.append(generateWrapperOpen(
                structure.innerWrapper,
                if (structure.outerWrapper != null) "$indent    " else indent,
                isOuter = false,
                modifiers = allModifiers,
                structure = structure
            ))

            // Generate content inside inner wrapper
            code.append(generateContent(structure, if (structure.outerWrapper != null) "$indent        " else "$indent    "))

            code.append(generateWrapperClose(if (structure.outerWrapper != null) "$indent    " else indent))
        } else if (structure.outerWrapper != null) {
            // No inner wrapper, content goes directly in outer wrapper
            code.append(generateContent(structure, "$indent    "))
        } else {
            // No wrappers at all - just render content with modifiers on a Box
            val allModifiers = buildModifierChain(structure, existingModifiers)
            if (allModifiers.isNotEmpty()) {
                code.append("${indent}Box(\n")
                code.append("$indent    modifier = modifier\n")
                allModifiers.filterNot { it.trim().startsWith("/*") }.forEach { mod ->
                    code.append("$indent        .$mod\n")
                }
                code.append("$indent) {\n")
                code.append(generateContent(structure, "$indent    "))
                code.append("$indent}\n")
            } else {
                code.append(generateContent(structure, indent))
            }
        }

        // Close outer wrapper (if exists)
        if (structure.outerWrapper != null) {
            code.append(generateWrapperClose(indent))
        }

        return code.toString()
    }

    /**
     * Builds the complete modifier chain including state-dependent and responsive modifiers
     */
    private fun buildModifierChain(structure: ComponentStructure, existingModifiers: List<String>): List<String> {
        val modifiers = mutableListOf<String>()
        val stateTypes = structure.stateDependentModifiers.map { it.stateType }.distinct()

        // Add focus tracking if needed
        if (SelectorType.FOCUS in stateTypes) {
            modifiers.add("onFocusChanged { isFocused = it.isFocused }")
            modifiers.add("focusable()")
        }

        // Add interaction detection modifiers for hover/active
        if (SelectorType.HOVER in stateTypes || SelectorType.ACTIVE in stateTypes) {
            val events = mutableListOf<String>()

            if (SelectorType.HOVER in stateTypes) {
                events.add("PointerEventType.Enter -> isHovered = true")
                events.add("PointerEventType.Exit -> isHovered = false")
            }
            if (SelectorType.ACTIVE in stateTypes) {
                events.add("PointerEventType.Press -> isPressed = true")
                events.add("PointerEventType.Release -> isPressed = false")
            }

            val eventHandlers = events.joinToString("\n                        ")

            val hoverCode = """pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            $eventHandlers
                        }
                    }
                }
            }"""
            modifiers.add(hoverCode)
        }

        // Add existing modifiers
        modifiers.addAll(existingModifiers)

        // Add state-dependent modifiers
        if (structure.stateDependentModifiers.isNotEmpty()) {
            for (stateMod in structure.stateDependentModifiers) {
                for (modifier in stateMod.modifiers) {
                    val stateVar = when (stateMod.stateType) {
                        SelectorType.HOVER -> "isHovered"
                        SelectorType.ACTIVE -> "isPressed"
                        SelectorType.FOCUS -> "isFocused"
                        SelectorType.DISABLED -> "isDisabled"
                    }

                    val modifierType = modifier.substringBefore("(")
                    val baseModifierIndex = modifiers.indexOfFirst { it.startsWith(modifierType) }

                    if (baseModifierIndex != -1) {
                        // Replace base modifier IN PLACE to preserve CSS box model ordering
                        val baseModifier = modifiers[baseModifierIndex]
                        modifiers[baseModifierIndex] = "then(if ($stateVar) Modifier.$modifier else Modifier.$baseModifier)"
                    } else {
                        modifiers.add("then(if ($stateVar) Modifier.$modifier else Modifier)")
                    }
                }
            }
        }

        // Add responsive modifiers
        if (structure.responsiveModifiers.isNotEmpty()) {
            for (responsiveMod in structure.responsiveModifiers) {
                for (modifier in responsiveMod.modifiers) {
                    val condition = buildResponsiveCondition(responsiveMod.minWidth, responsiveMod.maxWidth)
                    val modifierType = modifier.substringBefore("(")
                    val baseModifierIndex = modifiers.indexOfFirst { it.startsWith(modifierType) }

                    if (baseModifierIndex != -1) {
                        // Replace base modifier IN PLACE to preserve CSS box model ordering
                        val baseModifier = modifiers[baseModifierIndex]
                        modifiers[baseModifierIndex] = "then(if ($condition) Modifier.$modifier else Modifier.$baseModifier)"
                    } else {
                        modifiers.add("then(if ($condition) Modifier.$modifier else Modifier)")
                    }
                }
            }
        }

        return modifiers
    }

    /**
     * Builds a responsive condition based on min/max width
     */
    private fun buildResponsiveCondition(minWidth: Int?, maxWidth: Int?): String {
        return when {
            minWidth != null && maxWidth != null -> "maxWidth >= ${minWidth}.dp && maxWidth <= ${maxWidth}.dp"
            minWidth != null -> "maxWidth >= ${minWidth}.dp"
            maxWidth != null -> "maxWidth <= ${maxWidth}.dp"
            else -> "true"
        }
    }

    /**
     * Generates wrapper opening code
     */
    private fun generateWrapperOpen(
        wrapper: WrapperConfig,
        indent: String,
        isOuter: Boolean,
        modifiers: List<String> = emptyList(),
        structure: ComponentStructure? = null
    ): String {
        return when (wrapper) {
            is WrapperConfig.Row -> generateRowOpen(wrapper, indent, modifiers)
            is WrapperConfig.Column -> generateColumnOpen(wrapper, indent, modifiers)
            is WrapperConfig.FlowRow -> generateFlowRowOpen(wrapper, indent, modifiers)
            is WrapperConfig.FlowColumn -> generateFlowColumnOpen(wrapper, indent, modifiers)
            is WrapperConfig.Box -> generateBoxOpen(wrapper, indent, modifiers)
            is WrapperConfig.ScrollableRow -> generateScrollableRowOpen(wrapper, indent, modifiers)
            is WrapperConfig.ScrollableColumn -> generateScrollableColumnOpen(wrapper, indent, modifiers)
            is WrapperConfig.PositionedBox -> generatePositionedBoxOpen(wrapper, indent, isOuter)
            is WrapperConfig.LazyVerticalGrid -> generateLazyGridOpen(wrapper, indent, modifiers)
        }
    }

    private fun generateRowOpen(row: WrapperConfig.Row, indent: String, modifiers: List<String>): String {
        val code = StringBuilder()
        code.append("${indent}Row(\n")

        // Add modifier (filter out comments)
        code.append("$indent    modifier = modifier")
        modifiers.filterNot { it.trim().startsWith("/*") }.forEach { mod ->
            code.append("\n$indent        .$mod")
        }
        code.append(",\n")

        // Add arrangements and alignments
        if (row.horizontalArrangement != null) {
            code.append("$indent    horizontalArrangement = ${row.horizontalArrangement},\n")
        }
        if (row.verticalAlignment != null) {
            code.append("$indent    verticalAlignment = ${row.verticalAlignment},\n")
        }
        if (row.reverseLayout) {
            code.append("$indent    reverseLayout = true,\n")
        }

        // Remove trailing comma and add closing paren
        val result = code.toString().trimEnd(',', '\n') + "\n"
        return result + "$indent) {\n"
    }

    private fun generateColumnOpen(column: WrapperConfig.Column, indent: String, modifiers: List<String>): String {
        val code = StringBuilder()
        code.append("${indent}Column(\n")

        code.append("$indent    modifier = modifier")
        modifiers.filterNot { it.trim().startsWith("/*") }.forEach { mod ->
            code.append("\n$indent        .$mod")
        }
        code.append(",\n")

        if (column.verticalArrangement != null) {
            code.append("$indent    verticalArrangement = ${column.verticalArrangement},\n")
        }
        if (column.horizontalAlignment != null) {
            code.append("$indent    horizontalAlignment = ${column.horizontalAlignment},\n")
        }
        if (column.reverseLayout) {
            code.append("$indent    reverseLayout = true,\n")
        }

        val result = code.toString().trimEnd(',', '\n') + "\n"
        return result + "$indent) {\n"
    }

    private fun generateFlowRowOpen(flowRow: WrapperConfig.FlowRow, indent: String, modifiers: List<String>): String {
        val code = StringBuilder()
        code.append("${indent}FlowRow(\n")

        code.append("$indent    modifier = modifier")
        modifiers.filterNot { it.trim().startsWith("/*") }.forEach { mod ->
            code.append("\n$indent        .$mod")
        }
        code.append(",\n")

        if (flowRow.horizontalArrangement != null) {
            code.append("$indent    horizontalArrangement = ${flowRow.horizontalArrangement},\n")
        }
        if (flowRow.verticalArrangement != null) {
            code.append("$indent    verticalArrangement = ${flowRow.verticalArrangement},\n")
        }

        val result = code.toString().trimEnd(',', '\n') + "\n"
        return result + "$indent) {\n"
    }

    private fun generateFlowColumnOpen(flowColumn: WrapperConfig.FlowColumn, indent: String, modifiers: List<String>): String {
        val code = StringBuilder()
        code.append("${indent}FlowColumn(\n")

        code.append("$indent    modifier = modifier")
        modifiers.filterNot { it.trim().startsWith("/*") }.forEach { mod ->
            code.append("\n$indent        .$mod")
        }
        code.append(",\n")

        if (flowColumn.horizontalArrangement != null) {
            code.append("$indent    horizontalArrangement = ${flowColumn.horizontalArrangement},\n")
        }
        if (flowColumn.verticalArrangement != null) {
            code.append("$indent    verticalArrangement = ${flowColumn.verticalArrangement},\n")
        }

        val result = code.toString().trimEnd(',', '\n') + "\n"
        return result + "$indent) {\n"
    }

    private fun generateBoxOpen(box: WrapperConfig.Box, indent: String, modifiers: List<String>): String {
        val code = StringBuilder()
        code.append("${indent}Box(\n")

        code.append("$indent    modifier = modifier")
        modifiers.filterNot { it.trim().startsWith("/*") }.forEach { mod ->
            code.append("\n$indent        .$mod")
        }

        if (box.contentAlignment != null) {
            code.append(",\n$indent    contentAlignment = ${box.contentAlignment}")
        }

        return code.toString() + "\n$indent) {\n"
    }

    private fun generateScrollableRowOpen(scrollRow: WrapperConfig.ScrollableRow, indent: String, modifiers: List<String>): String {
        val code = StringBuilder()
        code.append("${indent}Row(\n")

        code.append("$indent    modifier = modifier\n")
        code.append("$indent        .horizontalScroll(${scrollRow.state})")
        modifiers.filterNot { it.trim().startsWith("/*") }.forEach { mod ->
            code.append("\n$indent        .$mod")
        }

        return code.toString() + "\n$indent) {\n"
    }

    private fun generateScrollableColumnOpen(scrollColumn: WrapperConfig.ScrollableColumn, indent: String, modifiers: List<String>): String {
        val code = StringBuilder()
        code.append("${indent}Column(\n")

        code.append("$indent    modifier = modifier\n")
        code.append("$indent        .verticalScroll(${scrollColumn.state})")
        modifiers.filterNot { it.trim().startsWith("/*") }.forEach { mod ->
            code.append("\n$indent        .$mod")
        }

        return code.toString() + "\n$indent) {\n"
    }

    private fun generatePositionedBoxOpen(posBox: WrapperConfig.PositionedBox, indent: String, isOuter: Boolean): String {
        val code = StringBuilder()

        if (isOuter) {
            // Outer positioning box needs fillMaxSize
            code.append("${indent}Box(modifier = Modifier.fillMaxSize()) {\n")
        } else {
            // Inner positioned element
            code.append("${indent}Box(\n")
            code.append("$indent    modifier = modifier\n")
            code.append("$indent        .align(${posBox.alignment})")

            if (posBox.offsetX != null || posBox.offsetY != null) {
                val x = posBox.offsetX ?: "0.dp"
                val y = posBox.offsetY ?: "0.dp"
                code.append("\n$indent        .offset(x = $x, y = $y)")
            }

            code.append("\n$indent) {\n")
        }

        return code.toString()
    }

    private fun generateLazyGridOpen(grid: WrapperConfig.LazyVerticalGrid, indent: String, modifiers: List<String>): String {
        val code = StringBuilder()
        code.append("${indent}LazyVerticalGrid(\n")
        code.append("$indent    columns = GridCells.Fixed(${grid.columns})")

        val filteredModifiers = modifiers.filterNot { it.trim().startsWith("/*") }
        if (filteredModifiers.isNotEmpty()) {
            code.append(",\n$indent    modifier = modifier")
            filteredModifiers.forEach { mod ->
                code.append("\n$indent        .$mod")
            }
        }

        return code.toString() + "\n$indent) {\n"
    }

    /**
     * Generates wrapper closing brace
     */
    private fun generateWrapperClose(indent: String): String {
        return "$indent}\n"
    }

    /**
     * Generates the actual content (children, text, or canvas)
     */
    private fun generateContent(structure: ComponentStructure, indent: String): String {
        return when {
            // Custom drawing (SVG, Canvas)
            structure.customDrawingCode != null -> {
                structure.customDrawingCode.lines().joinToString("\n") { line ->
                    if (line.isNotBlank()) "$indent$line" else line
                } + "\n"
            }

            // Text component
            structure.textConfig != null -> {
                generateTextComponent(structure.textConfig, indent)
            }

            // User content (children)
            structure.needsContentParameter -> {
                "${indent}content()\n"
            }

            // No content
            else -> ""
        }
    }

    /**
     * Generates Text component code
     */
    private fun generateTextComponent(textConfig: TextConfig, indent: String): String {
        val code = StringBuilder()
        code.append("${indent}Text(\n")
        code.append("$indent    text = text")

        textConfig.fontSize?.let {
            code.append(",\n$indent    fontSize = $it")
        }
        textConfig.fontWeight?.let {
            code.append(",\n$indent    fontWeight = $it")
        }
        textConfig.fontStyle?.let {
            code.append(",\n$indent    fontStyle = $it")
        }
        textConfig.color?.let {
            code.append(",\n$indent    color = $it")
        }
        textConfig.textAlign?.let {
            code.append(",\n$indent    textAlign = $it")
        }
        textConfig.textDecoration?.let {
            code.append(",\n$indent    textDecoration = $it")
        }
        textConfig.lineHeight?.let {
            code.append(",\n$indent    lineHeight = $it")
        }
        textConfig.letterSpacing?.let {
            code.append(",\n$indent    letterSpacing = $it")
        }
        textConfig.maxLines?.let {
            code.append(",\n$indent    maxLines = $it")
        }
        textConfig.overflow?.let {
            code.append(",\n$indent    overflow = $it")
        }

        code.append("\n$indent)\n")
        return code.toString()
    }

    /**
     * Generates import statements
     */
    fun generateImports(structure: ComponentStructure): List<String> {
        return structure.requiredImports.sorted()
    }
}
