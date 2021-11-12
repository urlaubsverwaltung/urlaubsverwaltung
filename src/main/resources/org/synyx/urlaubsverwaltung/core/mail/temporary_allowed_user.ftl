Hallo ${application.person.niceName},

dein am ${application.applicationDate.format("dd.MM.yyyy")} gestellte Abwesenheit wurde vorläufig genehmigt.
Bitte beachte, dass dieser erst noch von einem entsprechend Verantwortlichen freigegeben werden muss.

<#if (comment.text)?has_content>
Kommentar von ${comment.person.niceName}:
${comment.text}
</#if>

Es handelt sich um den Zeitraum von ${application.startDate.format("dd.MM.yyyy")} bis ${application.endDate.format("dd.MM.yyyy")}, ${dayLength}.

Link zur Abwesenheit: ${baseLinkURL}web/application/${application.id?c}
