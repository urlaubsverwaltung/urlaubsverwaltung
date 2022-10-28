Hallo ${application.person.niceName},

dein Antrag zum Stornieren deines bereits genehmigten Antrags vom
${application.startDate.format("dd.MM.yyyy")} bis ${application.endDate.format("dd.MM.yyyy")} wurde eingereicht.

<#if (comment.text)?has_content>
Kommentar von ${comment.person.niceName}:
${comment.text}

</#if>
Es handelt sich um folgende Abwesenheit: ${baseLinkURL}web/application/${application.id?c}

Überblick deiner offenen Stornierungsanträge findest du unter ${baseLinkURL}web/application#cancellation-requests
