# Urlaubsverwaltung

Die Urlaubsverwaltung ist eine Web-Anwendung, die es ermöglicht, Urlaubsanträge elektronisch zu verwalten. (Anträge stellen, genehmigen, ablehnen, stornieren, Übersicht über alle Anträge und Urlaubstage der Mitarbeiter erhalten) 

Die Anwendung entstand Ende 2011 als Azubi-Projekt, an dem ich Java, das Spring-Framework, uvm. kennenlernte.
Seit Anfang 2012 wird die Urlaubsverwaltung firmenintern bei [synyx](http://www.synyx.de/) produktiv eingesetzt.
 
Weitere Informationen zur Urlaubsverwaltung finden sich im [synyx Blog](http://blog.synyx.de)
[Stand November 2011](http://blog.synyx.de/2011/11/elektronische-urlaubsverwaltung-made-by-youngsters/)
[Stand November 2012](http://blog.synyx.de/2012/11/urlaubsverwaltung-was-hat-sich-getan/) 

## Getting Started

Repository clonen:

<pre>  # git clone git@github.com:synyx/urlaubsverwaltung.git</pre>

Spezifische Properties müssen angepasst werden unter src/main/resources/config.properties oder die dort angegebenen globalen Variablen z.B. in der .bashrc exportieren.

<pre>  # vim src/main/resources/config.properties</pre>

Lokalen Tomcat starten

<pre>  # mvn tomcat:run</pre>

Unter der URL `http://localhost:8080/urlaubsverwaltung` findet sich die Anwendung.

## Development

Spring MVC
jquery
jollycalendar


## License

synyx/urlaubsverwaltung is licensed under the 
[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)
