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

Das Repository enthält bereits vorkonfigurierte Keycloak-Importdateien im Verzeichnis `docker/keycloak/export/`:

- `urlaubsverwaltung-realm.json` - Vorkonfigurierter Realm mit Client-Einstellungen
- `urlaubsverwaltung-users-0.json` - Demo-Benutzer für Tests

**Wichtig für Produktion:**
- Die Konfiguration ist bereits für `urlaub.llv24.de` vorbereitet
- Die Demo-Benutzer sollten in Produktion gelöscht oder deaktiviert werden
- Das Client Secret sollte nach dem ersten Start geändert werden

Siehe `docker/keycloak/export/README.md` für weitere Details.

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

### 2. Automatischer Import

Der **Realm `urlaubsverwaltung`** und der **Client** werden automatisch beim ersten Start importiert!

Die Konfiguration beinhaltet:
- Realm: `urlaubsverwaltung`
- Client ID: `urlaubsverwaltung`
- Client Secret: `urlaubsverwaltung-secret` (sollte geändert werden!)
- Demo-Benutzer (siehe unten)

### 3. Client Secret ändern (empfohlen für Produktion)

1. Navigieren Sie zu **Clients** → **urlaubsverwaltung**
2. Gehen Sie zum Tab **Credentials**
3. Klicken Sie auf **Regenerate Secret**
4. Kopieren Sie das neue Secret
5. Aktualisieren Sie die `.env`-Datei:
   ```bash
   OAUTH_CLIENT_SECRET=das-neue-secret
   ```
6. Starten Sie die Urlaubsverwaltung neu:
   ```bash
   docker-compose -f docker-compose.production.yml restart urlaubsverwaltung
   ```

### 4. Demo-Benutzer

Die folgenden Demo-Benutzer sind bereits importiert:

| Benutzername                                 | Passwort | Rolle                            |
|----------------------------------------------|----------|----------------------------------|
| user@urlaubsverwaltung.cloud                 | secret   | User                             |
| departmentHead@urlaubsverwaltung.cloud       | secret   | User & Abteilungsleiter          |
| secondStageAuthority@urlaubsverwaltung.cloud | secret   | User & Freigabe-Verantwortlicher |
| boss@urlaubsverwaltung.cloud                 | secret   | User & Chef                      |
| office@urlaubsverwaltung.cloud               | secret   | User & Office                    |
| admin@urlaubsverwaltung.cloud                | secret   | User & Admin                     |

**Wichtig für Produktion:**
- Löschen oder deaktivieren Sie diese Demo-Benutzer
- Erstellen Sie eigene Benutzer mit sicheren Passwörtern

### 5. Eigene Benutzer erstellen

1. Navigieren Sie zu **Users** → **Add user**
2. Füllen Sie die Felder aus:
   - **Username**: z.B. `admin@llv24.de`
   - **Email**: z.B. `admin@llv24.de`
   - **First name**: Vorname
   - **Last name**: Nachname
3. Klicken Sie auf **Create**

**Passwort setzen**:
1. Gehen Sie zum Tab **Credentials**
2. Klicken Sie auf **Set password**
3. Geben Sie ein sicheres Passwort ein
4. **Temporary**: OFF
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
