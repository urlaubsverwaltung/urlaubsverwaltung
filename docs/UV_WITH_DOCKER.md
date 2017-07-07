## Externe Systeme für die Urlaubsverwaltung mit Docker aufsetzen
Die Urlaubsverwaltung hat zwei optionale Abhängigkeiten auf externe Systeme. Mit extern ist hier gemeint, dass die
Systeme außerhalb der JVM laufen in der die Urlaubsverwaltung mit Spring Boot läuft. Zum einen ist das die SQL Datenbank
für die Persistenz und ein Verzeichnisserver als Login-Backend.

In den meisten Fällen reicht es aus, diese Abhängigkeit intern, also mit H2 als Datenbank und der
default-Authentifizierung über die Datenbank, zu befriedigen. Für Fälle, in denen man das System mit den externen
Abhängigkeiten betreiben will, bietet es sich an diese externen Systeme mit Docker zu virtualisieren.

Dieses Dokument beschreibt wie man Docker Container für MariaDB und OpenLDAP für die Urlaubsverwaltung aufsetzen kann.

### Docker Installation
#### Linux/Mac OS und Windows
 * Alternativ CoreOS VM mit Docker über Vagrant.
  * Vagrant installieren https://www.vagrantup.com/downloads.html
  * Vagrant CoreOS gibt es hier https://github.com/coreos/coreos-vagrant
    
#### Mac OS
Docker läuft zur Zeit nicht nativ auf Mac OS. Es wird zwingend eine Linux VM gebraucht. Die hier genannten Tools 
 * Auf Mac OS X gibt es ein feines Toolset mit GUI: https://docs.docker.com/engine/installation/mac/
 
#### Linux
Je nach Distribution ist Docker bereits im Paketmanager verfügbar.

### Externe Abhängigkeiten
#### MariaDB Datenbank
Als erstes zieht man das offizielle Docker Image
```
$ docker pull mariadb
```

Dann wird ein MariaDB Container mit dem folgenden Kommando gestartet:
```
$ docker run --name uv-mariadb -e MYSQL_ROOT_PASSWORD=secret -d mariadb:latest
```

Der Datenbankserver ist nun über den Port, den Docker aufgamacht hat (oder den man beim Container-Start gesetzt hat), 
und die IP der VM in der Docker läuft (oder localhost, falls der Docker-Daemon lokal läuft) ansprechbar.
Jetzt kann man sich entweder direkt mit der Shell in den Container begeben und dort den mysql Client bedienen oder 
ein externes DB Tool verwenden. Hier ein Beispiel zum direkten Verbinden via `mysql`.

```
$ mysql -uroot -psecret -h $(docker inspect --format '{{ .NetworkSettings.IPAddress }}' uv-mariadb)
```
 
Sobald die Verbindung mit dem root Benutzer aufgebaut ist, müssen die Datenbank und der Nutzer für die Urlaubsverwaltung
angelegt werden.

``` sql
CREATE DATABASE uvdb CHARACTER SET utf8;
GRANT ALL ON uvdb.* TO 'uv'@'%' IDENTIFIED BY 'password' WITH GRANT OPTION;
```

Danach ist die Datenbank für die Urlaubsverwaltung verfügbar und kann in der Spring Konfiguration eingetragen werden. Am
besten legt man sich ein eigenes Properties-File dafür an und überschreibt die Datenbank Konfiguration. Die neue
Konfiguration lässt sich dann über ihren Namen als Spring Profile ansprechen. Wie das geht, steht [hier beschrieben](README.md#mvn_profiles).

#### OpenLDAP
Docker Image für OpenLDAP ziehen. Hier der Link in die Docker Registry: https://hub.docker.com/r/osixia/openldap/
```
$ docker pull osixia/openldap
```

Nun kann der OpenLDAP Container mit entsprechenden parametern gestartet werden.
```
$ docker run -p 389:389 --name openldap -e LDAP_TLS=false -e LDAP_ORGANISATION=acme -e LDAP_DOMAIN=corp -d osixia/openldap
```
Zu den Umgebungsvariablen:
* "LDAP_TLS=false" - Deaktiviert TLS. Damit muss man sich nicht um Zertifikate usw. kümmern. 
* "LDAP_ORGANISATION=example" - Die Firma oder Organisation für die der LDAP Server betrieben wird.
* "LDAP_DOMAIN=example.org" - Die Domain der Firma.
* "LDAP_ADMIN_PASSWORD" - das passwort für das Benutzerkonto "admin"

Sowohl DOMAIN als auch ORGANISATION müssen zum Wert des Parameters uv.security.ldap.sync.userSearchBase in den
application.properties passen.
Es empfiehlt sich, den Container-internen Port des OpenLDAD Servers auf einen festen externen Port zu mappen, da dieser
sowohl vom phpLDAPadmin Tool als auch von der Urlaubsverwaltungs Applikation selbst angesprochen wird und stabil bleiben
sollte.

#### phpLDAPadmin
Der OpenLDAP Server an sich reicht noch nicht, um sinnvoll damit zu arbeiten. Es müssen also Daten erzeugt werden.  Es
gibt sicherlich viele gute Frontends für LDAP. Da Docker ja bereits installiert ist, kann man hier auch gleich einen
Docker Container nehmen. phpLDAPadmin bietet sich hier an.

So besorgt man sich das Image für phpLDAPadmin(https://hub.docker.com/r/osixia/phpldapadmin/):
```
$ docker pull osixia/phpldapadmin
```

Der Container bekommt beim Start die IP-Adresse und den Port (falls er nicht dem Standardwert 389 entspricht) als
Umgebungsvariable mitgegeben.
```
docker run -p 6443:443 -e PHPLDAPADMIN_LDAP_HOSTS=<IP_OF_LDAP_SERVER> -d osixia/phpldapadmin:latest
```

Beim nun kann das phpLDAPadmin UI über https://<DOCKER_VM_IP>:6443 aufgerufen werden. Als Loginname muss folgendes
verwendet werden:
```
cn=admin,dc=acme,dc=corp
```

Das Passwort ist admin.


https://hub.docker.com/r/osixia/phpldapadmin/
