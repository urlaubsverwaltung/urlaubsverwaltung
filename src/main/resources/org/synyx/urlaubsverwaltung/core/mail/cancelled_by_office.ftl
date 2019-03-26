Hallo ${application.person.niceName},

dein Urlaubsantrag wurde von ${application.canceller.niceName} für dich storniert.
Falls Klärungsbedarf bestehen sollte, wende dich bitte an ${application.canceller.niceName}.

<#if (comment.text)??>
Kommentar zur Stornierung von ${comment.person.niceName} zum Antrag: ${comment.text}

</#if>
Es handelt sich um folgenden Urlaubsantrag: ${settings.baseLinkURL}web/application/${application.id?c}
