Hallo ${recipient.niceName},

dein Anspruch auf Lohnfortzahlung von ${maximumSickPayDays} Tag(en) <#if isLastDayOfSickPayDaysInPast>endete<#else>endet</#if> am ${endOfSickPayDays.format("dd.MM.yyyy")}.

    ${baseLinkURL}web/sicknote/${sickNote.id?c}

Informationen zur Krankmeldung:

    Mitarbeiter:                  ${sickNote.person.niceName}
    Zeitraum:                     ${sickNote.startDate.format("dd.MM.yyyy")} bis ${sickNote.endDate.format("dd.MM.yyyy")}
    Anspruch auf Lohnfortzahlung: ${sickNotePayFrom.format("dd.MM.yyyy")} bis ${sickNotePayTo.format("dd.MM.yyyy")}


Hinweis:
Der Anspruch auf Lohnfortzahlung durch den Arbeitgeber im Krankheitsfall besteht für maximal ${maximumSickPayDays} Tage
(fortlaufende Kalendertage ohne Rücksicht auf die Arbeitstage des erkrankten Arbeitnehmers, Sonn- oder Feiertage).
Danach wird für gesetzlich Krankenversicherte in der Regel Krankengeld von der Krankenkasse gezahlt.
