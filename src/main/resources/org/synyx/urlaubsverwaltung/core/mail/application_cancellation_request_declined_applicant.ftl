Hallo ${recipient.niceName},

dein Stornierungsantrag der genehmigten Abwesenheit vom ${application.startDate.format("dd.MM.yyyy")} bis ${application.endDate.format("dd.MM.yyyy")} wurde abgelehnt.

<#if (comment.text)?has_content>
Kommentar von ${comment.person.niceName}:
${comment.text}

</#if>
Es handelt sich um folgende Abwesenheit: ${baseLinkURL}web/application/${application.id?c}
