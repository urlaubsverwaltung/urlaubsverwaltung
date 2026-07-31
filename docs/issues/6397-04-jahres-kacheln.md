# Jahres-KPI-Kacheln

Typ: AFK

## Parent

[#6397 — Unternehmensweite Überstundenübersicht für Rollen Office und Chef](https://github.com/urlaubsverwaltung/urlaubsverwaltung/issues/6397)

## What to build

Drei Kacheln unterhalb des Balkendiagramms mit den Zahlen des gewählten Jahres: Aufgebaut, Abgebaut
und Saldo. Der Saldo startet am 1. Januar bei 0, es gibt keinen Übertrag aus Vorjahren. Damit sind
die Kacheln genau die Summe der Balken darüber — die Seite widerspricht sich an dieser Stelle nicht.

Der Abschnitt bekommt eine Überschrift mit Jahresbezug und einen Satz Erklärung, damit der
Unterschied zum Gesamtbestand über der Jahresauswahl ohne Vorwissen verständlich ist.

Die Werte werden im Format der Anwendung ausgegeben („x Std. y Min.", negative Werte mit führendem
Minus). Kein Dezimalformat, damit die Seite nicht von „Meine Überstunden" abweicht. Kein
Vorjahreswert in Klammern — der Jahresvergleich passiert im Linien-Chart.

Solange Slice 03 nicht gelandet ist, enthält der Abbau nur die negativen Einträge. Die Kacheln
funktionieren dann bereits korrekt, sie werden durch Slice 03 lediglich vollständig.

## Acceptance criteria

- [ ] Drei Kacheln: Aufgebaut, Abgebaut, Saldo — jeweils mit Jahresbezug im Label
- [ ] Die Werte sind die Summe der Balken des Diagramms darüber
- [ ] Saldo = Aufgebaut minus Abgebaut
- [ ] Format „x Std. y Min."; negative Werte mit führendem Minus; Null wird als Null-Meldung
      ausgegeben, nicht als leerer Text
- [ ] Der Abschnitt hat eine Überschrift mit Jahr und einen erklärenden Satz
- [ ] Kein Vorjahreswert in den Kacheln
- [ ] Ein Wechsel des Jahres aktualisiert alle drei Werte
- [ ] Ein Jahr ohne jegliche Überstunden zeigt drei Nullwerte statt eines Fehlers
- [ ] Tests für Summenbildung und Formatierung

## Blocked by

- [Slice 02 — Monatswerte und Balkendiagramm](./6397-02-monatswerte-balkendiagramm.md)
