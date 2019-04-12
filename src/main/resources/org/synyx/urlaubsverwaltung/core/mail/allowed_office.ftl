Hallo Office,

es liegt ein neuer genehmigter Antrag vor: ${settings.baseLinkURL}web/application/${application.id?c}

<#if (comment.text)?has_content>
Kommentar von ${comment.person.niceName} zum Antrag: ${comment.text}

</#if>
----------------------------------------------------------------------------------------------

Informationen zum Urlaubsantrag:

Mitarbeiter: ${application.person.niceName}
Antragsdatum: ${application.applicationDate.format("dd.MM.yyyy")}
Zeitraum des beantragten Urlaubs: ${application.startDate.format("dd.MM.yyyy")} bis ${application.endDate.format("dd.MM.yyyy")}, ${dayLength}
Art des Urlaubs: ${vacationType}
<#if (application.reason)?has_content>
Grund: ${application.reason}
</#if>
<#if (application.holidayReplacement.niceName)?has_content>
Vertreter: ${application.holidayReplacement.niceName}
</#if>
<#if (application.address)?has_content>
Anschrift/Telefon während des Urlaubs: ${application.address}
</#if>
