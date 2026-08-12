# 02 — Monatsaufteilung der Abwesenheitstage

**Abhängig von:** 01 (nur wegen des Packages)
**Berührt:** `org.synyx.urlaubsverwaltung.absence.statistics`

## Ziel

Der Rechenkern für Balkendiagramm und Kuchendiagramm: aus Anträgen und Arbeitszeitkalendern werden Werktage je Abwesenheitsart und Monat.

## Umsetzung

Neue Klasse `MonthlyAbsenceDays` — rein, ohne Spring, ohne Repository-Zugriff. Eingabe ist alles, was sie braucht; Beschaffung ist Aufgabe von Task 04.

Eingabe:
- `Year`
- Anträge (`List<Application>`), bereits auf `ApplicationStatus.activeStatuses()` gefiltert
- `Map<Person, WorkingTimeCalendar>`

Ausgabe: Werktage je `VacationType` und Monatsindex 0–11, als `BigDecimal`.

Regeln:

- Jeder Tag eines Antrags zählt in dem Monat, in dem er liegt. Anträge über Monats- und Jahresgrenzen werden tagesgenau gesplittet; Tage außerhalb des gefragten Jahres fallen weg.
- Gezählt wird über den `WorkingTimeCalendar` der jeweiligen Person: Wochenenden, Feiertage und nicht gearbeitete Wochentage ergeben 0, halbe Tage 0,5. `ApplicationForLeaveStatisticsBuilder` macht das für Zeiträume bereits vor — dieselbe Zählweise verwenden, nur monatsweise statt über den ganzen Zeitraum.
- Der Antragsstatus wird über die Filterung hinaus nicht unterschieden.
- Arten ohne Tage tauchen im Ergebnis nicht auf; das Ausblenden von Nullzeilen ist damit hier erledigt und muss nicht im Template passieren.

Zusätzlich soll das Modul die Jahressumme je Art liefern — das ist dieselbe Rechnung und vermeidet, dass der Kuchen die Zahlen an anderer Stelle noch einmal ableitet.

## Tests

`MonthlyAbsenceDaysTest`, ohne Spring:

- Antrag innerhalb eines Monats
- Antrag über den Monatswechsel — Tage landen in beiden Monaten
- Antrag über den Jahreswechsel — nur der Anteil im gefragten Jahr zählt, geprüft für beide betroffenen Jahre
- halber Tag ergibt 0,5
- Teilzeitperson mit freiem Wochentag: dieser Tag zählt nicht
- Feiertag innerhalb des Antragszeitraums zählt nicht
- Antrag mit `WAITING` zählt mit
- mehrere Abwesenheitsarten im selben Monat werden getrennt ausgewiesen
- Art ohne Tage erscheint nicht im Ergebnis
- leere Antragsliste ergibt ein leeres Ergebnis, keine Ausnahme

## Definition of Done

- [ ] `MonthlyAbsenceDays` implementiert, ohne Spring- und Datenbankbezug
- [ ] Jahressumme je Art wird vom selben Modul geliefert
- [ ] Tests grün, alle oben genannten Fälle abgedeckt
