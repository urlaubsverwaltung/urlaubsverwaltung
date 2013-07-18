# Urlaubsverwaltung

Die Urlaubsverwaltung ist eine Web-Anwendung, die es ermöglicht, Urlaubsanträge von Mitarbeitern elektronisch zu verwalten. Mitarbeiter stellen Urlaubsanträge, die von den jeweils Berechtigten genehmigt oder abgelehnt werden. Die Anwendung bietet Übersicht über die bestehenden Urlaubsanträge und ermöglicht außerdem Überblick und Pflege von Urlaubsanspruch und Anzahl verbleibender Urlaubstage der Mitarbeiter.

Die Anwendung entstand als [synyx](http://www.synyx.de/) Azubi Projekt, an dem ich Java, das Spring Framework, uvm. kennenlernte.
Seit Anfang 2012 wird die Urlaubsverwaltung bei [synyx](http://www.synyx.de/) produktiv eingesetzt. Weiterentwicklung der Anwendung erfolgt im Moment nur noch sporadisch. 
 
Weitere Informationen zur Urlaubsverwaltung findet man im [synyx Blog](http://blog.synyx.de):
* [Stand November 2011](http://blog.synyx.de/2011/11/elektronische-urlaubsverwaltung-made-by-youngsters/)
* [Stand November 2012](http://blog.synyx.de/2012/11/urlaubsverwaltung-was-hat-sich-getan/) 

## Getting Started

Repository clonen:

<pre>  git clone git@github.com:synyx/urlaubsverwaltung.git</pre>

Die Anwendung besitzt Properties, die zur Benutzung angepasst werden müssen. 
Eine Möglichkeit ist es, die Properties direkt unter `src/main/resources/config.properties` anzupassen. 

<pre>  vim src/main/resources/config.properties</pre>

Eine andere Möglichkeit ist, die dort definierten globalen Variablen bspw. in der `.bashrc` zu exportieren.

<pre>  export DATABASE_URL=jdbc:mysql://127.0.0.1:3306/urlaub</pre>

Lokalen Tomcat starten

<pre>  mvn tomcat:run</pre>

Im Browser lässt sich die Anwendung dann über `http://localhost:8080/urlaubsverwaltung` ansteuern.

## Benutzung

Die Authentifizierung erfolgt über LDAP (URL aus `src/main/resources/config.properties`) Ist die Authentifizierung erfolgreich und der Benutzer noch nicht im System der Urlaubsverwaltung eingepflegt, wird die Person automatisch angelegt. Dem ersten Benutzer, der auf diese Weise im System angelegt wird, wird die Rolle Admin zugewiesen (ermöglicht Rechteverwaltung). Alle anderen Benutzer, die automatisch beim Einloggen im System angelegt werden, erhalten die Rolle User (was dann selbstverständlich über die Rechteverwaltung von Benutzern mit der Rolle Admin angepasst werden kann)

Ein User hat immer eine oder mehrere folgender Berechtigungen inne:
* **inaktiv**: hat keinen Zugang mehr zur Urlaubsverwaltung (bestehende Daten des Benutzers bleiben zur Archivierung bestehen)
* **User**: darf Urlaub für sich selbst beantragen
* **Boss**:	darf Urlaubsanträge von Mitarbeitern einsehen, genehmigen und ablehnen
* **Office**: darf Mitarbeiterdaten verwalten, Urlaub für Mitarbeiter beantragen und Urlaubsanträge stornieren
* **Admin**: darf Rechte verwalten 

## Development

Die Anwendung basiert auf dem [Spring](http://www.springsource.org/) MVC Framework. Zur Ermittlung von Feiertagen wird das Framework [Jollyday](http://jollyday.sourceforge.net/) benutzt. Das Frontend beinhaltet Elemente von [Bootstrap](http://twitter.github.io/bootstrap/) gewürzt mit einer Prise [jquery](http://jquery.com/).   

## License

[synyx/urlaubsverwaltung](http://github.com/synyx/urlaubsverwaltung) is licensed under the 
[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

Alle Logos, Marken- und Warenzeichen unterliegen **nicht** der Apache License 2.0 und dürfen nur mit schriftlicher Genehmigung von [synyx](http://www.synyx.de/) weiterverwendet werden. 
