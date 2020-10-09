# Urlaubsverwaltung

[Urlaubsverwaltung](https://urlaubsverwaltung.cloud/) is an open source web application to manage absences like 
leave and sick days of employees.


## TL;DR;

```console
$ helm repo add synyx https://synyx.github.io/urlaubsverwaltung/chart_repo
$ helm install urlaubsverwaltung synyx/urlaubsverwaltung
```


## Introduction

This chart bootstraps a [Urlaubsverwaltung](https://urlaubsverwaltung.cloud/) deployment on a [Kubernetes](https://kubernetes.io) cluster
using the [Helm](https://helm.sh) package manager. It provisions a fully featured Urlaubsverwaltung installation.
For more information on Urlaubsverwaltung and its capabilities, see its [wiki](https://github.com/synyx/urlaubsverwaltung/wiki).


## Prerequisites Details

The chart has an optional dependency on the [MariaDB](https://github.com/bitnami/charts/tree/master/bitnami/mariadb) chart.
By default, the MariaDB chart requires PVC (persistent volume claim) support on underlying infrastructure (may be disabled).


## Installing the Chart

To install the chart with the release name `urlaubsverwaltung`:

```console
$ helm install urlaubsverwaltung synyx/urlaubsverwaltung
```


## Uninstalling the Chart

To uninstall the `urlaubsverwaltung` deployment:

```console
$ helm uninstall urlaubsverwaltung
```


## Configuration

The following table lists the configurable parameters of the Urlaubsverwaltung chart and their default values.

```console
$ helm chart values synyx/urlaubsverwaltung
```


### Usage of the `tpl` Function

The `tpl` function allows us to pass string values from `values.yaml` through the templating engine.
It is used for the following values:

* `extraEnv`
* `extraEnvFrom`
* `affinity`

It is important that these values be configured as strings. Otherwise, installation will fail.
See example for [Using external Database](#using-external-database).


### JVM Settings

Urlaubsverwaltung sets the following system properties by default:
`-XX:+PrintFlagsFinal`

You can override thes by setting the `JAVA_OPTS` environment variable.

```yaml
extraEnv: |
  - name: JAVA_OPTS
    value: >-
      -XX:+PrintFlagsFinal
      -Djava.net.preferIPv4Stack=true
      -Djava.awt.headless=true
```


### Database Setup

By default, Bitnami's [MariaDB](https://github.com/bitnami/charts/tree/master/bitnami/mariadb) chart is deployed and used as database.
Please refer to this chart for additional MariaDB configuration options.


#### Using external Database

```yaml
extraEnv: |
    - name: spring.datasource.url
      value: jdbc:mariadb://external-database:3306/urlaubsverwaltung

extraEnvFrom: |
    - secretRef:
        name: '{{ include "urlaubsverwaltung.fullname" . }}-database'

secrets:
  database:
    stringData:
      spring.datasource.username: '{{ .Values.db.username }}'
      spring.datasource.password: '{{ .Values.db.password }}'
```


### Authentication


#### LDAP

```yaml
extraEnv: |
    - name: UV_SECURITY_AUTH
      value: ldap
    - name: UV_SECURITY_DIRECTORYSERVICE_LDAP_URL
      value: "ldap://ldap.example.org/"
    - name: UV_SECURITY_DIRECTORYSERVICE_LDAP_BASE
      value: dc=example,dc=org
    - name: MANAGEMENT_HEALTH_LDAP_ENABLED
      value: "false"

extraEnvFrom: |
  - secretRef:
      name: '{{ include "urlaubsverwaltung.fullname" . }}-ldap-manager'

secrets:
  ldap-manager:
    stringData:
      UV_SECURITY_DIRECTORYSERVICE_LDAP_MANAGER_DN: '{{ .Values.ldap.manager.dn }}'
      UV_SECURITY_DIRECTORYSERVICE_LDAP_MANAGER_PASSWORD: '{{ .Values.ldap.manager.password }}'
```

###### LDAP Sync

```yaml
extraEnv: |
  - name: UV_SECURITY_DIRECTORYSERVICE_LDAP_SYNC_ENABLED
    value: "true"
  - name: UV_SECURITY_DIRECTORYSERVICE_LDAP_SYNC_USERSEARCHBASE
    value ou=employees,ou=accounts


extraEnvFrom: |
    - secretRef:
        name: '{{ include "urlaubsverwaltung.fullname" . }}-ldap-sync'

secrets:
  ldap-sync:
    stringData:
      UV_SECURITY_DIRECTORYSERVICE_LDAP_SYNC_USERDN: '{{ .Values.ldap.sync.userdn }}'
      UV_SECURITY_DIRECTORYSERVICE_LDAP_SYNC_PASSWORD: '{{ .Values.ldap.sync.password }}'
```


#### Active Directory

T.B.D - see [Konfiguration](https://github.com/synyx/urlaubsverwaltung/tree/master#security-provider-konfigurieren)


#### OpenID Connect

T.B.D - see [Konfiguration](https://github.com/synyx/urlaubsverwaltung/tree/master#security-provider-konfigurieren)


### Mailserver

```yaml
extraEnv: |
    - name: SPRING_MAIL_HOST
      value: localhost
    - name: SPRING_MAIL_PORT
      value: "25"
    - name: SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH
      value: true
    - name: SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE
      value: "true"
    - name: UV_MAIL_APPLICATIONURL
      value: https://urlaubsverwaltung.example.org
    - name: UV_MAIL_SENDER
      value: urlaubsverwaltung@example.org
    - name: UV_MAIL_ADMINISTRATOR
      value: root@example.org
    - name: MANAGEMENT_HEALTH_MAIL_ENABLED
      value: "false"

extraEnvFrom: |
    - secretRef:
        name: '{{ include "urlaubsverwaltung.fullname" . }}-mailserver'

secrets:
  mailserver:
    stringData:
      SPRING_MAIL_USERNAME: '{{ .Values.mailserver.username }}'
      SPRING_MAIL_PASSWORD: '{{ .Values.mailserver.password }}'

```


### Spring Boot Actuator Settings

By default, the Spring Boot Actuator is using the same port as Urlaubsverwaltung.
Exposing all management endpoints by using the default HTTP port is a sensible thing.
It's possible to expose the management endpoints to a different port.
A new service will be created, but not exposed via an ingress!

```yaml
customizedManagementServer:
  enabled: true

livenessProbe: |
  httpGet:
    path: /actuator/health
    port: http-management
  initialDelaySeconds: 120
  periodSeconds: 10
  failureThreshold: 10

readinessProbe: |
  httpGet:
    path: /actuator/health
    port: http-management
  initialDelaySeconds: 60
  periodSeconds: 10
  failureThreshold: 10
```


### Prometheus Operator Support

```yaml
serviceMonitor:
  enabled: true
```

Running Urlaubsverwaltung with different management port:

```yaml
serviceMonitor:
  enabled: true
  port: http-management
```


### From chart versions < 1.0.0

Version 1.0.0 is a major update.

* Several changes to the Deployment render makes an out-of-the-box upgrade impossible.
* The chart uses a flexible way to configure the Urlaubsverwaltung application.

However, with the following manual steps an you can migrate to the 1.0.0 version:

1. Adjust chart configuration as necessary
2. Make a database backup
3. Uninstall the old chart
3. Install the new version of the chart
4. Restore the database from your backup
