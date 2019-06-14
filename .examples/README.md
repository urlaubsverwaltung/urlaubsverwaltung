# Beispieldeployments auf Basis von Docker

Seit Version [2.30.0](https://github.com/synyx/urlaubsverwaltung/releases/tag/urlaubsverwaltung-2.30.0) der Urlaubsverwaltung
gibt es auch ein Container Image für Docker.

## Docker

Über `docker run -p 8080:8080 synyx/urlaubsverwaltung:latest` kann die Urlaubsverwaltung als Docker Container gestartet werden.


## docker-compose

### Mit MariaDB

Dieses Beispiel sollte nur zum Testen im lokalen Netzwerk verwendet werden, da eine unverschlüsselte HTTP-Verbindung
zur Urlaubsverwaltung verwendet wird.

Um dieses Beispiel zu verwenden sind folgende Schritte notwendig:

* Über `docker-compose pull` wird das neuste Container Image der Urlaubsverwaltung runtergeladen
* Der Start der Urlaubsverwaltung inkl. MariaDB erfolgt durch `docker-compose up -d`

Falls die Urlaubsverwaltung auf eine neue Version aktualisiert werden sollte,
müssen diese zwei Schritte wiederholt werden.


## kubernetes

Die kubernetes Deployments verwenden den helm Chart [urlaubsverwaltung](kubernetes/chart/urlaubsverwaltung). Die
nachfolgenden Beispiele verwenden `helm template`, um nicht extra einen tiller im k8s Cluster installiert haben zu müssen.

### Mit existierender MariaDB

In der Datei [values-existing-mariadb.yaml](kubernetes/chart/urlaubsverwaltung/values-existing-mariadb.yaml)
sind die Verbindungsdaten zu der existierenden MariaDB zu konfigurieren. Danach kann via helm das Deployment
durchgeführt werden:

```bash
cd kubernetes/chart/urlaubsverwaltung
helm template -f values-existing-mariadb.yaml --name uv . | kubectl apply -n urlaubsverwaltung -f -
```

### Mit MariaDB Deployment


```bash
cd kubernetes/chart/urlaubsverwaltung
helm dependency update
helm template -f values-mariadb.yaml --name uv . | kubectl apply -n urlaubsverwaltung -f -
```
