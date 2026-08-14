# 04 — Statistik-Service

**Abhängig von:** 01, 02, 03
**Berührt:** `org.synyx.urlaubsverwaltung.absence.statistics`

## Ziel

Ein `@Service`, der die Daten beschafft, die drei Kerne zusammensetzt und ein Ergebnisobjekt für ein Jahr liefert.

## Umsetzung

Neue Klasse `AbsenceStatisticsService` mit einer Methode in der Art von
`AbsenceStatistics createStatistics(Year year, Person signedInUser)`.

Beschaffung:

- Personen über `AbsenceStatisticsPersons`
- Anträge über `ApplicationService#getApplicationsForACertainPeriodAndStatus` für das **ganze** Jahr und `ApplicationStatus.activeStatuses()`
- Arbeitszeitkalender über `WorkingTimeCalendarService#getWorkingTimesByPersons`
- Urlaubskonten über `AccountService`, offene Tage über `VacationDaysService`

(`VacationTypeService` fällt als Beschaffung weg: Die Abwesenheitsart je Antrag steckt schon in `Application#getVacationType()`, und `MonthlyAbsenceDays` filtert Arten ohne Tage bereits selbst heraus. Eine separate Liste aller Arten — aktiv oder nicht — wird an keiner Stelle gebraucht.)

Zusammensetzung:

- `MonthlyAbsenceDays` für Monatswerte und Jahressummen je Art
- `VacationDaysTaken` für die Urlaubskennzahlen, mit dem Stichtag nach Regel aus Task 03 (dieser Service entscheidet, welcher Stichtag gilt — er kennt `Clock` und das gefragte Jahr)

Das Ergebnisobjekt `AbsenceStatistics` ist ein Wertobjekt ohne eigene Beschaffung.

Aufrufmuster: der Controller ruft den Service **einmal**, für das gewählte Jahr — es gibt keinen Vorjahresvergleich mehr auf der Seite. Personen, Kalender und Konten sollen innerhalb eines Aufrufs genau einmal beschafft und durch die Kerne gereicht werden — `ApplicationForLeaveStatisticsBuilder` macht das bereits so und begründet es dort im Kommentar.

Leere Eingaben (keine Personen, keine Anträge) müssen ein leeres, aber vollständiges Ergebnis liefern: zwölf Monatswerte, keine Arten, Urlaubskennzahlen auf 0. Kein Sonderpfad, keine Ausnahme.

## Tests

`AbsenceStatisticsServiceTest` mit gemockten Kollaborateuren. Hier wird **nicht** die Rechenlogik der Kerne noch einmal geprüft, sondern die Verdrahtung:

- Anträge werden für das ganze Jahr und mit `activeStatuses()` angefragt
- der Personenkreis stammt aus `AbsenceStatisticsPersons`
- Stichtagsregel: laufendes Jahr, vergangenes Jahr, zukünftiges Jahr ergeben den erwarteten Stichtag
- leerer Personenkreis führt zu einem leeren Ergebnis ohne Ausnahme und ohne unnötige Folgeabfragen

## Definition of Done

- [x] `AbsenceStatisticsService` und `AbsenceStatistics` implementiert
- [x] Personen, Kalender und Konten werden je Aufruf einmal beschafft
- [x] Stichtagsregel liegt im Service, nicht in den Kernen
- [x] Tests grün
