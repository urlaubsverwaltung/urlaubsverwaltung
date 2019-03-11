Hallo ${application.person.niceName},

dein am ${application.applicationDate.toString("dd.MM.yyyy")} gestellter Antrag wurde leider von ${comment.person.niceName} abgelehnt.

<#if (comment.text)??>
Begründung: ${comment.text}

</#if>
Link zum Antrag: ${settings.baseLinkURL}web/application/${application.id?c}
