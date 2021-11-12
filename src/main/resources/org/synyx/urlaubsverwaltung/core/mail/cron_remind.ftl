Hallo ${recipient.niceName},

Die folgenden Abwesenheiten warten auf Bearbeitung:

<#list applicationList as application>
Antrag von ${application.person.niceName} vom ${application.applicationDate.format("dd.MM.yyyy")}: ${baseLinkURL}web/application/${application.id?c}
</#list>
