[![Build Status](https://travis-ci.org/synyx/urlaubsverwaltung.png)](https://travis-ci.org/synyx/urlaubsverwaltung)

# Urlaubsverwaltung

* [Übersicht](https://github.com/synyx/urlaubsverwaltung#übersicht)
* [Installation](https://github.com/synyx/urlaubsverwaltung#installation)
* [Entwicklung](https://github.com/synyx/urlaubsverwaltung#entwicklung)
* [Hinweise zu Versionen](https://github.com/synyx/urlaubsverwaltung#hinweise-zu-versionen)
* [Technologien](https://github.com/synyx/urlaubsverwaltung#technologien)
* [Lizenz](https://github.com/synyx/urlaubsverwaltung#lizenz)

# Übersicht

Die Urlaubsverwaltung ist eine Web-Anwendung, die es ermöglicht, Urlaubsanträge von Mitarbeitern elektronisch zu
verwalten. Mitarbeiter stellen Urlaubsanträge, die von den jeweils Berechtigten genehmigt oder abgelehnt werden.
Die Anwendung bietet eine Übersicht über die bestehenden Urlaubsanträge und ermöglicht außerdem Überblick und Pflege von
Urlaubsanspruch und Anzahl verbleibender Urlaubstage der Mitarbeiter. Zusätzlich können Krankmeldungen erfasst und
überblickt werden.

![Screenshot Urlaubsverwaltung](http://synyx.de/images/opensource/screen_01.jpg)

## Demo System

Zum Ausprobieren der Anwendung gibt es ein [Demo System](http://urlaubsverwaltung-demo.synyx.de) mit einem Testbenutzer.

* Benutzername: test
* Passwort: secret

## Blogposts

Weitere Informationen zur Geschichte und Entwicklung der Urlaubsverwaltung findet man im [synyx Blog](http://blog.synyx.de):

* [Stand November 2011](http://blog.synyx.de/2011/11/elektronische-urlaubsverwaltung-made-by-youngsters/)
* [Stand November 2012](http://blog.synyx.de/2012/11/urlaubsverwaltung-was-hat-sich-getan/)
* [Stand Oktober 2014](http://blog.synyx.de/2014/10/urlaubsverwaltung-goes-mobile/)

## Berechtigungen

In der Urlaubsverwaltung gibt es aktuell folgende Arten von Berechtigungen:

* **inaktiv**: hat keinen Zugang mehr zur Urlaubsverwaltung (bestehende Daten des Benutzers bleiben zur
Archivierung bestehen)
* **User**: darf Urlaub für sich selbst beantragen
* **Boss**:	darf Urlaubsanträge von Mitarbeitern einsehen, genehmigen und ablehnen
* **Office**: darf Mitarbeiterdaten verwalten, Urlaub für Mitarbeiter beantragen und Urlaubsanträge stornieren

Eine aktive Person kann eine oder mehrere Rollen innehaben.

# Installation

Die folgende Anleitung beschreibt die Installation der Urlaubsverwaltung auf einem [Tomcat Server](http://tomcat.apache.org/).

## Systemvoraussetzungen

* Apache Tomcat Version 6
* JDK 7
* Maven 3
* MySQL Datenbank

## Download

Die Anwendung steht auf Github als ZIP oder Tarball zum Download zur Verfügung. Am Besten
[hier](https://github.com/synyx/urlaubsverwaltung/releases) die aktuellste Version auswählen und downloaden.

## Erstellen der WAR-Datei

Nach dem Entpacken der Datei kann die WAR-Datei mithilfe von [Maven](http://maven.apache.org/) erstellt werden.
Dies erfolgt durch Ausführen folgenden Befehls im Root-Verzeichnis der Anwendung:

<pre>mvn clean install</pre>

## Deployment unter Tomcat

Die erstellte WAR-Datei kann nun im installierten Tomcat Server deployed werden.
Dazu kopiert man die WAR-Datei in das Tomcat-Webapps-Verzeichnis.
([weiterführende Informationen zum Tomcat Deployment](http://tomcat.apache.org/tomcat-6.0-doc/deployer-howto.html))

## Konfiguration

### Überschreiben der Properties

Die Anwendung besitzt im Verzeichnis `src/main/resources` mehrere Properties Dateien, die zur Konfiguration genutzt werden.

* `config.properties` u.a. Konfiguration von LDAP
* `db.properties` Konfiguration der Datenbank (im `prod` Environment wird die Datei `db.prod.properties` herangezogen)
* `mail.properties` Konfiguration zum Mailversand (im `prod` Environment wird die Datei `mail.prod.properties` herangezogen)
* `business.properties` Individuelle Regelungen wie bspw. maximal möglicher Jahresurlaub

Die vorhandenen Properties können  entweder vor dem Erstellen der WAR-Datei direkt innerhalb der oben genannten Dateien
angepasst werden oder aber über durch Setzen der dort definierten globalen Variablen.

**Beispiel:**

<pre>export UV_DB_URL=jdbc:mysql://127.0.0.1:3306/urlaub</pre>

### Produktivumgebung aktivieren

Damit die Anwendung in der Produktivumgebung gestartet wird, muss man in den `CATALINA_OPTS` die System-Property `env`
auf `prod` setzen:
 
<pre>export CATALINA_OPTS=$CATALINA_OPTS -Denv=prod</pre>

**Hinweis:**

Die Anwendung verfügt über drei verschiedene Environment-Möglichkeiten. Standardmäßig (ohne Angabe) wird die
Anwendung in der `dev` Umgebung gestartet.

* `dev` nutzt eine H2-Datenbank, legt Testdaten an, nutzt als Mail-Sender einen Dummy (verschickt also keine E-Mails)
* `test` nutzt eine MySQL-Datenbank, legt keine Testdaten an, nutzt als Mail-Sender einen Dummy (verschickt also keine E-Mails)
* `prod` nutzt eine MySQL-Datenbank, legt keine Testdaten an, nutzt den Java-Mail-Sender von
[Spring](http://www.springsource.org/) (kann also E-Mails verschicken)

### Authentifizierung

Es kann gewählt werden zwischen Authentifizierung mittels LDAP und Authentifizierung mittels Active Directory. 

**Authentifizierung via LDAP:**

Voraussetzung: Es müssen die LDAP URL, die LDAP Base und LDAP User DN Patterns konfiguriert sein, damit eine Authentifizierung
via LDAP möglich ist. Die Properties sind unter `src/main/resources/config.properties` zu finden.

Die Anwendung mit dem Parameter `-Dspring.profiles.active=ldap` starten:

<pre>export CATALINA_OPTS=$CATALINA_OPTS -Denv=prod -Dspring.profiles.active=ldap</pre>

**Authentifizierung via Active Directory:**

Voraussetzung: Es müssen die Active Directory Domain und LDAP URL konfiguriert sein, damit eine Authentifizierung via
Active Directory möglich ist. Die Properties sind unter `src/main/resources/config.properties` zu finden.

Die Anwendung mit dem Parameter `-Dspring.profiles.active=activeDirectory` starten:

<pre>export CATALINA_OPTS=$CATALINA_OPTS -Denv=prod -Dspring.profiles.active=activeDirectory</pre>

## Getting started

Wenn die Anwendung im Tomcat beispielsweise im Verzeichnis `/home/user/tomcat/webapps/urlaubsverwaltung.war` deployed
wurde, ist sie erreichbar unter `<servername>:8080/urlaubsverwaltung`.

Der erste Benutzer, der sich erfolgreich im System einloggt, wird in der Urlaubsverwaltung mit der Rolle Office angelegt.
Dies ermöglicht Benutzer- und Rechteverwaltung innerhalb der Anwendung.

# Entwicklung

Im Folgenden werden die durchzuführenden Schritte beschrieben, wenn man an der Urlaubsverwaltung entwickeln möchte.

### Repository clonen

<pre>git clone git@github.com:synyx/urlaubsverwaltung.git</pre>

### Anwendung starten

Man kann die Anwendung lokal mit dem Maven Tomcat Plugin starten.
Ohne weitere Angabe wird das Development-Environment genutzt, d.h. es wird eine H2-Datenbank verwendet und es werden
keine E-Mails versendet.

<pre>mvn tomcat:run</pre>

Im Development-Environment werden für Entwicklungszwecke Benutzer, Urlaubsanträge und Krankmeldungen angelegt.
Man kann sich in dieser Umgebung ebenfalls mit dem Testbenutzer `test/secret` anmelden.

Im Browser lässt sich die Anwendung dann über `http://localhost:8080/urlaubsverwaltung` ansteuern.

### Environments

Die Anwendung verfügt über drei verschiedene Environment-Möglichkeiten:

* `dev` nutzt eine H2-Datenbank, legt Testdaten an, nutzt als Mail-Sender einen Dummy (verschickt also keine E-Mails)
* `test` nutzt eine MySQL-Datenbank, legt keine Testdaten an, nutzt als Mail-Sender einen Dummy (verschickt also keine E-Mails)
* `prod` nutzt eine MySQL-Datenbank, legt keine Testdaten an, nutzt den Java-Mail-Sender von
[Spring](http://www.springsource.org/) (kann also E-Mails verschicken)

Standardmäßig ohne jegliche Angabe wird als Environment `dev` genutzt.
Möchte man ein anderes Environment nutzen, muss man beim Starten des Tomcats die `env` Property mitgeben, z.B.:

<pre>mvn tomcat:run -Denv=test</pre>

### Authentifizierung

Es gibt drei mögliche Authentifizierungsmethoden:

* Authentifizierung für lokale Entwicklungsumgebung
* Authentifizierung via LDAP
* Authentifizierung via Active Directory

**Authentifizierung für lokale Entwicklungsumgebung:**

Möchte man die Anwendung lokal bei sich laufen lassen und sich Testdaten generieren lassen, reicht es folgenden
Befehl auszuführen:

<pre>mvn tomcat:run</pre>

Ohne Angabe des Parameters `spring.profiles.active` kann man sich nun mit dem Testbenutzer `test/secret` anmelden.

**Authentifizierung via LDAP:**

Es müssen die LDAP URL, die LDAP Base und LDAP User DN Patterns konfiguriert sein, damit eine Authentifizierung via
LDAP möglich ist. Die Properties sind unter `src/main/resources/config.properties` zu finden.

Die Anwendung mit dem Parameter `-Dspring.profiles.active=ldap` starten:

<pre>mvn tomcat:run -Dspring.profiles.active=ldap</pre>

**Authentifizierung via Active Directory:**

Es müssen die Active Directory Domain und LDAP URL konfiguriert sein, damit eine Authentifizierung via Active Directory
möglich ist. Die Properties sind unter `src/main/resources/config.properties` zu finden.

Die Anwendung mit dem Parameter `-Dspring.profiles.active=activeDirectory` starten:

<pre>mvn tomcat:run -Dspring.profiles.active=activeDirectory</pre>

# Hinweise zu Versionen

## Version 2.2.1

Wenn man die Urlaubsverwaltung schon länger nutzt und auf Version 2.2.1 oder höher updaten möchte, muss sichergestellt
sein, dass in der Datenbank keine Person mit gleichem Vor- und Nachnamen existiert. Dies führt ansonsten zu einem
Problem beim Update des Datenbankschemas und die Anwendung kann nicht starten.

# Technologien

Die Anwendung basiert auf dem [Spring](http://www.springsource.org/) MVC Framework.
Zur Ermittlung von Feiertagen wird das Framework [Jollyday](http://jollyday.sourceforge.net/) benutzt.
Das Frontend beinhaltet Elemente von [Bootstrap](http://getbootstrap.com/) gewürzt mit einer Prise
[jQuery](http://jquery.com/) und [Font Awesome](http://fontawesome.io/).

# Lizenz

[synyx/urlaubsverwaltung](http://github.com/synyx/urlaubsverwaltung) is licensed under the
[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

Alle Logos, Marken- und Warenzeichen unterliegen **nicht** der Apache License 2.0 und dürfen nur mit schriftlicher
Genehmigung von [synyx](http://www.synyx.de/) weiterverwendet werden.
