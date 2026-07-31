# Monatswerte und Balkendiagramm

Typ: AFK

## Parent

[#6397 — Unternehmensweite Überstundenübersicht für Rollen Office und Chef](https://github.com/urlaubsverwaltung/urlaubsverwaltung/issues/6397)

## What to build

Auf- und Abbau je Monat für das gewählte Jahr, dargestellt als Balkendiagramm am Anfang der Seite.

Datenquelle in diesem Slice sind ausschließlich die Überstunden-Einträge: positive Durations sind
Aufbau, negative Abbau. Einträge, die sich über mehrere Monate erstrecken, werden anteilig auf die
Monate verteilt; Einträge über eine Jahresgrenze zählen nur mit ihrem Anteil im gewählten Jahr.
Slice 03 erweitert den Abbau später um die Abbau-Anträge, ohne dass sich die Darstellung ändert.

Aggregiert wird über die Personen, die im gewählten Jahr ein Urlaubskonto hatten — die
Jahres-Kohorte. Damit ändern sich historische Auswertungen nicht rückwirkend, wenn jemand
ausscheidet.

Das Diagramm zeigt zwei Serien um die Nulllinie: Aufbau nach oben, Abbau nach unten, sodass je Monat
eine Spalte entsteht und der überwiegende Anteil sofort erkennbar ist. Bewusst **keine** Zahl an
jedem Balken — bei 24 Balken wird das unlesbar. Die exakten Werte kommen aus dem Tooltip.

Eine Tabellenansicht ist ausdrücklich **nicht** Teil der Seite.

## Acceptance criteria

- [ ] Das Balkendiagramm zeigt die zwölf Monate des gewählten Jahres
- [ ] Aufbau wird oberhalb, Abbau unterhalb der Nulllinie dargestellt
- [ ] Eine Legende mit beiden Serien ist vorhanden
- [ ] Der Tooltip nennt Aufbau, Abbau und den Monatssaldo im Format „x Std. y Min."
- [ ] Ein Eintrag über eine Monatsgrenze wird anteilig auf beide Monate verteilt
- [ ] Ein Eintrag über eine Jahresgrenze zählt nur mit seinem Anteil im gewählten Jahr
- [ ] Personen ohne Urlaubskonto im gewählten Jahr gehen nicht in die Aggregation ein
- [ ] Ausgeschiedene Personen bleiben in den Jahren enthalten, in denen sie ein Konto hatten
- [ ] Die Abfragen sind gebatcht; die Anzahl der Queries wächst nicht mit der Personenzahl
- [ ] Service-Unit-Tests für die Monatsaufteilung und die Personenmenge
- [ ] JS-Spec für den Chart-Aufbau

## Blocked by

- [Slice 01 — Statistikseite erreichbar](./6397-01-statistikseite-erreichbar.md)
