# 07 — Template und Übersetzungen

**Abhängig von:** 05
**Berührt:** `src/main/resources/templates/absences/`, `messages*.properties`

## Ziel

Die Seite rendert vollständig, ohne dass die Diagramme schon leben.

## Umsetzung

Neues Template `absences/absence_statistics.html`, aufgebaut wie `sicknote/sick_notes_statistics.html`:

- `_layout::head` mit `styles`, `scripts`, `scriptsDefer`, `preload`
- `fragments/section-heading::section-heading` für Überschrift und Jahresauswahl
- `fragments/year-selector::year-selector` mit `hrefPrefix=|/web/absence/statistics?year=|`
- Diagrammdaten über `th:inline="javascript"` an `window.absenceStatistics` hängen, wie es die Nachbarseiten mit `window.sicknoteStatistic` und `window.overtimeStatistics` tun. Monatsnamen dabei aus `#{month.*}` übergeben, nicht im Bundle hartkodieren.

Aufbau der Seite:

1. Überschrift mit Jahresauswahl
2. `<h2>` „Abwesenheitstage je Monat" mit Beschreibung, darunter der Diagramm-Container über die volle Breite
3. Zweispaltiger Bereich: links „Verteilung der Abwesenheitsarten" (Diagramm-Host plus Legendenliste), rechts „Genommener Urlaub" (Ring-Host plus Kennzahlen: Stand heute, Durchschnitt pro Mitarbeitende, verfallener Resturlaub). Auf schmalen Breiten untereinander.

Keine Kennzahlenzeile mehr — die Seite geht direkt von der Jahresauswahl in das Monatsdiagramm über.

Die Legende des Kuchendiagramms wird serverseitig als Liste gerendert — Farbe, Name, absolute Tage, Anteil. Sie ist damit auch ohne JavaScript lesbar und muss nicht im Bundle aufgebaut werden.

Kein Leerzustandszweig: ein Jahr ohne Daten rendert dieselbe Struktur mit leeren Diagrammen.

Zahlenformatierung über die vorhandenen Mittel (`fragments/number::number` bzw. `#numbers.formatDecimal`), Nachkommastellen nur wenn nötig — wie auf der Krankmeldungsseite.

## Übersetzungen

Alle Texte über `messages*.properties`, gepflegt in `messages.properties` (Deutsch, Vorgabe), `messages_en.properties`, `messages_de_AT.properties`, `messages_el.properties`. Schlüsselpräfix `absences.statistics.` in Anlehnung an `sicknotes.statistics.`.

Benötigt werden mindestens: Seitentitel, Überschrift, Achsentitel „Tage", Überschriften und Beschreibungen der beiden Bereiche, Label für „Stand heute", Label für den Durchschnitt pro Mitarbeitende, Label für verfallenen Resturlaub, Hinweis auf inaktive Abwesenheitsarten.

## Definition of Done

- [x] Template rendert für ein Jahr mit Daten und ein Jahr ohne Daten fehlerfrei
- [x] Legende des Kuchendiagramms ist serverseitig gerendert
- [x] Kein hartkodierter deutscher Text im Template
- [x] Alle Schlüssel in allen vier `messages`-Dateien vorhanden

## Anmerkungen

- `AbsenceTypeDto` (Task 05) bekam nachträglich ein `active`-Flag — für den „inaktive Art"-Hinweis in der Legende fehlte es, war aber nie Teil der ursprünglichen Task-05-Umsetzung.
- `<script defer type="module" asset:src="absence_statistics.js">` verlangt einen Eintrag im Asset-Manifest, sonst schlägt das Rendering hart fehl (anders als ein fehlendes CSS, das nur einen 404 im Browser gibt). Deshalb legt dieser Task bereits einen minimalen Platzhalter `src/main/javascript/bundles/absence-statistics.js` an (leere IIFE) — Task 08 ersetzt den Inhalt durch die echte Diagramm-Logik, die Datei selbst bleibt bestehen.
- `AbsenceStatistics.year()` ist ein `java.time.Year`, kein `int` — anders als bei `SickNoteStatistics.getYear()`. Für `{0, number, #}`-Übersetzungen und den `year-selector` wird deshalb `${selectedYearStatistics.year.value}` verwendet, nicht `${selectedYearStatistics.year}`.
- Die Rendering-Prüfung („Template rendert … fehlerfrei") läuft in `AbsenceStatisticsViewControllerSecurityIT` (echter Spring-Kontext, Thymeleaf inklusive) statt im `standaloneSetup`-Test aus Task 05, der gar nicht rendert. Dort jetzt auch ein Test mit echten Diagrammdaten (`ensureYearWithDataRendersSuccessfully`) sowie die Rollen-Tests, die zuvor nur „Controller lief" geprüft haben, jetzt aber echtes `status().isOk()` mit Inhaltsprüfung.
