## Externe Systeme für die Urlaubsverwaltung mit Docker aufsetzen
Die Urlaubsverwaltung hat zwei optionale Abhängigkeiten auf externe Systeme. Mit extern ist hier gemeint, dass die
Systeme außerhalb der JVM laufen in der die Urlaubsverwaltung mit Spring Boot läuft. Zum einen ist das die SQL Datenbank
für die Persistenz und ein Verzeichnisserver als Login-Backend.

In den meisten Fällen reicht es aus, diese Abhängigkeit intern, also mit H2 als Datenbank und der
default-Authentifizierung über die Datenbank, zu befriedigen. Für Fälle, in denen man das System mit den externen
Abhängigkeiten betreiben will, bietet es sich an diese externen Systeme mit Docker zu virtualisieren.

Dieses Dokument beschreibt wie man Docker Container für MariaDB und OpenLDAP für die Urlaubsverwaltung aufsetzen lassen.

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
$ docker run --name some-mariadb -e MYSQL_ROOT_PASSWORD=my-secret-pw -d mariadb:tag
```

Der Datenbankserver ist nun über den Port, den Docker aufgamacht hat (oder den man beim Container-Start gesetzt hat), 
und die IP der VM in der Docker läuft (oder localhost, falls der Docker-Daemon lokal läuft) ansprechbar.
Jetzt kann man sich entweder direkt mit der Shell in den Container begeben und dort den mysql Client bedienen oder 
ein externes DB Tool verwenden.
 
Sobald die Verbindung mit dem root Benutzer aufgebaut ist, müssen die Datenbank und der Nutzer für die Urlaubsverwaltung
angelegt werden.
``` sql
CREATE DATABASE uvdb CHARACTER SET utf8;
GRANT ALL ON uvdb.* TO 'uv'@'%' IDENTIFIED BY 'password' WITH GRANT OPTION;
```

Danach ist die Datenbank für die Urlaubsverwaltung verfügbar und kann in der Spring Konfiguration eingetragen werden. Am
besten legt man sich ein eigenes Properties-File dafür an und überschreibt die Datenbank Konfiguration. Die neue
Konfiguration lässt sich dann über ihren Namen als Spring Profile ansprechen. Wie das geht, steht [hier beschrieben](README#mvn-profiles).

#### OpenLDAP
https://hub.docker.com/r/osixia/openldap/
tbw
#### phpLDAPadmin
https://hub.docker.com/r/osixia/phpldapadmin/
tbw
