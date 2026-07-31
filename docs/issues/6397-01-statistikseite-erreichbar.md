# Statistikseite erreichbar

Typ: AFK

## Parent

[#6397 — Unternehmensweite Überstundenübersicht für Rollen Office und Chef](https://github.com/urlaubsverwaltung/urlaubsverwaltung/issues/6397)

## What to build

Eine neue unternehmensweite Überstundenstatistik unter `/web/overtime/statistics`, erreichbar für
Chef und Office. Inhaltlich zeigt die Seite in diesem Slice nur ihren Titel und den Jahres-Selector
— der vollständige Pfad steht aber: Navigation, Berechtigung, Feature-Toggle, Template und
Übersetzungen.

Zugriff über `IS_BOSS_OR_OFFICE`. Bei deaktiviertem Überstunden-Feature (`overtimeActive = false`)
entfällt der Navigationseintrag **und** der Controller sperrt den direkten Aufruf. Das ist bewusst
strenger als der bestehende lesende Überstunden-View, der das Toggle heute nicht prüft — eine
Statistikseite für ein abgeschaltetes Feature soll nicht erreichbar sein.

Der Navigationseintrag heißt „Überstunden" und sitzt in der Gruppe *Unternehmen*, obwohl der Pfad
unter `/web/overtime` liegt. Der Jahres-Selector folgt der Krankmeldungsstatistik: `?year=YYYY`,
Vorbelegung ist das aktuelle Jahr.

## Acceptance criteria

- [ ] `GET /web/overtime/statistics` ist für die Rollen BOSS und OFFICE erreichbar
- [ ] Alle anderen Rollen erhalten 403
- [ ] Bei `overtimeActive = false` erscheint kein Navigationseintrag und der direkte Aufruf der URL
      wird abgewiesen (404 oder Redirect)
- [ ] Der Eintrag „Überstunden" erscheint in der Navigationsgruppe *Unternehmen* und wird als aktiv
      markiert, solange die Seite geöffnet ist
- [ ] Der Jahres-Selector zeigt das aktuelle Jahr; `?year=YYYY` wechselt das Jahr; unbrauchbare
      Werte fallen auf das aktuelle Jahr zurück statt einen Fehler zu erzeugen
- [ ] Alle neuen Message-Keys sind in sämtlichen Locale-Dateien vorhanden
- [ ] Controller-Test deckt Rollen und Feature-Toggle ab
- [ ] Navigationstest deckt Sichtbarkeit des Menüeintrags ab

## Blocked by

None - can start immediately
