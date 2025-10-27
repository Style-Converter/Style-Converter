## Testing Strategy

### Current Status
- **Test Framework**: Not yet implemented (planned for M4)
- **Manual Testing**: CLI can be run manually with example files
- **Example Files**: Located in `examples/` directory

### Manual Testing (Current)

#### Running the CLI
```bash
./gradlew run --args="convert --from css --to compose,swiftui -i examples/mvp-border-per-side.json -o out"
```

#### Verifying Output
1. Check IR generation: `out/tmpOutput.json`
2. Check generated files: `out/androidStyles.json`, `out/iosStyles.json` (once generators implemented)
3. Inspect IR structure for correct parsing of properties, selectors, and media queries

#### Current Test Files
Located in `examples/`:
- `mvp-border-per-side.json` - Tests per-side borders, selectors, media queries
- `Accessibility_&_Semantic_Adjustments/testFile.json`
- `Backgrounds_&_Masks/testFile.json`
- `Border_&_Outlines/standard.json`, `hard.json`
- `PrimitiveParsers/ColorParser/ColorTest.json`
- `PrimitiveParsers/LengthParser/LenthTest.json`
- `extreme_longhand.json`

### Planned Testing Strategy (M4)

#### 1. Snapshot/Golden Tests
**What they are:**
- Store expected outputs ("goldens") for representative inputs in version control
- Each test converts a known input and compares result to stored golden
- Code changes causing different output fail the test, highlighting what changed
- Intended changes are accompanied by updating goldens

**Why this helps:**
- Ensures deterministic, stable output for the same input
- Prevents regressions in mappings and formatting
- Documents expected behavior through examples

**Implementation plan:**
- Use Gradle test framework (JUnit 5 or Kotlin Test)
- Test directory structure:
  ```
  src/test/kotlin/
  ├── parsing/
  │   ├── CssParsingTest.kt
  │   ├── PropertyParserTest.kt
  │   └── PrimitiveParserTests.kt
  ├── generation/
  │   ├── ComposeGeneratorTest.kt
  │   └── SwiftUIGeneratorTest.kt
  └── integration/
      └── EndToEndTests.kt

  src/test/resources/
  ├── goldens/
  │   ├── ir/              # Expected IR JSON outputs
  │   ├── compose/         # Expected Compose code outputs
  │   └── swiftui/         # Expected SwiftUI code outputs
  └── inputs/              # Test input files
  ```

#### 2. Unit Tests

**Parser Tests:**
- Test each primitive parser (Color, Length, Keyword, Function, Shadow, URL)
- Test GenericPropertyParser with various property types
- Test selector and media query parsing
- Test edge cases and malformed input

**Generator Tests:**
- Test IR → Compose code generation
- Test IR → SwiftUI code generation
- Test unit conversion (px → dp, px → sp, px → pt)
- Test color format conversion
- Test unsupported property warnings

**Model Tests:**
- Test IR serialization/deserialization
- Test data class equality and copying

#### 3. Integration Tests

**End-to-End:**
- Full pipeline: JSON input → IR → Generated code
- Multiple target generation (compose + swiftui simultaneously)
- File I/O and output directory creation
- CLI argument parsing

**Golden Test Coverage (20+ cases):**
1. **Colors** (3 cases)
   - Hex colors (#RGB, #RRGGBB, #RRGGBBAA)
   - RGB/RGBA functions
   - HSL/HSLA and named colors

2. **Typography** (4 cases)
   - Font size, weight, style
   - Line height (unitless and px)
   - Letter spacing
   - System fonts

3. **Spacing** (3 cases)
   - Margin shorthand (1, 2, 3, 4 values)
   - Padding shorthand (1, 2, 3, 4 values)
   - Individual sides

4. **Borders** (4 cases)
   - Uniform borders (shorthand)
   - Per-side borders (width, style, color)
   - Per-corner radii
   - Complex multi-side combinations

5. **Size** (2 cases)
   - Width/height (px, %, vh/vw)
   - Min/max constraints

6. **Layout** (3 cases)
   - Display flex with direction
   - Justify-content and align-items
   - Flex-wrap and gap

7. **Effects** (2 cases)
   - Single box-shadow
   - Opacity

8. **Selectors** (2 cases)
   - Hover states
   - Active/focus/disabled states

9. **Media Queries** (2 cases)
   - Min-width breakpoints
   - Max-width and combined queries

10. **Complex Cases** (2 cases)
    - Multiple properties + selectors + media
    - Edge cases and boundary conditions

#### 4. Error Handling Tests
- Invalid JSON input
- Unsupported property values
- Malformed CSS values
- Missing required fields
- Empty components

#### 5. Performance Tests (Future)
- Large input files (100+ components)
- Deep nesting (selectors + media)
- Parsing speed benchmarks

### Visual Tests (M7 - Future)
**Purpose:**
- Validate actual visual parity beyond code equivalence
- Catch platform-specific rendering differences

**Implementation:**
- Render small test components on each platform
- Screenshot capture automation
- Perceptual diff comparison (pixel-by-pixel with threshold)
- Regression suite for visual changes

**Tools (TBD):**
- Android: Screenshot testing (Paparazzi, Shot, or Compose Preview testing)
- iOS: Snapshot testing (SnapshotTesting library)
- Diff tool: ImageMagick, pixelmatch, or similar

### Test Execution

#### Running Tests (once implemented)
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests CssParsingTest

# Run with coverage
./gradlew test jacocoTestReport

# Update goldens (when intentionally changing output)
./gradlew test -PupdateGoldens
```

#### Continuous Integration
- Run tests on every commit
- Fail build on test failures
- Generate coverage reports
- Store test artifacts

### Test-Driven Development Workflow
1. Write failing test for new feature
2. Implement minimal code to pass test
3. Refactor while keeping tests green
4. Update goldens when output format changes intentionally
5. Document any breaking changes in commit message


