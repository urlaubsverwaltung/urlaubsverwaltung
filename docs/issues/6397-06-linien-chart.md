# Linien-Chart mit Jahresvergleich

Typ: AFK

## Parent

[#6397 — Unternehmensweite Überstundenübersicht für Rollen Office und Chef](https://github.com/urlaubsverwaltung/urlaubsverwaltung/issues/6397)

## What to build

Ein Linien-Chart am Ende der Seite mit dem im Jahr kumulierten Saldo: Januar startet bei 0, jeder
Monat addiert seinen Saldo hinzu. Der Dezemberwert ist damit identisch mit der Saldo-Kachel des
Jahres.

Bewusst **kein** Übertrag aus Vorjahren. Die Kurve zeigt die Veränderung im Jahr, nicht den Bestand —
der Bestand steht in den Kacheln über der Jahresauswahl. Dadurch sind zwei Jahre auf derselben Achse
direkt vergleichbar: die zweite Serie zeigt das Vorjahr, sodass „stehen wir im Juli besser als letztes
Jahr im Juli?" beantwortbar wird.

Legende und Tooltip im Format der Anwendung, keine Tabellenansicht. Der Sichtbarkeitszustand der
Serien wird wie in der Krankmeldungsstatistik im lokalen Speicher des Browsers gehalten, mit einem
Versionskey, der bei Änderungen an der übertragenen Datenstruktur erhöht wird — sonst hängen Nutzer
an einem Zustand, der nicht mehr zu den Serien passt.

Solange Slice 03 nicht gelandet ist, enthält der Abbau nur die negativen Einträge; die Kurvenform
bleibt korrekt, die Werte werden durch Slice 03 vollständig.

## Acceptance criteria

- [ ] Die Kurve des gewählten Jahres beginnt im Januar beim Saldo des Januars, nicht bei einem Übertrag
- [ ] Der Dezemberwert stimmt mit der Saldo-Kachel des Jahres überein
- [ ] Eine zweite Serie zeigt das Vorjahr auf derselben Achse und denselben Monatskategorien
- [ ] Eine Legende nennt beide Jahre; die Serien lassen sich ein- und ausblenden
- [ ] Der Sichtbarkeitszustand der Serien übersteht einen Reload
- [ ] Der Versionskey ist so dokumentiert, dass eine Strukturänderung den gespeicherten Zustand
      invalidiert
- [ ] Der Tooltip nennt die Werte beider Jahre für den jeweiligen Monat
- [ ] Ein Vorjahr ohne Daten führt nicht zu einem Fehler, sondern zu einer fehlenden zweiten Serie
- [ ] Service-Tests für die Kumulation und JS-Spec für den Chart-Aufbau

## Blocked by

- [Slice 02 — Monatswerte und Balkendiagramm](./6397-02-monatswerte-balkendiagramm.md)
