# 06 — Navigation

**Abhängig von:** 05
**Berührt:** `org.synyx.urlaubsverwaltung.web.FrameDataProvider`, `messages*.properties`

## Ziel

Der Navigationseintrag „Abwesenheiten" in der Gruppe *Unternehmen* bekommt zwei Unterpunkte.

## Umsetzung

In `FrameDataProvider#navCompanyGroup` liegt heute für Abwesenheiten ein einzelner Eintrag auf `/web/application/statistics`. Er wird auf dasselbe Muster umgestellt, das direkt darunter für Krankmeldungen bereits steht: ein Wurzeleintrag mit `withSubItems`, aktiv, wenn eine der beiden Unterrouten aktiv ist.

| Unterpunkt | Route |
|------------| --- |
| Übersicht  | `/web/application/statistics` |
| Statistik  | `/web/absence/statistics` |

Der Wurzeleintrag zeigt weiterhin auf die Übersicht, damit ein Klick auf „Abwesenheiten" dort landet, wo er heute landet.

Die Rollenprüfung bleibt unverändert (`OFFICE`, `BOSS`, `DEPARTMENT_HEAD`, `SECOND_STAGE_AUTHORITY`) und gilt für beide Unterpunkte gleichermaßen.

Neue Übersetzungsschlüssel in Anlehnung an `nav.company.sicknotes.overview` und `nav.company.sicknotes.statistics`.

## Tests

Ergänzung im bestehenden `FrameDataProviderTest`:

- der Abwesenheiten-Eintrag hat zwei Unterpunkte mit den erwarteten Routen
- der Wurzeleintrag ist aktiv, wenn eine der beiden Unterrouten aufgerufen ist
- Rollen ohne Berechtigung sehen den Eintrag weiterhin nicht

## Definition of Done

- [x] Unterpunkte vorhanden, bestehende Route unverändert
- [x] Übersetzungsschlüssel in allen vier `messages`-Dateien
- [x] Tests grün

## Anmerkung

Der zweite Unterpunkt heißt „Statistik" — analog zum Krankmeldungs-Pendant („Statistiken"), Schlüssel `nav.company.applications.statistics` in allen vier `messages`-Dateien.
