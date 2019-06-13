FROM mysql:8.0.16
MAINTAINER Tobias Schneider <Tobias.Schneider@synyx.de>

ENV MYSQL_USER=urlaubsverwaltung \
    MYSQL_PASSWORD=urlaubsverwaltung \
    MYSQL_RANDOM_ROOT_PASSWORD=yes

HEALTHCHECK --interval=10s --timeout=3s --start-period=15s \
  CMD /usr/bin/mysql --user=urlaubsverwaltung --password=urlaubsverwaltung --execute "SHOW DATABASES;"

ADD create-urlaubsverwaltung-database.sql /docker-entrypoint-initdb.d/001-create-urlaubsverwaltung-database.sql
