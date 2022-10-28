Hallo ${recipient.niceName},

es wurden Überstunden erfasst.

    ${baseLinkURL}web/overtime/${overtime.id?c}

<#if (comment.text)?has_content>
Kommentar von ${comment.person.niceName}:
${comment.text}
</#if>

Informationen zu den Überstunden:

    Mitarbeiter: ${overtime.person.niceName}
    Zeitraum:    ${overtime.startDate.format("dd.MM.yyyy")} - ${overtime.endDate.format("dd.MM.yyyy")}
    Dauer:       ${overtimeDurationHours} ${overtimeDurationMinutes}
