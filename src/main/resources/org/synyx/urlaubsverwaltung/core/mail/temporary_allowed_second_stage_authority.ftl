Hallo ${recipient.niceName},

es liegt ein neuer zu genehmigender Antrag vor: ${settings.baseLinkURL}web/application/${application.id?c}

Der Antrag wurde bereits vorläufig genehmigt und muss nun noch endgültig freigegeben werden.
<#if (comment.text)??>
Kommentar von ${comment.person.niceName} zum Antrag: ${comment.text}
</#if>

----------------------------------------------------------------------------------------------

Informationen zum Urlaubsantrag:

Mitarbeiter: ${application.person.niceName}
Datum der Antragsstellung: ${application.applicationDate.toString("dd.MM.yyyy")}
Zeitraum des beantragten Urlaubs: ${application.startDate.toString("dd.MM.yyyy")} bis ${application.endDate.toString("dd.MM.yyyy")}, ${dayLength}
Art des Urlaubs: ${vacationType}
<#if application.reason?has_content>
Grund: ${application.reason}
</#if>
<#if (application.holidayReplacement.niceName)?? >
Vertreter: ${application.holidayReplacement.niceName}
</#if>
<#if (application.address)??>
Anschrift/Telefon während des Urlaubs: ${application.address}
</#if>

Überschneidende Anträge in der Abteilung des Antragsstellers:
<#list departmentVacations as vacation>
${vacation.person.niceName}: ${vacation.startDate.toString("dd.MM.yyyy")} bis ${vacation.endDate.toString("dd.MM.yyyy")}
<#else>
Keine
</#list>

