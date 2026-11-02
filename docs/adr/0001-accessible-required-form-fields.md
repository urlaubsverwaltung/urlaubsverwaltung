# 1. Accessible required form fields

Date: 2026-10-30

## Status

Accepted

## Context

Required form fields were marked only visually: the `is-required` class on the label renders an asterisk.
Screen readers did not announce these fields as required, and the asterisk was maintained independently
of the control, so both could drift apart.

Validation happens on the server. The Spring validators render localised error messages next to the fields
and report all errors at once.

The `required` attribute adds the semantics for assistive technology, but also enables the browser's
constraint validation. The browser then blocks the submit with its own bubbles: in the browser's language
instead of the user's locale, one field at a time, styled differently than the server errors. A required
control inside a hidden section (`display: none`) even blocks the submit silently, since the browser can
not focus it.

## Decision

Prefer native HTML and visible (or visually hidden) text over ARIA attributes, e.g. `required` instead
of `aria-required`, a `<span class="sr-only">` instead of `aria-label`. ARIA is used where HTML has no
equivalent.

For required fields:

- The control gets the `required` attribute. A control composed of several inputs which are mandatory
  each (e.g. hours and minutes of a time) gets it on every input.
- The label asterisk is derived from it via CSS (`label:not(:has(input)):has(+ :required, + * :required)`),
  so no class is needed on the label. Labels nesting their own radio or checkbox are excluded, otherwise the
  asterisk would show behind the option preceding a required radio.
- Every form containing a required control gets `novalidate`. The server side validation stays the only
  validation UX.
- Radio and checkbox groups keep `uv-form__label--required` on their `<p class="uv-form__label">` for the
  asterisk. The radios of a required group additionally get the `required` attribute. A checkbox group can
  not express "at least one" with `required`, it has the marker class only.
- `is-required` on the `<label>` stays only for a control composed of several inputs where one of them is
  sufficient, e.g. hours or minutes.
- Screen readers announce the asterisk as part of the label ("Titel star"). Where the required state is
  announced already, i.e. for labels of required controls and radio groups with required radios, the
  asterisk gets an empty alternative text (`content: "*" / ""`, preceded by `content: "*"` as fallback for
  browsers without support). Checkbox groups and `is-required` keep the announced asterisk, it is their only
  hint.
- Fragments rendering a control accept a `required` parameter (`fragments/select::one(..., required=true)`,
  `fragments/hour-and-minute-input::hour-and-minute-input(..., required=true)`). The parameter is optional
  and defaults to `false` via `th:with`. Thymeleaf can not mix positional and named arguments, so a caller
  passing `required` names all arguments. The datepicker passes `required` on to the duet date input.

The Java validator stays the source of truth. Keeping it in sync with the `required` attributes is a manual
task and not verified by a test: many rules are conditional (e.g. a reason only for special leave, hours or
minutes) and a mismatch only affects the marker, the server still validates.

## Consequences

- Screen readers announce required fields, and the asterisk always matches the control.
- Checkbox groups and controls where one of several inputs is sufficient are still marked only visually.
- `AccessibilityCrawlerAccessibilityIT` fails for a crawled page containing a `form:not([novalidate])`
  with a required control.
- Changing a validator to make a field (not) mandatory requires adjusting the `required` attribute in the
  template as well.
- `novalidate` disables all native constraint validation of the form, e.g. for `type="number"`. Invalid
  input is sent as an empty value and reported by the server validation.
- The HTML spec expects a required `select` to start with an empty placeholder option. Most required selects
  have a preselected value and no such option. Browsers handle this fine, an HTML validator reports it.
