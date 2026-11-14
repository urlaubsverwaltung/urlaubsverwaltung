# AGENTS.md

## Decisions

Decisions are recorded in [docs/adr](docs/adr), see its [README](docs/adr/README.md) for how to add one.
Before working in the area of an ADR, read its Decision section, e.g. `sed '/^## Context/q' <file>`. Read the rest
only to question or change the decision.

- [Mark required form fields with the required attribute and leave their validation to the server](docs/adr/2026-10-30-accessible-required-form-fields.md):
  when adding or changing form fields, their labels or their validation.
- [Prefer native HTML and visible text over ARIA](docs/adr/2026-11-08-prefer-native-html-over-aria.md):
  when adding interactive elements, icons or labels without visible text, or ARIA attributes.

## Java conventions

Apply them to code you add or change. When you change a method, they apply to the whole method. Leave other code
as it is.

- Declare local variables with their explicit type instead of `var`, e.g.
  `final List<Person> persons = new ArrayList<>();`.
- Do not negate a condition just to return early from a `void` method. When the remaining body has no nested blocks
  (a loop, another condition, a `try`), check the positive condition and put the body inside it:

  ```java
  // do
  if (persons.isEmpty()) {
      createDefaults();
  }

  // don't
  if (!persons.isEmpty()) {
      return;
  }
  createDefaults();
  ```

- Keep the early return in every other case: the guard returns a value, its condition is not negated, the remaining
  body contains nested blocks, or the method checks several preconditions one after the other.

### Maintaining the conventions

The conventions stay inline while they are short, so they apply without an extra file read. When this section exceeds
about ten rules, or conventions for another language are added, suggest moving each language to
`docs/development/<language>.md` and keeping one line per language here that says when to read the file.

## Frontend assets

The running app serves the built assets from `target/classes/static`. Rebuild them after a change:

- `src/main/javascript/**` (including CSS imported by a component, e.g. `calendar.css`): `npm run js`
- `src/main/css/**`: `npm run css`
- both, or unsure: `npm run build`
