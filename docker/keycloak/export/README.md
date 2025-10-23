# Keycloak Import Konfiguration

Dieses Verzeichnis enthält die Keycloak Realm-Konfiguration für die Urlaubsverwaltung.

## Inhalt

- `urlaubsverwaltung-realm.json` - Realm-Konfiguration mit Client-Einstellungen
- `urlaubsverwaltung-users-0.json` - Demo-Benutzer für Entwicklung und Tests

## Verwendung

Die Dateien werden automatisch beim Start von Keycloak importiert, wenn Sie den Container mit dem `--import-realm` Flag starten (bereits in docker-compose.production.yml konfiguriert).

## Demo-Benutzer

Die folgenden Demo-Benutzer sind in `urlaubsverwaltung-users-0.json` definiert:

| Benutzername                                 | Passwort | Rolle                            |
|----------------------------------------------|----------|----------------------------------|
| user@urlaubsverwaltung.cloud                 | secret   | User                             |
| departmentHead@urlaubsverwaltung.cloud       | secret   | User & Abteilungsleiter          |
| secondStageAuthority@urlaubsverwaltung.cloud | secret   | User & Freigabe-Verantwortlicher |
| boss@urlaubsverwaltung.cloud                 | secret   | User & Chef                      |
| office@urlaubsverwaltung.cloud               | secret   | User & Office                    |
| admin@urlaubsverwaltung.cloud                | secret   | User & Admin                     |

**WICHTIG für Produktion:**
- Löschen oder deaktivieren Sie die Demo-Benutzer
- Erstellen Sie eigene Benutzer mit sicheren Passwörtern
- Oder löschen Sie die `urlaubsverwaltung-users-0.json` Datei vor dem ersten Start

## Client-Konfiguration

Der importierte Realm enthält bereits einen vorkonfigurierten Client:
- **Client ID**: `urlaubsverwaltung`
- **Client Secret**: `urlaubsverwaltung-secret` (sollte in Produktion geändert werden!)

### Client Secret in Produktion ändern

1. Melden Sie sich in der Keycloak Admin-Konsole an: `https://auth.llv24.de`
2. Navigieren Sie zu **Clients** → **urlaubsverwaltung**
3. Gehen Sie zum Tab **Credentials**
4. Klicken Sie auf **Regenerate Secret**
5. Kopieren Sie das neue Secret
6. Aktualisieren Sie die `.env` Datei:
   ```bash
   OAUTH_CLIENT_SECRET=das-neue-secret
   ```
7. Starten Sie die Urlaubsverwaltung neu:
   ```bash
   docker-compose -f docker-compose.production.yml restart urlaubsverwaltung
   ```

## Anpassungen für Produktion

Bearbeiten Sie `urlaubsverwaltung-realm.json` vor dem ersten Start, um:

1. **Redirect URIs** zu aktualisieren (Zeile mit `redirectUris`):
   ```json
   "redirectUris": ["https://urlaub.llv24.de/*"]
   ```

2. **Web Origins** zu aktualisieren:
   ```json
   "webOrigins": ["https://urlaub.llv24.de"]
   ```

Diese Anpassungen sind bereits in der Beispiel-Konfiguration für `urlaub.llv24.de` vorbereitet.

## Neuimport

**ACHTUNG:** Der Import funktioniert nur beim **ersten Start** von Keycloak.

Wenn Sie die Konfiguration später ändern möchten:
1. Löschen Sie das PostgreSQL Volume: `docker-compose -f docker-compose.production.yml down -v`
2. Starten Sie neu: `docker-compose -f docker-compose.production.yml up -d`

**ODER** nehmen Sie die Änderungen direkt in der Keycloak Admin-Konsole vor.
