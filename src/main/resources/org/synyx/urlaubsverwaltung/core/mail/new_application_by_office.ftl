Hallo ${application.person.niceName},

${application.applier.niceName} hat einen Urlaubsantrag für dich gestellt.

---------------------------------------------------------------------------------------------------------

Informationen zum Urlaubsantrag:

Antragsdatum: ${application.applicationDate.format("dd.MM.yyyy")}
Zeitraum des beantragten Urlaubs: ${application.startDate.format("dd.MM.yyyy")} bis ${application.endDate.format("dd.MM.yyyy")}, ${dayLength}
Art des Urlaubs: ${vacationType}
<#if (application.reason)??>
Grund: ${application.reason}
</#if>
<#if (application.holidayReplacement.niceName)??>
Vertreter: ${application.holidayReplacement.niceName}
</#if>
<#if (application.address)??>
Anschrift/Telefon während des Urlaubs: ${application.address}
</#if>
<#if (comment.text)??>
Kommentar: ${comment.text}
</#if>

Link zum Antrag: ${settings.baseLinkURL}web/application/${application.id?c}
