Hallo ${recipient.niceName},

es liegt ein neuer zu genehmigender Antrag vor: ${settings.baseLinkURL}web/application/${application.id?c}

----------------------------------------------------------------------------------------------

Informationen zum Urlaubsantrag:

Mitarbeiter: ${application.person.niceName}
Datum der Antragsstellung: ${application.applicationDate.toString("dd.MM.yyyy")}
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

Überschneidende Anträge in der Abteilung des Antragsstellers:
<#list departmentVacations as vacation>
${vacation.person.niceName}: ${vacation.startDate.toString("dd.MM.yyyy")} bis ${vacation.endDate.toString("dd.MM.yyyy")}
<#else>
Keine
</#list>