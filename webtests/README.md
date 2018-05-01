# Urlaubsverwaltung - Webtests

webtests für die Urlaubsverwaltung

## How To

akutell benötigt wird

* NodeJS 8
* Chrome Browser

Aktuell sind die Tests nicht im MavenBuild integriert. Sie können proof of
concept mäßig nur in diese Verzeichnis gestartet werden.

zuerst müssen benötigte Dependencies installiert werden:

```bash
cd webtests
npm i
```

Dann können die Tests gestartet werden

```bash
npm run test
```

Die Tests laufen mit diesem npm task gegen den Chrome Browser. Möchte man z. B.
gegen den Firefox gehen, muss dieser lokal installiert sein und kann dann wie
folgt verwendet werden:

```
npx testcafe firefox tests
```
