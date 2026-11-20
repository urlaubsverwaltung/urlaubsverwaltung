# AGENTS.md

## Decisions

Decisions are recorded in [docs/adr](docs/adr), see its [README](docs/adr/README.md) for how to add one.
Before working in the area of an ADR, read its Decision section, e.g. `sed '/^## Context/q' <file>`. Read the rest
only to question or change the decision.

- [Mark required form fields with the required attribute and leave their validation to the server](docs/adr/2026-10-30-accessible-required-form-fields.md):
  when adding or changing form fields, their labels or their validation.
- [Prefer native HTML and visible text over ARIA](docs/adr/2026-11-08-prefer-native-html-over-aria.md):
  when adding interactive elements, icons or labels without visible text, or ARIA attributes.

## Conventions

Before adding or changing code, read the conventions of its language:

- Java: [docs/development/java.md](docs/development/java.md)
- CSS in `src/main/css` and `src/main/javascript`: [docs/development/css.md](docs/development/css.md)
- Thymeleaf templates in `src/main/resources/templates`: [docs/development/thymeleaf.md](docs/development/thymeleaf.md)

Conventions of another language go into `docs/development/<language>.md` with one line here that says when to
read the file.

## User input and errors

Applies to view controllers, their templates and API controllers in code you add or change.

Input that is invalid or violates a business rule must never cause a 5xx response, neither in the UI nor in the
REST API. In the UI this concerns what a user can enter or choose in a page it renders, not requests the UI cannot
produce.

- In a view controller validate user input before calling a service, with bean validation on the DTO or a Spring
  `Validator`. On errors render the form again as an HTML page and show a message at the field that names the
  expectation, e.g. "must be a positive number" or the pattern to match.
- Do not validate or handle what only a manipulated request can send to a view controller, e.g. a changed hidden
  field, an option the form does not offer or a missing parameter. Whatever response such a request causes, a 5xx
  included, is fine. Permission checks still apply to every request.
- In an API controller check the input before calling a service and answer with a 4xx status and a message that
  names the violated rule, e.g. `throw new ResponseStatusException(BAD_REQUEST, "Period must not be longer than …")`.
- The validation covers every precondition of the services the controller calls that the input of the UI or of an
  API client can violate. An `IllegalArgumentException` or `IllegalStateException` thrown by a service guards
  against programming errors, it is not user feedback.
- Do not catch such an exception in the controller to turn it into a message, and do not add a catch-all handler.
  Add the missing validation instead. A 5xx caused by a bug or an outage stays a 5xx.
- What the UI itself offers never leads to a 4xx response: a form it renders can be submitted and a link it renders
  can be followed by the user who sees it. Do not render links or actions the user is not allowed to use.
- In the UI a 403 or 404 remains the answer to a manipulated URL or an outdated bookmark, e.g. an unknown id or a
  page of another person.

## Frontend assets

The running app serves the built assets from `target/classes/static`. Rebuild them after a change:

- `src/main/javascript/**` (including CSS imported by a component, e.g. `calendar.css`): `npm run js`
- `src/main/css/**`: `npm run css`
- both, or unsure: `npm run build`
