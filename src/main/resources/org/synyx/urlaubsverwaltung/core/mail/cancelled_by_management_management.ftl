Hallo ${recipient.niceName},

${application.canceller.niceName} hat die Abwesenheit von ${application.person.niceName} vom ${application.applicationDate.format("dd.MM.yyyy")} storniert.

<#if (comment.text)?has_content>
Kommentar von ${comment.person.niceName}:
${comment.text}

</#if>
Es handelt sich um folgende Abwesenheit: ${baseLinkURL}web/application/${application.id?c}
