# Urlaubsverwaltung [![Build Status](https://github.com/synyx/urlaubsverwaltung/workflows/Urlaubsverwaltung%20CI/badge.svg)](https://github.com/synyx/urlaubsverwaltung/actions?query=workflow%3A%22Urlaubsverwaltung+CI%22) [![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=org.synyx:urlaubsverwaltung&metric=coverage)](https://sonarcloud.io/dashboard?id=org.synyx:urlaubsverwaltung) [![Docker Pulls](https://img.shields.io/docker/pulls/synyx/urlaubsverwaltung.svg)](https://hub.docker.com/r/synyx/urlaubsverwaltung) [![Total Downloads](https://img.shields.io/github/downloads/synyx/urlaubsverwaltung/total.svg)](https://github.com/synyx/urlaubsverwaltung/releases/latest)

Die Urlaubsverwaltung ist eine Web-Anwendung, um *Abwesenheiten* elektronisch verwalten zu können.

Anhand von **Urlaubsanträgen** kann ein Mitarbeiter eine Anfrage stellen, die von den jeweils berechtigten Personen genehmigt,
abgelehnt oder storniert werden kann. Jeder Mitarbeiter kann seine **Überstunden** pflegen, um immer den Überblick
zu behalten und falls doch mal eine Person ausfallen sollte, so kann die **Krankmeldung** direkt gepflegt werden.

Wenn du mehr Informationen und Bilder über dieses Projekt sehen möchtest dann schaue auf unserer [Landingpage] vorbei.

#### End-Of-Life: Version 3.x

| Version 3.x wird nur noch bis zum **31.12.2020** mit Sicherheitsupdates unterstützt.|
| --- |

Du bist auf der Suche nach Version 3.x? Diese findest du diese auf dem [v3.x branch](https://github.com/synyx/urlaubsverwaltung/tree/v3.x).
Wenn du wissen möchtest, was alles zu tun ist, um von 3.x auf 4.x umzusteigen?
Dann wirf einen Blick in den [Migration Guide](https://github.com/synyx/urlaubsverwaltung/wiki/Urlaubsverwaltung-4.0-Migration-Guide).

* [Demo-System](#demo-system)
* [FAQ](#faq)
* [Berechtigungen](#berechtigungen)
* [Betrieb](#betrieb)
  * [Konfiguration](#konfiguration)
* [Demodaten Modus](#demodaten-modus)
* [Entwicklung](#entwicklung)


## Demo-System

Möchtest du die Urlaubsverwaltung **ohne** eine langwierige **Registrierung** ausprobieren?  
Dann steige über unsere [Landingpage] direkt in das [Demo-System](https://urlaubsverwaltung.cloud/demo) ein.


## FAQ

Für Fragen, die bei der Benutzung der Urlaubsverwaltung aufkommen, gibt es ein [FAQ](https://github.com/synyx/urlaubsverwaltung/wiki/FAQ).  
Sollte dieser Fragenkatalog nicht weiterhelfen, kannst du gerne [ein neues Issue](https://github.com/synyx/urlaubsverwaltung/issues/new/choose)
vom Typ "Question" erstellen.


## Berechtigungen

In der Urlaubsverwaltung gibt es aktuell folgende Arten von Berechtigungen:

* **inaktiv**: hat keinen Zugang mehr zur Urlaubsverwaltung (Daten des Benutzers bleiben zur Archivierung bestehen)
* **User**: darf Urlaub für sich selbst beantragen
* **Abteilungsleiter**: darf Urlaubsanträge für die Benutzer seiner Abteilungen einsehen, genehmigen und ablehnen
* **Freigabe-Verantwortlicher**: ist bei der zweistufigen Genehmigung von Anträgen verantwortlich für die endgültige Freigabe
* **Chef**: darf Urlaubsanträge aller Benutzer einsehen, genehmigen und ablehnen
* **Office**: darf Einstellungen zur Anwendung vornehmen, Mitarbeiter verwalten, Urlaub für Mitarbeiter
beantragen/stornieren und Krankmeldungen pflegen
* **Admin**: Keine fachliche Rolle sondern nur für den Zugriff von Management Schnittstellen ([Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html)).

Eine aktive Person kann eine oder mehrere Rollen innehaben.
  
---
  
## Betrieb

### Voraussetzungen

* [JDK 11](https://openjdk.java.net/install/)
* [MariaDB Datenbank (v10.5)](https://mariadb.org/)
* [Security Provider](#security-provider-konfigurieren)

### Download

Die Anwendung steht als
* [Web Application Archive (.war)](https://github.com/synyx/urlaubsverwaltung/releases/latest)
* [Docker Image](https://hub.docker.com/r/synyx/urlaubsverwaltung)

zur Verfügung.


#### Installation .war Variante

* [Konfiguration Datenbank](#datenbank-konfigurieren)
* [Konfiguration Security Provider](#security-provider-konfigurieren)
* Lege ein Verzeichnis für die Urlaubsverwaltung an (z.B. `/opt/urlaubsverwaltung`). Kopiere die .war-Datei dorthin.
* Erstelle in dem Verzeichnis eine Konfigurationsdatei namens `application.properties`, welche die Konfiguration für
die Urlaubsverwaltung enthält und die Standardwerte überschreibt.
 Die vollständigen Konfigurationsoptionen sind unter [Konfiguration](#konfiguration) dokumentiert.
  
Nach der [Konfiguration](#konfiguration) lässt sich die Urlaubsverwaltung starten.

```bash
java -jar urlaubsverwaltung.war
``` 

Falls es Probleme beim Starten der Anwendung gibt, ist es hilfreich das [Logging der Anwendung](#logging-konfigurieren)
zu konfigurieren, damit erhält man mehr Informationen über den Fehlerzustand.


#### Docker Variante

Alle Informationen zum Betrieb mit unserem Docker Image sind im Ordner [.example](.examples) zu finden.


### Konfiguration

Die Anwendung besitzt im Verzeichnis `src/main/resources` eine [Konfigurationsdatei](https://github.com/synyx/urlaubsverwaltung/blob/master/src/main/resources/application.properties).
Diese beinhaltet gewisse Grundeinstellungen und Standardwerte. Diese allein reichen für die Produktivnahme der
Anwendung allerdings nicht aus. Spezifische Konfigurationen wie z.B. die [Datenbank Einstellungen](#datenbank-konfigurieren)
und [Security Provider](#security-provider-konfigurieren) müssen in einer eigenen Konfigurationsdatei hinterlegt werden.

Welche Möglichkeiten es bei Spring Boot gibt, damit die eigene Konfigurationsdatei genutzt wird, kann in der
['External Config' Reference](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html#boot-features-external-config-application-property-files)
nachgelesen werden.

Nachstehend alle spezifischen Konfigurationsmöglichkeiten der Urlaubsverwaltung mit ihren Standartwerten.

```properties
# account
uv.account.default-vacation-days=20
uv.account.update.cron=0 0 5 1 1 *

# application
uv.application.reminder-notification.cron=0 0 7 * * *

# ical calendar
uv.calendar.organizer
uv.calendar.refresh-interval=P1D

# development
uv.development.demodata.create=false
uv.development.demodata.additional-active-user=0
uv.development.demodata.additional-inactive-user=0

# mail
uv.mail.administrator
uv.mail.application-url
uv.mail.sender

# security
uv.security.auth=default

uv.security.directory-service.identifier
uv.security.directory-service.last-name
uv.security.directory-service.first-name
uv.security.directory-service.mail-address
uv.security.directory-service.sync.cron=0 0 1 * * ?
uv.security.directory-service.filter.member-of
uv.security.directory-service.filter.object-class=person

## active directory
uv.security.directory-service.active-directory.url=ldap://ad.example.org/
uv.security.directory-service.active-directory.domain=example.org
uv.security.directory-service.active-directory.searchFilter=
uv.security.directory-service.active-directory.sync.enabled=false
uv.security.directory-service.active-directory.sync.password=password
uv.security.directory-service.active-directory.sync.user-dn=cn=Administrator,cn=users,dc=example,dc=org
uv.security.directory-service.active-directory.sync.user-search-base=dc=example,dc=org

## ldap
uv.security.directory-service.ldap.url=ldap://ldap.example.org/
uv.security.directory-service.ldap.base=dc=example,dc=org
uv.security.directory-service.ldap.manager-dn
uv.security.directory-service.ldap.manager-password
uv.security.directory-service.ldap.user-search-filter=(uid={0})
uv.security.directory-service.ldap.user-search-base=ou=accounts
uv.security.directory-service.ldap.sync.enabled=false
uv.security.directory-service.ldap.sync.password=password
uv.security.directory-service.ldap.sync.user-dn=uid=username,ou=other,ou=accounts,dc=example,dc=org
uv.security.directory-service.ldap.sync.user-search-base=ou=people,ou=accounts

# oidc (openid connect)
uv.security.oidc.client-id
uv.security.oidc.client-secret
uv.security.oidc.issuer-uri
uv.security.oidc.logout-uri

# jsp template engine
uv.template-engine.jsp.use-precompiled=false

# sick-note
uv.sick-note.end-of-pay-notification.cron=0 0 6 * * *

# workingtime
# (monday till friday)
uv.workingtime.default-working-days[0]=1
uv.workingtime.default-working-days[1]=2
uv.workingtime.default-working-days[2]=3
uv.workingtime.default-working-days[3]=4
uv.workingtime.default-working-days[4]=5
```


#### Security Provider konfigurieren

Die Anwendung verfügt über **vier** verschiedene Authentifizierungsmöglichkeiten:

* `default`
    * für lokalen Entwicklungsmodus und [Demodaten-Modus](#demodaten-modus)
* `ldap`
    * Authentifizierung via LDAP
    * Es müssen die LDAP URL, die LDAP Base und LDAP User DN Patterns
      konfiguriert sein, damit eine Authentifizierung via LDAP möglich ist.
    * Wenn ldaps verwendet werden soll, dann muss die url
      `uv.security.directory-service.ldap.url=ldaps://oc.example.org`
      angepasst und am LDAP Server der entsprechende Port freigegeben werden.
* `activedirectory`
    * Authentifizierung via Active Directory
    * Es müssen die Active Directory Domain und LDAP URL konfiguriert
      sein, damit eine Authentifizierung via Active Directory möglich ist.
* `oidc`
    * Authentifizierung via OpenID Connect (OIDC)
    * Es müssen die OIDC issuerUri sowie die client id/secret definiert werden.
      Außerdem müssen bei dem gewählten OIDC Provider die 'Allowed Logout URLs',
      die 'Allowed Callback URLs' und ggf. weitere Einstellungen vorgenommen werden.

Der erste Benutzer, welcher sich erfolgreich bei der Urlaubsverwaltung anmeldet, wird mit der Rolle `Office` angelegt.
Dies ermöglicht Benutzer- und Rechteverwaltung und das Pflegen der Einstellungen innerhalb der Anwendung.

Der Authentifizierungsmodus muss über die Property `uv.security.auth` in der eigenen Konfigurationsdatei gesetzt werden.


#### Datenbank konfigurieren

Die Anwendung verwendet zur Speicherung der Daten ein MariaDB-Datenbankmanagementsystem. 
Erstelle in deinem MariaDB-Datenbankmanagementsystem eine Datenbank mit z.B. dem Namen `urlaubsverwaltung`
sowie einen Benutzer mit Zugriffsrechten für diese Datenbank und konfiguriere diese

```properties
spring.datasource.url=jdbc:mariadb://$HOST:$PORT/$NAME_DER_DATENBANK
spring.datasource.username=$BENUTZER
spring.datasource.password=$PASSWORT
```
Wenn Sie die Urlaubsverwaltung das erste Mal starten, werden automatisch alle Datenbanktabellen angelegt.


#### E-Mail-Server konfigurieren

Um den E-Mail-Server zu konfigurieren müssen folgende Konfigurationen vorgenommen werden.

```properties
uv.mail.sender=absender@example.org         # Absender der E-Mails
uv.mail.administrator=admin@example.org     # E-Mail-Adresse des Administrators
uv.mail.application-url=https://example.org # Diese URL wird in den E-Mails zur Link-Generierung verwendet

spring.mail.host=$HOST
spring.mail.port=$PORT
spring.mail.username=$USERNAME
spring.mail.password=$PASSWORT
```

Alle weiteren `spring.mail.*` Konfigurationen können in der [Spring Dokumentation](https://docs.spring.io/spring-boot/docs/2.1.x/reference/html/boot-features-email.html)
eingesehen werden.

#### Benutzer-Synchronisation konfigurieren

Seit der Version 2.14 werden die LDAP/AD-Benutzer nicht mehr automatisch in die Urlaubsverwaltung synchronisiert,
sondern nur noch beim Login des jeweiligen Users in die Datenbank übertragen.
Man kann die automatische Synchronisation aller Benutzer aktivieren, indem der Konfigurationsparameter
`uv.security.directory-service.ldap.sync.enabled` bzw. `uv.security.directory-service.active-directory.sync.enabled`
 auf `true` gesetzt wird.


#### Logging konfigurieren

Sollten beim Starten der Anwendung Probleme auftreten, lässt sich in der Konfigurationsdatei eine
ausführliche Debug-Ausgabe konfigurieren, indem das `logging.level.*` pro Paket konfiguriert wird,

```properties
logging.level.org.synyx.urlaubsverwaltung=TRACE
logging.level.org.springframework.security=TRACE
```

sowie eine Logdatei

```properties
logging.file.name=logs/urlaubsverwaltung.log
```

geschrieben wird.


### Anwendung als Service

Da die Anwendung auf Spring Boot basiert, lässt sie sich sehr komfortabel als Service installieren. Wie genau dies
funktioniert, kann den entsprechenden Kapiteln der Spring Boot Dokumentation entnommen werden:

* [Linux Service](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html#deployment-service)
* [Windows Service](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html#deployment-windows)


---
  
## Demodaten-Modus

### Starten der Anwendung im Demodaten-Modus

Um die Anwendung möglichst schnell lokal ausprobieren zu können, bietet es sich an
die Datenbank via [Docker Compose](https://docs.docker.com/compose/overview/) zu starten:

```bash
docker-compose up
```

und die Anwendung mit dem Profil `demodata` zu starten:

```bash
java -jar -Dspring.profiles.active=demodata urlaubsverwaltung.war
```

Auf diese Weise wird die Anwendung mit einer MariaDB-Datenbankmanagementsystem gestartet und Demodaten generiert.

Die Demodaten enthalten folgende **Benutzer**:

| Rolle                            | Benutzername           | Passwort |
| -------------------------        | -------------          | -------- |
| User                             | user                   | secret   |
| User & Abteilungsleiter          | departmentHead         | secret   |
| User & Freigabe-Verantwortlicher | secondStageAuthority   | secret   |
| User & Chef                      | boss                   | secret   |
| User & Chef & Office             | office                 | secret   |
| User & Admin                     | admin                  | secret   |


Möchte man, dass beim Starten der Anwendung keine Demodaten generiert werden, muss die Konfiguration

`uv.development.demodata.create`

in den [application-demodata.properties](https://github.com/synyx/urlaubsverwaltung/blob/master/src/main/resources/application-demodata.properties)
auf `false` gesetzt werden.


### Aufrufen der Anwendung

Folgende Systeme sind erreichbar unter `localhost`

| Service                   | Port    |
| ------------------------- | ------- |
| [Urlaubsverwaltung](http://localhost:8080)         | 8080    |
| [Mailhog](http://localhost:8025)                   | 8025    |
| Mailhog SMTP              | 1025    |
  
---
  
## Entwicklung

Wenn du uns bei der **Entwicklung** der Urlaubsverwaltung **unterstützen** möchtest,
dann schau dir die [Contributing to Urlaubsverwaltung](./CONTRIBUTING.md) Referenz und die folgenden
Abschnitte an. Bei Fragen kannst du gerne [ein neues Issue](https://github.com/synyx/urlaubsverwaltung/issues/new/choose)
vom Typ "Question" erstellen.


### Voraussetzungen

* [JDK 11](https://openjdk.java.net/install/)
* [Docker 17.12.0+](https://docs.docker.com/get-docker/)
* [Docker Compose](https://docs.docker.com/compose/install/)


### Repository clonen

```bash
git clone git@github.com:synyx/urlaubsverwaltung.git
```

### git hooks (optional)

Zum Automatisieren verschiedener Dinge bietet dir das Projekt [git hooks](https://git-scm.com/book/uz/v2/Customizing-Git-Git-Hooks)
an. Diese kannst du mit folgendem Befehl installieren:

```bash
./scripts/install-git-hooks.sh
```

Folgende git hooks werden installiert:

* **post-merge**
  * schaut nach einen `pull` ob sich die `package.lock` geändert hat und installiert ggfs. npm dependencies

### Anwendung starten

Da die Urlaubsverwaltung abhängig von einer MariaDB-Datenbank ist, kann diese über

```bash
docker-compose up
```
gestartet werden.

Die Urlaubsverwaltung ist eine [Spring Boot](http://projects.spring.io/spring-boot/) Anwendung und kann mit dem Maven
Plugin gestartet werden. Es bietet sich an, die Anwendung mit dem Profil `demodata` zu starten, um Testdaten generieren
zu lassen:

```bash
./mvnw clean spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=demodata"
```

bzw. für Windows Benutzer über:

```bash
./mvnw.cmd clean spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=demodata"
```

### Anwendung nutzen

Im Browser lässt sich die Anwendung dann über [http://localhost:8080/](http://localhost:8080/) ansteuern.

Mit dem `demodata` Profil wird eine MariaDB-Datenbank verwendet und es werden Demodaten angelegt,
d.h. Benutzer, Urlaubsanträge und Krankmeldungen. Daher kann man sich in der Weboberfläche nun mit verschiedenen
[Demodaten-Benutzer](#demodaten-benutzer) anmelden.


### Frontend Entwicklung

Die User Experience einiger Seiten wird zur Laufzeit mit JavaScript weiter verbessert.

Assets sind in `<root>/src/main/javascript` zu finden

* `bundles` sind in den JSPs zu integrieren
* `components` sind einzelne Komponenten zur Wiederverwendung wie z. B. der _datepicker_
* `js` beinhaltet seitenspezifische Dinge
* `lib` sind third-party Bibliotheken

Der Frontend Build ist in Maven integriert. Isoliert können die Assets aber auch auf der Kommandozeile gebaut werden.

* `npm run build`
  * baut optimierte, minifizierte Assets
  * Info: der Dateiname beinhaltet einen Hash welcher eindeutig zum Inhalt des Assets passt 
* `npm run build:dev`
  * baut nicht minifizierte Assets
* `npm run build:watch`
  * baut automatisch nach dem Editieren von JavaScript / CSS Dateien neue Assets

#### Long term caching von Assets

Startet man den Maven Build oder baut man die Assets mit dem NPM Task `npm run build` wird eine JSON Datei `assets-manifest.json` angelegt.
Das Manifest beschreibt ein Mapping der bundles zum generierten Dateinamen inklusive Hash. Dieser gemappte Dateiname muss
in den JSPs integriert werden. Damit das nicht bei jeder Änderung manuell gemacht werden muss, kann der Dateiname mit Hilfe der
Taglib `AssetsHashResolverTag.java` zur Kompilierungszeit der JSP automatisiert werden.

```jsp
<%@taglib prefix="asset" uri = "/WEB-INF/asset.tld"%>

<script defer src="<asset:url value='npm.jquery.js' />"></script>
```

Während der Weiterentwicklung ist es sinnvoll das Caching zu deaktivieren. Wird das `demodata` Profil verwendet muss
nichts weiter getan werden. Verwendest du das Profil nicht, kannst du das Caching mit folgenden application Properties
deaktivieren:

```properties
spring.web.resources.chain.cache=false
spring.web.resources.cache.cachecontrol.max-age=0
spring.web.resources.chain.strategy.content.enabled=false
```


### API

Die Urlaubsverwaltung verfügt über eine API, die unter [http://localhost:8080/api](http://localhost:8080/api) erreichbar ist.


### Release

Für ein Release wird das [maven-release-plugin](http://maven.apache.org/maven-release/maven-release-plugin/) verwendet. 
Zum sorgenfreien Erstellen eines Release kann folgendes Skript verwendet werden.

```bash
export RELEASE_VERSION=0.10.0
export NEW_VERSION=0.11.0-SNAPSHOT
./release.sh
git fetch
```

[Landingpage]: https://urlaubsverwaltung.cloud
