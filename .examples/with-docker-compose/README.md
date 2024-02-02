## Mit Keycloak / OpenID Connect

Dieses Beispiel zeigt, wie die Urlaubsverwaltung mit [PostgreSQL](https://www.postgresql.org/) und
[Keycloak](https://www.keycloak.org/) bzw. einem OpenID Connect fähigen Autorisierungsserver verwendet werden kann.

* In diesem Verzeichnis `docker-compose up -d`

Logins sind auf Basis von [Demodata](../../README.md#Demodaten-Modus). 


### Service Account

Der `urlaubsverwaltung` OIDC-Client hat den Client-Credentials Grant (Service Account) aktiviert.  
In einer Produktiv-Umgebung wäre ein eigener OIDC-Client genau hierfür schöner, aber fürs Testen reicht auch ein Client für beides.
```bash
# Access token erzeugen (gültig für ein paar Minuten)
ACCESS_TOKEN=$(curl -s http://localhost:8090/realms/urlaubsverwaltung/protocol/openid-connect/token \
    --data-urlencode 'client_id=urlaubsverwaltung' \
    --data-urlencode 'client_secret=urlaubsverwaltung-secret' \
    --data-urlencode 'scope=email' \
    --data-urlencode 'grant_type=client_credentials' \
    | jq -r '.access_token')

# Abfrage mit Bearer Token (geht nur auf API-Endpunkte)
curl localhost:8080/api/persons -H "Authorization: Bearer $ACCESS_TOKEN"
```
