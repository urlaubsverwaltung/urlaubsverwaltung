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

- [ ] Template rendert für ein Jahr mit Daten und ein Jahr ohne Daten fehlerfrei
- [ ] Legende des Kuchendiagramms ist serverseitig gerendert
- [ ] Kein hartkodierter deutscher Text im Template
- [ ] Alle Schlüssel in allen vier `messages`-Dateien vorhanden
