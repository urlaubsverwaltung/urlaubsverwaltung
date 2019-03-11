Hallo ${recipient.niceName},

Die folgenden gestellten Urlaubsanträge warten auf ihre Bearbeitung:

<#list applicationList as application>
Antrag von ${application.person.niceName} vom ${application.applicationDate.toString("dd.MM.yyyy")}: ${settings.baseLinkURL}web/application/${application.id?c}
</#list>

Ohne eine Bearbeitung kann es passieren, dass weitere Erinnerungen folgen ;-)
