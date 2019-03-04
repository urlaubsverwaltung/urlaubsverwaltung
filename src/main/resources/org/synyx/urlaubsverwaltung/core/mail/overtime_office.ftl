Hallo Office,

es wurden Überstunden erfasst: ${settings.baseLinkURL}web/overtime/<#if overtime.id??>${overtime.id}</#if>

Mitarbeiter: ${overtime.person.niceName}

Datum: ${overtime.startDate.toString("dd.MM.yyyy")} - ${overtime.endDate.toString("dd.MM.yyyy")}
Anzahl der Stunden: ${overtime.hours}

<#if (comment.text)??>
Kommentar von ${comment.person.niceName} zum Überstundeneintrag: ${comment.text}
</#if>
