Hallo ${application.person.niceName},

dein am ${application.applicationDate.format("dd.MM.yyyy")} gestellter Antrag wurde vorläufig genehmigt.
Bitte beachte, dass dieser erst noch von einem entsprechend Verantwortlichen freigegeben werden muss.

<#if (comment.text)?has_content>
Kommentar von ${comment.person.niceName} zum Antrag: ${comment.text}

</#if>
Es handelt sich um den Zeitraum von ${application.startDate.format("dd.MM.yyyy")} bis ${application.endDate.format("dd.MM.yyyy")}, ${dayLength}.

Link zum Antrag: ${settings.baseLinkURL}web/application/${application.id?c}
