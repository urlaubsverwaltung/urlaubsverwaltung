Hallo ${application.person.niceName},

${application.canceller.niceName} hat einen deine Abwesenheit storniert.

<#if (comment.text)?has_content>
Kommentar von ${comment.person.niceName}:
${comment.text}

</#if>
Es handelt sich um folgende Abwesenheit: ${baseLinkURL}web/application/${application.id?c}
