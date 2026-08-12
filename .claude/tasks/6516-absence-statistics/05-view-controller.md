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
- Service zweimal aufrufen: gewähltes Jahr und Vorjahr

Model:

- `selectedYearStatistics`, `previousSelectedYearStatistics`
- `currentYear` für den `year-selector`
- ein Graph-DTO für die Diagrammdaten, als Record im Controller — wie `GraphDto` / `DataSeries` bei den Krankmeldungen

Das Graph-DTO trägt:

- Monatswerte je Abwesenheitsart für das gewählte Jahr, absteigend nach Jahressumme sortiert, mit Name und `VacationTypeColor` je Art
- Jahressummen je Art für das Kuchendiagramm, mit Anteil und Vorjahreswert
- die beiden Prozentwerte für den Gauge

Die Sortierung nach Jahressumme gehört ins DTO-Mapping, damit Balkenstapel, Kuchen und Legende garantiert dieselbe Reihenfolge haben.

Sichtbare Arten sind nur solche mit Tagen im gewählten Jahr **oder** im Vorjahr — das `active`-Flag des `VacationType` spielt keine Rolle.

## Tests

`AbsenceStatisticsViewControllerTest` mit MockMvc, Vorbild `SickNoteStatisticsViewControllerTest`:

- Zugriff erlaubt für `OFFICE`, `BOSS`, `DEPARTMENT_HEAD`, `SECOND_STAGE_AUTHORITY`
- Zugriff verweigert für `USER` und andere Rollen
- ohne `year`-Parameter wird das laufende Jahr verwendet
- mit `year`-Parameter wird das gefragte Jahr verwendet und das Vorjahr zusätzlich geladen
- erwarteter View-Name und die oben genannten Model-Attribute sind gesetzt
- Arten sind absteigend nach Jahressumme sortiert
- eine Art, die nur im Vorjahr Tage hat, ist enthalten; eine Art ohne Tage in beiden Jahren nicht

## Definition of Done

- [ ] Route erreichbar, Berechtigungen greifen
- [ ] Graph-DTO enthält Monatswerte, Jahressummen mit Vorjahr und Gauge-Werte
- [ ] Sortierung im Mapping, nicht im Template
- [ ] Tests grün
