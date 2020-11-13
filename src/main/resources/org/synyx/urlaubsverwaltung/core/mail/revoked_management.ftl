Hallo ${recipient.niceName},

der am ${application.applicationDate.format("dd.MM.yyyy")} gestellter, nicht genehmigter Antrag wurde von ${application.person.niceName} wurde durch ${comment.person.niceName} storniert.

<#if (comment.text)?has_content>
Begründung: ${comment.text}

</#if>
Link zum Antrag: ${baseLinkURL}web/application/${application.id?c}
