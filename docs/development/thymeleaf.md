# Thymeleaf conventions

Apply them to lines you add or change in the templates in `src/main/resources/templates`. Leave other code as it is.

- Use `th:if`, not `th:unless`. Write the condition under which the element is shown, so it reads without inverting
  it in your head, e.g. `th:if="${monthDay != null}"` instead of `th:unless="${monthDay == null}"` and
  `th:if="${not #lists.isEmpty(persons)}"` instead of `th:unless="${#lists.isEmpty(persons)}"`.
