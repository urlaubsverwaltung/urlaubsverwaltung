Hallo ${holidayReplacement.niceName},

${application.person.niceName} hat dich bei einer Abwesenheit als Vertretung vorgesehen.
Es handelt sich um den Zeitraum von ${application.startDate.format("dd.MM.yyyy")} bis ${application.endDate.format("dd.MM.yyyy")}, ${dayLength}.

<#if (holidayReplacementNote)?has_content>
Notiz von ${application.person.niceName} an dich:
${holidayReplacementNote}
</#if>

Einen Überblick deiner aktuellen und zukünftigen Vertretungen findest du unter ${baseLinkURL}web/application#holiday-replacement
