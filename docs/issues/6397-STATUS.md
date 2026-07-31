# 6397 — Arbeitsstand

Stand: 2026-07-31. Branch `feature/6397-overtime-statistics`.

## Fertig: Slice 01 — Statistikseite erreichbar

Volle Testsuite grün: 4750 Tests, 0 Failures.

Entstanden ist:

- `overtime/statistics/OvertimeStatisticsViewController` — `/web/overtime/statistics`,
  `@PreAuthorize(IS_BOSS_OR_OFFICE)`, 404 bei `overtimeActive = false`, Jahres-Selector über
  `?year=YYYY` mit Rückfall auf das aktuelle Jahr bei unbrauchbarem Wert.
- Navigationseintrag `company-overtime-link` in `FrameDataProvider.navCompanyGroup`, zwischen
  Abwesenheiten und Krankmeldungen, Icon `clock-arrow-up`, nur für Office/Chef und nur bei aktivem
  Überstunden-Feature.
- Template `templates/overtime/overtime_statistics.html`. Der Jahres-Selector steht **absichtlich in
  einer eigenen Zeile** unterhalb der Section-Heading, damit Slice 05 die all-time Kacheln darüber
  einsetzen kann, ohne Heading oder Selector anzufassen.
- Neue Message-Keys in allen vier Locale-Dateien: `overtime.statistics.header.title`,
  `overtime.statistics.title`, `overtime.statistics.year.label`.
  `nav.company.overtimes` war bereits vorhanden.

Tests:

- `OvertimeStatisticsViewControllerTest` — 4 Tests, Jahreslogik und Feature-Toggle.
- `OvertimeStatisticsViewControllerSecurityIT` — 8 Tests, Rollen-Gate und echtes Rendering der Seite.
  Die Content-Assertion wurde per absichtlich falscher Erwartung gegengeprüft, greift also wirklich.
- `FrameDataProviderTest` — 5 neue Tests, zwei bestehende `containsExactly`-Erwartungen um den neuen
  Eintrag ergänzt.

## Fundstück, das jemand kennen sollte

In `target/classes/.../overtime/statistics/` lagen **veraltete Kompilate eines früheren Versuchs**
(`OvertimeStatistics`, `DeltaTrend`, `MonthlyDeltaDto`, `OvertimeStatisticsService`) ohne zugehörige
Quellen und ohne git-Historie. Maven legt `target/classes` auf den Test-Classpath, dadurch löste der
Compiler eine Klasse auf, die es im Quellcode nicht gab — die Fehlermeldung war entsprechend
verwirrend. Das Paket wurde aus `target` entfernt. Bei ähnlich unerklärlichen Compilerfehlern lohnt
ein Blick in `target/classes`.

## Als Nächstes: Slice 02

[Monatswerte und Balkendiagramm](./6397-02-monatswerte-balkendiagramm.md).

Vorarbeit, die schon geklärt ist:

- Personenmenge über `getAllPersonsHavingAccountInYear(year)`.
- Anteilige Verteilung von Einträgen über Monatsgrenzen mit `Overtime.durationForDateRange`.
- Zahlenformat über `DurationFormatter`, Diagramme intern in Dezimalstunden.
- Chartfarben validiert: Aufbau `#8b5cf6`, Abbau `#0891b2`, beide Themes.
  Jahreslinien `#2563eb` (light) / `#3b82f6` (dark) gegen `#d97706`.
- Vorlage für den ApexCharts-Aufbau ist `bundles/sick-notes-statistics.js`.
- Optisches Ziel: [6397-prototype.html](./6397-prototype.html) — Wegwerf-Mockup, im Browser öffnen.

## Entschieden: die all-time Kacheln haben keinen Jahresbezug

Bestätigt am 2026-07-31: die Kacheln oberhalb der Jahresauswahl zeigen den Gesamtbestand über die
gesamte Historie — **ohne jeden Jahresbezug**. Sie reagieren nicht auf den Selector, und sie sind
auch nicht auf das Ende des gewählten Jahres begrenzt.

Damit ist die frühere Zwischenentscheidung „all-time bis Ende des gewählten Jahres" endgültig
aufgehoben. Wer sie im Verlauf des Issues liest: sie gilt nicht mehr.

Die Konsequenz für Slice 05: es braucht keinen Stichtag im Aggregat und keinen Jahresparameter im
zugehörigen Service-Aufruf. Ein Test muss festhalten, dass ein Wechsel des Jahres die drei Werte
nicht verändert.
