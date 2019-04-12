Hallo Office,

${application.person.niceName} hat beantragt den bereits genehmigten Urlaub vom
${application.startDate.format("dd.MM.yyyy")} bis ${application.endDate.format("dd.MM.yyyy")} zu stornieren.

<#if (comment.text)?has_content>
Kommentar zur Stornierung von ${comment.person.niceName} zum Antrag: ${comment.text}

</#if>
Es handelt sich um folgenden Urlaubsantrag: ${settings.baseLinkURL}web/application/${application.id?c}
