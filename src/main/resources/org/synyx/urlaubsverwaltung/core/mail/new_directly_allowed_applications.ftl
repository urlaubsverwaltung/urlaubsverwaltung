Hallo ${recipient.niceName},

es wurde eine neue, nicht zu genehmigende, Abwesenheit eingestellt.

    ${baseLinkURL}web/application/${application.id?c}

Informationen zum Urlaubsantrag:

    Mitarbeiter: ${application.person.niceName}
    Datum der Antragsstellung: ${application.applicationDate.format("dd.MM.yyyy")}
    Zeitraum des beantragten Urlaubs: ${application.startDate.format("dd.MM.yyyy")} bis ${application.endDate.format("dd.MM.yyyy")}, ${dayLength}
    Art des Urlaubs: ${vacationType}
    <#if (application.reason)?has_content>
    Grund: ${application.reason}
    </#if>
    <#if application.holidayReplacements?has_content >
    Vertretung: <#list application.holidayReplacements as replacement>${replacement.person.niceName}<#if !replacement?is_last>, </#if></#list>
    </#if>
    <#if (application.address)?has_content>
    Anschrift/Telefon während des Urlaubs: ${application.address}
    </#if>
    <#if (comment.text)?has_content>
    Kommentar: ${comment.text}
    </#if>
