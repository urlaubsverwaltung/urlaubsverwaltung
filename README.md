# Urlaubsverwaltung [![Build](https://github.com/urlaubsverwaltung/urlaubsverwaltung/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/urlaubsverwaltung/urlaubsverwaltung/actions/workflows/build.yml) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=urlaubsverwaltung_urlaubsverwaltung&metric=coverage)](https://sonarcloud.io/summary/new_code?id=urlaubsverwaltung_urlaubsverwaltung) [![Docker Pulls](https://badgen.net/docker/pulls/synyx/urlaubsverwaltung?icon=docker&label=pulls)](https://hub.docker.com/r/urlaubsverwaltung/urlaubsverwaltung/)

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
Dann steige über unsere [Landingpage] direkt in das [Demo-System] ein.

## FAQ

Für Fragen, die bei der Benutzung der Urlaubsverwaltung aufkommen, gibt es eine [Hilfe].  
Sollte dieser Fragenkatalog nicht weiterhelfen, kannst du gerne
[ein neue Q&A](https://github.com/urlaubsverwaltung/urlaubsverwaltung/discussions/new?category=q-a) erstellen.

## Berechtigungen

In der Urlaubsverwaltung gibt es aktuell folgende Arten von Berechtigungen:

* **inaktiv**: hat keinen Zugang mehr zur Urlaubsverwaltung (Daten des Benutzers bleiben zur Archivierung bestehen)
* **User**: darf Urlaub für sich selbst beantragen
* **Abteilungsleiter**: darf Urlaubsanträge für die Benutzer seiner Abteilungen einsehen, genehmigen und ablehnen
* **Freigabe-Verantwortlicher**: ist bei der zweistufigen Genehmigung von Anträgen verantwortlich für die endgültige Freigabe
* **Chef**: darf Urlaubsanträge aller Benutzer einsehen, genehmigen und ablehnen
* **Office**: darf Einstellungen zur Anwendung vornehmen, Mitarbeiter verwalten, Urlaub für Mitarbeiter
beantragen/stornieren und Krankmeldungen pflegen

Eine aktive Person kann eine oder mehrere Rollen innehaben.
  
## Betrieb

Wir bieten auf GitHub für das Open-Source-Projekt [urlaubsverwaltung](https://github.com/urlaubsverwaltung/urlaubsverwaltung) **keinen Support für den Betrieb** an.
Für den Austausch mit anderen Personen steht jedoch der **Discussions-Bereich** unter
[Operations](https://github.com/urlaubsverwaltung/urlaubsverwaltung/discussions/categories/operation) zur Verfügung.
Dort können Erfahrungen geteilt und Fragen diskutiert werden.

Falls ein **bezahlter Support** für die On-Premise-Installation oder den Betrieb gewünscht wird,
kann man sich unter [urlaubsverwaltung.cloud/preis](https://urlaubsverwaltung.cloud/preis/) über die verfügbaren Support-Optionen informieren.

### Voraussetzungen

* [JDK 21](https://adoptium.net)
* [PostgreSQL Datenbank (v15.3)](https://www.postgresql.org/)
* [E-Mail-Server](#e-mail-server-konfigurieren)
* [Security Provider](#security-provider-konfigurieren)

#### Installation .jar Variante

Die Urlaubsverwaltung steht in den [Releases](https://github.com/urlaubsverwaltung/urlaubsverwaltung/releases/latest) zur Verfügung.

* [Konfiguration Datenbank](#datenbank-konfigurieren)
* [Konfiguration Security Provider](#security-provider-konfigurieren)
* Lege ein Verzeichnis für die Urlaubsverwaltung an (z.B. `/opt/urlaubsverwaltung`). Kopiere die .jar-Datei dorthin.
* Erstelle in dem Verzeichnis eine Konfigurationsdatei namens `application.yaml`, welche die Konfiguration für
  die Urlaubsverwaltung enthält und die Standardwerte überschreibt.
  Die vollständigen Konfigurationsoptionen sind unter [Konfiguration](#konfiguration) dokumentiert.
  
Nach der [Konfiguration](#konfiguration) lässt sich die Urlaubsverwaltung starten.

```bash
java -jar urlaubsverwaltung.jar
``` 

Falls es Probleme beim Starten der Anwendung gibt, ist es hilfreich das [Logging der Anwendung](#logging-konfigurieren)
zu konfigurieren, damit erhält man mehr Informationen über den Fehlerzustand.

#### Docker Variante

Alle Informationen zum Betrieb mit unserem [Docker Image](https://hub.docker.com/r/urlaubsverwaltung/urlaubsverwaltung) sind im Ordner [.example](.examples) zu finden.

### Konfiguration

Die Anwendung besitzt im Verzeichnis `src/main/resources` eine [Konfigurationsdatei](https://github.com/urlaubsverwaltung/urlaubsverwaltung/blob/main/src/main/resources/application.yaml).
Diese beinhaltet gewisse Grundeinstellungen und Standardwerte. Diese allein reichen für die Produktivnahme der
Anwendung allerdings nicht aus. Spezifische Konfigurationen wie z.B. die [Datenbank Einstellungen](#datenbank-konfigurieren)
und [Security Provider](#security-provider-konfigurieren) müssen in einer eigenen Konfigurationsdatei hinterlegt werden.

Welche Möglichkeiten es bei Spring Boot gibt, damit die eigene Konfigurationsdatei genutzt wird, kann in der
['External Config' Reference](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html#boot-features-external-config-application-property-files)
nachgelesen werden.

Nachstehend alle spezifischen Konfigurationsmöglichkeiten der Urlaubsverwaltung mit ihren Standartwerten.

```yaml
uv:

  mail:
    from: ''
    fromDisplayName: Urlaubsverwaltung
    replyTo: ''
    replyToDisplayName: Urlaubsverwaltung
    application-url: ''

  development:
    demodata:
      create: 'false'
      additional-active-user: '0'
      additional-inactive-user: '0'

  calendar:
    organizer: ''
    refresh-interval: P1D

  security:
    oidc:
      claim-mappers:
        group-claim:
          enabled: 'false'
          claim-name: groups
        resource-access-claim:
          enabled: 'false'
          resource-app: urlaubsverwaltung
        role-prefix: urlaubsverwaltung_
      post-logout-redirect-uri: '{baseUrl}'

  application:
    upcoming-holiday-replacement-notification:
      cron: 0 0 7 * * *
    reminder-notification:
      cron: 0 0 7 * * *
    upcoming-notification:
      cron: 0 0 7 * * *

  account:
    update:
      cron: 0 0 5 1 1 *

  sick-note:
    end-of-pay-notification:
      cron: 0 0 6 * * *

```

#### Security Provider konfigurieren

Siehe die [Spring Boot oAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html#oauth2login-boot-property-mappings) konfiguration und für die Konfiguration des [Resource Servers via JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html) z.B.

Zudem kann das Verhalten der Urlaubsverwaltung anhand von `uv.security.oidc` beeinflusst werden.

Es wird erwartet, dass der OIDC Provider im Access Token folgende Attribute enthält: `given_name`, `family_name`, `email`. Der client registration muss deshalb mit den Scopes `openid`,`profile` und `email` konfiguriert werden.
    
Der erste Benutzer, welcher sich erfolgreich bei der Urlaubsverwaltung anmeldet, wird mit der Rolle `Office` angelegt. Dies ermöglicht Benutzer- und Rechteverwaltung und das Pflegen der Einstellungen innerhalb der Anwendung.

#### Datenbank konfigurieren

Die Anwendung verwendet zur Speicherung der Daten ein PostgreSQL-Datenbankmanagementsystem. 
Erstelle in deinem PostgreSQL-Datenbankmanagementsystem eine Datenbank sowie einen Benutzer mit Zugriffsrechten für diese Datenbank und konfiguriere diese

```yaml
spring:
  datasource:
    url: jdbc:postgresql://$HOST:$PORT/$NAME_DER_DATENBANK
    username: $BENUTZER
    password: $PASSWORT
```
Wenn Sie die Urlaubsverwaltung das erste Mal starten, werden automatisch alle Datenbanktabellen angelegt.


#### E-Mail-Server konfigurieren

Um den E-Mail-Server zu konfigurieren, müssen folgende Konfigurationen vorgenommen werden.

```yaml
uv:
  mail:
    from: absender@example.org
    fromDisplayName: Urlaubsverwaltung
    replyTo: no-reply@example.org
    replyToDisplayName: Urlaubsverwaltung
    application-url: https://example.org
spring:
  mail:
    host: $HOST
    port: $PORT
    username: $USERNAME
    password: $PASSWORT
```

Alle weiteren `spring.mail.*` Konfigurationen können in der [Spring Dokumentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#io.email)
eingesehen werden.

#### Logging konfigurieren

Sollten beim Starten der Anwendung Probleme auftreten, lässt sich in der Konfigurationsdatei eine
ausführliche Debug-Ausgabe konfigurieren, indem das `logging.level.*` pro Paket konfiguriert wird,

```yaml
logging:
  level:
    org.springframework.security: TRACE
    org.synyx.urlaubsverwaltung: TRACE
```

sowie eine Logdatei

```yaml
logging.file.name: logs/urlaubsverwaltung.log
```

geschrieben wird.

#### Info-Banner

Es kann ein Info-Banner konfiguriert werden, um z. B. Wartungsarbeiten anzukündigen.
Der Banner ist dann ganz oben zu sehen.

```properties
uv.info-banner.enabled=true
uv.info-banner.text.de=Wartungsarbeiten ab Freitag 14:00. Es kann zu Beeinträchtigungen kommen.
```

| Property                        | Type    | Description                                          |
|---------------------------------|---------|------------------------------------------------------|
| uv.info-banner.enabled          | Boolean | (default) `false`, `true` zum aktivieren des Banners |
| uv.info-banner.text.de          | String  | Text des Info-Banners für das Deutsche Locale.       |

#### Launchpad

Es kann ein Launchpad konfiguriert werden, welches einen Absprung zu anderen Anwendungen ermöglicht. 

```yaml
launchpad:
  name-default-locale: de
  apps[1]:
    icon: ''
    name:
      de: Anwendung 2
      en: App 2
    url: https://example-2.org
  apps[0]:
    icon: ''
    name:
      en: App 1
      de: Anwendung 1
    url: https://example.org
```

| Property                        | Type     | Description                                                                                                                                                |
|---------------------------------|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| launchpad.name-default-locale   | Locale   | Standard Name der Anwendung wenn für ein Locale keine Übersetzung gefunden wird.                                                                           |
| launchpad.apps[x].url           | String   | URL der Anwendung.                                                                                                                                         |
| launchpad.apps[x].name.[locale] | String   | Name der Anwendung für ein Locale.                                                                                                                         |
| launchpad.apps[x].icon          | String   | URL eines Bildes oder ein base64 encodiertes Bild. Wird in das `<img src="" />` Attribut geschrieben.<br/>Das Bild sollte optimalerweise ein Quadrat sein. |

Das Launchpad hat eigene Übersetzungen. Spring muss entsprechend konfiguriert werden, damit die messages.properties gefunden wird:

```yaml
spring.messages.basename: messages,launchpad-core
```

* **(required)** `messages` standardmäßige application messages properties
* **(required)** `launchpad-core` launchpad message properties

### Anwendung als Service

Da die Anwendung auf Spring Boot basiert, lässt sie sich sehr komfortabel als Service installieren. Wie genau dies
funktioniert, kann den entsprechenden Kapiteln der Spring Boot Dokumentation entnommen werden:

* [Linux Service](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html#deployment-service)
* [Windows Service](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html#deployment-windows)


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

Auf diese Weise wird die Anwendung mit einem PostgreSQL-Datenbankmanagementsystem gestartet und Demodaten generiert.

Die Demodaten enthalten folgende **Benutzer**, ein Passwort wird nicht benötigt:

| Benutzername                                 | Passwort | Rolle                            |
|----------------------------------------------|----------|----------------------------------|
| user@urlaubsverwaltung.cloud                 | secret   | User                             |
| departmentHead@urlaubsverwaltung.cloud       | secret   | User & Abteilungsleiter          |
| secondStageAuthority@urlaubsverwaltung.cloud | secret   | User & Freigabe-Verantwortlicher |
| boss@urlaubsverwaltung.cloud                 | secret   | User & Chef                      |
| office@urlaubsverwaltung.cloud               | secret   | User & Office                    |
| admin@urlaubsverwaltung.cloud                | secret   | User & Admin                     |

Möchte man, dass beim Starten der Anwendung keine Demodaten generiert werden, muss die Konfiguration

`uv.development.demodata.create`

in den [application-demodata.yaml](https://github.com/urlaubsverwaltung/urlaubsverwaltung/blob/main/src/main/resources/application-demodata.yaml)
auf `false` gesetzt werden.

### Aufrufen der Anwendung

Folgende Systeme sind erreichbar unter `localhost`

| Service                                    | Port |
|--------------------------------------------|------|
| [Urlaubsverwaltung](http://localhost:8080) | 8080 |
| [Mailhog](http://localhost:8025)           | 8025 |
| Mailhog SMTP                               | 1025 |


## Entwicklung

Wenn du uns bei der **Entwicklung** der Urlaubsverwaltung **unterstützen** möchtest,
dann schau dir die [Contributing to Urlaubsverwaltung](CONTRIBUTING.md) Referenz und die folgenden
Abschnitte an. Bei Fragen kannst du gerne [ein neue Q&A](https://github.com/urlaubsverwaltung/urlaubsverwaltung/discussions/new?category=q-a) erstellen.

### Voraussetzungen

* [JDK 21](https://adoptium.net)
* [Docker 20.10.+](https://docs.docker.com/get-docker/)
* [Docker Compose](https://docs.docker.com/compose/install/)

### Repository

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

Die Git-Hooks sind im [.githooks](.githooks/) Verzeichnis zu finden.

### Anwendung starten

Die Urlaubsverwaltung ist eine [Spring Boot](http://projects.spring.io/spring-boot/) Anwendung und kann mit dem Maven
Plugin gestartet werden. Alle Abhängigkeiten, wie die Datenbank oder der Mail-Server werden automatisch gestartet.
Es bietet sich an, die Anwendung mit dem Profil `demodata` zu starten, um Testdaten generieren
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

Mit dem `demodata` Profil wird eine PostgreSQL-Datenbank verwendet und es werden Demodaten angelegt,
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
  * Information: der Dateiname beinhaltet einen Hash welcher eindeutig zum Inhalt des Assets passt 
* `npm run build:dev`
  * baut nicht minifizierte Assets
* `npm run build:watch`
  * baut automatisch nach dem Editieren von JavaScript / CSS Dateien neue Assets

#### Long term caching von Assets

Startet man den Maven Build oder baut man die Assets mit dem NPM Task `npm run build` wird eine JSON Datei `assets-manifest.json` angelegt.
Das Manifest beschreibt ein Mapping der bundles zum generierten Dateinamen inklusive Hash. Dieser gemappte Dateiname muss
in den Html-Seiten integriert werden. Damit das nicht bei jeder Änderung manuell gemacht werden muss, kann der Dateiname mithilfe der
Taglib `AssetsHashResolverTag.java` zur Kompilierungszeit automatisiert werden.

```html
<script defer asset:src="npm.jquery.js"></script>
```

Während der Weiterentwicklung ist es sinnvoll das Caching zu deaktivieren. Wird das `demodata` Profil verwendet muss
nichts weiter getan werden. Verwendest du das Profil nicht, kannst du das Caching mit folgenden application Properties
deaktivieren:

```yaml
spring:
  web:
    resources:
      chain:
        cache: 'false'
        strategy:
          content:
            enabled: 'false'
      cache:
        cachecontrol:
        max-age: '0'
```

#### Icons

Wir nutzen das großartige Lucide Icon Set. Vielen Dank! ♥️

- https://lucide.dev
- https://github.com/lucide-icons/lucide

### API

Die Urlaubsverwaltung verfügt über eine API, die unter [http://localhost:8080/api](http://localhost:8080/api) erreichbar ist.

### UI Tests mit Playwright

Im [test ui](src/test/java/org/synyx/urlaubsverwaltung/ui) Package befinden sich UI Tests. Diese testen einige
End to End Anwendungsfälle wie z. B. in den Einstellungen etwas aktivieren/deaktivieren und dessen Auswirkungen.

Als Testrunner und auch Assertion Lib wird [Playwright-Java](https://playwright.dev/java/) verwendet.  
Details siehe [Playwright for Java Getting Started](https://playwright.dev/java/docs/intro). 

#### Headless Browser

Die Tests laufen standardmäßig ohne sichtbaren Browser (headless).
Das kann in der [UiTest](src/test/java/org/synyx/urlaubsverwaltung/ui/extension/UiTest.java) Annotation
mit `Options#setHeadless` entsprechend konfiguriert werden.

#### Debugging

Playwright bietet einen eigenen Inspector an. Siehe [Playwright Debugging Tests](https://playwright.dev/java/docs/debug)
für weitere Informationen.

Um diesen zu nutzen, muss beim Starten des Tests die Umgebungsvariable `PWDEBUG=1` gesetzt werden.

#### UI-Test Artefakte

Unsere Playwright UI Tests erzeugen bei Fehlschlag zwei Artefakte:

- Video
- [Trace Report](https://playwright.dev/java/docs/trace-viewer-intro)

Die Artefakte sind im `target` Verzeichnis zu finden.  

Videos sind zur schnellen Analyse nützlich, da sie z. B. direkt im Browser angeschaut werden können.

Wenn das Video nicht ausreicht, kann der Trace Report zur detaillierten Analyse genutzt werden.  
Hierzu das `zip` entweder auf https://trace.playwright.dev hochladen oder die lokale laufende progressive Webapp
bei sich selbst starten mit z. B. maven:

```bash
./mvnw exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.classpathScope="test" -D exec.args="show-trace target/ui-test/<browser>/FAILED-test.zip"
```

### Release

### GitHub action

Go to the GitHub action with the name [release trigger][github-action-release-trigger].
* Click on "Run workflow"
* Add the "Milestone ID" (see in the uri of a milestone)
* Add "Release version"
* Add "Next version"
* Run the workflow


[Landingpage]: https://urlaubsverwaltung.cloud "Landingpage"
[Demo-System]: https://urlaubsverwaltung.cloud/demo "Demo-System"
[Hilfe]: https://urlaubsverwaltung.cloud/hilfe/ "Hilfe"
[Migration-Guide-v5]: https://github.com/urlaubsverwaltung/urlaubsverwaltung/wiki/Urlaubsverwaltung-5.0-Migration-Guide "Migration Guide v5"
[github-action-release-trigger]: https://github.com/urlaubsverwaltung/urlaubsverwaltung/actions/workflows/release-trigger.yml "Release Trigger"
[PostgreSQL]: https://www.postgresql.org/ "PostgreSQL"
