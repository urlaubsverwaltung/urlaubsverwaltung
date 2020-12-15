# Urlaubsverwaltung [![Build](https://github.com/urlaubsverwaltung/urlaubsverwaltung/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/urlaubsverwaltung/urlaubsverwaltung/actions/workflows/build.yml) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=urlaubsverwaltung_urlaubsverwaltung&metric=coverage)](https://sonarcloud.io/summary/new_code?id=urlaubsverwaltung_urlaubsverwaltung) [![Docker Pulls](https://badgen.net/docker/pulls/synyx/urlaubsverwaltung?icon=docker&label=pulls)](https://hub.docker.com/r/urlaubsverwaltung/urlaubsverwaltung/) [![Crowdin](https://badges.crowdin.net/urlaubsverwaltung/localized.svg)](https://crowdin.com/project/urlaubsverwaltung)

Die Urlaubsverwaltung ist eine Web-Anwendung, um *Abwesenheiten* elektronisch verwalten zu können.

Anhand von **Urlaubsanträgen** kann ein Mitarbeiter eine Anfrage stellen, die von den jeweils berechtigten Personen genehmigt,
abgelehnt oder storniert werden kann. Jeder Mitarbeiter kann seine **Überstunden** pflegen, um immer den Überblick
zu behalten und falls doch mal eine Person ausfallen sollte, so kann die **Krankmeldung** direkt gepflegt werden.

Wenn du mehr Informationen und Bilder über dieses Projekt sehen möchtest dann schaue auf unserer [Landingpage] vorbei.

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

Für Fragen, die bei der Benutzung der Urlaubsverwaltung aufkommen, gibt es ein [Hilfe](https://urlaubsverwaltung.cloud/hilfe/).  
Sollte dieser Fragenkatalog nicht weiterhelfen, kannst du gerne
[ein neue Q&A](https://github.com/urlaubsverwaltung/urlaubsverwaltung/discussions/new?category=q-a) erstellen.


## 🎉 Version 5.x 

Die nächste große Version der Urlaubsverwaltung 5.0.0 wird zeitnah mit den ersten Milestone zur Verfügung gestellt. In den [Milestones](https://github.com/urlaubsverwaltung/urlaubsverwaltung/milestones) werden wir größere Anpassungen an der Datenbank und den Security Providern vornehmen, sowie die weichen für die weitere Entwicklung der Urlaubsverwaltung stellen. Daher gibt es für den ein oder anderen nicht nur gute Nachrichten.

* Wir werden in der 5.x keine Unterstützung für MariaDB und MySQL anbieten und komplett auf PostgreSQL setzen. Einen Migrationspfad ist bereits im [Migration Guide](https://github.com/urlaubsverwaltung/urlaubsverwaltung/wiki/Urlaubsverwaltung-5.0-Migration-Guide) vorhanden.
* Wir werden die security provider LDAP und active directory entfernen und nur noch OIDC unterstützen. Hierzu haben wir auch eine Umfrage in [den Discussions](https://github.com/urlaubsverwaltung/urlaubsverwaltung/discussions/3616) aufgesetzt. Wir freuen uns über eine rege Teilnahme!

Dies sind bisher die größeren Maßnahmen für die 5.x - kleine Anpassungen findet ihr dann im [Migration Guide](https://github.com/urlaubsverwaltung/urlaubsverwaltung/wiki/Urlaubsverwaltung-5.0-Migration-Guide) 

## Berechtigungen

In der Urlaubsverwaltung gibt es aktuell folgende Arten von Berechtigungen:

* **inaktiv**: hat keinen Zugang mehr zur Urlaubsverwaltung (Daten des Benutzers bleiben zur Archivierung bestehen)
* **User**: darf Urlaub für sich selbst beantragen
* **Abteilungsleiter**: darf Urlaubsanträge für die Benutzer seiner Abteilungen einsehen, genehmigen und ablehnen
* **Freigabe-Verantwortlicher**: ist bei der zweistufigen Genehmigung von Anträgen verantwortlich für die endgültige Freigabe
* **Chef**: darf Urlaubsanträge aller Benutzer einsehen, genehmigen und ablehnen
* **Office**: darf Einstellungen zur Anwendung vornehmen, Mitarbeiter verwalten, Urlaub für Mitarbeiter
beantragen/stornieren und Krankmeldungen pflegen
* **Admin**: Keine fachliche Rolle, sondern nur für den Zugriff von Management Schnittstellen ([Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html)).

Eine aktive Person kann eine oder mehrere Rollen innehaben.
  
---
  
## Betrieb

### Voraussetzungen

* [JDK 17](https://adoptium.net)
* [MariaDB Datenbank (v10.6)](https://mariadb.org/)
* [Security Provider](#security-provider-konfigurieren)

### Download

Die Anwendung steht als
* [Java Archive (.jar)](https://github.com/urlaubsverwaltung/urlaubsverwaltung/releases/latest)
* [Docker Image](https://hub.docker.com/r/urlaubsverwaltung/urlaubsverwaltung)

zur Verfügung.


#### Installation .jar Variante

* [Konfiguration Datenbank](#datenbank-konfigurieren)
* [Konfiguration Security Provider](#security-provider-konfigurieren)
* Lege ein Verzeichnis für die Urlaubsverwaltung an (z.B. `/opt/urlaubsverwaltung`). Kopiere die .jar-Datei dorthin.
* Erstelle in dem Verzeichnis eine Konfigurationsdatei namens `application.properties`, welche die Konfiguration für
die Urlaubsverwaltung enthält und die Standardwerte überschreibt.
 Die vollständigen Konfigurationsoptionen sind unter [Konfiguration](#konfiguration) dokumentiert.
  
Nach der [Konfiguration](#konfiguration) lässt sich die Urlaubsverwaltung starten.

```bash
java -jar urlaubsverwaltung.jar
``` 

Falls es Probleme beim Starten der Anwendung gibt, ist es hilfreich das [Logging der Anwendung](#logging-konfigurieren)
zu konfigurieren, damit erhält man mehr Informationen über den Fehlerzustand.


#### Docker Variante

Alle Informationen zum Betrieb mit unserem Docker Image sind im Ordner [.example](.examples) zu finden.


### Konfiguration

Die Anwendung besitzt im Verzeichnis `src/main/resources` eine [Konfigurationsdatei](https://github.com/urlaubsverwaltung/urlaubsverwaltung/blob/main/src/main/resources/application.properties).
Diese beinhaltet gewisse Grundeinstellungen und Standardwerte. Diese allein reichen für die Produktivnahme der
Anwendung allerdings nicht aus. Spezifische Konfigurationen wie z.B. die [Datenbank Einstellungen](#datenbank-konfigurieren)
und [Security Provider](#security-provider-konfigurieren) müssen in einer eigenen Konfigurationsdatei hinterlegt werden.

Welche Möglichkeiten es bei Spring Boot gibt, damit die eigene Konfigurationsdatei genutzt wird, kann in der
['External Config' Reference](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html#boot-features-external-config-application-property-files)
nachgelesen werden.

Nachstehend alle spezifischen Konfigurationsmöglichkeiten der Urlaubsverwaltung mit ihren Standartwerten.

```properties
# account
uv.account.default-vacation-days=20 # deprecated - kann über 'Einstellungen' gesetzt werden wenn auf '-1' gesetzt
uv.account.update.cron=0 0 5 1 1 *

# application
uv.application.reminder-notification.cron=0 0 7 * * *
uv.application.upcoming-holiday-replacement-notification.cron=0 0 7 * * *
uv.application.upcoming-notification.cron=0 0 7 * * *

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
uv.mail.senderDisplayName=Urlaubsverwaltung

# security
uv.security.auth=default

uv.security.directory-service.identifier=sAMAccountName
uv.security.directory-service.last-name=givenName
uv.security.directory-service.first-name=sn
uv.security.directory-service.mail-address=mail
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
uv.security.oidc.scopes=openid,profile,email

# sick-note
uv.sick-note.end-of-pay-notification.cron=0 0 6 * * *

# workingtime - deprecated - kann über 'Einstellungen' gesetzt werden 
# wenn auf uv.workingtime.default-working-days[0]=-1 gesetzt
# (monday till friday)
uv.workingtime.default-working-days[0]=1
uv.workingtime.default-working-days[1]=2
uv.workingtime.default-working-days[2]=3
uv.workingtime.default-working-days[3]=4
uv.workingtime.default-working-days[4]=5
```


#### Security Provider konfigurieren

Die Anwendung verfügt über **vier** verschiedene Authentifizierungsmöglichkeiten:

* `oidc`
    * Authentifizierung via OpenID Connect (OIDC)
    * Es müssen die OIDC issuerUri sowie die client id/secret definiert werden.
      Außerdem müssen bei dem gewählten OIDC Provider die 'Allowed Logout URLs',
      die 'Allowed Callback URLs' und ggf. weitere Einstellungen vorgenommen werden.
    * Es wird erwartet, dass der OIDC Provider im Access Token folgende Attribute enthält: `given_name`, `family_name`, `email`.
      Die Urlaubsverwaltung fragt deswegen standardmäßig den OIDC Provider mit den Scopes `openid`,`profile` und `email` an.
      Sollten diese Scopes nicht passen, können sie mit dem Property `uv.security.oidc.scopes` überschrieben werden.
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
* `development`
    * für lokalen Entwicklungsmodus und [Demodaten-Modus](#demodaten-modus)
    
Der erste Benutzer, welcher sich erfolgreich bei der Urlaubsverwaltung anmeldet, wird mit der Rolle `Office` angelegt.
Dies ermöglicht Benutzer- und Rechteverwaltung und das Pflegen der Einstellungen innerhalb der Anwendung.

Der Authentifizierungsmodus muss über die Property `uv.security.auth` in der eigenen Konfigurationsdatei gesetzt werden.


#### Datenbank konfigurieren

Die Anwendung verwendet zur Speicherung der Daten ein MariaDB-Datenbankmanagementsystem. 
Erstelle in deinem MariaDB-Datenbankmanagementsystem eine Datenbank mit z.B. dem Namen `urlaubsverwaltung`.

```sql
CREATE DATABASE urlaubsverwaltung DEFAULT CHARACTER SET = utf8mb4 DEFAULT COLLATE = utf8mb4_unicode_ci;
```

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
uv.mail.senderDisplayName=Urlaubsverwaltung # Schönere Darstellung im Postfach
uv.mail.administrator=admin@example.org     # E-Mail-Adresse des Administrators
uv.mail.application-url=https://example.org # Diese URL wird in den E-Mails zur Link-Generierung verwendet

spring.mail.host=$HOST
spring.mail.port=$PORT
spring.mail.username=$USERNAME
spring.mail.password=$PASSWORT
```

Alle weiteren `spring.mail.*` Konfigurationen können in der [Spring Dokumentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#io.email)
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

#### Launchpad

Es kann ein Launchpad konfiguriert werden, welches einen Absprung zu anderen Anwendungen ermöglicht. 

```properties
launchpad.name-default-locale=de

launchpad.apps[0].url=https://example.org
launchpad.apps[0].name.de=Anwendung 1
launchpad.apps[0].name.en=App 1
launchpad.apps[0].icon=

launchpad.apps[1].url=https://example-2.org
launchpad.apps[1].name.de=Anwendung 2
launchpad.apps[1].name.en=App 2
launchpad.apps[1].icon=
```

| Property                        | Type     | Description                                                                                                                                                |
|---------------------------------|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| launchpad.name-default-locale   | Locale   | Standard Name der Anwendung wenn für ein Locale keine Übersetzung gefunden wird.                                                                           |
| launchpad.apps[x].url           | String   | URL der Anwendung.                                                                                                                                         |
| launchpad.apps[x].name.[locale] | String   | Name der Anwendung für ein Locale.                                                                                                                         |
| launchpad.apps[x].icon          | String   | URL eines Bildes oder ein base64 encodiertes Bild. Wird in das `<img src="" />` Attribut geschrieben.<br/>Das Bild sollte optimalerweise ein Quadrat sein. |

Das Launchpad hat eigene Übersetzungen. Spring muss entsprechend konfiguriert werden, damit die messages.properties gefunden wird:

```properties
spring.messages.basename=messages,launchpad-core
```

* **(required)** `messages` standardmäßige application messages properties
* **(required)** `launchpad-core` launchpad message properties

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
java -jar -Dspring.profiles.active=demodata urlaubsverwaltung.jar
```

Auf diese Weise wird die Anwendung mit einer MariaDB-Datenbankmanagementsystem gestartet und Demodaten generiert.

Die Demodaten enthalten folgende **Benutzer**, ein Passwort wird nicht benötigt:

| Benutzername         | Rolle                            |
|----------------------|----------------------------------|
| user                 | User                             |
| departmentHead       | User & Abteilungsleiter          |
| secondStageAuthority | User & Freigabe-Verantwortlicher |
| boss                 | User & Chef                      |
| office               | User & Office                    |
| admin                | User & Admin                     |

Möchte man, dass beim Starten der Anwendung keine Demodaten generiert werden, muss die Konfiguration

`uv.development.demodata.create`

in den [application-demodata.properties](https://github.com/urlaubsverwaltung/urlaubsverwaltung/blob/main/src/main/resources/application-demodata.properties)
auf `false` gesetzt werden.


### Aufrufen der Anwendung

Folgende Systeme sind erreichbar unter `localhost`

| Service                                    | Port |
|--------------------------------------------|------|
| [Urlaubsverwaltung](http://localhost:8080) | 8080 |
| [Mailhog](http://localhost:8025)           | 8025 |
| Mailhog SMTP                               | 1025 |
  
---
  
## Entwicklung

Wenn du uns bei der **Entwicklung** der Urlaubsverwaltung **unterstützen** möchtest,
dann schau dir die [Contributing to Urlaubsverwaltung](./CONTRIBUTING.md) Referenz und die folgenden
Abschnitte an. Bei Fragen kannst du gerne [ein neue Q&A](https://github.com/urlaubsverwaltung/urlaubsverwaltung/discussions/new?category=q-a) erstellen.

### Voraussetzungen

* [JDK 17](https://adoptium.net)
* [Docker 20.10.+](https://docs.docker.com/get-docker/)
* [Docker Compose](https://docs.docker.com/compose/install/)


### Repository clonen

Ohne GitHub Account

```bash
https://github.com/urlaubsverwaltung/urlaubsverwaltung.git
```

mit GitHub Account

```bash
git clone git@github.com:urlaubsverwaltung/urlaubsverwaltung.git
```

### git hooks (optional)

Zum Automatisieren verschiedener Dinge bietet dir das Projekt [git hooks](https://git-scm.com/book/uz/v2/Customizing-Git-Git-Hooks)
an. Diese kannst du mit folgendem Befehl installieren:

```bash
git config core.hooksPath '.githooks'
```

Die Git-Hooks sind im [.githooks](./.githooks/) Verzeichnis zu finden.

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

Die 'User Experience' einiger Seiten wird zur Laufzeit mit JavaScript weiter verbessert.

Assets sind in `<root>/src/main/javascript` zu finden

* `bundles` sind in den HTML-Seiten zu integrieren
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
in den Html-Seiten integriert werden. Damit das nicht bei jeder Änderung manuell gemacht werden muss, kann der Dateiname mit Hilfe der
Taglib `AssetsHashResolverTag.java` zur Kompilierungszeit automatisiert werden.

```html
<script defer asset:src="npm.jquery.js"></script>
```

Während der Weiterentwicklung ist es sinnvoll das Caching zu deaktivieren. Wird das `demodata` Profil verwendet muss
nichts weiter getan werden. Verwendest du das Profil nicht, kannst du das Caching mit folgenden application Properties
deaktivieren:

```properties
spring.web.resources.chain.cache=false
spring.web.resources.cache.cachecontrol.max-age=0
spring.web.resources.chain.strategy.content.enabled=false
```

#### Icons

Wir nutzen das großartige Lucide Icon Set. Vielen Dank! ♥️

- https://lucide.dev
- https://github.com/lucide-icons/lucide

### API

Die Urlaubsverwaltung verfügt über eine API, die unter [http://localhost:8080/api](http://localhost:8080/api) erreichbar ist.


### Release

### GitHub action

Go to the GitHub action with the name [release trigger][github-action-release-trigger].
* Click on "Run workflow"
* Add the "Milestone ID" (see in the uri of a milestone)
* Add "Release version"
* Add "Next version"
* Run the workflow


[Landingpage]: https://urlaubsverwaltung.cloud
[github-action-release-trigger]: https://github.com/urlaubsverwaltung/urlaubsverwaltung/actions/workflows/release-trigger.yml "Release Trigger"

