Hallo ${recipient.niceName},

<#if daysBeforeUpcomingHolidayReplacement == 1>morgen<#else>in ${daysBeforeUpcomingHolidayReplacement} Tagen</#if> beginnt deine Urlaubsvertretung für ${application.person.niceName}.

<#if replacementNote?has_content>Notiz: "${replacementNote}"</#if>

Einen Überblick deiner aktuellen und zukünftigen Vertretungen findest du unter ${baseLinkURL}web/application#holiday-replacement
