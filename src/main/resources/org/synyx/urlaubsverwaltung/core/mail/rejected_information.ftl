Hallo ${recipient.niceName},

der von ${application.person.niceName} am ${application.applicationDate.format("dd.MM.yyyy")} gestellte Antrag wurde von ${comment.person.niceName} abgelehnt.

<#if (comment.text)?has_content>
Begründung: ${comment.text}

</#if>
Link zum Antrag: ${baseLinkURL}web/application/${application.id?c}
