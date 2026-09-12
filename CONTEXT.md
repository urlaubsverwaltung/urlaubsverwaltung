# Urlaubsverwaltung

Anwendung zur Verwaltung von Abwesenheiten. Mitarbeitende beantragen Abwesenheiten (Erholungsurlaub, Sonderurlaub, Überstundenabbau, …); die Anwendung verwaltet Genehmigung, Kontingente und teilt Abwesenheiten über Kalender.

## Sprache

**Vacation Type** (Abwesenheitsart):
Eine konfigurierbare Art von Abwesenheit, die Mitarbeitende beantragen können (z. B. Erholungsurlaub, Home Office, Außer Haus). Trägt Anzeige- und Workflow-Einstellungen (Farbe, Genehmigungsregeln, Sichtbarkeit). Der Name im Code ist `VacationType`; die Admin-Oberfläche nennt dasselbe "Abwesenheitsart". Es ist dasselbe Konzept — im Code "Vacation Type", in der deutschen Oberfläche "Abwesenheitsart".
_Vermeiden_: Leave type, absence category (category ist ein anderes, engeres Konzept)

**Vacation Category** (Kategorie):
Eine feste Klassifizierung einer Abwesenheitsart für die Kontingent-/Verrechnungslogik (`HOLIDAY`, `SPECIALLEAVE`, `UNPAIDLEAVE`, `OVERTIME`, `OTHER`). Sagt nichts über Anwesenheit aus — sie steuert, wie Tage gezählt werden, nie ob die Person bei der Arbeit ist.
_Vermeiden_: Type (das ist die Abwesenheitsart)

**Available For Work** (erreichbar / arbeitet woanders):
Ein Boolean-Feld auf einer Abwesenheitsart. `true` bedeutet: die Person ist nicht wirklich abwesend, sondern arbeitet weiter, nur an einem anderen Ort (z. B. Home Office, Außer Haus) und bleibt daher erreichbar/verfügbar. `false` (der Normalfall, z. B. Erholungsurlaub) bedeutet tatsächlich abwesend. Wirkt sich **nur auf die Darstellung** aus — Kalender-Export, Kalender-Sync und Abwesenheitsübersicht zeigen die Person als verfügbar — und ändert nie Kontingente, Genehmigung oder Vertretung.
_Vermeiden_: Works remotely, Home-Office-Flag, Anwesenheits-Flag

**Absence** (Abwesenheit / Abwesenheitseintrag):
Ein konkreter halb- oder ganztägiger Zeitraum, in dem eine Person Abwesenheit hat oder krank ist, abgeleitet aus einem Antrag (Application) oder einer Krankmeldung (SickNote). Die bloße Existenz einer Abwesenheit bedeutete bisher immer "nicht bei der Arbeit"; "Available For Work" ist die erste Einstellung, mit der eine Abwesenheit trotzdem "anwesend/erreichbar" heißen kann.