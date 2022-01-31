Hallo ${recipient.niceName},

deine Vertretung für ${application.person.niceName} vom ${application.startDate.format("dd.MM.yyyy")} bis zum ${application.endDate.format("dd.MM.yyyy")} beginnt <#if daysBeforeUpcomingHolidayReplacement == 0>heute<#elseif daysBeforeUpcomingHolidayReplacement == 1>morgen<#else>in ${daysBeforeUpcomingHolidayReplacement} Tagen</#if>.

<#if replacementNote?has_content>
Notiz:
${replacementNote}${'\n'}
</#if>

Einen Überblick deiner aktuellen und zukünftigen Vertretungen findest du unter ${baseLinkURL}web/application/replacement
