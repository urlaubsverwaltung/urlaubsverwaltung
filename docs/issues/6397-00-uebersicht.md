# 6397 — Unternehmensweite Überstundenübersicht: Slices

Aufteilung von [#6397](https://github.com/urlaubsverwaltung/urlaubsverwaltung/issues/6397) in vertikale Slices.
Die fachlichen Entscheidungen stehen im
[Kommentar am Issue](https://github.com/urlaubsverwaltung/urlaubsverwaltung/issues/6397#issuecomment-5143414054).

| # | Slice | Typ | Blockiert von |
|---|---|---|---|
| 01 | [Statistikseite erreichbar](./6397-01-statistikseite-erreichbar.md) | AFK | — |
| 02 | [Monatswerte und Balkendiagramm](./6397-02-monatswerte-balkendiagramm.md) | AFK | 01 |
| 03 | [Abbau um Abbau-Anträge erweitern](./6397-03-abbau-antraege.md) | AFK | 02 |
| 04 | [Jahres-KPI-Kacheln](./6397-04-jahres-kacheln.md) | AFK | 02 |
| 05 | [Gesamtbestand-Kacheln](./6397-05-gesamtbestand-kacheln.md) | AFK | 02 |
| 06 | [Linien-Chart mit Jahresvergleich](./6397-06-linien-chart.md) | AFK | 02 |
| 07 | [Design- und Accessibility-Review](./6397-07-design-review.md) | HITL | 02, 06 |

04, 05 und 06 können nach 02 parallel laufen.

Slice 03 ist bewusst **kein** Blocker für 04–06: die Kacheln und die Kurve entstehen zuerst mit
Zahlen aus den Überstunden-Einträgen und werden durch 03 vollständig, ohne dass sich ihre
Darstellung ändert.

Labels für alle Slices: `type: enhancement`, `topic: overtime`, `topic: statistics`.
Ein Triage-Label-Vokabular war in dieser Session nicht konfiguriert — bitte beim Anlegen im
Tracker ergänzen.
