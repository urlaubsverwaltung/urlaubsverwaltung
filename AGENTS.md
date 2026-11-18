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
- Do not pass a multi-line expression as an argument, e.g. a stream pipeline or a constructor call whose arguments
  span several lines. Assign it to a variable whose name says what it holds and pass the variable:

  ```java
  // do
  final List<PersonDto> persons = personService.getActivePersons().stream()
      .map(PersonDto::from)
      .toList();
  model.addAttribute("persons", persons);

  // don't
  model.addAttribute("persons", personService.getActivePersons().stream()
      .map(PersonDto::from)
      .toList());
  ```

- This does not apply to builder chains that an API expects inline, e.g. a MockMvc request builder or the Spring
  Security configuration, nor to lambdas, see the next rule.
- Extract the body of a lambda into a private method when it is a block, combines conditions (`&&`, `||`, `?:`),
  builds an object from parts of its parameter or uses its parameter more than once. Name the method after what it
  returns, make it `static` when it uses no instance state and pass it as method reference:

  ```java
  // do
  .map(this::publicHolidaysOf)

  private List<PublicHoliday> publicHolidaysOf(Map.Entry<DateRange, WorkingTime> workingTimeByDateRange) {
      final DateRange dateRange = workingTimeByDateRange.getKey();
      final FederalState federalState = workingTimeByDateRange.getValue().getFederalState();
      return publicHolidaysService.getPublicHolidays(dateRange.startDate(), dateRange.endDate(), federalState);
  }

  // don't
  .map(entry -> publicHolidaysService.getPublicHolidays(entry.getKey().startDate(), entry.getKey().endDate(), entry.getValue().getFederalState()))
  ```

- When the extracted method needs a local variable, pass it in a lambda that only calls the method, e.g.
  `closure -> toDto(closure, locale)`.
- Keep a lambda inline when its body is a single call, which may read one getter of the parameter or pass the
  parameter as it is, e.g. `entry -> entry.monthDay().equals(monthDay)`, `entry -> publish(entry.monthDay())` or
  `closure -> new CompanyClosureDto(closure, locale)`. Keep it inline as well in tests for the lambda of `satisfies`,
  `isInstanceOfSatisfying` or `thenAnswer`.
- In tests, capture an argument with `ArgumentCaptor` and assert it instead of combining conditions in `argThat`. A
  failing assertion names the field with expected and actual value, a failing `argThat` only reports a different
  argument.

### Maintaining the conventions

The conventions stay inline while they are short, so they apply without an extra file read. When this section exceeds
about ten rules, or conventions for another language are added, suggest moving each language to
`docs/development/<language>.md` and keeping one line per language here that says when to read the file.

## CSS conventions

Apply them to CSS you add or change in `src/main/css` and `src/main/javascript`. Leave other code as it is.

- Do not use `@apply`. Write the native declaration with the theme variable of Tailwind, e.g.
  `font-weight: var(--font-weight-bold);`, `border-radius: var(--radius-sm);`,
  `border-color: var(--color-gray-200);` or `padding: --spacing(4);`.
- A font size is the declaration `font-size: var(--text-sm);`. Add `line-height: var(--text-sm--line-height);` when
  the element should not keep the line height it has, e.g. the one a heading gets from the base styles.
- Use `@variant` for breakpoints and states, e.g. `@variant md`, `@variant dark` or `@variant hover`.
- A utility without a single native declaration, e.g. `sr-only`, stays a class in the template.
- A bundle with `@reference` only gets the theme variables that `style.css` contains, and Tailwind only adds those in
  use. After `npm run css` check that a variable you use for the first time is defined in
  `target/classes/static/css/style.css`.

## User input and errors

Applies to view controllers, their templates and API controllers in code you add or change.

Input that is invalid or violates a business rule must never cause a 5xx response, neither in the UI nor in the
REST API.

- In a view controller validate user input before calling a service, with bean validation on the DTO or a Spring
  `Validator`. On errors render the form again as an HTML page and show a message at the field that names the
  expectation, e.g. "must be a positive number" or the pattern to match.
- In an API controller check the input before calling a service and answer with a 4xx status and a message that
  names the violated rule, e.g. `throw new ResponseStatusException(BAD_REQUEST, "Period must not be longer than …")`.
- The validation covers every precondition of the services the controller calls. An `IllegalArgumentException` or
  `IllegalStateException` thrown by a service guards against programming errors, it is not user feedback.
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
