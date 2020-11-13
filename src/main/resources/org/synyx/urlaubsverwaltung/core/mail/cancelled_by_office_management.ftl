Hallo ${recipient.niceName},

${application.canceller.niceName} hat den Urlaubsantrag von ${application.person.niceName} vom ${application.applicationDate.format("dd.MM.yyyy")} storniert.

<#if (comment.text)?has_content>
Kommentar zur Stornierung von ${comment.person.niceName} zum Antrag: ${comment.text}

</#if>
Es handelt sich um folgenden Urlaubsantrag: ${baseLinkURL}web/application/${application.id?c}
