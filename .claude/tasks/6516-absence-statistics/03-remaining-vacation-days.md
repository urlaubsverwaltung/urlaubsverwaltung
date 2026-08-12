# 03 — Verbleibende Urlaubstage

**Abhängig von:** 01 (nur wegen des Packages)
**Berührt:** `org.synyx.urlaubsverwaltung.absence.statistics`

## Ziel

Der Rechenkern für den Gauge: aus Urlaubskonten und den offenen Urlaubstagen je Person werden Summe, Anspruch, Prozentwert, Durchschnitt und verfallener Resturlaub — für zwei Stichtage.

## Umsetzung

Neue Klasse `RemainingVacationDays` — rein, ohne Spring. Beschaffung der Konten ist Aufgabe von Task 04.

Eingabe:
- `Year`
- je Person das Urlaubskonto und die daraus abgeleiteten offenen Tage (`VacationDaysLeft`)
- zwei Stichtage

Ausgabe je Stichtag: Summe verbleibender Tage, Summe des noch gültigen Anspruchs, Prozentwert, Durchschnitt pro Person, Summe verfallener Resturlaubstage.

Regeln:

- `VacationDaysLeft#getLeftVacationDays(today, doRemainingVacationDaysExpire, expiryDate)` nimmt den Stichtag bereits als Parameter — dort einsetzen, nicht selbst nachbauen. Ob und wann Resturlaub verfällt, steht am `Account` (`doRemainingVacationDaysExpireLocally` / `…Globally`, `expiryDateLocally` / `…Globally`) und ist je Person unterschiedlich.
- Prozentwert = verbleibende Tage geteilt durch **noch gültigen** Anspruch. Verfallener Resturlaub fällt aus Zähler *und* Nenner; der Wert springt am Verfallsdatum also nicht.
- Verfallene Tage werden separat aufsummiert und ausgewiesen (`VacationDaysLeft#getExpiredRemainingVacationDays`).
- Stichtage: laufendes Jahr → heute und 31.12. des Jahres; vergangenes Jahr → beide 31.12. des Jahres; zukünftiges Jahr → beide 01.01. des Jahres. Welcher Stichtag gilt, entscheidet der Aufrufer; dieses Modul rechnet nur.
- Personen ohne Urlaubskonto für das Jahr gehen in keine der Zahlen ein — weder in Summe noch in Nenner noch in den Divisor des Durchschnitts. Es wird **kein** Hinweis auf ihre Zahl erzeugt.
- Ist der Anspruch in Summe 0, ist der Prozentwert 0 und keine Division durch Null.

## Tests

`RemainingVacationDaysTest`, ohne Spring:

- Summe und Prozentwert über mehrere Personen
- Stichtag vor dem Verfallsdatum: Resturlaub ist in Zähler und Nenner enthalten
- Stichtag nach dem Verfallsdatum: Prozentwert bleibt gegenüber vorher unverändert, wenn kein Urlaub genommen wurde
- verfallene Tage werden korrekt aufsummiert
- Person ohne Urlaubskonto verändert weder Summe noch Durchschnitt
- Durchschnitt pro Person bezieht sich nur auf Personen mit Konto
- Person mit abweichendem Verfallsdatum wird individuell behandelt
- Prognose zum 31.12. liegt unter dem Stand heute, wenn im Restjahr Urlaub genehmigt ist
- vergangenes Jahr: beide Stichtage ergeben identische Zahlen
- Anspruchssumme 0 ergibt Prozentwert 0 ohne Ausnahme

## Definition of Done

- [ ] `RemainingVacationDays` implementiert, ohne Spring- und Datenbankbezug
- [ ] Verfallslogik über `VacationDaysLeft`, nicht nachgebaut
- [ ] Tests grün, alle oben genannten Fälle abgedeckt
