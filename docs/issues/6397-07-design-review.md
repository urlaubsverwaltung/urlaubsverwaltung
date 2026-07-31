# Design- und Accessibility-Review

Typ: HITL — braucht ein menschliches Review, weil hier über Aussehen und Verständlichkeit
entschieden wird, nicht über Verhalten.

## Parent

[#6397 — Unternehmensweite Überstundenübersicht für Rollen Office und Chef](https://github.com/urlaubsverwaltung/urlaubsverwaltung/issues/6397)

## What to build

Abschließender Durchgang über die fertige Seite. Die Chart-Farben wandern aus dem Prototyp in die
Theme-Tokens der Anwendung, der Dark Mode wird als eigenständige Auswahl gesetzt statt als Umkehrung
des Light Mode, und die Seite wird auf Kontrast, Farbfehlsichtigkeit und Verhalten auf schmalen
Viewports geprüft.

Aus dem Prototyp bereits validiert — diese Werte bestehen alle Prüfungen gegen die helle und die
dunkle Chart-Fläche und sollten nur mit Grund geändert werden:

```
Balken   Aufbau  violet-500   Abbau  cyan-600     (beide Themes)
Linien   Jahr    blue-600 (light) / blue-500 (dark)
         Vorjahr amber-600                        (beide Themes)
```

Grau für das Vorjahr fällt durch, weil es als unbunt gelesen wird — deshalb Amber statt der
Hell/Dunkel-Variante desselben Farbtons, die die Krankmeldungsstatistik für Jahrespaare verwendet.

Der zweite, wichtigere Teil ist die Beschriftung: die Seite trägt zwei KPI-Blöcke mit
unterschiedlichem Zeitbezug (gesamte Historie oben, gewähltes Jahr in der Mitte). Ob dieser
Unterschied ohne Vorwissen ankommt, kann nur ein Mensch beurteilen.

## Acceptance criteria

- [ ] Die Chart-Farben sind als Theme-Tokens definiert; keine Farbliterale im JavaScript
- [ ] Die Dark-Mode-Werte sind eigenständig gewählt und gegen die dunkle Chart-Fläche geprüft, nicht
      aus dem Light Mode abgeleitet
- [ ] Kontrast und Farbfehlsichtigkeit sind für beide Themes geprüft und das Ergebnis ist im Pull
      Request dokumentiert
- [ ] Identität hängt nie allein an der Farbe: Legende in beiden Diagrammen vorhanden.
      Eine Tabellenansicht gibt es bewusst nicht, deshalb muss die Farbunterscheidung der Serien für
      sich tragen — inklusive Farbfehlsichtigkeit
- [ ] Die Seite scrollt auf schmalen Viewports nicht horizontal; breite Inhalte scrollen in ihrem
      eigenen Container
- [ ] `prefers-reduced-motion` wird respektiert
- [ ] Beschriftung abgenommen: der Unterschied zwischen Gesamtbestand und Jahreszahlen ist ohne
      Vorwissen verständlich
- [ ] Review durch @honnel abgenommen

## Blocked by

- [Slice 02 — Monatswerte und Balkendiagramm](./6397-02-monatswerte-balkendiagramm.md)
- [Slice 06 — Linien-Chart mit Jahresvergleich](./6397-06-linien-chart.md)
