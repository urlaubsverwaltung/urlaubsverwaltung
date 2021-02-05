## Mit Keycloak / OpenID Connect

Dieses Beispiel zeigt, wie die Urlaubsverwaltung mit [Keycloak](https://www.keycloak.org/)
bzw. einem OpenID Connect fähigen Autorisierungsserver verwendet werden kann.

* In diesem Verzeichnis `docker-compose up -d`
* Vom Root-Verzeichnis `java -jar target/urlaubsverwaltung-*.war \
  --spring.config.location=file:./src/main/resources/ \
  --spring.profiles.active=keycloak`


Logins sind auf Basis von Demodata. Dabei ist zu beachten, dass sich der Benutzername aus `Benutzer@example.org`
zusammen setzt.
