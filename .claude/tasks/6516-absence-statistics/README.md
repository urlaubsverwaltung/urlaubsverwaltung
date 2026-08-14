# Unternehmen Detailseite Abwesenheiten — #6516

Arbeitsstand und Reihenfolge. Fachliche Begründung steht im [PRD](PRD.md), nicht in den Tasks.

Issue: https://github.com/urlaubsverwaltung/urlaubsverwaltung/issues/6516

## Reihenfolge

Die Kerne 02–04 hängen nur am Package aus 01 und können unabhängig voneinander bearbeitet werden. Ab 05 ist die Reihenfolge bindend.

| # | Task | Abhängig von | Status |
| --- | --- | --- | --- |
| 01 | [Package und Personenauflösung](01-persons.md) | — | erledigt |
| 02 | [Monatsaufteilung der Abwesenheitstage](02-monthly-absence-days.md) | 01 | erledigt |
| 03 | [Genommener Urlaub](03-vacation-days-taken.md) | 01 | offen |
| 04 | [Statistik-Service](04-statistics-service.md) | 01, 02, 03 | offen |
| 05 | [View-Controller und Route](05-view-controller.md) | 04 | offen |
| 06 | [Navigation](06-navigation.md) | 05 | offen |
| 07 | [Template und Übersetzungen](07-template-and-i18n.md) | 05 | offen |
| 08 | [Diagramme im Frontend](08-frontend-charts.md) | 07 | offen |
| 09 | [Abnahme](09-verification.md) | 01–08 | offen |

## Entscheidungen auf einen Blick

Vollständig im PRD, hier die, gegen die man beim Umsetzen am ehesten verstößt:

- Nur Abwesenheitsarten, **keine** Krankmeldungen.
- Status: `ApplicationStatus.activeStatuses()` — `WAITING` zählt mit, wird aber nicht abgesetzt dargestellt.
- Anträge werden tagesgenau auf Monate **und Jahre** gesplittet.
- Verfallener Resturlaub fällt aus Zähler **und** Nenner des Prozentwerts.
- Personen ohne Urlaubskonto: raus aus der Urlaubskennzahl, **ohne** Hinweis auf der Seite.
- Personenkreis über `PersonActivePeriod`, nicht über das heutige `INACTIVE`-Flag.
- Sichtbare Arten: nur solche mit Tagen im gewählten Jahr, unabhängig vom `active`-Flag.
- Kein Vorjahresvergleich — an keiner Stelle der Seite (siehe „Änderungshistorie" unten).
- „Genommener Urlaub" zeigt einen Ring mit **einem** Stichtag; Prozentwert ist `100 % − verbleibend-Prozentwert`, keine Prognose zum Jahresende mehr.
- Keine Personensuche, kein Abteilungsfilter, kein CSV-Export.
- Jahre ohne Daten rendern leere Diagramme, keinen Leerzustandstext.

## Änderungshistorie

**14.08.2026 — Refinement, Mockup-Diskussion.** Gegenüber dem ursprünglichen Entwurf gestrichen bzw. geändert:

- Die Kennzahlenzeile (Gesamt-Abwesenheitstage, Durchschnitt pro Mitarbeitende) fällt komplett weg.
- Der Kuchen „Verteilung der Abwesenheitsarten" verliert die Vorjahresspalte in der Legende.
- Aus „Verbleibende Urlaubstage" (zwei Ringe: Stand heute + Prognose 31.12.) wird „Genommener Urlaub" (ein Ring, ein Stichtag): der Prozentwert zeigt jetzt den bereits genommenen/geplanten Anteil, nicht den offenen — am Jahresende ist ein möglichst voller statt ein möglichst leerer Ring das Ziel. Rechnerisch bleibt es dieselbe Zahl, nur umgekehrt ausgewiesen (`100 % − verbleibend`).
- Damit verschwindet der Vorjahresvergleich vollständig von der Seite; die Kernklasse aus Task 03 wurde von `RemainingVacationDays` in `VacationDaysTaken` umbenannt.
- Die „Sichtbare Arten"-Regel vereinfacht sich auf „Tage im gewählten Jahr" (die „oder Vorjahr"-Klausel entfällt, da sie nur den jetzt gestrichenen Vergleich stützte).

Betroffen: PRD, Tasks 03–09, Mockup.

## Mockup

`mockup/absence-statistics.html` — self-contained, im Browser zu öffnen. Zeigt den Zielzustand mit acht Abwesenheitsarten und Beispieldaten.