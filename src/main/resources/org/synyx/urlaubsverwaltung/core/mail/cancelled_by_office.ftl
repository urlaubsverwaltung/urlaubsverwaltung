Hallo ${application.person.niceName},

${application.canceller.niceName} hat einen deiner Urlaubsanträge storniert.

<#if (comment.text)?has_content>
Kommentar zur Stornierung von ${comment.person.niceName} zum Antrag: ${comment.text}

</#if>
Es handelt sich um folgenden Urlaubsantrag: ${settings.baseLinkURL}web/application/${application.id?c}
