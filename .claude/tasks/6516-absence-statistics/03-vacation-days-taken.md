# 03 — Genommener Urlaub

**Abhängig von:** 01 (nur wegen des Packages)
**Berührt:** `org.synyx.urlaubsverwaltung.absence.statistics`

## Ziel

Der Rechenkern für den Ring: aus Urlaubskonten und den offenen Urlaubstagen je Person werden Summe, Anspruch, Prozentwert, Durchschnitt und verfallener Resturlaub — für einen Stichtag.

## Umsetzung

Neue Klasse `VacationDaysTaken` — rein, ohne Spring. Beschaffung der Konten ist Aufgabe von Task 04.

Eingabe:
- `Year`
- je Person das Urlaubskonto und die daraus abgeleiteten offenen Tage (`VacationDaysLeft`)
- ein Stichtag

Ausgabe: Summe genommener bzw. geplanter Tage, Summe des noch gültigen Anspruchs, Prozentwert, Durchschnitt genommener bzw. geplanter Tage pro Person, Summe verfallener Resturlaubstage.

Regeln:

- `VacationDaysLeft#getLeftVacationDays(today, doRemainingVacationDaysExpire, expiryDate)` nimmt den Stichtag bereits als Parameter — dort einsetzen, nicht selbst nachbauen. Ob und wann Resturlaub verfällt, steht am `Account` (`doRemainingVacationDaysExpireLocally` / `…Globally`, `expiryDateLocally` / `…Globally`) und ist je Person unterschiedlich.
- Genommene bzw. geplante Tage einer Person = gültiger Anspruch minus `getLeftVacationDays(...)`. Prozentwert = genommene/geplante Tage geteilt durch **noch gültigen** Anspruch — also `100 % − (verbleibende Tage / Anspruch)`, rechnerisch dieselbe Zahl wie ein „verbleibend"-Prozentwert, nur umgekehrt ausgewiesen. Verfallener Resturlaub fällt aus Zähler *und* Nenner; der Wert springt am Verfallsdatum also nicht.
- Verfallene Tage werden separat aufsummiert und ausgewiesen (`VacationDaysLeft#getExpiredRemainingVacationDays`).
- Stichtag: laufendes Jahr → heute; vergangenes Jahr → 31.12. des Jahres; zukünftiges Jahr → 01.01. des Jahres. Welcher Stichtag gilt, entscheidet der Aufrufer; dieses Modul rechnet nur.
- Personen ohne Urlaubskonto für das Jahr gehen in keine der Zahlen ein — weder in Summe noch in Nenner noch in den Divisor des Durchschnitts. Es wird **kein** Hinweis auf ihre Zahl erzeugt.
- Ist der Anspruch in Summe 0, ist der Prozentwert 0 und keine Division durch Null.
- Eine Deckelung bei über 100 % ist nicht nötig: Anträge über den gültigen Anspruch hinaus sind im System nicht möglich.

## Tests

`VacationDaysTakenTest`, ohne Spring:

- Summe und Prozentwert über mehrere Personen
- Stichtag vor dem Verfallsdatum: Resturlaub ist in Zähler und Nenner enthalten
- Stichtag nach dem Verfallsdatum: Prozentwert bleibt gegenüber vorher unverändert, wenn kein Urlaub genommen wurde
- verfallene Tage werden korrekt aufsummiert
- Person ohne Urlaubskonto verändert weder Summe noch Durchschnitt
- Durchschnitt pro Person bezieht sich nur auf Personen mit Konto
- Person mit abweichendem Verfallsdatum wird individuell behandelt
- Anspruchssumme 0 ergibt Prozentwert 0 ohne Ausnahme

## Definition of Done

- [ ] `VacationDaysTaken` implementiert, ohne Spring- und Datenbankbezug
- [ ] Verfallslogik über `VacationDaysLeft`, nicht nachgebaut
- [ ] Tests grün, alle oben genannten Fälle abgedeckt
