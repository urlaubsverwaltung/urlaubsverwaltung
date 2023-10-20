# Helm Charts

## kubernetes

Die kubernetes Deployments verwenden den helm Chart [urlaubsverwaltung](urlaubsverwaltung). Die
nachfolgenden Beispiele verwenden `helm template`, um nicht extra einen tiller im k8s Cluster installiert haben zu müssen.

### Mit existierender PostgreSQL

In der Datei [values-existing-postgresql.yaml](kubernetes/chart/charts/values-existing-postgresql.yaml)
sind die Verbindungsdaten zu der existierenden PostgreSQL zu konfigurieren. Danach kann via helm das Deployment
durchgeführt werden:

```bash
cd kubernetes/chart/urlaubsverwaltung
helm template -f values-existing-postgresql.yaml --name uv . | kubectl apply -n urlaubsverwaltung -f -
```

### Mit PostgreSQL Deployment


```bash
cd kubernetes/chart/urlaubsverwaltung
helm dependency update
helm template -f values-postgresql.yaml --name uv . | kubectl apply -n urlaubsverwaltung -f -
```
