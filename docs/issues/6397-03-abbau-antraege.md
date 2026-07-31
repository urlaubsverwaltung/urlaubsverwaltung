# Abbau um Abbau-Anträge erweitern

Typ: AFK

## Parent

[#6397 — Unternehmensweite Überstundenübersicht für Rollen Office und Chef](https://github.com/urlaubsverwaltung/urlaubsverwaltung/issues/6397)

## What to build

Überstundenabbau existiert in dieser Anwendung in zwei Formen, und die Statistik muss beide zählen:
den negativen Überstunden-Eintrag („Überstunden abbauen statt hinzufügen") und den Antrag der
Kategorie Überstundenabbau. Slice 02 kennt nur die erste Form. Ohne die zweite weicht der
Unternehmenssaldo von dem ab, was Mitarbeitende auf ihrer eigenen Überstundenseite als verbleibende
Überstunden sehen — und genau diese Übereinstimmung ist der Zweck dieses Slices.

Berücksichtigt werden Anträge in den aktiven Status: wartend, vorläufig genehmigt, genehmigt und
genehmigt mit Stornierungsanfrage. Noch nicht genehmigte Anträge senken den Saldo also bereits. Das
folgt dem Verhalten, das die Anwendung für den persönlichen Saldo schon heute zeigt; eine Abweichung
davon würde die beiden Zahlen auseinanderlaufen lassen.

Die Verteilung auf Monate erfolgt tagesgenau über den bestehenden anteiligen Abbau-Anteil, damit
Anträge über Monats- und Jahresgrenzen korrekt aufgeteilt werden. Bewusst werden die vorhandenen
Services wiederverwendet statt eigener SQL-Aggregate: nur so ist garantiert, dass die Zahlen zur
persönlichen Überstundenseite passen.

Der Arbeitszeitkalender wird für die anteilige Verteilung über die gesamte betrachtete Historie
geladen. Das ist der Speicher-Hotspot dieses Slices und soll gemessen werden, bevor optimiert wird.

## Acceptance criteria

- [ ] Der Abbau je Monat umfasst negative Überstunden-Einträge und Abbau-Anträge
- [ ] Nur Anträge in aktiven Status zählen; abgelehnte, storniert und widerrufene nicht
- [ ] Ein Antrag über eine Monatsgrenze wird anteilig auf die betroffenen Monate verteilt
- [ ] Ein Antrag über eine Jahresgrenze zählt nur mit seinem Anteil im gewählten Jahr
- [ ] Über die gesamte Historie gerechnet entspricht der Saldo der Summe der persönlichen
      verbleibenden Überstunden — mit einem Test, der genau das gegenüberstellt
- [ ] Die Arbeitszeitkalender werden in einer gebatchten Abfrage geladen, nicht je Person oder Antrag
- [ ] Laufzeit und Speicherbedarf für eine realistische Historie sind gemessen und im Pull Request
      dokumentiert; eine Optimierung erfolgt nur, wenn die Messung sie rechtfertigt
- [ ] Unit-Tests für den Status-Filter und die Grenzfälle an Monats- und Jahresgrenzen

## Blocked by

- [Slice 02 — Monatswerte und Balkendiagramm](./6397-02-monatswerte-balkendiagramm.md)
