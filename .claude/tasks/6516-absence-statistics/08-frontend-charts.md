# 08 — Diagramme im Frontend

**Abhängig von:** 07
**Berührt:** `src/main/javascript/bundles/`, `src/main/css/bundles/`

## Ziel

Die drei Diagramme leben, in hellem und dunklem Design.

## Umsetzung

Neues Bundle `src/main/javascript/bundles/absence-statistics.js` und neues Stylesheet `src/main/css/bundles/absence-statistics.css`. Beide werden automatisch eingesammelt — JS über den `glob` in `rollup.config.mjs`, CSS über PostCSS aus dem `bundles`-Verzeichnis. Keine Registrierung nötig.

Vorbild ist `bundles/sick-notes-statistics.js`; von dort werden `useTheme`, `useMedia` für `prefers-reduced-motion` und die Themenumschaltung per `theme.subscribe` übernommen.

**Monatsdiagramm** — `type: "bar"`, `stacked: true`, eine Serie je Abwesenheitsart in der Reihenfolge aus dem Model. Farben als `var(--absence-color-…)`, wie es die Nachbarseiten mit ihren Farbtokens auch tun. `dataLabels` aus. `stroke` 2px transparent als Trennung zwischen Segmenten. Eigener Tooltip: Monatsname als Titel, je Art eine Zeile mit Farbschwatch und Tagen, absteigend sortiert, darunter eine abgesetzte Zeile mit der Monatssumme.

**Kuchendiagramm** — `type: "pie"`, ApexCharts-Legende aus (`legend.show: false`), `dataLabels` aus. Die Zahlen stehen in der serverseitig gerenderten Legende daneben. Tooltip mit Name, Tagen und Anteil.

**Ring** — `type: "radialBar"` mit genau **einer** Serie (Prozentwert genommener/geplanter Urlaub), `startAngle: 0`, `endAngle: 270`, `barLabels` aktiv mit Prozentformatierung — dieselben `plotOptions` wie der Gauge in `sick-notes-statistics.js`, nur mit einer statt zwei Serien.

**Stylesheet** — Container- und Diagrammhöhen reservieren, damit die Seite beim Nachladen des Bundles nicht springt; `sick-note-statistics.css` erklärt in seinen Kommentaren, warum und was mit den Konstanten im JS synchron zu halten ist. Dazu Tooltip-Zeilenlayout und das zweispaltige Raster für Kuchen und Ring.

Die Serien-Sichtbarkeitspersistenz (`apexOptionsWithPersistence`) ist optional. Wird sie verwendet, braucht sie stabile IDs je Abwesenheitsart — nicht den Index, der sich mit der Sortierung ändert — und einen `version`-Schlüssel.

## Tests

`src/main/javascript/bundles/__tests__/absence-statistics.spec.js`, Vitest, Vorbild `__tests__/sick-notes-statistics.spec.js`:

- Balkenserien werden in der Reihenfolge des Models aufgebaut, mit den Farben des Models
- Tooltip enthält je Art eine Zeile, absteigend sortiert, plus die Monatssumme
- Arten ohne Tage im Monat erscheinen nicht im Tooltip
- der Ring erzeugt genau eine Serie aus dem Prozentwert im Model
- leeres Model erzeugt Diagramme ohne Ausnahme
- `prefers-reduced-motion` schaltet Animationen ab

## Definition of Done

- [x] Alle drei Diagramme rendern mit echten Daten
- [x] Helles und dunkles Design gleichwertig, Umschaltung zur Laufzeit aktualisiert die Diagramme
- [x] Kein horizontales Scrollen der Seite auf schmalen Breiten (Layout aus dem bereits abgenommenen Mockup übernommen — `flex-wrap`, einspaltiges Band unter `md`; endgültige Sichtprüfung ist Sache von Task 09)
- [x] `npm run lint` und `npx vitest run` grün

## Anmerkungen

- Serien-Sichtbarkeitspersistenz (`apexOptionsWithPersistence`) bewusst weggelassen: laut Task optional, nur für kartesische Diagramme (Balken) nutzbar — nicht für Kuchen oder Ring —, und diese neue Seite hat noch keinen bestehenden lokalen Zustand, den es zu erhalten gälte.
- Das Kuchendiagramm braucht ein eigenes Modul: `sick-notes-statistics.js` importiert nur `bar`/`line`/`radialBar`, hier kommt zusätzlich `import "apexcharts/pie";` dazu.
- Der Ring hat keine passende `--absence-color-*`-Farbe (keine Abwesenheitsart entspricht "genommen"). Neuer Token `--absence-vacation-ring-color` lokal in `absence-statistics.css` definiert (hell/dunkel), statt `theme.css` anzufassen — Task 08 berührt laut Kopfzeile nur `bundles/`.
- Beim Schreiben des Tooltip-Zahlenformats ist aufgefallen, dass eine feste `Intl.NumberFormat("de-DE", …)` für nicht-deutschsprachige Nutzer falsch formatierte Zahlen im Tooltip gezeigt hätte, während die serverseitige Legende korrekt lokalisiert. Behoben über `document.documentElement.lang` (gesetzt via `th:lang` in `_layout.html`) — derselbe Mechanismus, den `company-overview.js` bereits nutzt.
