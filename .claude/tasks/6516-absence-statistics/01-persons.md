# 01 — Package und Personenauflösung

**Abhängig von:** —
**Berührt:** neues Package `org.synyx.urlaubsverwaltung.absence.statistics`

## Ziel

Ein Modul, das für ein Jahr und eine angemeldete Person den Personenkreis liefert, über den aggregiert wird.

## Umsetzung

Neue package-private Klasse `AbsenceStatisticsPersons` im neuen Package. Keine Spring-Annotation an der Rechenlogik selbst — sie bekommt ihre Kollaborateure (`PersonService`, `DepartmentService`, `PersonActivePeriodService`) im Konstruktor und ist ansonsten eine reine Abbildung.

Regeln:

- `OFFICE` oder `BOSS` → alle Personen des Systems.
- `DEPARTMENT_HEAD` oder `SECOND_STAGE_AUTHORITY` → `DepartmentService#getManagedMembersOfPerson`.
- Keine dieser Rollen → leere Liste.
- Anschließend über `PersonActivePeriodService#getActivePeriodsOverlapping` auf die Personen einschränken, die im gefragten Jahr mindestens einen Tag aktiv waren. Der Zeitraum ist `[01.01. des Jahres, 01.01. des Folgejahres)`; `getActivePeriodsOverlapping` erwartet `Instant` und ein exklusives Ende.

Eine Person, die inzwischen deaktiviert ist, bleibt für ein Jahr enthalten, in dem sie aktiv war. Das heutige `INACTIVE`-Flag wird bewusst **nicht** als Filter verwendet — anders als es `SickNoteStatisticsService` heute noch tut, weil dort die Aktivzeiträume aus #6402 noch nicht genutzt werden.

## Tests

`AbsenceStatisticsPersonsTest`, ohne Spring-Kontext, Kollaborateure gemockt:

- je Rolle der erwartete Personenkreis, inklusive leerer Liste ohne passende Rolle
- Person mit Eintritt zur Jahresmitte ist im Jahr enthalten
- Person mit Austritt zur Jahresmitte ist im Jahr enthalten
- Person, die erst im Folgejahr eintritt, ist nicht enthalten
- inzwischen deaktivierte Person ist in einem vergangenen Jahr, in dem sie aktiv war, weiterhin enthalten
- `DEPARTMENT_HEAD` erhält nur verwaltete Mitglieder, nicht die ganze Belegschaft

## Definition of Done

- [ ] Package angelegt, `AbsenceStatisticsPersons` implementiert
- [ ] Tests grün, alle oben genannten Fälle abgedeckt
- [ ] Keine Nutzung von `Person#isActive` als Jahresfilter