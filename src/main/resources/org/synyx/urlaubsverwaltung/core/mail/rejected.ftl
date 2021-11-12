Hallo ${application.person.niceName},

dein am ${application.applicationDate.format("dd.MM.yyyy")} gestellte Abwesenheit wurde leider von ${comment.person.niceName} abgelehnt.

<#if (comment.text)?has_content>
Begründung:
${comment.text}

</#if>
Link zur Abwesenheit: ${baseLinkURL}web/application/${application.id?c}
