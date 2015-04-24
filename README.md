[![Build Status](https://travis-ci.org/synyx/urlaubsverwaltung.png)](https://travis-ci.org/synyx/urlaubsverwaltung)

# Urlaubsverwaltung

* [Übersicht](https://github.com/synyx/urlaubsverwaltung#übersicht)
* [Installation](https://github.com/synyx/urlaubsverwaltung#installation)
* [Konfiguration < Version 2.7.0](https://github.com/synyx/urlaubsverwaltung#konfiguration--version-270)
* [Konfiguration ab Version 2.7.0](https://github.com/synyx/urlaubsverwaltung#konfiguration-ab-version-270)
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

* Apache Tomcat Version 7
* JDK 8
* Maven 3.3
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

## Konfiguration < Version 2.7.0

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

## Konfiguration ab Version 2.7.0

### Überschreiben der Properties

Die Anwendung besitzt im Verzeichnis `src/main/resources` jeweils eine `application.properties` Datei pro Umgebung.

* `application-dev.properties` zur lokalen Entwicklung mit einer H2 Datenbank
* `application-test.properties` zum Testen der Anwendung mit einer MySQL Datenbank
* `application-prod.properties` zum Ausführen der produktiven Anwendung

Diese beinhalten gewisse Grundeinstellungen und Standardwerte. Diese allein reichen für die Produktivnahme der Anwendung allerdings noch nicht aus.
Spezifische Konfigurationen wie z.B. die Datenbank Einstellungen müssen durch eine eigene Properties-Datei hinterlegt werden.
Dazu kann die in der Anwendung befindliche Beispieldatei `urlaubsverwaltung.properties` als Vorlage verwendet werden,
um die gewünschten Einstellungen zu überschreiben.

Damit die eigene Konfigurationsdatei benutzt wird, gibt es zwei Möglichkeiten:

* Eine eigene Properties-Datei erstellen und diese unter dem Namen `urlaubsverwaltung.properties` in das `/conf` Verzeichnis unterhalb von `CATALINA_BASE`
ablegen, z.B. `tomcat/conf/urlaubsverwaltung.properties`, und den Tomcat neu starten
* Die erstellte Properties-Datei an einem beliebigen Ort ablegen und mithilfe der System Property `config` innerhalb der `CATALINA_OPTS`
den absoluten Pfad der Konfigurationsdatei angeben:
<pre>export CATALINA_OPTS=$CATALINA_OPTS -Denv=prod -Dauth=activeDirectory -Dconfig=/home/urlaub/config/my.properties</pre>

### Produktivumgebung aktivieren

Damit die Anwendung in der Produktivumgebung gestartet wird, muss man in den `CATALINA_OPTS` die System-Property `env`
auf `prod` setzen:

<pre>export CATALINA_OPTS=$CATALINA_OPTS -Denv=prod</pre>

**Hinweis:**

Die Anwendung verfügt über drei verschiedene Environment-Möglichkeiten:

* `dev` nutzt eine H2-Datenbank, legt Testdaten an, nutzt als Mail-Sender einen Dummy (verschickt also keine E-Mails)
* `test` nutzt eine MySQL-Datenbank, legt keine Testdaten an, nutzt als Mail-Sender einen Dummy (verschickt also keine E-Mails)
* `prod` nutzt eine MySQL-Datenbank, legt keine Testdaten an, nutzt den Java-Mail-Sender von
[Spring](http://www.springsource.org/) (kann also E-Mails verschicken)

### Authentifizierung

Es kann gewählt werden zwischen Authentifizierung mittels LDAP und Authentifizierung mittels Active Directory.

**Authentifizierung via LDAP:**

Voraussetzung: Es müssen die LDAP URL, die LDAP Base und LDAP User DN Patterns konfiguriert sein, damit eine Authentifizierung
via LDAP möglich ist.

Die Anwendung mit dem Parameter `-Dauth=ldap` starten:

<pre>export CATALINA_OPTS=$CATALINA_OPTS -Denv=prod -Dauth=ldap</pre>

**Authentifizierung via Active Directory:**

Voraussetzung: Es müssen die Active Directory Domain und LDAP URL konfiguriert sein, damit eine Authentifizierung via
Active Directory möglich ist.

Die Anwendung mit dem Parameter `-Dauth=activeDirectory` starten:

<pre>export CATALINA_OPTS=$CATALINA_OPTS -Denv=prod -Dauth=activeDirectory</pre>

## Getting started

Wenn die Anwendung im Tomcat beispielsweise im Verzeichnis `/home/user/tomcat/webapps/urlaubsverwaltung.war` deployed
wurde, ist sie erreichbar unter `<servername>:8080/urlaubsverwaltung`.

Der erste Benutzer, der sich erfolgreich im System einloggt, wird in der Urlaubsverwaltung mit der Rolle Office angelegt.
Dies ermöglicht Benutzer- und Rechteverwaltung innerhalb der Anwendung und das Pflegen der Einstellungen für die Anwendung.

# Entwicklung

Im Folgenden werden die durchzuführenden Schritte beschrieben, wenn man an der Urlaubsverwaltung entwickeln möchte.

### Repository clonen

<pre>git clone git@github.com:synyx/urlaubsverwaltung.git</pre>

### Anwendung starten

Man kann die Anwendung lokal mit dem Maven Jetty Plugin starten.
Ohne weitere Angabe wird das Development-Environment genutzt, d.h. es wird eine H2-Datenbank verwendet und es werden
keine E-Mails versendet.

<pre>mvn jetty:run</pre>

Im Development-Environment werden für Entwicklungszwecke Benutzer, Urlaubsanträge und Krankmeldungen angelegt.
Man kann sich in dieser Umgebung ebenfalls mit dem Testbenutzer `test/secret` anmelden.

Im Browser lässt sich die Anwendung dann über `http://localhost:8080/` ansteuern.

### API

Die Urlaubsverwaltung verfügt über eine API, die unter `http://localhost:8080/api` erreichbar ist.

### Environments

Die Anwendung verfügt über drei verschiedene Environment-Möglichkeiten:

* `dev` nutzt eine H2-Datenbank, legt Testdaten an, nutzt als Mail-Sender einen Dummy (verschickt also keine E-Mails)
* `test` nutzt eine MySQL-Datenbank, legt keine Testdaten an, nutzt als Mail-Sender einen Dummy (verschickt also keine E-Mails)
* `prod` nutzt eine MySQL-Datenbank, legt keine Testdaten an, nutzt den Java-Mail-Sender von
[Spring](http://www.springsource.org/) (kann also E-Mails verschicken)

Standardmäßig ohne jegliche Angabe wird als Environment `dev` genutzt.
Möchte man ein anderes Environment nutzen, muss man beim Starten des Maven Jetty Plugins die `env` Property mitgeben, z.B.:

<pre>mvn jetty:run -Denv=test</pre>

### Authentifizierung

Es gibt drei mögliche Authentifizierungsmethoden:

* Authentifizierung für lokale Entwicklungsumgebung
* Authentifizierung via LDAP
* Authentifizierung via Active Directory

**Authentifizierung für lokale Entwicklungsumgebung:**

Möchte man die Anwendung lokal mit generierten Testdaten bei sich laufen lassen, reicht es folgenden
Befehl auszuführen:

<pre>mvn jetty:run</pre>

Man kann man sich nun mit verschiedenen Testbenutzern anmelden:

* `testUser/secret`: Benutzer mit der User Rolle
* `testBoss/secret`: Benutzer mit der Boss Rolle
* `test/secret`: Benutzer mit der Office Rolle

**Authentifizierung via LDAP:**

Es müssen die LDAP URL, die LDAP Base und LDAP User DN Patterns konfiguriert sein, damit eine Authentifizierung via
LDAP möglich ist.

Die Anwendung mit dem Parameter `-Dauth=ldap` starten:

<pre>mvn jetty:run -Dauth=ldap</pre>

**Authentifizierung via Active Directory:**

Es müssen die Active Directory Domain und LDAP URL konfiguriert sein, damit eine Authentifizierung via Active Directory
möglich ist.

Die Anwendung mit dem Parameter `-Dauth=activeDirectory` starten:

<pre>mvn jetty:run -Dauth=activeDirectory</pre>

**Kombination:**

Selbstverständlich können beide Properties gleichzeitig gesetzt werden, zum Beispiel:

<pre>mvn jetty:run -Denv=test -Dauth=ldap</pre>

# Hinweise zu Versionen

## Version 2.2.1

Wenn man die Urlaubsverwaltung schon länger nutzt und auf Version 2.2.1 oder höher updaten möchte, muss sichergestellt
sein, dass in der Datenbank keine Person mit gleichem Vor- und Nachnamen existiert. Dies führt ansonsten zu einem
Problem beim Update des Datenbankschemas und die Anwendung kann nicht starten.

## Version 2.7.0

Die Anwendung hat nicht mehr mehrere unterschiedliche Properties-Dateien, sondern je eine `application.properties` pro Umgebung.
Außerdem heißt die System Property für die Authentifizierungsmethode nicht mehr `spring.profiles.active`, sondern `auth`.
Die fachlichen Einstellungen werden nicht mehr in einer Properties-Datei gepflegt, sondern innerhalb der Anwendung selbst unter dem
Menüpunkt "Einstellungen".

# Technologien

Die Anwendung basiert auf dem [Spring](http://www.springsource.org/) MVC Framework.
Zur Ermittlung von Feiertagen wird das Framework [Jollyday](http://jollyday.sourceforge.net/) benutzt.
Das Frontend beinhaltet Elemente von [Bootstrap](http://getbootstrap.com/) gewürzt mit einer Prise
[jQuery](http://jquery.com/) und [Font Awesome](http://fontawesome.io/).
Für die Darstellung der Benutzer Avatare wird [Gravatar](http://de.gravatar.com/) benutzt.

# Lizenz

[synyx/urlaubsverwaltung](http://github.com/synyx/urlaubsverwaltung) is licensed under the
[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

Alle Logos, Marken- und Warenzeichen unterliegen **nicht** der Apache License 2.0 und dürfen nur mit schriftlicher
Genehmigung von [synyx](http://www.synyx.de/) weiterverwendet werden.
