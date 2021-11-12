Hallo ${recipient.niceName},

<#if application.holidayReplacements?has_content>
<#if daysBeforeUpcomingApplication == 1>in ${daysBeforeUpcomingApplication} Tag<#else>in ${daysBeforeUpcomingApplication} Tagen</#if> beginnt deine Abwesenheit und du wirst vertreten durch:
<#else>
<#if daysBeforeUpcomingApplication == 1>in ${daysBeforeUpcomingApplication} Tag<#else>in ${daysBeforeUpcomingApplication} Tagen</#if> beginnt deine Abwesenheit.
</#if>

<#if application.holidayReplacements?size == 1>
<#list application.holidayReplacements as replacement>
${replacement.person.niceName}<#if replacement.note?has_content>, "${replacement.note}"</#if>
</#list>
<#else>
<#list application.holidayReplacements as replacement>
- ${replacement.person.niceName}<#if replacement.note?has_content>
  "${replacement.note}"</#if>
</#list>
</#if>
<#if application.holidayReplacements?has_content>

</#if>
Da du vom ${application.startDate.format("dd.MM.yyyy")} bis zum ${application.endDate.format("dd.MM.yyyy")} nicht anwesend bist, denke bitte an die Übergabe.
Dazu gehören z.B. Abwesenheitsnotiz, E-Mail- & Telefon-Weiterleitung, Zeiterfassung, etc.

Link zur Abwesenheit: ${baseLinkURL}web/application/${application.id?c}
