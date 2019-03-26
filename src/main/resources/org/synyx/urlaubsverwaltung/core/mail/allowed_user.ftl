Hallo ${application.person.niceName},

dein am ${application.applicationDate.toString("dd.MM.yyyy")} gestellter Antrag wurde von ${application.boss.niceName} genehmigt.
Es handelt sich um den Zeitraum von ${application.startDate.toString("dd.MM.yyyy")} bis ${application.endDate.toString("dd.MM.yyyy")}, ${dayLength}.

<#if (comment.text)??>
Kommentar von ${comment.person.niceName} zum Antrag: ${comment.text}

</#if>
Link zum Antrag: ${settings.baseLinkURL}web/application/${application.id?c}
