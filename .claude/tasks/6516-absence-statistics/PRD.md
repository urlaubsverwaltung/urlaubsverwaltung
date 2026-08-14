# PRD — Unternehmen Detailseite Abwesenheiten

Issue: https://github.com/urlaubsverwaltung/urlaubsverwaltung/issues/6516
Mockup: `absence-statistics.html` (siehe `mockup/` neben dieser Datei)

## Problem Statement

Office, Geschäftsführung, Abteilungsleitung und Freigabeverantwortliche sehen unter *Unternehmen → Abwesenheiten* heute nur eine Tabelle: eine Zeile je Person, gefiltert über einen frei wählbaren Zeitraum. Damit lässt sich nachschlagen, was eine einzelne Person genommen hat — aber nicht erkennen, wie sich Abwesenheiten über das Jahr verteilen, welche Abwesenheitsarten das Volumen ausmachen und wie viel Urlaub am Ende des Jahres noch offen ist.

Für Krankmeldungen und Überstunden gibt es diese grafische Auswertung bereits (`/web/sicknote/statistics`, `/web/overtime/statistics`). Für Abwesenheiten fehlt sie. Wer den Urlaubsberg abschätzen oder die Belastung im Sommer planen will, exportiert heute CSV und rechnet außerhalb der Anwendung weiter.

## Solution

Eine neue Seite *Unternehmen → Abwesenheiten → Auswertung* unter `/web/absence/statistics`, aufgebaut wie die Krankmeldungs- und Überstundenauswertung: Jahresauswahl oben, darunter drei Darstellungen.

1. **Abwesenheitstage je Monat** — gestapelte Balken, ein Segment je Abwesenheitsart, in der Farbe, die die Art auch im Kalender hat.
2. **Verteilung der Abwesenheitsarten** — Kuchendiagramm über das Kalenderjahr, mit einer Legende, die je Art absolute Tage und Anteil trägt.
3. **Genommener Urlaub** — ein Ring im Stil der Krankmeldungsseite: Anteil des bereits genommenen bzw. geplanten Erholungsurlaubs am gültigen Anspruch, dazu Durchschnitt pro Person und verfallener Resturlaub.

Kein Vorjahresvergleich auf der ganzen Seite — weder als Kennzahlenzeile noch je Abwesenheitsart noch am Ring. Das war ein früherer Entwurf; das Refinement vom 14.08.2026 hat ihn zugunsten einer einfacheren Seite verworfen (siehe „Entscheidungen auf einen Blick" in der README für den Verlauf).

## User Stories

1. Als Office möchte ich die Abwesenheitstage eines Jahres nach Monaten aufgeschlüsselt sehen, damit ich erkenne, in welchen Monaten die Belegschaft besonders dünn besetzt ist.
2. Als Office möchte ich die Monatsbalken nach Abwesenheitsart aufgeschlüsselt sehen, damit ich Erholungsurlaub von Elternzeit und Überstundenabbau unterscheiden kann.
3. Als Office möchte ich, dass eine Abwesenheitsart im Diagramm dieselbe Farbe hat wie im Kalender, damit ich sie ohne Legendenblick wiedererkenne.
4. Als Geschäftsführung möchte ich die Verteilung der Abwesenheitsarten über das ganze Jahr als Kuchendiagramm sehen, damit ich das Verhältnis der Arten zueinander auf einen Blick erfasse.
5. Als Geschäftsführung möchte ich zu jeder Abwesenheitsart die absoluten Tage sehen, damit ich nicht nur Anteile, sondern Größenordnungen beurteilen kann.
6. ~~Als Geschäftsführung möchte ich zu jeder Abwesenheitsart den Vorjahreswert sehen~~ — gestrichen im Refinement vom 14.08.2026, kein Vorjahresvergleich mehr auf der Seite.
7. Als Geschäftsführung möchte ich sehen, wie viel vom gültigen Urlaubsanspruch bereits genommen oder geplant ist, damit ich den noch offenen Urlaubsberg und die daraus folgende Rückstellung abschätzen kann.
8. Als Geschäftsführung möchte ich den genommenen bzw. geplanten Urlaub zusätzlich als Prozentwert sehen, damit ich Jahre und Personenkreise unterschiedlicher Größe vergleichen kann.
9. ~~Als Geschäftsführung möchte ich neben dem heutigen Stand eine Prognose zum 31.12. sehen~~ — gestrichen im Refinement vom 14.08.2026, nur noch ein Stichtag und ein Ring.
10. Als Office möchte ich sehen, wie viele Resturlaubstage verfallen sind, damit ich den Verlust beziffern kann, obwohl er den Prozentwert nicht beeinflusst.
11. Als Office möchte ich den Durchschnitt der genommenen bzw. geplanten Urlaubstage pro Mitarbeitende sehen, damit ich einschätzen kann, ob der Urlaubsberg auf wenige Personen konzentriert ist.
12. Als Abteilungsleitung möchte ich dieselbe Auswertung für meine Abteilungen sehen, damit ich meine Urlaubsplanung auf derselben Datenbasis führe wie das Office.
13. Als Freigabeverantwortliche:r möchte ich die Auswertung für die von mir verantworteten Personen sehen, damit ich Freigabeentscheidungen im Jahreskontext treffe.
14. Als angemeldete Person ohne diese Rollen möchte ich die Seite nicht sehen, damit Abwesenheitsdaten anderer nicht offenliegen.
15. Als Nutzende:r möchte ich zwischen Jahren umschalten wie auf der Krankmeldungsseite, damit ich mich nicht an eine zweite Bedienlogik gewöhnen muss.
16. Als Nutzende:r möchte ich beim Wechsel zwischen Übersicht und Auswertung keine unerwarteten Filterübernahmen erleben, damit die beiden Seiten vorhersagbar bleiben.
17. Als Nutzende:r möchte ich ein Zukunftsjahr wählen können, damit ich die bereits genehmigte Planung des nächsten Jahres sehe.
18. Als Nutzende:r möchte ich in einem Jahr ohne Daten leere, aber vollständige Diagramme sehen, damit ich erkenne, dass die Seite funktioniert und schlicht nichts vorliegt.
19. Als Nutzende:r möchte ich im Monatsdiagramm einen Tooltip mit den Werten je Art und der Monatssumme, damit ich exakte Zahlen bekomme, ohne dass 96 Zahlen im Diagramm stehen.
20. Als Nutzende:r möchte ich die Seite in hellem und dunklem Design gleichwertig nutzen können, damit sie sich in den Rest der Anwendung einfügt.
21. Als Nutzende:r einer schmalen Bildschirmbreite möchte ich, dass Kuchendiagramm und Gauge untereinander rücken statt seitlich zu überlaufen.
22. Als Office möchte ich, dass ein Urlaub über den Jahreswechsel in beiden Jahren anteilig auftaucht, damit die Jahressumme zum Urlaubskonto passt.
23. Als Office möchte ich, dass halbe Tage und Teilzeitmodelle korrekt gezählt werden, damit die Zahlen zu Kalender und CSV-Export passen.
24. Als Office möchte ich, dass eine inzwischen deaktivierte Abwesenheitsart in historischen Jahren weiterhin erscheint, damit alte Auswertungen vollständig bleiben.
25. Als Office möchte ich, dass eine Abwesenheitsart ohne Tage im gewählten Jahr gar nicht erst erscheint, damit die Legende nicht von Nullzeilen verstopft wird.
26. Als Office möchte ich, dass Personen, die im ausgewerteten Jahr aktiv waren, weiter mitzählen, auch wenn sie inzwischen deaktiviert wurden, damit vergangene Auswertungen reproduzierbar bleiben.
27. Als nicht-deutschsprachige:r Nutzende:r möchte ich die Seite in meiner Sprache sehen, damit sie so lokalisiert ist wie der Rest der Anwendung.
28. Als Nutzende:r mit Tastaturbedienung möchte ich Jahresauswahl und Diagrammlegenden erreichen können, damit die Seite ohne Maus bedienbar ist.
29. Als Nutzende:r mit `prefers-reduced-motion` möchte ich keine Diagrammanimationen sehen, damit die Seite meine Systemeinstellung respektiert.

## Implementation Decisions

### Route, Navigation, Berechtigung

- Neue Route `/web/absence/statistics`. Die bestehende Tabelle unter `/web/application/statistics` bleibt unverändert, damit Bookmarks halten.
- Der Navigationseintrag „Abwesenheiten" in der Gruppe *Unternehmen* bekommt Unterpunkte `Übersicht` und `Auswertung`, exakt wie der Krankmeldungseintrag.
- Sichtbar und aufrufbar für `OFFICE`, `BOSS`, `DEPARTMENT_HEAD`, `SECOND_STAGE_AUTHORITY` — dieselbe Menge wie die Übersicht daneben.
- Kein Hinweis auf der Seite, worauf sich die Zahlen beziehen. `DEPARTMENT_HEAD` und `SECOND_STAGE_AUTHORITY` sehen ihren Ausschnitt ohne Kennzeichnung.

### Module

Neues Package `org.synyx.urlaubsverwaltung.absence.statistics` mit drei rechnenden Kernen ohne Spring- und Datenbankbezug, einem dünnen Service davor und einem Controller darüber.

**`AbsenceStatisticsPersons`** — löst den relevanten Personenkreis auf.
Eingabe: angemeldete Person, Jahr. Ausgabe: Liste der Personen. `OFFICE` und `BOSS` erhalten alle im Jahr aktiven Personen, `DEPARTMENT_HEAD` und `SECOND_STAGE_AUTHORITY` ihre verwalteten Mitglieder, jeweils gefiltert über `PersonActivePeriod`: wer im Jahr mindestens einen Tag aktiv war, zählt. Damit löst dieses Modul die Einschränkung ab, die `SickNoteStatisticsService` heute noch dokumentiert („we do not know whether a person has been active or inactive in a year before this year") — die Information existiert seit #6402.

**`MonthlyAbsenceDays`** — der Rechenkern für die Balken und den Kuchen.
Eingabe: Anträge, Arbeitszeitkalender je Person, Jahr. Ausgabe: Werktage je Abwesenheitsart und Monat. Ein Antrag wird tagesgenau auf Monate und Jahre gesplittet; gezählt wird über `WorkingTimeCalendar`, also nach individueller Arbeitszeit, Feiertagen und `DayLength`. Berücksichtigt werden Anträge mit `ApplicationStatus.activeStatuses()`; der Status wird nicht weiter unterschieden.

**`VacationDaysTaken`** — der Rechenkern für den Ring.
Eingabe: Urlaubskonten und `VacationDaysLeft` je Person, ein Stichtag. Ausgabe: Summe genommener bzw. geplanter Tage, Summe des noch gültigen Anspruchs, Prozentwert, Durchschnitt genommener bzw. geplanter Tage pro Person und Summe verfallener Resturlaubstage. Nutzt `VacationDaysLeft#getLeftVacationDays(today, doRemainingVacationDaysExpire, expiryDate)`, das den Stichtag bereits als Parameter führt — der Prozentwert ist `100 % − (verbleibende Tage / gültiger Anspruch)`, rechnerisch dieselbe Zahl wie ein „verbleibend"-Prozentwert, nur umgekehrt ausgewiesen; eine Deckelung bei über 100 % ist nicht nötig, weil Anträge über den gültigen Anspruch hinaus im System nicht möglich sind. Verfallener Resturlaub fällt aus Zähler *und* Nenner, der Prozentwert springt am Verfallsdatum also nicht. Personen ohne Urlaubskonto für das Jahr gehen in keine dieser Zahlen ein und werden auch nicht ausgewiesen.

**`AbsenceStatisticsService`** — Orchestrator.
Beschafft Personen (`AbsenceStatisticsPersons`), Anträge (`ApplicationService#getApplicationsForACertainPeriodAndStatus` mit `activeStatuses()`), Arbeitszeitkalender (`WorkingTimeCalendarService#getWorkingTimesByPersons`), Abwesenheitsarten (`VacationTypeService`) und Konten (`AccountService`, `VacationDaysService`) und setzt daraus das Ergebnisobjekt für ein Jahr zusammen. Wird je Aufruf **einmal** ausgeführt, für das gewählte Jahr — es gibt keinen Vorjahresvergleich mehr.

**`AbsenceStatisticsViewController`** — Web.
`@PreAuthorize` auf die vier Rollen, `year`-Parameter optional mit `Year.now(clock)` als Vorgabe, Model-Attribute für Template und Graph-DTOs. Die DTO-Records für die Diagrammdaten liegen wie bei `SickNoteStatisticsViewController` im Controller.

### Sichtbare Abwesenheitsarten

Es erscheinen nur Arten mit Tagen im gewählten Jahr — unabhängig vom `active`-Flag des `VacationType`. Eine inzwischen deaktivierte Art bleibt damit in historischen Jahren sichtbar, in denen sie tatsächlich genutzt wurde, und verschwindet aus aktuellen Jahren ohne Nutzung. Sortiert wird absteigend nach Tagen des gewählten Jahres, damit Balkenstapel, Kuchen und Legende dieselbe Reihenfolge haben.

### Darstellung

- Monatsdiagramm: gestapelte Balken, `dataLabels` aus (acht Arten × zwölf Monate wären rund 96 Zahlen im Plot), Werte im Tooltip, dort absteigend sortiert und mit Monatssumme. 2px transparenter `stroke` als Trennung zwischen Segmenten, wie in `sick-notes-statistics.js`.
- Kuchendiagramm ohne eigene ApexCharts-Legende. Die Zahlen stehen in einer HTML-Legende daneben: Farbe, Name, absolute Tage, Anteil. Damit gibt es je Art genau eine Stelle mit Zahlen.
- Ring als `radialBar` mit genau **einer** Serie (Prozentwert genommener/geplanter Urlaub zum jeweiligen Stichtag), Aufbau wie der Gauge auf der Krankmeldungsseite, nur mit einer statt zwei Serien. Der Ring füllt sich mit zunehmend genommenem Urlaub — bewusst umgekehrt zur ursprünglichen „verbleibend"-Lesart, damit ein Jahresende mit weitgehend ausgeschöpftem Urlaub nicht wie ein leerer, kaputt wirkender Ring aussieht.
- Farben aus den bestehenden `--absence-color-*`-Tokens in `theme.css`, die die konfigurierte `VacationTypeColor` abbilden.
- Jahre ohne Daten rendern leere Diagramme (Achsen, alle zwölf Monate, Ring auf 0 %) — kein eigener Leerzustandszweig.

### Bekanntes Risiko

`--absence-color-BLUE` (`#3b82f6`) und `--absence-color-VIOLET` (`#8b5cf6`) liegen bei ΔE 12 in Normalsicht und ΔE 1,3 unter Deuteranopie. Direkt übereinander gestapelt sind sie praktisch eine Fläche. Abgefedert wird das durch Legende und Tooltip, die jedes Segment benennen. Eine Änderung der Farbpalette ist nicht Teil dieser Umsetzung.

## Testing Decisions

Ein guter Test hier prüft beobachtbares Verhalten: eine Eingabemenge geht rein, geprüfte Zahlen kommen raus. Keine Zusicherungen auf private Methoden, keine Mock-Verifikationen auf Aufrufreihenfolgen innerhalb eines Moduls. Die drei Kernmodule sind genau deshalb frei von Spring und Repository-Zugriffen geschnitten.

**`MonthlyAbsenceDays`** — der wichtigste Test. Abgedeckt werden: Antrag über den Monatswechsel, Antrag über den Jahreswechsel (erscheint anteilig in beiden Jahren), halber Tag, Teilzeitperson mit freiem Wochentag, Feiertag innerhalb des Antragszeitraums, Antrag mit `WAITING` (zählt mit), Antrag mit `REJECTED`/`CANCELLED` (zählt nicht), mehrere Arten im selben Monat.

**`VacationDaysTaken`** — Verfall vor und nach dem Stichtag, Prozentwert bleibt am Verfallsdatum stabil, Person ohne Urlaubskonto fließt nicht ein, Durchschnitt über den Personenkreis, Person mit abweichendem Verfallsdatum wird individuell behandelt, Anspruchssumme 0 ergibt Prozentwert 0 ohne Ausnahme.

**`AbsenceStatisticsPersons`** — je Rolle der erwartete Personenkreis, unterjähriger Eintritt und Austritt über `PersonActivePeriod`, inzwischen deaktivierte Person bleibt in einem vergangenen Jahr enthalten, Person ohne Rolle bekommt eine leere Menge.

**`AbsenceStatisticsViewController`** — MockMvc-Test in der Art von `SickNoteStatisticsViewControllerTest`: Zugriff je Rolle erlaubt bzw. verweigert, `year`-Parameter und Vorgabe ohne Parameter, erwartetes View-Name und Model-Attribute.

**JS-Bundle** — Vitest analog `src/main/javascript/bundles/__tests__/sick-notes-statistics.spec.js`: Aufbau der Balkenserien aus dem Model, Tooltip-Inhalt inklusive Monatssumme, Aufbau der Legendendaten, Verhalten bei leerem Datensatz.

Prior Art im Repo: `SickNoteStatisticsServiceTest`, `SickNoteStatisticsViewControllerTest`, `ApplicationForLeaveStatisticsBuilderTest`, `bundles/__tests__/sick-notes-statistics.spec.js`.

## Out of Scope

- Personensuche auf der Auswertungsseite
- Abteilungsfilter
- CSV-Export der Auswertung
- Krankmeldungen als weitere Abwesenheitsart — dafür gibt es `/web/sicknote/statistics`
- Visuelle Trennung zwischen beantragten und genehmigten Tagen
- Ein eigener Leerzustand für Jahre ohne Daten
- Änderungen an der `VacationTypeColor`-Palette
- Änderungen an der bestehenden Tabelle unter `/web/application/statistics`
- Vorjahresvergleich, an jeglicher Stelle der Seite (gestrichen im Refinement vom 14.08.2026)
- Kennzahlenzeile mit Gesamt-Abwesenheitstagen und Durchschnitt pro Mitarbeitende (gestrichen im selben Refinement)
- Prognose zum Jahresende beim Ring „Genommener Urlaub" (gestrichen im selben Refinement, nur noch ein Stichtag)

## Further Notes

- Die Seite lädt je Aufruf die Daten genau eines Jahres — es gibt keinen Vorjahresvergleich. Personenkreis, Arbeitszeitkalender und Konten sollten innerhalb eines Aufrufs einmal beschafft und durch die Kerne gereicht werden, wie es `ApplicationForLeaveStatisticsBuilder` bereits vormacht.
- JS-Bundles werden über `glob` aus `src/main/javascript/bundles/*.js` eingesammelt, CSS-Bundles über PostCSS aus `src/main/css/bundles` — eine neue Datei genügt jeweils, keine Registrierung nötig.
- Übersetzungen sind in `messages.properties` (Deutsch, Vorgabe), `messages_en.properties`, `messages_de_AT.properties` und `messages_el.properties` zu pflegen.
- Wird das Format der Graph-DTOs später geändert, muss der `version`-Schlüssel der Serien-Sichtbarkeitspersistenz im JS-Bundle hochgezogen werden, sonst zeigt gespeicherter lokaler Zustand die falschen Serien.