# Style-Converter
Convert JSON-based style specs into Android Jetpack Compose (Material 3) and iOS SwiftUI code.

## Quick start
1) Install deps
```bash
npm i
```
2) Run tests (includes first golden test)
```bash
npm test
```
3) Convert an example
```bash
npx ts-node src/cli/index.ts convert --from json --to compose,swiftui -i test/goldens/mvp-border-per-side/input.webStyles.json -o out
```

Outputs:
- out/androidStyles.json
- out/iosStyles.json

Note: Build requires TypeScript in dev deps. If `tsc` is not found globally, use `npm test` or `npx ts-node` for development.
