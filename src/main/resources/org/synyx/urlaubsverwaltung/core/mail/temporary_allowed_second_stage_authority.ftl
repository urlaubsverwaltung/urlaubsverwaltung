Hallo ${recipient.niceName},

es liegt ein neuer zu genehmigende Abwesenheit vor.

    ${baseLinkURL}web/application/${application.id?c}

Die Abwesenheit wurde bereits vorläufig genehmigt und muss nun noch endgültig freigegeben werden.

<#if (comment.text)?has_content>
Kommentar von ${comment.person.niceName}:
${comment.text}
</#if>

Informationen zur Abwesenheit:

    Mitarbeiter:               ${application.person.niceName}
    Zeitraum:                  ${application.startDate.format("dd.MM.yyyy")} bis ${application.endDate.format("dd.MM.yyyy")}, ${dayLength}
    Art der Abwesenheit:       ${vacationType}
    <#if application.reason?has_content>
    Grund:                     <@compress single_line=true>${application.reason}</@compress>
    </#if>
    <#if application.holidayReplacements?has_content >
    Vertretung:                <#list application.holidayReplacements as replacement>${replacement.person.niceName}<#if !replacement?is_last>, </#if></#list>
    </#if>
    <#if (application.address)?has_content>
    Anschrift/Telefon:         <@compress single_line=true>${application.address}</@compress>
    </#if>
    Erstellungsdatum:          ${application.applicationDate.format("dd.MM.yyyy")}

Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:

    <#list departmentVacations as vacation>
    ${vacation.person.niceName}: ${vacation.startDate.format("dd.MM.yyyy")} bis ${vacation.endDate.format("dd.MM.yyyy")}
    <#else>
    Keine
    </#list>
