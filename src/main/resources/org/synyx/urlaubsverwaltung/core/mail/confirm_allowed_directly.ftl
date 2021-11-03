Hallo ${application.person.niceName},

dein Abwesenheitsantrag wurde erfolgreich eingestellt.

Informationen zum Abwesenheitsantrag:

    Antragsdatum: ${application.applicationDate.format("dd.MM.yyyy")}
    Zeitraum der Abwesenheit: ${application.startDate.format("dd.MM.yyyy")} bis ${application.endDate.format("dd.MM.yyyy")}, ${dayLength}
    Art der Abwesenheit: ${vacationType}
    <#if (application.reason)?has_content>
    Grund: ${application.reason}
    </#if>
    <#if application.holidayReplacements?has_content >
    Vertretung: <#list application.holidayReplacements as replacement>${replacement.person.niceName}<#if !replacement?is_last>, </#if></#list>
    </#if>
    <#if (application.address)?has_content>
    Anschrift/Telefon während der Abwesenheit: ${application.address}
    </#if>
    <#if (comment.text)?has_content>
    Kommentar: ${comment.text}
    </#if>

Link zur eingestellten Abwesenheit: ${baseLinkURL}web/application/${application.id?c}
