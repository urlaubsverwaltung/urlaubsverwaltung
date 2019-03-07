Hallo ${application.person.niceName},

dein am ${application.applicationDate.toString("dd.MM.yyyy")} gestellter Antrag wurde vorläufig genehmigt.
Bitte beachte, dass dieser erst noch von einem entsprechend Verantwortlichen freigegeben werden muss.

<#if (comment.text)??>
Kommentar von ${comment.person.niceName} zum Antrag: ${comment.text}

</#if>
Es handelt sich um den Zeitraum von ${application.startDate.toString("dd.MM.yyyy")} bis ${application.endDate.toString("dd.MM.yyyy")}, ${dayLength}

Link zum Antrag: ${settings.baseLinkURL}web/application/<#if application.id??>${application.id}</#if>