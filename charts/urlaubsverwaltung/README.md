# Urlaubsverwaltung

[Urlaubsverwaltung](https://urlaubsverwaltung.cloud/) is an open source web application to manage absences like
leave and sick days of employees.

## TL;DR;

```console
$ helm repo add urlaubsverwaltung https://urlaubsverwaltung.github.io/urlaubsverwaltung
$ helm install urlaubsverwaltung urlaubsverwaltung/urlaubsverwaltung
```

## Introduction

This chart bootstraps a [Urlaubsverwaltung](https://urlaubsverwaltung.cloud/) deployment on
a [Kubernetes](https://kubernetes.io) cluster
using the [Helm](https://helm.sh) package manager. It provisions a fully featured Urlaubsverwaltung installation.
For more information on Urlaubsverwaltung and its capabilities, see
its [wiki](https://github.com/urlaubsverwaltung/urlaubsverwaltung/wiki).

## Prerequisites Details

The chart has an optional dependency on
the [PostgreSQL](https://github.com/bitnami/charts/tree/master/bitnami/postgresql) chart.
By default, the PostgreSQL chart requires PVC (persistent volume claim) support on underlying infrastructure (may be
disabled).

## Installing the Chart

To install the chart with the release name `urlaubsverwaltung`:

```console
$ helm install urlaubsverwaltung urlaubsverwaltung/urlaubsverwaltung
```

## Uninstalling the Chart

To uninstall the `urlaubsverwaltung` deployment:

```console
$ helm uninstall urlaubsverwaltung
```

## Configuration

The following table lists the configurable parameters of the Urlaubsverwaltung chart and their default values.

```console
$ helm chart values urlaubsverwaltung/urlaubsverwaltung
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

You can override these by setting the `JAVA_OPTS` environment variable.

```yaml
extraEnv: |
    - name: JAVA_OPTS
      value: >-
        -XX:+PrintFlagsFinal
        -Djava.net.preferIPv4Stack=true
        -Djava.awt.headless=true
```

### Database Setup

By default, Bitnami's [PostgreSQL](https://github.com/bitnami/charts/tree/master/bitnami/postgresql) chart is deployed
and used as database.
Please refer to this chart for additional PostgreSQL configuration options.

#### Using external Database

```yaml
extraEnv: |
    - name: spring.datasource.url
      value: jdbc:postgresql://external-database:5435/urlaubsverwaltung

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

#### OpenID Connect

```yaml
extraEnv: |
    - name: SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_DEFAULT_AUTHORIZATION-GRANT-TYPE
      value: "authorization_code"
    - name: SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_DEFAULT_CLIENT-ID
      value: $yourClientId
    - name: SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_DEFAULT_CLIENT-SECRET
      value: $yourClientSecret
    - name: SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_DEFAULT_PROVIDER
      value: "default"
    - name: SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_DEFAULT_SCOPE
      value: "openid,profile,email,roles"

    - name: SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_DEFAULT_ISSUER-URI
      value: $yourIssuerURI

    - name: SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER-URI
      value: $yourIssuerURI
```

see
for [oidc configuration](https://github.com/urlaubsverwaltung/urlaubsverwaltung/tree/master#security-provider-konfigurieren)

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
    - name: UV_MAIL_APPLICATION-URL
      value: https://urlaubsverwaltung.example.org
    - name: UV_MAIL_FROM
      value: urlaubsverwaltung@example.org
    - name: UV_MAIL_REPLYTO
      value: no-reply@example.org
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

## Further information

### From chart versions < 6.0.0

Only support k8s version >= 1.19

### From chart versions < 2.1.0

improve k8s support for version >= 1.19

* See `ingress.hosts[]` structure for migration

### From chart versions < 2.0.0

Version 2.0.0 is a major update.

* Several changes to the Deployment render makes an out-of-the-box upgrade impossible.
* The chart uses a flexible way to configure the Urlaubsverwaltung application.

However, with the following manual steps you can migrate to the 2.0.0 version:

1. Adjust chart configuration as necessary
2. Make a database backup
3. Uninstall the old chart
4. Install the new version of the chart
5. Restore the database from your backup
