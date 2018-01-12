### [urlaubsverwaltung-2.26.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.26.1)
* Bug: Google Calendar Synchronisation funktioniert nur mit localhost [#377](https://github.com/synyx/urlaubsverwaltung/pull/377) 

### [urlaubsverwaltung-2.26.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.26.0)
* Anbindung an Google Calendar [#8](https://github.com/synyx/urlaubsverwaltung/issues/8)
* Bug: Urlaubsantrag für ganztägig und morgens/mittags an einem Tag möglich [#257](https://github.com/synyx/urlaubsverwaltung/issues/257)
* Kleinere Refactorings (Entfernen von Unterschriftssystem)

### [urlaubsverwaltung-2.25.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.25.0)
* Übersicht über alle Abwesenheiten von [@ajanus](https://github.com/ajanus) hinzu [#350](https://github.com/synyx/urlaubsverwaltung/pull/350)

### [urlaubsverwaltung-2.24.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.24.1)
* Bug: Fix H2 Konfiguration für Entwicklungsumgebung

### [urlaubsverwaltung-2.24.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.24.0)
* Erweiterung der Benachrichtigung bei vorläufiger Genehmigung von Urlaubsanträgen: Hier werden jetzt auch mehrere Abteilungen beachtet.
* Kleiner Refactorings (Paketstruktur und Sonar Issues)

### [urlaubsverwaltung-2.23.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.23.0)
* Kommentar zu Urlaubsanspruch in Urlaubsverwaltung pflegen [#238](https://github.com/synyx/urlaubsverwaltung/issues/238)
* Probleme beim Einrichten einer neuen Installation (Schemamigration) [#264](https://github.com/synyx/urlaubsverwaltung/issues/264)
* Einmaligen Feiertag: Reformationstag [#265](https://github.com/synyx/urlaubsverwaltung/issues/265)
* Depencency Updates: Spring Boot 1.4.2 [#301](https://github.com/synyx/urlaubsverwaltung/pull/301) and Swagger 1.0.2 [#277](https://github.com/synyx/urlaubsverwaltung/issues/277)
* Benutzer-Liste wird nicht vollständig angezeigt [#256](https://github.com/synyx/urlaubsverwaltung/issues/256)

### [urlaubsverwaltung-2.22.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.22.0)
* Verbessertes Logging für die Erinnerungsfunktion bei lange wartenden Urlaubsanträgen
* Verbesserte Beschreibung der Office-Rolle
* Update test-emailaddresses to reduce chance to send spam [#253](https://github.com/synyx/urlaubsverwaltung/issues/253)
* Betreff in E-Mail bei neu beantragtem Urlaub sollte Namen enthalten [#249](https://github.com/synyx/urlaubsverwaltung/issues/249)
* Erweiterung der REST-API um die Schnittstelle [`/availabilities`](http://urlaubsverwaltung-demo.synyx.de/api/index.html#!/availabilities) [#208](https://github.com/synyx/urlaubsverwaltung/issues/208)

### [urlaubsverwaltung-2.21.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.21.1)
* Added additional exchange connection configuration (without domain) [#241](https://github.com/synyx/urlaubsverwaltung/issues/241) 
* crash on start urlaubsverwaltung-2.21.0.jar [#239](https://github.com/synyx/urlaubsverwaltung/issues/239) 
* Regelmäßige Erinnerungsmail bei wartenden Anträgen Einstellungen [#227](https://github.com/synyx/urlaubsverwaltung/issues/227)
* Temporär genehmigte Urlaubsanträge stornieren Abteilungen [#229](https://github.com/synyx/urlaubsverwaltung/issues/229) 
* Berechtigungsanzeige: Abteilungen werden nicht angezeigt [#234](https://github.com/synyx/urlaubsverwaltung/issues/234)

### [urlaubsverwaltung-2.21.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.21.0)
* Fehler beim Anpassen der Benutzerberichtigungen [#226](https://github.com/synyx/urlaubsverwaltung/issues/226) 
* Regelmäßige Erinnerungsmail bei wartenden Anträgen Einstellungen [#227](https://github.com/synyx/urlaubsverwaltung/issues/227) 
* Antrag von Abteilungsleiter nur durch Chef bewilligen [#228](https://github.com/synyx/urlaubsverwaltung/issues/228)
* Anrede mit Vor- und Nachname bei Chef-Mails [#225](https://github.com/synyx/urlaubsverwaltung/issues/225) 

### [urlaubsverwaltung-2.20.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.20.1)
* Bundeslandanzeige bezieht sich auf den Arbeitsort nicht Wohnort [#222](https://github.com/synyx/urlaubsverwaltung/issues/222)

### [urlaubsverwaltung-2.20.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.20.0)
* Benutzerformular: UX verbessern [#216](https://github.com/synyx/urlaubsverwaltung/issues/216)

### [urlaubsverwaltung-2.19.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.19.0)
#### Bug Fix / Konfigurationsänderung

* Problembehebung LDAP/AD Authentifizierung/Sync: Update von Spring Boot Version und Spring LDAP Core [#215](https://github.com/synyx/urlaubsverwaltung/issues/215)

### [urlaubsverwaltung-2.18.2](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.18.2)
#### Bug Fixes
* Bug: Error beim klicken von "Benutzer anlegen" [#213](https://github.com/synyx/urlaubsverwaltung/issues/213)
* Bug: Urlaub genehmigen in Übersicht "offene Urlaubsanträge" bei zweistufigem Genehmigungsprozess [#212](https://github.com/synyx/urlaubsverwaltung/issues/212) 
* Bug: Urlaub ablehnen in Übersicht "offene Urlaubsanträge" [#209](https://github.com/synyx/urlaubsverwaltung/issues/209) 
* Bug: Editieren von Benutzer fehlende Validierung für invaliden Urlaubsanspruch [#204](https://github.com/synyx/urlaubsverwaltung/issues/204) 

#### Change Request
* Noch nicht genehmigten Urlaub im Kalendar farblich hervorheben [#200](https://github.com/synyx/urlaubsverwaltung/issues/200) 


### [ urlaubsverwaltung-2.18.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.18.1)
####Bug Fixes

*  Korrekte Auswahl des Krankmedlungstyps beim Editieren von Krankmeldungen [#201](https://github.com/synyx/urlaubsverwaltung/issues/201)   

### [urlaubsverwaltung-2.18.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.18.0)
#####Features

*  Bundeslandauswahl pro User ermöglichen [#178](https://github.com/synyx/urlaubsverwaltung/issues/178) 
*  Benutzerliste filterbar nach Abteilung [#136](https://github.com/synyx/urlaubsverwaltung/issues/136) 

#### Bug Fixes

*  deaktivierter User loggt sich ein - Problem im Browser [#190](https://github.com/synyx/urlaubsverwaltung/issues/190) 

### [urlaubsverwaltung-2.17.3](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.17.3)
#### Bug Fixes

*  Benutzer deaktivieren nicht möglich [#188](https://github.com/synyx/urlaubsverwaltung/issues/188)

### [urlaubsverwaltung-2.17.2](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.17.2)
#### Bug Fixes

* Anzeige der Anträge von Mitarbeitern fehlerhaft (moment is not defined) [#176](https://github.com/synyx/urlaubsverwaltung/issues/176) 
* Einstellung: Überstundenverwaltung deaktivieren wird nicht dauerhaft gespeichert [#183](https://github.com/synyx/urlaubsverwaltung/issues/183) 
* Komma-Zahlen eintragen mit englischer Browser Locale [#186](https://github.com/synyx/urlaubsverwaltung/issues/186)

### [urlaubsverwaltung-2.17.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.17.1)
Mini Fix in Personenformular: Label fixen

### [urlaubsverwaltung-2.17.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.17.0)
#### Bug Fixes

* Bug: Umwandeln von Krankheitstagen in Urlaub funktioniert nicht [#170](https://github.com/synyx/urlaubsverwaltung/issues/170)
* Bug: Fehlerseite bei ungültigem Zeitraum einer Krankmeldung mit AU-Bescheinigung [#164](https://github.com/synyx/urlaubsverwaltung/issues/164)

#### Features

* Urlaubsantrag: Anzeige von Arbeitszeiten [#169](https://github.com/synyx/urlaubsverwaltung/issues/169)
* Urlaubsantrag: Anzeige von Wochentagen [#167](https://github.com/synyx/urlaubsverwaltung/issues/167)
* Benutzerpflege: Vereinfachung der Pflege von Urlaubsanspruch Benutzerpflege [#168](https://github.com/synyx/urlaubsverwaltung/issues/168)
* Benutzerpflege: Validierung bei Vergabe von Berechtigungen verbessern [#163](https://github.com/synyx/urlaubsverwaltung/issues/163)
* Urlaubsantrag: Überstundenanzahl optional bei deaktivierter Überstundenfunktion Einstellungen [#161](https://github.com/synyx/urlaubsverwaltung/issues/161)

### [urlaubsverwaltung-2.16.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.16.0)
#### [Milestone 2.16.0](https://github.com/synyx/urlaubsverwaltung/issues?q=milestone%3Aurlaubsverwaltung-2.16.0+is%3Aclosed)
* Konfiguration: Standardmäßig Cache aktiv und JSP Servlet Development Mode inaktiv
* Feature: Einstellungen E-Mail-Versand erweitern um URL der Anwendung

### [urlaubsverwaltung-2.15.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.15.0)
#### [Milestone 2.15.0](https://github.com/synyx/urlaubsverwaltung/issues?q=milestone%3Aurlaubsverwaltung-2.15.0+is%3Aclosed)
* Feature: Zweistufiger Genehmigungsprozess für Urlaubsanträge [#148](https://github.com/synyx/urlaubsverwaltung/issues/148)
* Feature: E-Mail-Benachrichtung bei neuen Überstundeneinträgen [#147](https://github.com/synyx/urlaubsverwaltung/issues/147)
* Feature: Validierung für maximal mögliche Minusstunden [#146](https://github.com/synyx/urlaubsverwaltung/issues/146)
* Feature: Urlaubsantrag erweitern um Uhrzeit [#145](https://github.com/synyx/urlaubsverwaltung/issues/145) 
* Feature: Urlaubsarten pflegbar machen (Datenbank only) [#144](https://github.com/synyx/urlaubsverwaltung/issues/144)
* Feature: Krankmeldungsarten pflegbar machen (Datenbank only) [#143](https://github.com/synyx/urlaubsverwaltung/issues/143)

### [urlaubsverwaltung-2.14.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.14.1)
* Bug Fix: Es ist nicht möglich, halbtägigen Urlaub zu beantragen [#156](https://github.com/synyx/urlaubsverwaltung/issues/156)

### [urlaubsverwaltung-2.14.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.14.0)
* Enhancement: LDAP/AD Sync optional machen [#142](https://github.com/synyx/urlaubsverwaltung/issues/142)
* Enhancement: Als Mitarbeiter nicht genommenen genehmigten Urlaub stornieren können #11
* Enhancement: Update auf Spring Boot 1.3, Spring Security 4 [#126](https://github.com/synyx/urlaubsverwaltung/issues/126)

### [urlaubsverwaltung-2.13.2](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.13.2)
* Bug Fix: Krankheitsübersicht nicht möglich, wenn ein Mitarbeiter keine Arbeitszeiten konfiguriert hat [#129](https://github.com/synyx/urlaubsverwaltung/issues/129)

### [urlaubsverwaltung-2.13.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.13.1)
* Bug Fix: (Teilweise) Doppelte Urlaubskontos nach Cronjob zum Jahresanfang [#137](https://github.com/synyx/urlaubsverwaltung/issues/137)
* Bug Fix: Validierung von deaktivierter Exchange Konfiguration [#135](https://github.com/synyx/urlaubsverwaltung/issues/135)

### [urlaubsverwaltung-2.13.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.13.0)
* Maximale Überstunden konfigurieren / Überstundenfunktion implizit deaktivieren [#133](https://github.com/synyx/urlaubsverwaltung/issues/133)
* Zeitraum für Urlaubsstatistik und Krankheitsübersicht kann nun tagesgenau ausgewählt werden [#124](https://github.com/synyx/urlaubsverwaltung/issues/124)

### [urlaubsverwaltung-2.12.2](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.12.2)
* Exchange Anbindung erfolgt nun über E-Mail-Adresse statt Domäne und Benutzername
* Besseres Logging für Exchange Anbindung für bessere Fehlerverfolgbarkeit

### [urlaubsverwaltung-2.12.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.12.1)
* Bug Fix: Exchange 2013 Kalender Anbindung [#117](https://github.com/synyx/urlaubsverwaltung/issues/117)

### [urlaubsverwaltung-2.12.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.12.0)
* Technisches Feature: Umbau der Urlaubsverwaltung zu einer Spring Boot Anwendung. Ab dieser Version ist die Anwendung eine [Spring Boot](http://projects.spring.io/spring-boot/) Anwendung, d.h. sie wird nicht mehr als WAR in einem Tomcat installiert, sondern als JAR ausgeführt.

### [urlaubsverwaltung-2.11.4](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.11.4)
* Bug Fix: (Teilweise) Doppelte Urlaubskontos nach Cronjob zum Jahresanfang [#137](https://github.com/synyx/urlaubsverwaltung/issues/137)

### [urlaubsverwaltung-2.11.3](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.11.3)
* Bug Fix: Exchange 2013 Kalender Anbindung [#117](https://github.com/synyx/urlaubsverwaltung/issues/117)

### [urlaubsverwaltung-2.11.2](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.11.2)
* Bug Fix: Überstundenanzahl in Urlaubsstatistik und Überstundenliste wird auf eine Kommastelle aufgerundet
* Genauere Beschreibung siehe in [Milestone Tickets](https://github.com/synyx/urlaubsverwaltung/issues?q=milestone%3Aurlaubsverwaltung-2.11.2+is%3Aclosed)

### [urlaubsverwaltung-2.11.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.11.1)
* Bug Fix: Überstundeneintrag wird aufgerundet
* Genauere Beschreibung siehe in [Milestone Tickets](https://github.com/synyx/urlaubsverwaltung/issues?q=milestone%3Aurlaubsverwaltung-2.11.1+is%3Aclosed)

### [urlaubsverwaltung-2.11.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.11.0)
* Feature: Import/Sync der Benutzerstammdaten aus LDAP/AD bei Anwendungsstart und nächtlich
* Feature: Möglichkeit die LDAP/AD Authentifizierung nur für bestimmte Gruppe zuzulassen
* Feature: Urlaubsstatistik detailliert angezeigt nach Urlaubskategorie
* Feature: Eintragen von Überstunden ermöglichen
* Feature: Urlaub zum Überstundenabbau verknüpfen mit eingetragenen Überstunden
* Bug Fix: Kaputter "Abbrechen" Button im Personenformular
* Genauere Beschreibung siehe in [Tickets](https://github.com/synyx/urlaubsverwaltung/issues?q=milestone%3Aurlaubsverwaltung-2.11.0+is%3Aclosed)

### [urlaubsverwaltung-2.10.5](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.10.5)
* Bug Fix: (Teilweise) Doppelte Urlaubskontos nach Cronjob zum Jahresanfang [#137](https://github.com/synyx/urlaubsverwaltung/issues/137)

### [urlaubsverwaltung-2.10.4](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.10.4)
* Exchange Anbindung über E-Mail-Adresse statt Domäne und Benutzername

### [urlaubsverwaltung-2.10.2](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.10.2)
* Bug Fix: Exchange 2013 Kalender Anbindung [#117](https://github.com/synyx/urlaubsverwaltung/issues/117)

### [urlaubsverwaltung-2.10.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.10.1)
* Bug: Fix für kaputte Icons und Benutzer-Avatar im Offline-Modus

### [urlaubsverwaltung-2.10.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.10.0)
* Bug Fix: Klick auf Urlaub/Krankmeldung im Übersichtskalender liefert 404
* Feature: Eintragen von halben Krankheitstagen

### [urlaubsverwaltung-2.9.3](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.9.3)
* Bug Fix: Nullpointer in Krankmeldungsübersicht verhindern für inaktive Personen mit Krankmeldungen
* UX: Übersichtskalender Farbschema optimiert und Animation hinzugefügt

### [urlaubsverwaltung-2.9.2](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.9.2)
* Feature: Beim Einloggen werden Vorname, Nachname und E-Mail-Adresse aus LDAP/AD übernommen
* Feature: Die Einstellungen wurden erweitert um E-Mail-Versand-Konfiguration und Exchange-Kalender-Konfiguration, sodass dies nicht mehr in Property Files gepflegt werden muss.
* Bug Fix: Bei Authentifizierung mit AD kann man sich nun sowohl mit dem Benutzernamen als auch mit der E-Mail einloggen, ohne dass unterschiedliche Benutzer dafür angelegt werden.
* Bug Fix: Für die Exchange Kalender Anbindung kann man nun auch die Domain und entweder E-Mail-Adresse oder Benutzername angeben.

### [urlaubsverwaltung-2.9.1](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.9.1)
* UX: Wenn ungültiger Zeitraum beim Urlaubsantrag gewählt wird, wird eine Fehlermeldung statt "NaN Tage" als Dauer angezeigt
* UX: Die Mitarbeiterliste kann nun nach Vorname/Nachname gefiltert werden
* Feature: Im Übersichtskalender werden nun auch Krankmeldungen (in rot) angezeigt
* Feature: Im Übersichtskalender werden nun auch noch nicht genehmigte Urlaubsanträge angezeigt. Diese haben die gleiche Farbe wie die genehmigten Urlaubsanträge, um zu vermeiden, dass der Kalender zu bunt wird (Unterscheidung in Feiertag, Urlaub und Krankmeldungen)

### [urlaubsverwaltung-2.9.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.9.0)
* Feature: Anlegen und Bearbeiten von Abteilungen
* Feature: Mitarbeiter zu vorhandenen Abteilungen zuordnen
* Feature: Mitarbeiter zu Abteilungsleitern ernennen. Abteilungsleiter haben die selben Rechte wie Benutzer mit der Rolle Chef - allerdings nur für die Benutzer der Abteilungen, für die sie Abteilungsleiter sind.
* Feature: Beim Beantragen von Urlaub anzeigen, wer aus der eigenen Abteilung zu dem Zeitraum ebenfalls Urlaub hat.
* Feature: Beim Genehmigen/Ablehnen von Urlaub anzeigen, wer aus der Abteilung der Person zu dem Zeitraum ebenfalls Urlaub hat.
* Feature: Die Urlaubsverwaltung kann an einen Exchange Kalender angebunden werden. Dann werden automatisch Termine angelegt, wenn Urlaub beantragt/genehmigt bzw. Krankmeldungen angelegt werden.
* Feature: Benutzer kann auf einer Extraseite seine Daten (Stammdaten, Rollen, Abteilungen, Arbeitszeiten, Urlaubsanspruch) sehen
* UX: Pflichtfelder sind nun mit '\*' markiert
* UX: Formulare zum Antrag stellen, Personen bearbeiten und Einstellungen pflegen wurden überarbeitet und mit Hilfetexten versehen
* UX: Wenn man Urlaub zu weit in der Zukunft beantragt, sieht man bei der Fehlermeldung nun, wie weit man im Voraus Urlaub beantragen darf.
