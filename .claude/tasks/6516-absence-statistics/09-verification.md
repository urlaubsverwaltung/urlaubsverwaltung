# 09 — Abnahme

**Abhängig von:** 01–08

## Ziel

Nachweisen, dass die Seite die Entscheidungen aus dem PRD tatsächlich erfüllt — nicht nur, dass sie rendert.

## Automatisiert

- [ ] `./mvnw test` grün
- [ ] `npx vitest run` grün
- [ ] `npm run lint` grün

## Am laufenden System zu prüfen

Rechnen:

- [ ] Ein Urlaub über den Jahreswechsel erscheint anteilig in beiden Jahren; die Anteile addieren sich zur Gesamtdauer.
- [ ] Ein halber Tag zählt als 0,5.
- [ ] Bei einer Teilzeitperson zählt ein arbeitsfreier Wochentag nicht.
- [ ] Ein Antrag mit `WAITING` erscheint im Diagramm; nach Ablehnung verschwindet er wieder.
- [ ] Die Summe der Jahreswerte je Art entspricht der Gesamtzahl in der Kennzahlenkarte.
- [ ] Die Anteile im Kuchendiagramm addieren sich auf 100 %.

Urlaubstage:

- [ ] Der Prozentwert macht am Verfallsdatum keinen Sprung, wenn zwischenzeitlich kein Urlaub genommen wurde.
- [ ] Verfallene Tage werden separat ausgewiesen.
- [ ] Die Prognose zum 31.12. liegt unter dem Stand heute, wenn im Restjahr Urlaub genehmigt ist.
- [ ] Bei einem vergangenen Jahr sind beide Stichtage identisch.
- [ ] Eine Person ohne Urlaubskonto verändert die Zahlen nicht und wird nirgends erwähnt.

Personenkreis und Berechtigungen:

- [ ] `OFFICE` und `BOSS` sehen die ganze Belegschaft.
- [ ] `DEPARTMENT_HEAD` sieht nur die eigenen Abteilungen — ohne Hinweis darauf auf der Seite.
- [ ] `USER` bekommt weder den Navigationseintrag noch Zugriff auf die Route.
- [ ] Eine Person, die im ausgewerteten Jahr aktiv war und inzwischen deaktiviert ist, zählt weiterhin mit.

Darstellung:

- [ ] Ein Jahr vor Einführung der Anwendung rendert leere Diagramme ohne Fehler.
- [ ] Ein Zukunftsjahr zeigt bereits genehmigte Planung.
- [ ] Eine inzwischen deaktivierte Abwesenheitsart erscheint in einem historischen Jahr.
- [ ] Eine Art ohne Tage in Jahr und Vorjahr erscheint nirgends.
- [ ] Vorjahreswerte stehen an allen Kennzahlen und an jeder Abwesenheitsart.
- [ ] Helles und dunkles Design sind gleichwertig lesbar.
- [ ] Auf schmaler Breite rücken Kuchen und Gauge untereinander, die Seite scrollt nicht horizontal.
- [ ] Jahresauswahl und Legenden sind per Tastatur erreichbar, Fokus ist sichtbar.

## Bewusst nicht geprüft

Die Unterscheidbarkeit von `--absence-color-BLUE` und `--absence-color-VIOLET` im gestapelten Balken. Das Problem ist im PRD als bekanntes Risiko festgehalten; eine Änderung der Farbpalette gehört nicht in diese Umsetzung.
