# Beispieldeployments auf Basis von Docker

## Docker

Über `docker run -p 8080:8080 urlaubsverwaltung/urlaubsverwaltung:${version}` kann die Urlaubsverwaltung als Docker Container gestartet werden.

Die Version des Images findest du auf [docker hub](https://hub.docker.com/r/urlaubsverwaltung/urlaubsverwaltung/tags) 

## docker-compose

### Urlaubsverwaltung

Dieses Beispiel sollte nur zum Testen im lokalen Netzwerk verwendet werden, da eine unverschlüsselte HTTP-Verbindung
zur Urlaubsverwaltung verwendet wird.

Um dieses Beispiel zu verwenden sind folgende Schritte notwendig:

* Über `docker-compose pull` wird das neuste Container-Image der Urlaubsverwaltung geladen
* Der Start der Urlaubsverwaltung inkl. PostgreSQL erfolgt durch `docker-compose up -d`

Falls die Urlaubsverwaltung auf eine neue Version aktualisiert werden sollte,
müssen diese zwei Schritte wiederholt werden.
