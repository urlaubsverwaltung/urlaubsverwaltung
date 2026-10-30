# AGENTS.md

Decisions are recorded in [docs/adr](docs/adr). Read the relevant ones before changing the affected code.

## Accessibility

- Prefer native HTML and visible (or visually hidden) text over ARIA attributes. Use ARIA only where HTML
  has no equivalent.
- Required form fields: `required` on the control, `novalidate` on the form, no `is-required` on the label.
  Radio and checkbox groups keep `uv-form__label--required` on their group label, required radios get
  `required` as well. `is-required` stays only for a control composed of several inputs where one of them
  is sufficient. The server side validation is the source of truth, keep it in sync with the `required`
  attributes and marker classes.
  See [ADR 1](docs/adr/0001-accessible-required-form-fields.md).
