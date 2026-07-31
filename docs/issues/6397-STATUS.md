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

## Fertig: Slice 03 — Abbau umfasst die Abbau-Anträge

Volle Suite grün: 4772 Java-Tests, 689 JS-Tests.

Der Abbau je Monat setzt sich jetzt aus negativen Überstunden-Einträgen **und** Anträgen der Kategorie
Überstundenabbau in `activeStatuses()` zusammen. Wartende Anträge senken den Saldo also bereits — das
folgt dem Verhalten, das die Anwendung für den persönlichen Saldo schon zeigt.

Zwei Details, die beim Lesen des Codes sonst überraschen:

- Die Monatsanteile werden über `Application.getOvertimeReductionShareFor(monatsBereich, …)` erfragt,
  also zwölfmal pro Antrag. Das ist bewusst so: der Antrag beantwortet selbst, wie viel von ihm in einen
  Zeitraum fällt, und die Regel bleibt in `Application` statt sich über zwei Klassen zu verteilen.
  Intern baut jeder Aufruf die Tageskarte neu auf — bei einem Abbau-Antrag, der selten mehr als zwei
  Monate umfasst, ist das vernachlässigbar. Eine frühere Variante hat die Karte einmal pro Antrag geholt
  und selbst nach Monaten sortiert; die wurde verworfen, weil sie die Fachlogik aus `Application`
  herausgezogen hat. Der Abbau-Pfad ist damit auch symmetrisch zum Einträge-Pfad.
- Der Arbeitszeitkalender wird über die **volle Spanne der Anträge** geladen, nicht über das Jahr.
  `getOvertimeReductionShares` verteilt über alle Arbeitstage des Antrags; ein am 31.12. abgeschnittener
  Kalender würde den Nenner verfälschen und damit jeden Tagesanteil.

### Zwei Akzeptanzkriterien sind offen

- **„Saldo = Summe der persönlichen verbleibenden Überstunden"** lässt sich hier noch nicht prüfen. Die
  Aussage gilt über die gesamte Historie, und diesen Pfad gibt es erst mit Slice 05. Ein Test, der beide
  Seiten mockt, wäre zirkulär. Das Kriterium wandert damit nach Slice 05 und braucht dort einen Test
  gegen eine echte Datenbank.
- **Laufzeit- und Speichermessung** ist nicht erfolgt, weil dafür eine befüllte Datenbank nötig ist.
  Was sich ohne Messung sagen lässt und durch Tests abgedeckt ist: die Abfragezahl ist konstant und
  wächst nicht mit der Personenzahl — Personen, Überstunden, Anträge, Kalender, also vier Abfragen. Die
  im Slice notierte Sorge „Kalender über die gesamte Historie" trifft auf den Jahrespfad **nicht** zu:
  die Spanne ist durch das gewählte Jahr plus die Ausläufer grenzüberschreitender Anträge begrenzt.
  Für Slice 05 bleibt die Sorge bestehen, dort geht es wirklich über alle Jahre.

## Als Nächstes: Slice 04 oder 05

Beide sind nach Slice 02 startklar, [Jahres-Kacheln](./6397-04-jahres-kacheln.md) ist die kleinere
Aufgabe: `OvertimeStatistics` liefert `accrued()`, `reduction()` und `balance()` schon, es fehlen nur
die Kacheln im Template und die Message-Keys.

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
