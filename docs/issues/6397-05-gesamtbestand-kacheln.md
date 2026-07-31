# Gesamtbestand-Kacheln

Typ: AFK

## Parent

[#6397 — Unternehmensweite Überstundenübersicht für Rollen Office und Chef](https://github.com/urlaubsverwaltung/urlaubsverwaltung/issues/6397)

## What to build

Drei Kacheln **oberhalb** der Jahresauswahl: Aufgebaut insgesamt, Abgebaut insgesamt und Saldo — über
die gesamte Historie. Sie reagieren nicht auf den Jahres-Selector, weil sie über ihm stehen; ihre
Position ist die Aussage.

Diese Saldo-Kachel beantwortet „wie viele Überstunden hat das Unternehmen offen" und ist damit die
Zahl für Gespräche mit der Geschäftsführung. Sie ist außerdem der Wert, der der Summe der
persönlichen verbleibenden Überstunden entspricht.

Der Abschnitt braucht eine Überschrift und einen Satz, der beides benennt: den Bezug auf die gesamte
Historie und die Unabhängigkeit von der Jahresauswahl darunter. Ohne diesen Satz liest ein Nutzer die
Differenz zu den Jahres-Kacheln als Rechenfehler.

Solange Slice 03 nicht gelandet ist, enthält der Abbau nur die negativen Einträge; die
Übereinstimmung mit den persönlichen Salden gilt erst danach.

## Acceptance criteria

- [ ] Drei Kacheln oberhalb der Jahresauswahl: Aufgebaut insgesamt, Abgebaut insgesamt, Saldo
- [ ] Die Werte ändern sich beim Wechsel des Jahres nicht
- [ ] Der Abschnitt hat eine Überschrift und einen Satz, der die gesamte Historie und die
      Unabhängigkeit von der Jahresauswahl benennt
- [ ] Format identisch zu den Jahres-Kacheln
- [ ] Die Aggregation ist gebatcht; die Anzahl der Queries wächst nicht mit der Personenzahl
- [ ] Ein Unternehmen ohne jegliche Überstunden zeigt drei Nullwerte statt eines Fehlers
- [ ] Tests für die Aggregation und für die Unabhängigkeit vom gewählten Jahr

## Blocked by

- [Slice 02 — Monatswerte und Balkendiagramm](./6397-02-monatswerte-balkendiagramm.md)
