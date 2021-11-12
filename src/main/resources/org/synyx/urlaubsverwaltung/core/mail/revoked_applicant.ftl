Hallo ${application.person.niceName},

dein am ${application.applicationDate.format("dd.MM.yyyy")} gestellter, nicht genehmigter Antrag wurde von dir erfolgreich storniert.

<#if (comment.text)?has_content>
Begründung:
${comment.text}

</#if>
Link zur Abwesenheit: ${baseLinkURL}web/application/${application.id?c}
