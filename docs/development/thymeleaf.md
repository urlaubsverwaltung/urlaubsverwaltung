# Thymeleaf conventions

Apply them to lines you add or change in the templates in `src/main/resources/templates`. Leave other code as it is.

- Use `th:if`, not `th:unless`. Write the condition under which the element is shown, so it reads without inverting
  it in your head, e.g. `th:if="${monthDay != null}"` instead of `th:unless="${monthDay == null}"` and
  `th:if="${not #lists.isEmpty(persons)}"` instead of `th:unless="${#lists.isEmpty(persons)}"`.
- Give a `<ul>` or `<ol>` without list markers `role="list"`. The base styles set `list-style: none` on every list,
  and WebKit then drops the list semantics, so VoiceOver in Safari announces neither the list nor its number of items.
  This is a browser workaround, not a use of ARIA in place of HTML, see
  [Prefer native HTML and visible text over ARIA](../adr/2026-11-08-prefer-native-html-over-aria.md). SonarCloud
  reports it as redundant role; accept that finding.
