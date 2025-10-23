# Urlaubsverwaltung - Produktionsumgebung mit Docker

Diese Anleitung beschreibt, wie Sie die Urlaubsverwaltung mit Docker in einer Produktionsumgebung mit den folgenden Domains betreiben:

- **Keycloak (Authentifizierung)**: `auth.llv24.de`
- **Urlaubsverwaltung (Anwendung)**: `urlaub.llv24.de`
- **Mailpit (optional, für E-Mail-Tests)**: `mail.llv24.de`

## Architektur

Die Produktionsumgebung verwendet:

- **Traefik** als Reverse Proxy mit automatischer SSL-Zertifikatsverwaltung (Let's Encrypt)
- **Keycloak** für OAuth2/OIDC-Authentifizierung
- **PostgreSQL** als Datenbank
- **Urlaubsverwaltung** als Hauptanwendung
- **Mailpit** für E-Mail-Tests (optional, kann durch einen echten SMTP-Server ersetzt werden)

## Voraussetzungen

1. **Docker** (Version 20.10 oder höher)
2. **Docker Compose** (Version 2.0 oder höher)
3. **Domain-Namen** mit DNS-Einträgen:
   - `auth.llv24.de` → IP-Adresse Ihres Servers
   - `urlaub.llv24.de` → IP-Adresse Ihres Servers
   - `mail.llv24.de` → IP-Adresse Ihres Servers (optional)
4. **Offene Ports**:
   - Port 80 (HTTP, für Let's Encrypt ACME Challenge)
   - Port 443 (HTTPS)

## Installation

### 1. Umgebungsvariablen konfigurieren

Kopieren Sie die Beispiel-Umgebungsdatei und passen Sie die Werte an:

```bash
cp .env.example .env
```

Bearbeiten Sie die `.env`-Datei und ändern Sie mindestens folgende Werte:

```bash
# Domains
KEYCLOAK_DOMAIN=auth.llv24.de
APP_DOMAIN=urlaub.llv24.de

# Let's Encrypt E-Mail
ACME_EMAIL=admin@llv24.de

# Sichere Passwörter setzen!
POSTGRES_PASSWORD=IHR_SICHERES_PASSWORT
KEYCLOAK_ADMIN_PASSWORD=IHR_SICHERES_ADMIN_PASSWORT
OAUTH_CLIENT_SECRET=IHR_SICHERES_CLIENT_SECRET
```

### 2. Keycloak-Konfiguration vorbereiten

Erstellen Sie das Verzeichnis für Keycloak-Importe (falls noch nicht vorhanden):

```bash
mkdir -p docker/keycloak/export
```

### 3. Docker-Container starten

Starten Sie alle Services mit Docker Compose:

```bash
docker-compose -f docker-compose.production.yml up -d
```

### 4. Überprüfen Sie den Status

Prüfen Sie, ob alle Container laufen:

```bash
docker-compose -f docker-compose.production.yml ps
```

Logs anzeigen:

```bash
# Alle Services
docker-compose -f docker-compose.production.yml logs -f

# Nur ein bestimmter Service
docker-compose -f docker-compose.production.yml logs -f urlaubsverwaltung
docker-compose -f docker-compose.production.yml logs -f keycloak
docker-compose -f docker-compose.production.yml logs -f traefik
```

## Keycloak einrichten

### 1. Keycloak Admin-Konsole aufrufen

Öffnen Sie in Ihrem Browser: `https://auth.llv24.de`

Melden Sie sich mit den Admin-Credentials an:
- **Benutzername**: `admin` (oder der Wert aus `KEYCLOAK_ADMIN`)
- **Passwort**: Der Wert aus `KEYCLOAK_ADMIN_PASSWORD`

### 2. Realm erstellen

1. Klicken Sie auf **Create Realm**
2. Name: `urlaubsverwaltung`
3. Klicken Sie auf **Create**

### 3. Client erstellen

1. Navigieren Sie zu **Clients** → **Create client**
2. **Client ID**: `urlaubsverwaltung` (muss mit `OAUTH_CLIENT_ID` übereinstimmen)
3. **Client Protocol**: `openid-connect`
4. Klicken Sie auf **Next**

**Capability config**:
- **Client authentication**: ON
- **Authorization**: OFF
- **Authentication flow**:
  - Standard flow: ON
  - Direct access grants: ON

Klicken Sie auf **Next**

**Login settings**:
- **Valid redirect URIs**: `https://urlaub.llv24.de/*`
- **Web origins**: `https://urlaub.llv24.de`

Klicken Sie auf **Save**

### 4. Client Secret kopieren

1. Gehen Sie zum Tab **Credentials**
2. Kopieren Sie das **Client Secret**
3. Aktualisieren Sie die `.env`-Datei:
   ```bash
   OAUTH_CLIENT_SECRET=das-kopierte-secret
   ```
4. Starten Sie die Urlaubsverwaltung neu:
   ```bash
   docker-compose -f docker-compose.production.yml restart urlaubsverwaltung
   ```

### 5. Benutzer erstellen

1. Navigieren Sie zu **Users** → **Add user**
2. Füllen Sie die Felder aus:
   - **Username**: z.B. `admin`
   - **Email**: z.B. `admin@llv24.de`
   - **First name**: Vorname
   - **Last name**: Nachname
3. Klicken Sie auf **Create**

**Passwort setzen**:
1. Gehen Sie zum Tab **Credentials**
2. Klicken Sie auf **Set password**
3. Geben Sie ein Passwort ein
4. **Temporary**: OFF (damit der Benutzer das Passwort nicht ändern muss)
5. Klicken Sie auf **Save**

## Urlaubsverwaltung aufrufen

Öffnen Sie in Ihrem Browser: `https://urlaub.llv24.de`

Sie werden zu Keycloak weitergeleitet. Melden Sie sich mit dem erstellten Benutzer an.

Der **erste Benutzer**, der sich anmeldet, erhält automatisch die **Office-Rolle** und kann weitere Benutzer verwalten.

## Wartung

### Container stoppen

```bash
docker-compose -f docker-compose.production.yml down
```

### Container mit Datenbanklöschung stoppen

**ACHTUNG**: Dies löscht alle Daten!

```bash
docker-compose -f docker-compose.production.yml down -v
```

### Updates durchführen

```bash
# Neue Images herunterladen
docker-compose -f docker-compose.production.yml pull

# Container neu starten
docker-compose -f docker-compose.production.yml up -d
```

### Datenbank-Backup erstellen

```bash
docker-compose -f docker-compose.production.yml exec postgres pg_dump -U urlaubsverwaltung urlaubsverwaltung > backup-$(date +%Y%m%d-%H%M%S).sql
```

### Datenbank-Backup wiederherstellen

```bash
cat backup-YYYYMMDD-HHMMSS.sql | docker-compose -f docker-compose.production.yml exec -T postgres psql -U urlaubsverwaltung urlaubsverwaltung
```

## E-Mail-Konfiguration

### Mailpit (nur für Tests)

Mailpit ist bereits in der docker-compose.production.yml enthalten und fängt alle ausgehenden E-Mails ab.

Weboberfläche: `https://mail.llv24.de`

### Produktions-SMTP-Server

Für die Produktion sollten Sie einen echten SMTP-Server verwenden. Bearbeiten Sie die `.env`-Datei:

```bash
# Beispiel für Gmail
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=ihre-email@gmail.com
MAIL_PASSWORD=ihr-app-passwort

# Oder für SendGrid
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=ihr-sendgrid-api-key
```

Starten Sie die Container neu:

```bash
docker-compose -f docker-compose.production.yml restart urlaubsverwaltung
```

## Troubleshooting

### SSL-Zertifikate werden nicht erstellt

1. Überprüfen Sie, ob Port 80 und 443 erreichbar sind
2. Überprüfen Sie die DNS-Einträge
3. Prüfen Sie die Traefik-Logs:
   ```bash
   docker-compose -f docker-compose.production.yml logs traefik
   ```

### Keycloak-Verbindung funktioniert nicht

1. Überprüfen Sie die Keycloak-Logs:
   ```bash
   docker-compose -f docker-compose.production.yml logs keycloak
   ```
2. Stellen Sie sicher, dass der Client korrekt konfiguriert ist
3. Überprüfen Sie das Client Secret in der `.env`-Datei

### Datenbank-Verbindungsfehler

1. Überprüfen Sie, ob PostgreSQL läuft:
   ```bash
   docker-compose -f docker-compose.production.yml ps postgres
   ```
2. Prüfen Sie die Datenbank-Logs:
   ```bash
   docker-compose -f docker-compose.production.yml logs postgres
   ```

## Sicherheitshinweise

1. **Ändern Sie alle Standardpasswörter** in der `.env`-Datei!
2. **Sichern Sie die `.env`-Datei** - sie enthält sensible Daten
3. **Führen Sie regelmäßige Backups** der Datenbank durch
4. **Halten Sie die Docker-Images aktuell**
5. **Überwachen Sie die Logs** auf verdächtige Aktivitäten

## Support

Bei Fragen oder Problemen:
- [GitHub Discussions](https://github.com/urlaubsverwaltung/urlaubsverwaltung/discussions)
- [GitHub Issues](https://github.com/urlaubsverwaltung/urlaubsverwaltung/issues)
- [Dokumentation](https://urlaubsverwaltung.cloud/hilfe/)
