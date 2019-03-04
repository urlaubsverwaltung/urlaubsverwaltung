## Installation der Urlaubsverwaltung bis Version 2.11.2

* [Installation](#installation)
* [Konfiguration](#konfiguration)
    * [Umgebungen](#umgebungen)
    * [Authentifizierung](#authentifizierung)
    * [Konfiguration ab Version 2.7.0](#konfiguration-ab-version-270)
    * [Konfiguration bis Version 2.6.4](#konfiguration-bis-version-264)

Die folgende Anleitung beschreibt die Installation der Urlaubsverwaltung auf einem
[Tomcat Server](http://tomcat.apache.org/).

## ACHTUNG!

Ab Version 2.12.0 läuft die Urlaubsverwaltung als [Spring Boot](http://projects.spring.io/spring-boot/) Anwendung.
Die folgende Anleitung gilt nur für Versionen < 2.12.0.

---

## Installation

#### Systemvoraussetzungen

* Apache Tomcat Version 7
* JDK 8
* MySQL Datenbank

#### Download

Die Anwendung steht auf Github bereits als deploybare WAR-Datei zum Download zur Verfügung.
Einfach die WAR-Datei der letzten Version
[hier](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.11.2) downloaden.


#### Deployment unter Tomcat

Die heruntergeladene WAR-Datei kann nun im installierten Tomcat Server deployed werden.
Dazu kopiert man die WAR-Datei in das Tomcat Verzeichnis `/webapps` und benennt sie in `urlaubsverwaltung.war` um
([weiterführende Informationen zum Tomcat Deployment](http://tomcat.apache.org/tomcat-6.0-doc/deployer-howto.html)).

#### Starten der Anwendung

Damit man die Anwendung möglichst schnell ausprobieren kann, bietet es sich an die Anwendung in der Umgebung
Entwicklung (`env=dev`) zu starten.

Zusätzlich wird die standardmäßige Authentifizierung (`auth=default`) angegeben.

Hierzu wird `CATALINA_OPTS` folgendermaßen gesetzt:

<pre>export CATALINA_OPTS="-Denv=dev -Dauth=default"</pre>

#### Aufrufen der Anwendung

Wenn die Anwendung im Tomcat Verzeichnis `webapps/` mit dem Dateinamen `urlaubsverwaltung.war` deployed wurde,
ist sie nach dem Starten des Tomcat unter

`<servername>:8080/urlaubsverwaltung`

erreichbar.

Der erste Benutzer, der sich erfolgreich im System einloggt, wird in der Urlaubsverwaltung mit der Rolle Office angelegt.
Dies ermöglicht Benutzer- und Rechteverwaltung innerhalb der Anwendung und das Pflegen der Einstellungen für die
Anwendung.

---

## Konfiguration

#### Umgebungen

Die Anwendung verfügt über **drei** verschiedene Umgebungsmöglichkeiten:

* `dev`
    * zum lokalen Entwickeln
    * nutzt eine H2-Datenbank
    * legt Testdaten an
* `test`
    * zum Testen der Anwendung
    * nutzt eine MySQL-Datenbank
    * legt keine Testdaten an
* `prod`
    * zum Ausführen der produktiven Anwendung
    * nutzt eine MySQL-Datenbank
    * legt keine Testdaten an

##### Umgebung aktivieren

Damit die Anwendung in einer Umgebung gestartet wird, muss man in den `CATALINA_OPTS` die System-Property `env` auf
`dev`, `test` oder `prod` setzen:

<pre>export CATALINA_OPTS="$CATALINA_OPTS -Denv=UMGEBUNG"</pre>

#### Authentifizierung

Die Anwendung verfügt über **drei** verschiedene Authentifizierungsmöglichkeiten:

* `default`
    * für lokale Entwicklungsumgebung
* `ldap`
    * Authentifizierung via LDAP
    * Es müssen die LDAP URL, die LDAP Base und LDAP User DN Patterns konfiguriert sein, damit eine Authentifizierung
    via LDAP möglich ist.
* `activeDirectory`
    * Authentifizierung via Active Directory
    * Es müssen die Active Directory Domain und LDAP URL konfiguriert sein, damit eine Authentifizierung via Active
    Directory möglich ist.

#### Konfiguration ab Version 2.7.0

##### Überschreiben der Properties

Die Anwendung besitzt im Verzeichnis `src/main/resources` jeweils eine `.properties` Datei zur Konfiguration der
jeweiligen [Umgebung](#umgebungen).

* eine standardmäßige `application.properties`
* `application-test.properties`
* `application-prod.properties`

Diese beinhalten gewisse Grundeinstellungen und Standardwerte. Diese allein reichen für die Produktivnahme der Anwendung
allerdings noch nicht aus. Spezifische Konfigurationen wie z.B. die Datenbank Einstellungen müssen durch eine eigene
Properties-Datei hinterlegt werden. Welche Konfigurationen überschrieben werden können/müssen, sind in der
standardmäßigen `application.properties` des Projekts einsehbar.

Damit die eigene Konfigurationsdatei benutzt wird, gibt es zwei Möglichkeiten:

* Eine eigene Properties-Datei erstellen und diese unter dem Namen `urlaubsverwaltung.properties` in das `/conf`
Verzeichnis unterhalb von `CATALINA_BASE` ablegen, z.B. `tomcat/conf/urlaubsverwaltung.properties`. Danach den Tomcat
neu starten.

* Die erstellte Properties-Datei an einem beliebigen Ort ablegen und mithilfe der System Property `config` innerhalb
der `CATALINA_OPTS` den absoluten Pfad der Konfigurationsdatei angeben:

<pre>export CATALINA_OPTS="$CATALINA_OPTS -Dconfig=/home/urlaub/config/my.properties"</pre>

##### Produktivumgebung aktivieren

Die Anwendung mit dem Parameter `-Denv=prod` starten:

<pre>export CATALINA_OPTS="$CATALINA_OPTS -Denv=prod"</pre>

###### LDAP

Um LDAP zur Authentifizierung zu nutzen, zusätzlich den Parameter `-Dauth=ldap` angeben:

<pre>export CATALINA_OPTS="$CATALINA_OPTS -Denv=prod -Dauth=ldap"</pre>

###### Active Directory

Um Active Directory zur Authentifizierung zu nutzen, zusätzlich den Parameter `-Dauth=activeDirectory` angeben:

<pre>export CATALINA_OPTS="$CATALINA_OPTS -Denv=prod -Dauth=activeDirectory"</pre>

#### Konfiguration bis Version 2.6.4

##### Überschreiben der Properties

Die Anwendung besitzt im Verzeichnis `src/main/resources` mehrere Properties Dateien, die zur Konfiguration genutzt
werden.

* `config.properties` u.a. Konfiguration von LDAP
* `db.properties` Konfiguration der Datenbank (im `prod` Environment wird die Datei `db.prod.properties` herangezogen)
* `mail.properties` Konfiguration zum Mailversand (im `prod` Environment wird die Datei `mail.prod.properties` herangezogen)
* `business.properties` Individuelle Regelungen wie bspw. maximal möglicher Jahresurlaub

Die vorhandenen Properties können entweder vor dem Erstellen der WAR-Datei direkt innerhalb der oben genannten Dateien
angepasst werden oder aber über durch Setzen der dort definierten globalen Variablen.

###### Beispiel:

<pre>export UV_DB_URL=jdbc:mysql://127.0.0.1:3306/urlaub</pre>

##### Produktivumgebung aktivieren

Die Anwendung mit dem Parameter `-Denv=prod` starten:

<pre>export CATALINA_OPTS="$CATALINA_OPTS -Denv=prod"</pre>

##### Authentifizierung

###### LDAP

Die Anwendung mit dem Parameter `-Dspring.profiles.active=ldap` starten:

<pre>export CATALINA_OPTS="$CATALINA_OPTS -Dspring.profiles.active=ldap"</pre>

###### Active Directory

Die Anwendung mit dem Parameter `-Dspring.profiles.active=activeDirectory` starten:

<pre>export CATALINA_OPTS="$CATALINA_OPTS -Dspring.profiles.active=activeDirectory"</pre>
