# Java conventions

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
