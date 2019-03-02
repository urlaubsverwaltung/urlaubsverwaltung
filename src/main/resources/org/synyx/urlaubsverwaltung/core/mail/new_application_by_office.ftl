Hallo ${application.person.niceName},

${application.applier.niceName} hat einen Urlaubsantrag für dich gestellt, der in Kürze von einem der Chefs bearbeitet werden sollte.

---------------------------------------------------------------------------------------------------------

Informationen zum Urlaubsantrag:

Antragsdatum: ${application.applicationDate.toString("dd.MM.yyyy")}
Zeitraum des beantragten Urlaubs: ${application.startDate.toString("dd.MM.yyyy")} bis ${application.endDate.toString("dd.MM.yyyy")}, ${dayLength}
Art des Urlaubs: ${application.vacationType.displayName}
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

Link zum Antrag: ${settings.baseLinkURL}web/application/<#if application.id??>${application.id}</#if>
