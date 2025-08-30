## Contributing Notes (for AI + Humans)

### Principles
- Preserve visual parity
- Prefer explicit, test-backed rules
- Make unsupported mappings obvious, not silent

### Adding/Editing Mappings
- Update mapping YAML/JSON
- Add/adjust unit tests and golden outputs
- Document any lossy conversions

### Logging and Traceability
- Every conversion run should produce diagnostics
- Keep `AiLogs/` updated with session notes and decisions

### Code Style
- Clear naming, early returns, explicit types
- Avoid catching exceptions unless actionable


