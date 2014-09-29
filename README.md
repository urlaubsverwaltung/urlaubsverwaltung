# Urlaubsverwaltung

Die Urlaubsverwaltung ist eine Web-Anwendung, die es ermöglicht, Urlaubsanträge von Mitarbeitern elektronisch zu verwalten. Mitarbeiter stellen Urlaubsanträge, die von den jeweils Berechtigten genehmigt oder abgelehnt werden. Die Anwendung bietet Übersicht über die bestehenden Urlaubsanträge und ermöglicht außerdem Überblick und Pflege von Urlaubsanspruch und Anzahl verbleibender Urlaubstage der Mitarbeiter.

Weitere Informationen zur Urlaubsverwaltung findet man im [synyx Blog](http://blog.synyx.de):
* [Stand November 2011](http://blog.synyx.de/2011/11/elektronische-urlaubsverwaltung-made-by-youngsters/)
* [Stand November 2012](http://blog.synyx.de/2012/11/urlaubsverwaltung-was-hat-sich-getan/)

## Demo System

Zum Ausprobieren der Anwendung gibt es ein [Demo System](http://urlaubsverwaltung-demo.synyx.de) mit einem Testbenutzer.
* Benutzername: test
* Passwort: secret

## Getting Started

### Repository clonen

<pre>  git clone git@github.com:synyx/urlaubsverwaltung.git</pre>

### Anwendung starten

Man kann die Anwendung mit dem Maven Tomcat Plugin starten. Ohne weitere Angabe wird eine H2-Datenbank genutzt.

<pre>  mvn tomcat:run</pre>

Möchte man auch lokal eine MySQL-Datenbank nutzen, muss man die `db` Property auf `mysql` setzen:

<pre>  mvn tomcat:run -Ddb=mysql</pre>

### Anwendung öffnen

Im Browser lässt sich die Anwendung dann über `http://localhost:8080/urlaubsverwaltung` ansteuern.

### Testdaten

Startet man die Anwendung mit der H2-Datenbank werden für Entwicklungszwecke Benutzer, Urlaubsanträge und Krankmeldungen angelegt. 
Man kann sich in dieser Umgebung ebenfalls mit dem Testbenutzer `test/secret` anmelden.

## Konfiguration

Die Anwendung besitzt im Verzeichnis `src/main/resources` mehrere Properties Dateien, die zur Konfiguration genutzt werden.

### Properties Dateien

* `config.properties` u.a. Konfiguration von LDAP
* `db.properties` Konfiguration der Datenbank
* `mail.properties` Konfiguration des Mail-Hosts und Absender E-Mail-Adressen
* `business.properties` Individuelle Regelungen wie bspw. maximal möglicher Jahresurlaub


### Überschreiben der Properties

Die vorhandenen Properties können direkt innerhalb der oben genannten Dateien angepasst werden oder über die dort definierten globalen Variablen.

Globale Variablen können bspw. in der `.bashrc` überschrieben werden:

<pre>  export DATABASE_URL=jdbc:mysql://127.0.0.1:3306/urlaub</pre>


## Authentifizierung

Es gibt bisher zwei Authentifizierungsmethoden: LDAP und Demo.

Bei der Authentifizierungsmethode Demo gibt es einen Testbenutzer mit Benutzernamen "test" und Passwort "secret".
Möchte man besagten Testbenutzer über Liquibase anlegen lassen, muss die Anwendung im Kontext Demo gestartet werden, dies funktioniert folgendermaßen:
<pre>  mvn tomcat:run -Dliquibase.context=demo</pre>


Bei der Authentifizierungsmethode LDAP wird die LDAP-URL aus `src/main/resources/config.properties` genutzt.
Ist die Authentifizierung erfolgreich und der Benutzer noch nicht im System der Urlaubsverwaltung eingepflegt, wird die Person automatisch angelegt. Dem ersten Benutzer, der auf diese Weise im System angelegt wird, wird die Rolle Office zugewiesen (ermöglicht Rechteverwaltung). Alle anderen Benutzer, die automatisch beim Einloggen im System angelegt werden, erhalten initial die Rolle User.

Ein User hat immer eine oder mehrere folgender Berechtigungen inne:
* **inaktiv**: hat keinen Zugang mehr zur Urlaubsverwaltung (bestehende Daten des Benutzers bleiben zur Archivierung bestehen)
* **User**: darf Urlaub für sich selbst beantragen
* **Boss**:	darf Urlaubsanträge von Mitarbeitern einsehen, genehmigen und ablehnen
* **Office**: darf Mitarbeiterdaten verwalten, Urlaub für Mitarbeiter beantragen und Urlaubsanträge stornieren

## Development

Die Anwendung basiert auf dem [Spring](http://www.springsource.org/) MVC Framework. Zur Ermittlung von Feiertagen wird das Framework [Jollyday](http://jollyday.sourceforge.net/) benutzt. Das Frontend beinhaltet Elemente von [Bootstrap](http://twitter.github.io/bootstrap/) gewürzt mit einer Prise [jquery](http://jquery.com/).

## License

[synyx/urlaubsverwaltung](http://github.com/synyx/urlaubsverwaltung) is licensed under the
[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

Alle Logos, Marken- und Warenzeichen unterliegen **nicht** der Apache License 2.0 und dürfen nur mit schriftlicher Genehmigung von [synyx](http://www.synyx.de/) weiterverwendet werden.
