Hallo ${application.holidayReplacement.niceName},

die Abwesenheit von ${application.person.niceName} wurde genehmigt.
Du wurdest damit für den Zeitraum vom ${application.startDate.format("dd.MM.yyyy")} bis ${application.endDate.format("dd.MM.yyyy")}, ${dayLength} als Vertretung eingetragen.
<#if (application.holidayReplacementNote)?has_content>
Notiz von ${application.person.niceName} and dich:
${application.holidayReplacementNote}
</#if>

Überblick deiner aktuellen und zukünftigen Vertretungen findest du unter ${baseLinkURL}web/application#holiday-replacement
