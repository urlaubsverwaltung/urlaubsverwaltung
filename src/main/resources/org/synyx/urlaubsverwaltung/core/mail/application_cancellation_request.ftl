Hallo ${recipient.niceName},

${application.person.niceName} möchte die bereits genehmigte Abwesenheit vom
${application.startDate.format("dd.MM.yyyy")} bis ${application.endDate.format("dd.MM.yyyy")} stornieren.

<#if (comment.text)?has_content>
Kommentar von ${comment.person.niceName}:
${comment.text}

</#if>
Es handelt sich um folgende Abwesenheit: ${baseLinkURL}web/application/${application.id?c}

Überblick aller offenen Stornierungsanträge findest du unter ${baseLinkURL}web/application#cancellation-requests
