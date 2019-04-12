Hallo ${application.person.niceName},

dein am ${application.applicationDate.format("dd.MM.yyyy")} gestellter Antrag wurde von ${application.boss.niceName} genehmigt.
Es handelt sich um den Zeitraum von ${application.startDate.format("dd.MM.yyyy")} bis ${application.endDate.format("dd.MM.yyyy")}, ${dayLength}.

<#if (comment.text)?has_content>
Kommentar von ${comment.person.niceName} zum Antrag: ${comment.text}

</#if>
Link zum Antrag: ${settings.baseLinkURL}web/application/${application.id?c}
