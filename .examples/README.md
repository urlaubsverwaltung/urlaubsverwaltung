# Beispieldeployments auf Basis von Docker

Seit Version [2.30.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.30.0) der Urlaubsverwaltung
gibt es auch ein Container Image für Docker.

## Docker

Über `docker run synyx/urlaubsverwaltung:latest -p 8080:8080` kann die Urlaubsverwaltung als Docker Container gestartet werden.


## docker-compose

### Mit MariaDB

Dieses Beispiel sollte nur zum Testen im lokalen Netzwerk verwendet werden, da eine unverschlüsselte HTTP-Verbindung
zur Urlaubsverwaltung verwendet wird.

Um dieses Beispiel zu verwenden sind folgende Schritte notwendig:

* Über `docker-compose pull` wird das neuste Container Image der Urlaubsverwaltung runtergeladen
* Der Start der Urlaubsverwaltung inkl. MariaDB erfolgt durch `docker-compose up -d`

Falls die Urlaubsverwaltung auf eine neue Version aktualisiert werden sollte,
müssen diese zwei Schritte wiederholt werden.
