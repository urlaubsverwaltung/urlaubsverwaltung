Hallo ${recipient.niceName},

der am ${application.applicationDate.format("dd.MM.yyyy")} gestellte, nicht genehmigte Antrag von ${application.person.niceName} wurde <#if application.person.niceName != comment.person.niceName>von ${comment.person.niceName} </#if>storniert.

<#if (comment.text)?has_content>
Begründung:
${comment.text}

</#if>
Link zur Abwesenheit: ${baseLinkURL}web/application/${application.id?c}
