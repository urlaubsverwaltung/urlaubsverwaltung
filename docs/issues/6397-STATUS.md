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

## Fertig: Slice 02 — Monatswerte und Balkendiagramm

Volle Suite grün: 4764 Java-Tests, 689 JS-Tests.

- `OvertimeStatistics` — Domänenobjekt, Auf- und Abbau je Monat als `Duration`, Abbau als positiver
  Betrag. Auf- und Abbau bleiben getrennt, weil ein Monat mit 10 h auf und 10 h ab nicht dasselbe ist
  wie ein Monat ohne Bewegung.
- `OvertimeStatisticsService` — aggregiert über die Jahres-Kohorte, eine gebatchte Abfrage, Verteilung
  auf Monate über `Overtime.durationForDateRange`. **Nur Überstunden-Einträge**, die Abbau-Anträge
  kommen erst mit Slice 03.
- Controller liefert `GraphDto`: Dezimalstunden für die Achse plus fertig formatierte Texte aus dem
  `DurationFormatter` für den Tooltip. Der Abbau wird negiert übergeben, damit ApexCharts ihn unter
  die Nulllinie zeichnet.
- `bundles/overtime-statistics.js` — gestapeltes Balkendiagramm, keine Zahlen an den Balken, Legende,
  Tooltip mit Aufbau, Abbau und Saldo, Theme-Wechsel.
- Neue Theme-Tokens `--overtime-accrued-color` (violet-500) und `--overtime-reduction-color`
  (cyan-600), bewusst **ohne** Dark-Override, weil beide Werte gegen beide Chart-Flächen geprüft sind.

**Keine Tabellenansicht.** Sie war zunächst als zugängliche Alternative gebaut und wurde am 2026-07-31
auf Wunsch wieder entfernt — die Seite bietet die Zahlen nur im Tooltip an. Folge fürs Design-Review:
die Serien werden ausschließlich über Farbe und Legende unterschieden, es gibt keinen textuellen
Ausweichweg mehr. Für Slice 06 gilt dasselbe, dort ist die Tabelle ebenfalls gestrichen.

Ein Punkt für das Design-Review in Slice 07: die Farbwerte sind als Tailwind-Tokens hinterlegt
(`--color-violet-500`, `--color-cyan-600`). Validiert wurden die Hex-Werte `#8b5cf6` und `#0891b2`;
Tailwind 4 rechnet in oklch, die gerenderten Werte weichen minimal ab und sollten im Review
nachgemessen werden.

## Als Nächstes: Slice 03

[Abbau um Abbau-Anträge erweitern](./6397-03-abbau-antraege.md). Der Einstiegspunkt ist
`OvertimeStatisticsService.getStatistics`: dort kommen die Anträge der Kategorie Überstundenabbau in
den Abbau-Topf, tagesgenau verteilt über `Application.getOvertimeReductionShareFor`.

Weiterhin gültige Vorarbeit:

- Chartfarben für die Jahreslinien in Slice 06: `#2563eb` (light) / `#3b82f6` (dark) gegen `#d97706`.
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
