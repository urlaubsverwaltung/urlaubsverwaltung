Hallo ${application.person.niceName},

dein Urlaubsantrag wurde erfolgreich eingereicht und wird in Kürze von einem der Chefs bearbeitet werden.

---------------------------------------------------------------------------------------------------------

Informationen zum Urlaubsantrag:

Antragsdatum: ${application.applicationDate.toString("dd.MM.yyyy")}
Zeitraum des beantragten Urlaubs: ${application.startDate.toString("dd.MM.yyyy")} bis ${application.endDate.toString("dd.MM.yyyy")}, ${dayLength}
Art des Urlaubs: ${application.vacationType.messageKey}
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
