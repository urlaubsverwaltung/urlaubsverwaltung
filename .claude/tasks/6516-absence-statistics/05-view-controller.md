# 05 — View-Controller und Route

**Abhängig von:** 04
**Berührt:** `org.synyx.urlaubsverwaltung.absence.statistics`

## Ziel

Die Seite ist unter `/web/absence/statistics` erreichbar, für die richtigen Rollen und mit allen Daten im Model.

## Umsetzung

Neue Klasse `AbsenceStatisticsViewController`, aufgebaut wie `SickNoteStatisticsViewController`:

- `@Controller`, `@RequestMapping("/web/absence/statistics")`, `implements HasLaunchpad, HasPersonSearch`
- `@PreAuthorize("hasAnyAuthority('OFFICE', 'BOSS', 'DEPARTMENT_HEAD', 'SECOND_STAGE_AUTHORITY')")`
- `@RequestParam(value = "year", required = false) Optional<Year>`, Vorgabe `Year.now(clock)`
- Service **einmal** aufrufen, für das gewählte Jahr — kein Vorjahresvergleich

Model:

- `selectedYearStatistics`
- `currentYear` für den `year-selector`
- ein Graph-DTO für die Diagrammdaten, als Record im Controller — wie `GraphDto` / `DataSeries` bei den Krankmeldungen

Das Graph-DTO trägt:

- Monatswerte je Abwesenheitsart für das gewählte Jahr, absteigend nach Jahressumme sortiert, mit Name und `VacationTypeColor` je Art
- Jahressummen je Art für das Kuchendiagramm, mit Anteil
- den Prozentwert für den Ring

Die Sortierung nach Jahressumme gehört ins DTO-Mapping, damit Balkenstapel, Kuchen und Legende garantiert dieselbe Reihenfolge haben.

Sichtbare Arten sind nur solche mit Tagen im gewählten Jahr — das `active`-Flag des `VacationType` spielt keine Rolle.

## Tests

Zwei Testklassen statt einer — `SickNoteStatisticsViewControllerTest` als Vorbild deckt nur die Verdrahtung ab, weil ihr `standaloneSetup` keine Spring Security verdrahtet und `@PreAuthorize` damit gar nicht greifen kann. Berechtigungen werden im Repo statt dessen über eine separate `*SecurityIT`-Klasse geprüft (Vorbild: `OvertimeStatisticsViewControllerSecurityIT`, `@SpringBootTest` + `webAppContextSetup(...).apply(springSecurity())`).

`AbsenceStatisticsViewControllerTest` mit MockMvc (`standaloneSetup`):

- ohne `year`-Parameter wird das laufende Jahr verwendet
- mit `year`-Parameter wird das gefragte Jahr verwendet
- erwarteter View-Name und die oben genannten Model-Attribute sind gesetzt
- Arten sind absteigend nach Jahressumme sortiert
- (eine Art ohne Tage im gewählten Jahr taucht schon in `MonthlyAbsenceDaysTest` nicht auf — hier gibt es nichts zusätzlich zu filtern, das Graph-DTO mappt nur, was `AbsenceStatistics` bereits liefert)

`AbsenceStatisticsViewControllerSecurityIT` mit vollem Spring-Kontext, Testcontainers:

- Zugriff verweigert (403) für `USER`, `INACTIVE`, ohne dass der Controller überhaupt aufgerufen wird
- Zugriff erlaubt für `OFFICE`, `BOSS`, `DEPARTMENT_HEAD`, `SECOND_STAGE_AUTHORITY` — geprüft daran, dass der Controller tatsächlich ausgeführt wird, nicht daran, dass die Seite rendert (das Template kommt erst mit Task 07; bis dahin wirft das Rendering erwartungsgemäß eine `TemplateInputException`, die der Test explizit abfängt und verifiziert)

## Definition of Done

- [x] Route erreichbar, Berechtigungen greifen
- [x] Graph-DTO enthält Monatswerte, Jahressummen und den Ring-Prozentwert
- [x] Sortierung im Mapping, nicht im Template
- [x] Tests grün
