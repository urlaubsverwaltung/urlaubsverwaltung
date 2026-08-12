# 04 — Statistik-Service

**Abhängig von:** 01, 02, 03
**Berührt:** `org.synyx.urlaubsverwaltung.absence.statistics`

## Ziel

Ein `@Service`, der die Daten beschafft, die drei Kerne zusammensetzt und ein Ergebnisobjekt je Jahr liefert.

## Umsetzung

Neue Klasse `AbsenceStatisticsService` mit einer Methode in der Art von
`AbsenceStatistics createStatistics(Year year, Person signedInUser)`.

Beschaffung:

- Personen über `AbsenceStatisticsPersons`
- Anträge über `ApplicationService#getApplicationsForACertainPeriodAndStatus` für das **ganze** Jahr und `ApplicationStatus.activeStatuses()`
- Arbeitszeitkalender über `WorkingTimeCalendarService#getWorkingTimesByPersons`
- Abwesenheitsarten über `VacationTypeService`
- Urlaubskonten über `AccountService`, offene Tage über `VacationDaysService`

Zusammensetzung:

- `MonthlyAbsenceDays` für Monatswerte und Jahressummen je Art
- `RemainingVacationDays` für die Urlaubskennzahlen, mit den Stichtagen nach Regel aus Task 03 (dieser Service entscheidet, welche Stichtage gelten — er kennt `Clock` und das gefragte Jahr)
- Gesamtsumme aller Abwesenheitstage und Durchschnitt pro Mitarbeitende

Das Ergebnisobjekt `AbsenceStatistics` ist ein Wertobjekt ohne eigene Beschaffung.

Aufrufmuster: der Controller ruft den Service zweimal, für das gewählte Jahr und für das Vorjahr. Innerhalb eines Aufrufs sollen Personen, Kalender und Konten genau einmal beschafft und durch die Kerne gereicht werden — `ApplicationForLeaveStatisticsBuilder` macht das bereits so und begründet es dort im Kommentar.

Leere Eingaben (keine Personen, keine Anträge) müssen ein leeres, aber vollständiges Ergebnis liefern: zwölf Monatswerte, keine Arten, Urlaubskennzahlen auf 0. Kein Sonderpfad, keine Ausnahme.

## Tests

`AbsenceStatisticsServiceTest` mit gemockten Kollaborateuren. Hier wird **nicht** die Rechenlogik der Kerne noch einmal geprüft, sondern die Verdrahtung:

- Anträge werden für das ganze Jahr und mit `activeStatuses()` angefragt
- der Personenkreis stammt aus `AbsenceStatisticsPersons`
- Stichtagsregel: laufendes Jahr, vergangenes Jahr, zukünftiges Jahr ergeben die erwarteten Stichtage
- leerer Personenkreis führt zu einem leeren Ergebnis ohne Ausnahme und ohne unnötige Folgeabfragen

## Definition of Done

- [ ] `AbsenceStatisticsService` und `AbsenceStatistics` implementiert
- [ ] Personen, Kalender und Konten werden je Aufruf einmal beschafft
- [ ] Stichtagsregel liegt im Service, nicht in den Kernen
- [ ] Tests grün
