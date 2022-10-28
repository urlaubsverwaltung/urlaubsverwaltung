Hallo ${application.person.niceName},

deine Abwesenheit vom ${application.startDate.format("dd.MM.yyyy")} bis zum ${application.endDate.format("dd.MM.yyyy")} wurde erfolgreich storniert.

    ${baseLinkURL}web/application/${application.id?c}

<#if (comment.text)?has_content>
Kommentar von ${comment.person.niceName}:
${comment.text}

</#if>
Informationen zur Abwesenheit:

    Zeitraum:            ${application.startDate.format("dd.MM.yyyy")} bis ${application.endDate.format("dd.MM.yyyy")}, ${dayLength}
    Art der Abwesenheit: ${vacationType}
    <#if (application.reason)?has_content>
    Grund:               <@compress single_line=true>${application.reason}</@compress>
    </#if>
    <#if application.holidayReplacements?has_content >
    Vertretung:          <#list application.holidayReplacements as replacement>${replacement.person.niceName}<#if !replacement?is_last>, </#if></#list>
    </#if>
    <#if (application.address)?has_content>
    Anschrift/Telefon:   <@compress single_line=true>${application.address?trim}</@compress>
    </#if>
    Erstellungsdatum:    ${application.applicationDate.format("dd.MM.yyyy")}
