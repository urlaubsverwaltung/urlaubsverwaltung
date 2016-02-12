## Installation der Urlaubsverwaltung ab Version 2.12.0

* [Installation](#installation)
* [Konfiguration](#konfiguration)
    * [Konfigurationsdatei](#konfigurationsdatei)
    * [Datenbank](#datenbank)
    * [Hinweis](#hinweis)
    * [Authentifizierung](#authentifizierung)

Die folgende Anleitung beschreibt die Installation der Urlaubsverwaltung als
[Spring Boot](http://projects.spring.io/spring-boot/) Anwendung.

---

## Installation

#### Systemvoraussetzungen

* JDK 8
* MySQL Datenbank

#### Download

Die Anwendung steht auf Github bereits als deploybare JAR-Datei zum Download zur Verfügung.
Einfach die JAR-Datei der aktuellsten Version [hier](https://github.com/synyx/urlaubsverwaltung/releases/latest)
downloaden.

#### Starten der Anwendung

Damit man die Anwendung möglichst schnell ausprobieren kann, bietet es sich an die Anwendung im Entwicklungsmodus
zu starten:

<pre>java -jar urlaubsverwaltung.jar -Dspring.profiles.active=dev</pre>

Auf diese Weise wird die Anwendung mit einer In-Memory-Datenbank und Testdaten gestartet.

#### Aufrufen der Anwendung

Die Anwendung ist nun erreichbar unter

`<servername>:8080/urlaubsverwaltung`

#### Anwendung als Service

Da die Anwendung auf Spring Boot basiert, lässt sie sich sehr komfortabel als Service installieren. Wie genau dies
funktioniert, kann den entsprechenden Kapiteln in der Spring Boot Dokumentation nachgelesen werden:

* [Linux Service](http://docs.spring.io/spring-boot/docs/1.3.1.RELEASE/reference/html/deployment-install.html#deployment-service)
* [Windows Service](http://docs.spring.io/spring-boot/docs/1.3.1.RELEASE/reference/html/deployment-windows.html)

---

## Konfiguration

#### Konfigurationsdatei

Die Anwendung besitzt im Verzeichnis `src/main/resources` eine `application.properties` Datei zur Konfiguration.
Diese beinhaltet gewisse Grundeinstellungen und Standardwerte. Diese allein reichen für die Produktivnahme der
Anwendung allerdings noch nicht aus. Spezifische Konfigurationen wie z.B. die Datenbank Einstellungen müssen durch eine
eigene Properties-Datei hinterlegt werden. Welche Konfigurationen überschrieben werden können/müssen, sind in der
[`application-example.properties`](https://raw.githubusercontent.com/synyx/urlaubsverwaltung/master/src/main/resources/example.properties)
des Projekts einsehbar. Diese kann einfach als Grundlage genommen werden, um eine eigene Konfigurationsdatei zu
erstellen.

Welche Möglichkeiten es bei Spring Boot gibt, damit die eigene Konfigurationsdatei genutzt wird, kann
[hier](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html#boot-features-external-config-application-property-files)
nachgelesen werden.

#### Datenbank

Hinweis: Die in der Konfigurationsdatei konfigurierte Datenbank muss existieren.

#### Hinweis

Wenn eine eigene Konfigurationsdatei hinterlegt ist, darf die Anwendung natürlich **nicht** mehr im Entwicklungsmodus
gestartet werden, d.h. die Anwendung muss ohne `-Dspring.profiles.active=dev` gestartet werden:

<pre>java -jar urlaubsverwaltung.jar</pre>

#### Authentifizierung

Die Anwendung verfügt über **drei** verschiedene Authentifizierungsmöglichkeiten:

* `default`
    * für lokalen Entwicklungsmodus
* `ldap`
    * Authentifizierung via LDAP
    * Es müssen die LDAP URL, die LDAP Base und LDAP User DN Patterns konfiguriert sein, damit eine Authentifizierung via LDAP möglich ist.
* `activeDirectory`
    * Authentifizierung via Active Directory
    * Es müssen die Active Directory Domain und LDAP URL konfiguriert sein, damit eine Authentifizierung via Active Directory möglich ist.

Der erste Benutzer, der sich erfolgreich im System einloggt, wird in der Urlaubsverwaltung mit der Rolle Office angelegt.
Dies ermöglicht Benutzer- und Rechteverwaltung innerhalb der Anwendung und das Pflegen der Einstellungen für die Anwendung.

##### LDAP

Um LDAP zur Authentifizierung zu nutzen, muss die Property `auth` in der eigenen Konfigurationsdatei auf `ldap` gesetzt
werden:

<pre>auth=ldap</pre>

##### Active Directory

Um Active Directory zur Authentifizierung zu nutzen, muss die Property `auth` in der eigenen Konfigurationsdatei auf
`activeDirectory` gesetzt werden:

<pre>auth=activeDirectory</pre>
