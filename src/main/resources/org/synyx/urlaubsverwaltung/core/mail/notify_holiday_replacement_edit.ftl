Hallo ${holidayReplacement.niceName},

der Zeitraum für die Abwesenheit von ${application.person.niceName} bei dem du als Vertretung vorgesehen bist, hat sich geändert.
Der neue Zeitraum ist von ${application.startDate.format("dd.MM.yyyy")} bis ${application.endDate.format("dd.MM.yyyy")}, ${dayLength}.

<#if holidayReplacementNote?has_content>
Notiz von ${application.person.niceName} and dich:
${holidayReplacementNote}
</#if>

Einen Überblick deiner aktuellen und zukünftigen Vertretungen findest du unter ${baseLinkURL}web/application#holiday-replacement
