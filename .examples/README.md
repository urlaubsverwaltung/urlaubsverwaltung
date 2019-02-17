# Beispieldeployments auf Basis von Docker

Seit Version [2.30.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.30.0) der Urlaubsverwaltung
gibt es auch ein Container Image für Docker.

## Docker

Über `docker run synyx/urlaubsverwaltung:latest -p 8080:8080` kann die Urlaubsverwaltung als Docker Container gestartet werden.


## docker-compose

### Mit MariaDB

Dieses Beispiel sollte nur zum Testen im lokalen Netzwerk verwendet werden, da der eine unverschlüsselte HTTP-Verbindung
zur Urlaubsverwaltung verwendet wird.

Um dieses Beispiel zu verwenden sind folg. Schritte Notwendig:

* Das `MYSQL_ROOT_PASSWORD=` muss für die MariaDB im [docker-compose-File](docker-compose/with-mariadb/docker-compose.yaml) gesetzt werden
* Das `MYSQL_PASSWORD=` muss für die MariaDB im [db.env-File](docker-compose/with-mariadb/db.env) gesetzt werden
* Über `docker-compose pull` wird das neuste Container Image der Urlaubsverwaltung runtergeladen
* Der Start der Urlaubsverwaltung inkl. MariaDB erfolgt durch `docker-compose up -d`

Falls die Urlaubsverwaltung auf eine neue Version aktualisiert werdne sollte, müssen Schritte 3 und 4 wiederholt werden.
