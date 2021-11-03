Hallo ${holidayReplacement.niceName},

eine Abwesenheit von ${application.person.niceName} wurde eingestellt und
du wurdest für den Zeitraum vom ${application.startDate.format("dd.MM.yyyy")} bis ${application.endDate.format("dd.MM.yyyy")}, ${dayLength} als Vertretung eingetragen.

<#if holidayReplacementNote?has_content>
Notiz von ${application.person.niceName} an dich:
${holidayReplacementNote}
</#if>

Einen Überblick deiner aktuellen und zukünftigen Vertretungen findest du unter ${baseLinkURL}web/application#holiday-replacement
