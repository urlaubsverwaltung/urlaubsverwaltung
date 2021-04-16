Hallo ${recipient.niceName},

es wurden Überstunden erfasst: ${baseLinkURL}web/overtime/${overtime.id?c}

Mitarbeiter: ${overtime.person.niceName}

Datum: ${overtime.startDate.format("dd.MM.yyyy")} - ${overtime.endDate.format("dd.MM.yyyy")}
Dauer: ${overtimeDurationHours} ${overtimeDurationMinutes}

<#if (comment.text)?has_content>
Kommentar von ${comment.person.niceName} zum Überstundeneintrag: ${comment.text}
</#if>
