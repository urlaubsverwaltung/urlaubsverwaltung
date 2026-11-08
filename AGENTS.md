# AGENTS.md

## Decisions

Decisions are recorded in [docs/adr](docs/adr), see its [README](docs/adr/README.md) for how to add one.
Before working in the area of an ADR, read its Decision section, e.g. `sed '/^## Context/q' <file>`. Read the rest
only to question or change the decision.

- [Mark required form fields with the required attribute and leave their validation to the server](docs/adr/2026-10-30-accessible-required-form-fields.md):
  when adding or changing form fields, their labels or their validation.
- [Prefer native HTML and visible text over ARIA](docs/adr/2026-11-08-prefer-native-html-over-aria.md):
  when adding interactive elements, icons or labels without visible text, or ARIA attributes.

## Frontend assets

The running app serves the built assets from `target/classes/static`. Rebuild them after a change:

- `src/main/javascript/**` (including CSS imported by a component, e.g. `calendar.css`): `npm run js`
- `src/main/css/**`: `npm run css`
- both, or unsure: `npm run build`
